package com.example.musicapp.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.example.musicapp.dbhelper.DatabaseHelper;


public class SignInFragment extends Fragment {

    private TextView dontHaveAnAccount, resetPassword;
    private EditText editTextEmail, editTextPassword;
    private Button buttonSignIn;
    private DatabaseHelper dbHelper;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        dontHaveAnAccount = view.findViewById(R.id.dont_have_an_account);
        resetPassword = view.findViewById(R.id.reset_password);
        editTextEmail = view.findViewById(R.id.emailTextLogin);
        editTextPassword = view.findViewById(R.id.passwordTextLogin);
        buttonSignIn = view.findViewById(R.id.LoginBtn);
        dbHelper = new DatabaseHelper(getActivity());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        navController = Navigation.findNavController(view);

        dontHaveAnAccount.setOnClickListener(v -> navController.navigate(R.id.action_signInFragment_to_signUpFragment));
        resetPassword.setOnClickListener(v -> navController.navigate(R.id.action_signInFragment_to_resetPasswordFragment));
        buttonSignIn.setOnClickListener(v -> userLogin());
    }

    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }
        if (!email.contains("@")) {
            editTextEmail.setError("Invalid email format");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (dbHelper.isUserLogin(email, password)) {
            Cursor cursor = dbHelper.findUser(email);
            String username = "";
            if (cursor != null && cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndexOrThrow("USER_NAME"));

            } else {
                Log.e("HomeFragment", "User not found or cursor is null");
                Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
//            if(((MainActivity) getActivity()).checkConnection()) {
//                Toast.makeText(getActivity(), "Login Successful", Toast.LENGTH_SHORT).show();
//
//            }else {
//                Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
//            }

            ((MainActivity) requireActivity()).onLoginSuccess(email, username);



        } else {
            Toast.makeText(getActivity(), "Invalid email or password", Toast.LENGTH_SHORT).show();
            editTextEmail.setError("Invalid email or password");
            editTextEmail.requestFocus();
        }

        editTextEmail.setText("");
        editTextPassword.setText("");
    }
}