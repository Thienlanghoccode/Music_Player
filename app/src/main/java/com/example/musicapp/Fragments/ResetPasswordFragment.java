package com.example.musicapp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicapp.R;
import com.example.musicapp.ResetPasswordActivity;
import com.example.musicapp.dbhelper.DatabaseHelper;

import java.util.Objects;

public class ResetPasswordFragment extends Fragment {

    private TextView back;
    private EditText emailResetText;
    private Button resetBtn;
    private DatabaseHelper dbHelper;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_reset_password, container, false);
        back= view.findViewById(R.id.back);
        FrameLayout frameLayout = requireActivity().findViewById(R.id.register_frame_layout);
        emailResetText = view.findViewById(R.id.emailResetText);
        resetBtn = view.findViewById(R.id.resetBtn);
        dbHelper = new DatabaseHelper(getActivity());

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            navController = Navigation.findNavController(view);
        } catch (IllegalStateException e) {
            Toast.makeText(requireContext(), "Lỗi điều hướng", Toast.LENGTH_SHORT).show();
            Log.e("MyApp", "Lỗi khi xử lý: " + e.getMessage(), e);
            return;
        }

        back.setOnClickListener(v -> navController.navigate(R.id.action_resetPasswordFragment_to_signInFragment));

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email= emailResetText.getText().toString();
                if(email.isEmpty()){
                    emailResetText.setError("email is missing");
                    emailResetText.setFocusable(true);
                }
                else if(!email.contains("@")){
                    emailResetText.setError("invalid email");
                }

                boolean check = dbHelper.checkEmail(email);
                if(check){
                    emailResetText.setText("");
                    Intent intent= new Intent(getActivity(), ResetPasswordActivity.class);
                    intent.putExtra("email",email);
                    startActivity(intent);
                    requireActivity().finish();
                }
                else {
                    Toast.makeText(getActivity(), "email not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    private void setFragment(Fragment fragment){
//        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.setCustomAnimations(R.anim.from_left,R.anim.out_from_right);
//        fragmentTransaction.replace(frameLayout.getId(),fragment);
//        fragmentTransaction.commit();
//    }
}