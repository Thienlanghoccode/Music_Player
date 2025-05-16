package com.example.musicapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapters.SongAdapter;
import com.example.musicapp.entity.Song;
import com.example.musicapp.service.MusicService;
import com.example.musicapp.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class Playlist2Fragment extends Fragment {

    private static final String TAG = "Playlist2Fragment";
    private static final String ACTION_PLAY = "com.example.musicapp.PLAY";
    private static final String EXTRA_SONG = "extra_song";
    private static final String EXTRA_PLAYLIST = "extra_playlist";

    private RecyclerView recyclerView;
    private Button btnPlay;
    private SongAdapter songAdapter;
    private final List<Song> playlistSongs = new ArrayList<>();
    private HomeViewModel viewModel;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_playlist2, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        btnPlay = view.findViewById(R.id.btnPlay);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        navController = Navigation.findNavController(requireActivity(), R.id.fragment_container);

        setupRecyclerView();
        loadPlaylistData();
        setupPlayButton();

        return view;
    }

    private void setupRecyclerView() {
        songAdapter = new SongAdapter(playlistSongs, this::playSong, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(songAdapter);
    }

    private void loadPlaylistData() {
        viewModel.getSections().observe(getViewLifecycleOwner(), sections -> {
            if (sections == null) return;
            List<Song> songs = sections.getOrDefault("top_50_vietnam", new ArrayList<>());
            playlistSongs.clear();
            assert songs != null;
            playlistSongs.addAll(songs.size() > 50 ? songs.subList(0, 50) : songs);
            songAdapter.updateSongs(playlistSongs);
        });
    }

    private void setupPlayButton() {
        btnPlay.setOnClickListener(v -> {
            if (!playlistSongs.isEmpty()) {

                Intent intent = new Intent(requireActivity(), MusicService.class);
                intent.setAction(ACTION_PLAY);
                intent.putExtra(EXTRA_SONG, playlistSongs.get(0));
                intent.putParcelableArrayListExtra(EXTRA_PLAYLIST, new ArrayList<>(playlistSongs));
                requireActivity().startService(intent);

                Bundle bundle = new Bundle();
                bundle.putParcelable("current_song", playlistSongs.get(0));
                navController.navigate(R.id.action_playlist2Fragment_to_musicPlayerFragment, bundle);
            }
        });
    }

    private void playSong(Song song) {
        Intent intent = new Intent(requireActivity(), MusicService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_SONG, song);
        requireActivity().startService(intent);

        Bundle bundle = new Bundle();
        bundle.putParcelable("current_song", song);
        navController.navigate(R.id.action_playlist2Fragment_to_musicPlayerFragment, bundle);
    }
}