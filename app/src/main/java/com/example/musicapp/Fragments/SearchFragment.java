package com.example.musicapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.musicapp.R;
import com.example.musicapp.adapters.SongAdapter;
import com.example.musicapp.databinding.FragmentSearchBinding;
import com.example.musicapp.entity.Song;
import com.example.musicapp.service.MusicService;
import com.example.musicapp.viewmodels.SearchViewModel;
import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final String KEY_QUERY = "search_query";
    private static final String ACTION_PLAY = "com.example.musicapp.PLAY";
    private static final String EXTRA_SONG = "extra_song";

    private FragmentSearchBinding binding;
    private SongAdapter songAdapter;
    private SearchViewModel viewModel;
    private final List<Song> searchResults = new ArrayList<>();
    private String lastQuery;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        if (savedInstanceState != null) {
            lastQuery = savedInstanceState.getString(KEY_QUERY);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setupRecyclerView();
        setupSearchView();
        observeData();
        if (lastQuery != null && !lastQuery.isEmpty()) {
            viewModel.searchSongs(lastQuery);
        }

        return view;
    }


    private void setupRecyclerView() {
        songAdapter = new SongAdapter(searchResults, this::playSong, true);
        binding.searchResultsList.setAdapter(songAdapter);
        binding.searchResultsList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
    }


    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.searchSongs(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    viewModel.searchSongs(newText);
                }
                return true;
            }
        });
    }


    private void observeData() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), songs -> {
            searchResults.clear();
            if (songs != null && !songs.isEmpty()) {
                searchResults.addAll(songs);
                binding.noResultsText.setVisibility(View.GONE);
                Log.d(TAG, "Tìm thấy " + songs.size() + " bài hát cho truy vấn: " + viewModel.getLastQuery());
            } else {
                binding.noResultsText.setVisibility(View.VISIBLE);
                binding.noResultsText.setText("Không tìm thấy bài hát");
                Log.w(TAG, "Không tìm thấy bài hát cho truy vấn: " + viewModel.getLastQuery());
            }
            songAdapter.updateSongs(searchResults);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), error, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
            }
        });
    }


    private void playSong(Song song) {
        if (song == null) return;

        Intent intent = new Intent(getActivity(), MusicService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_SONG, song);
        requireActivity().startService(intent);
        Log.d(TAG, "Phát bài hát: " + song.getTitle());

        Bundle bundle = new Bundle();
        bundle.putParcelable("current_song", song);
        androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireView());
        navController.navigate(R.id.action_searchFragment_to_musicPlayerFragment, bundle);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_QUERY, viewModel.getLastQuery());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}