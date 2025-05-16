package com.example.musicapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.example.musicapp.MainActivity;
import com.example.musicapp.R;


public class InfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_info, container, false);
        ImageButton btnBackInfo = view.findViewById(R.id.btnBackInfo);
        btnBackInfo.setOnClickListener(v -> navigateBack());
        return view;
    }
    private void navigateBack() {
        ((MainActivity) requireActivity()).gotoHome();
    }
}