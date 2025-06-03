package com.example.musicapp.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.musicapp.R;
import com.example.musicapp.dbhelper.DatabaseHelper;

public class SignUpFragment extends Fragment {
    private TextView alreadyHaveAnAccount;
    private EditText userText, emailText, passwordText, passwordConfirmText;
    private Button signupBtn;
    private DatabaseHelper dbHelper;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        alreadyHaveAnAccount = view.findViewById(R.id.already_have_account);
        userText = view.findViewById(R.id.userText);
        emailText = view.findViewById(R.id.emailText);
        passwordText = view.findViewById(R.id.passwordText);
        passwordConfirmText = view.findViewById(R.id.passwordConfirmText);
        signupBtn = view.findViewById(R.id.signupBtn);
        dbHelper = new DatabaseHelper(getActivity());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        alreadyHaveAnAccount.setOnClickListener(v -> navController.navigate(R.id.action_signUpFragment_to_signInFragment));
        signupBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = userText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordConfirm = passwordConfirmText.getText().toString();

        if (TextUtils.isEmpty(username)) {
            userText.setError("Username is missing");
            userText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailText.setError("Email is missing");
            emailText.requestFocus();
            return;
        }
        if (dbHelper.checkEmail(email)) {
            emailText.setError("Email đã được sử dụng");
            emailText.requestFocus();
            return;
        }
        if (!email.contains("@")) {
            emailText.setError("Invalid email");
            emailText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordText.setError("Password is missing");
            passwordText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(passwordConfirm)) {
            passwordConfirmText.setError("Password is missing");
            passwordConfirmText.requestFocus();
            return;
        }
        if (!password.equals(passwordConfirm)) {
            passwordConfirmText.setError("Password does not match");
            passwordConfirmText.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordText.setError("Mật khẩu phải có ít nhất 6 ký tự");
            passwordText.requestFocus();
            return;
        }

        dbHelper.isUserRegister(username, email, password);
        Toast.makeText(getActivity(), "Register Successfully", Toast.LENGTH_SHORT).show();

        userText.setText("");
        emailText.setText("");
        passwordText.setText("");
        passwordConfirmText.setText("");

        navController.navigate(R.id.action_signUpFragment_to_signInFragment);
    }
}