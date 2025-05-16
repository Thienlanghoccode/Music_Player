package com.example.musicapp.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.example.musicapp.adapters.SongAdapter;
import com.example.musicapp.databinding.FragmentHomeBinding;
import com.example.musicapp.dbhelper.DatabaseHelper;
import com.example.musicapp.entity.Song;
import com.example.musicapp.service.MusicService;
import com.example.musicapp.viewmodels.HomeViewModel;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String ACTION_SONG_STATE_CHANGED = "com.example.musicapp.SONG_STATE_CHANGED";
    private static final String EXTRA_SONG = "extra_song";
    private static final String EXTRA_IS_PLAYING = "extra_is_playing";
    private static final String ACTION_PLAY = "com.example.musicapp.PLAY";
    private static final String ACTION_PAUSE = "com.example.musicapp.PAUSE";
    private static final String ACTION_RESUME = "com.example.musicapp.RESUME";

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private NavController navController;
    private SongAdapter myPlaylistAdapter;
    private SongAdapter recentlyPlayedAdapter;
    private SongAdapter sonTungMtpAdapter;
    private SongAdapter hieuThuHaiAdapter;
    private SongAdapter featuredSongsAdapter;
    private SongAdapter trendingSongsAdapter;
    private SongAdapter tabListAdapter;
    private List<Song> myPlaylistSongs = new ArrayList<>();
    private List<Song> recentlyPlayedSongs = new ArrayList<>();
    private List<Song> sonTungMtpSongs = new ArrayList<>();
    private List<Song> hieuThuHaiSongs = new ArrayList<>();
    private List<Song> featuredSongs = new ArrayList<>();
    private List<Song> trendingSongs = new ArrayList<>();
    private List<Song> tabListSongs = new ArrayList<>();
    private boolean isDataLoaded = false;
    private boolean isReceiverRegistered = false;
    private DrawerLayout drawerLayout;

    private final BroadcastReceiver songStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Song song = intent.getParcelableExtra(EXTRA_SONG);
            boolean isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false);
            updateMusicControlBar(song, isPlaying);
            Log.d(TAG, "Nhận broadcast, bài hát: " + (song != null ? song.getTitle() : "null") + ", đang phát: " + isPlaying);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(ACTION_SONG_STATE_CHANGED);
            ContextCompat.registerReceiver(requireActivity(), songStateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            isReceiverRegistered = true;
            Log.d(TAG, "Đăng ký BroadcastReceiver trong onCreate");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        Cursor cursor = null;
        String currentEmail = ((MainActivity) requireActivity()).getCurrentEmail();

        if (currentEmail == null) {
            navController = Navigation.findNavController(requireActivity(), R.id.fragment_container);
            navController.navigate(R.id.action_homeFragment_to_signInFragment);
            return view;
        }

        try {
            cursor = dbHelper.findUser(currentEmail);
            drawerLayout = view.findViewById(R.id.home_drawer);
            viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
            navController = Navigation.findNavController(requireActivity(), R.id.fragment_container);
            NavigationView navigationView = view.findViewById(R.id.nv_side);

            setupRecyclerViews();
            setupFilterButtons();
            setupMusicControlBar();
            observeData();
            setupViewAllListeners();

            View header = navigationView.getHeaderView(0);
            ImageView userImage = header.findViewById(R.id.userImage);
            TextView usernameText = header.findViewById(R.id.usernameText);
            TextView userEmailText = header.findViewById(R.id.userEmailText);

            String username = "Unknown User";

            if (cursor != null && cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndexOrThrow("USER_NAME"));
                byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow("IMAGE"));
                if (image != null) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                        userImage.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        Log.e("MyApp", "Lỗi khi xử lý: " + e.getMessage(), e);
                        userImage.setImageResource(R.drawable.user_icon);
                    }
                }
            }
            usernameText.setText(username);
            userEmailText.setText(currentEmail);

            binding.btnPersonal.setOnClickListener(v -> {
                drawerLayout.openDrawer(GravityCompat.START);
            });

            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.snv_info) {
                    navController.navigate(R.id.action_homeFragment_to_infoFragment);
                }
                if (itemId == R.id.snv_user) {
                    navController.navigate(R.id.action_homeFragment_to_UserFragment);
                }
                if (itemId == R.id.snv_logout) {
                    navController.navigate(R.id.action_homeFragment_to_signInFragment);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return view;
    }



    private void setupRecyclerViews() {
        Consumer<Song> onSongClick = this::playSong;
        myPlaylistAdapter = new SongAdapter(myPlaylistSongs, onSongClick, false);
        recentlyPlayedAdapter = new SongAdapter(recentlyPlayedSongs, onSongClick, false);
        sonTungMtpAdapter = new SongAdapter(sonTungMtpSongs, onSongClick, false);
        hieuThuHaiAdapter = new SongAdapter(hieuThuHaiSongs, onSongClick, false);
        featuredSongsAdapter = new SongAdapter(featuredSongs, onSongClick, false);
        trendingSongsAdapter = new SongAdapter(trendingSongs, onSongClick, false);
        tabListAdapter = new SongAdapter(tabListSongs, onSongClick, true); // Dọc cho tab_list

        setupRecyclerView(binding.myPlaylistList, myPlaylistSongs, myPlaylistAdapter);
        setupRecyclerView(binding.recentlyPlayedList, recentlyPlayedSongs, recentlyPlayedAdapter);
        setupRecyclerView(binding.sonTungMtpList, sonTungMtpSongs, sonTungMtpAdapter);
        setupRecyclerView(binding.hieuthuhaiList, hieuThuHaiSongs, hieuThuHaiAdapter);
        setupRecyclerView(binding.featuredSongsList, featuredSongs, featuredSongsAdapter);
        setupRecyclerView(binding.trendingSongsList, trendingSongs, trendingSongsAdapter);
        setupRecyclerView(binding.tabList, tabListSongs, tabListAdapter, true);
    }

    private void setupRecyclerView(androidx.recyclerview.widget.RecyclerView recyclerView, List<Song> songs, SongAdapter adapter, boolean isVertical) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), isVertical ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }

    private void setupRecyclerView(androidx.recyclerview.widget.RecyclerView recyclerView, List<Song> songs, SongAdapter adapter) {
        setupRecyclerView(recyclerView, songs, adapter, false);
    }

    private void setupFilterButtons() {
        binding.btnAll.setChecked(true); // Mặc định chọn "Tất cả"
        binding.btnAll.setOnClickListener(v -> {
            toggleButton(binding.btnAll, true);
            toggleButton(binding.btnMusic, false);
            toggleButton(binding.btnPodcasts, false);
            binding.nestedScrollView.setVisibility(View.VISIBLE);
            binding.tabList.setVisibility(View.GONE);
            Log.d(TAG, "Chọn tab Tất cả");
        });

        binding.btnMusic.setOnClickListener(v -> {
            toggleButton(binding.btnAll, false);
            toggleButton(binding.btnMusic, true);
            toggleButton(binding.btnPodcasts, false);
            binding.nestedScrollView.setVisibility(View.GONE);
            binding.tabList.setVisibility(View.VISIBLE);
            viewModel.getMusicSong().observe(getViewLifecycleOwner(), songs -> {
                tabListSongs = songs != null ? new ArrayList<>(songs) : new ArrayList<>();
                tabListAdapter.updateSongs(tabListSongs);
                Log.d(TAG, "Chọn tab Music, hiển thị " + tabListSongs.size() + " bài hát");
            });
        });

        binding.btnPodcasts.setOnClickListener(v -> {
            toggleButton(binding.btnAll, false);
            toggleButton(binding.btnMusic, false);
            toggleButton(binding.btnPodcasts, true);
            binding.nestedScrollView.setVisibility(View.GONE);
            binding.tabList.setVisibility(View.VISIBLE);
            viewModel.getPodcastSong().observe(getViewLifecycleOwner(), songs -> {
                tabListSongs = songs != null ? new ArrayList<>(songs) : new ArrayList<>();
                tabListAdapter.updateSongs(tabListSongs);
                Log.d(TAG, "Chọn tab Podcasts, hiển thị " + tabListSongs.size() + " podcast");
            });
        });
    }

    private void setupMusicControlBar() {
        binding.musicControlBar.setVisibility(View.GONE); // Ẩn mặc định
        binding.btnPlayPause.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MusicService.class);
            intent.setAction(isPlaying ? ACTION_PAUSE : ACTION_RESUME);
            requireActivity().startService(intent);
        });

        binding.musicControlBar.setOnClickListener(v -> {
            if (currentSong != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("current_song", currentSong);
                navController.navigate(R.id.action_homeFragment_to_musicPlayerFragment, bundle);
            }
        });
    }

    private Song currentSong;
    private boolean isPlaying;

    private void updateMusicControlBar(Song song, boolean isPlaying) {
        this.currentSong = song;
        this.isPlaying = isPlaying;

        // Kiểm tra binding để tránh NullPointerException
        if (binding == null) {
            Log.w(TAG, "Binding is null, cannot update music control bar");
            return;
        }

        if (song == null) {
            binding.musicControlBar.setVisibility(View.GONE);
            return;
        }

        binding.musicControlBar.setVisibility(View.VISIBLE);
        binding.currentSongTitle.setText(song.getTitle() != null ? song.getTitle() : "Không rõ tiêu đề");
        binding.currentSongArtist.setText(song.getArtist() != null ? song.getArtist() : "Không rõ nghệ sĩ");
        binding.btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    /**
     * @param button ToggleButton cần thay đổi.
     * @param checked Trạng thái checked.
     */
    private void toggleButton(ToggleButton button, boolean checked) {
        button.setChecked(checked);
        button.setBackgroundResource(checked ? R.drawable.button_background_selected : R.drawable.button_background_unselected);
    }

    private void observeData() {
        binding.loadingProgress.setVisibility(View.VISIBLE); // Hiển thị loading

        viewModel.getSections().observe(getViewLifecycleOwner(), sections -> {
            binding.loadingProgress.setVisibility(View.GONE); // Ẩn loading khi có dữ liệu

            if (sections == null) {
                Log.e(TAG, "Dữ liệu danh mục null");
                isDataLoaded = false;
                return;
            }

            myPlaylistSongs = sections.getOrDefault("my_playlist", new ArrayList<>());
            recentlyPlayedSongs = sections.getOrDefault("recently_played", new ArrayList<>());
            sonTungMtpSongs = sections.getOrDefault("son_tung_mtp", new ArrayList<>());
            hieuThuHaiSongs = sections.getOrDefault("hieuthuhai", new ArrayList<>());
            featuredSongs = sections.getOrDefault("featured_songs", new ArrayList<>());
            trendingSongs = sections.getOrDefault("trending_songs", new ArrayList<>());

            assert trendingSongs != null;
            Log.d(TAG, "Cập nhật dữ liệu: Playlist của tôi (" + myPlaylistSongs.size() + "), Gần đây (" + recentlyPlayedSongs.size() + "), Sơn Tùng M-TP (" + sonTungMtpSongs.size() + "), HIEUTHUHAI (" + hieuThuHaiSongs.size() + "), Nổi bật (" + featuredSongs.size() + "), Thịnh hành (" + trendingSongs.size() + ")");

            myPlaylistAdapter.updateSongs(new ArrayList<>(myPlaylistSongs));
            recentlyPlayedAdapter.updateSongs(new ArrayList<>(recentlyPlayedSongs));
            sonTungMtpAdapter.updateSongs(new ArrayList<>(sonTungMtpSongs));
            hieuThuHaiAdapter.updateSongs(new ArrayList<>(hieuThuHaiSongs));
            featuredSongsAdapter.updateSongs(new ArrayList<>(featuredSongs));
            trendingSongsAdapter.updateSongs(new ArrayList<>(trendingSongs));

            isDataLoaded = true;
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.loadingProgress.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi từ ViewModel: " + error);
                // Hiển thị lỗi trên UI
                com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), error, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
                isDataLoaded = false;
            }
        });
    }

    private void setupViewAllListeners() {
        setupViewAllListener(binding.myPlaylistViewAll, "Playlist của tôi", "my_playlist");
        setupViewAllListener(binding.recentlyPlayedViewAll, "Gần đây", "recently_played");
        setupViewAllListener(binding.sonTungMtpViewAll, "Sơn Tùng M-TP", "son_tung_mtp");
        setupViewAllListener(binding.hieuthuhaiViewAll, "HIEUTHUHAI", "hieuthuhai");
        setupViewAllListener(binding.featuredSongsViewAll, "Danh sách bài hát nổi bật", "featured_songs");
        setupViewAllListener(binding.trendingSongsViewAll, "Nhạc thịnh hành", "trending_songs");
    }

    private void setupViewAllListener(View view, String title, String sectionKey) {
        view.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("section_title", title);
            args.putParcelableArrayList("songs", new ArrayList<>(viewModel.getSongsBySection(sectionKey)));
            navController.navigate(R.id.action_homeFragment_to_artistSongsFragment, args);
        });
    }

    private void playSong(Song song) {
        if (song == null) return;

        if (!isDataLoaded) {
            Log.w(TAG, "Dữ liệu chưa tải xong, không thể phát bài hát");
            com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), "Đang tải dữ liệu, vui lòng thử lại sau", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireActivity(), MusicService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_SONG, song);
        requireActivity().startService(intent);

        Map<String, List<Song>> playlistMap = new HashMap<>();
        playlistMap.put("my_playlist", myPlaylistSongs);
        playlistMap.put("recently_played", recentlyPlayedSongs);
        playlistMap.put("son_tung_mtp", sonTungMtpSongs);
        playlistMap.put("hieuthuhai", hieuThuHaiSongs);
        playlistMap.put("featured_songs", featuredSongs);
        playlistMap.put("trending_songs", trendingSongs);
        playlistMap.put("tab_list", tabListSongs);

        playlistMap.forEach((sectionKey, songs) -> {
            int index = songs.indexOf(song);
            if (index != -1) {
                Intent broadcastIntent = new Intent(ACTION_SONG_STATE_CHANGED).setPackage(ACTION_SONG_STATE_CHANGED);
                broadcastIntent.putExtra(EXTRA_SONG, song);
                broadcastIntent.putExtra(EXTRA_IS_PLAYING, true);
                requireActivity().sendBroadcast(broadcastIntent);
            }
        });

        Bundle bundle = new Bundle();
        bundle.putParcelable("current_song", song);
        navController.navigate(R.id.action_homeFragment_to_musicPlayerFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isReceiverRegistered) {
            try {
                requireActivity().unregisterReceiver(songStateReceiver);
                isReceiverRegistered = false;
                Log.d(TAG, "Hủy đăng ký BroadcastReceiver trong onDestroyView");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "BroadcastReceiver không được đăng ký hoặc đã hủy đăng ký trước đó", e);
            }
        }
        binding = null;
    }
}