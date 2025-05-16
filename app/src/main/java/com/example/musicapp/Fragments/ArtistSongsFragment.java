package com.example.musicapp.Fragments;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.musicapp.R;
import com.example.musicapp.adapters.SongAdapter;
import com.example.musicapp.databinding.FragmentArtistSongsBinding;
import com.example.musicapp.entity.Song;
import com.example.musicapp.service.MusicService;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách bài hát dọc của một danh mục.
 */
public class ArtistSongsFragment extends Fragment {

    private static final String TAG = "ArtistSongsFragment";
    private FragmentArtistSongsBinding binding;
    private MusicService musicService;
    private boolean isServiceBound = false;
    private final List<Song> songs = new ArrayList<>();
    private NavController navController;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MusicBinder) service).getService();
            isServiceBound = true;
            Log.d(TAG, "Đã kết nối với MusicService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            musicService = null;
            Log.d(TAG, "Ngắt kết nối với MusicService");
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(requireActivity(), MusicService.class);
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentArtistSongsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        binding = FragmentArtistSongsBinding.bind(view);

        setupBackButton();

        if (!loadBundleData()) {
            showError("Không có bài hát để hiển thị");
            return;
        }

        setupRecyclerView();
    }

    private boolean loadBundleData() {
        Bundle args = getArguments();
        if (args == null) {
            Log.e(TAG, "Bundle null");
            return false;
        }

        String sectionTitle = args.getString("section_title", "Danh sách bài hát");
        binding.sectionTitle.setText(sectionTitle);

        ArrayList<Song> receivedSongs = args.getParcelableArrayList("songs");
        Log.d(TAG, "Received songs: " + (receivedSongs == null ? "null" : receivedSongs.size()));
        if (receivedSongs == null || receivedSongs.isEmpty()) {
            Log.w(TAG, "Danh sách bài hát rỗng hoặc null");
            return false;
        }

        songs.clear();
        songs.addAll(receivedSongs);
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupRecyclerView() {
        SongAdapter songAdapter = new SongAdapter(songs, this::playSong, true);
        binding.songsList.setAdapter(songAdapter);
        binding.songsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        songAdapter.notifyDataSetChanged();
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> navController.navigate(R.id.action_artistSongsFragment_to_homeFragment));
    }

    private void playSong(Song song) {
        if (!isServiceBound || musicService == null) {
            showError("Không thể phát nhạc, vui lòng thử lại");
            return;
        }

        int index = songs.indexOf(song);
        if (index == -1) {
            showError("Bài hát không khả dụng");
            return;
        }

        musicService.setPlaylist(songs, index);
        musicService.playSong(song.getPreviewUrl(), song);
        Log.d(TAG, "Phát bài hát: " + song.getTitle());

        Bundle bundle = new Bundle();
        bundle.putParcelable("current_song", song);
        navController.navigate(R.id.action_artistSongsFragment_to_musicPlayerFragment, bundle);
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setAction("Quay lại", v -> navController.navigate(R.id.action_artistSongsFragment_to_homeFragment))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            requireActivity().unbindService(connection);
            isServiceBound = false;
        }
    }
}