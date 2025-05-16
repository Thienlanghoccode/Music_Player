package com.example.musicapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.musicapp.R;

public class LibraryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        Button btnPlaylist1 = view.findViewById(R.id.btnRounded);
        Button btnPlaylist2 = view.findViewById(R.id.btnPlaylist2);
        Button btnPlaylist3 = view.findViewById(R.id.btnPlaylist3);
        Button btnPlaylist4 = view.findViewById(R.id.btnPlaylist4);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_container);

        btnPlaylist1.setOnClickListener(v -> {
            navController.navigate(R.id.action_libraryFragment_to_playlist1Fragment);
        });

        btnPlaylist2.setOnClickListener(v -> {
            navController.navigate(R.id.action_libraryFragment_to_playlist2Fragment);
        });

        btnPlaylist3.setOnClickListener(v -> {
            navController.navigate(R.id.action_libraryFragment_to_playlist3Fragment);
        });

        btnPlaylist4.setOnClickListener(v -> {
            navController.navigate(R.id.action_libraryFragment_to_playlist4Fragment);
        });

        return view;
    }
}