package com.example.musicapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.musicapp.dbhelper.DatabaseHelper;
import com.google.android.material.textfield.TextInputLayout;

public class ResetPasswordActivity extends AppCompatActivity {
    private TextView email, backReset;
    private EditText passwordResetText, confirmPasswordResetText;
    private Button resetPasswordBtn;
    private DatabaseHelper dbHelper;
    private TextInputLayout passwordResetInput, passwordConfirmResetInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Bind các view
        email = findViewById(R.id.email);
        backReset = findViewById(R.id.backReset);
        passwordResetText = findViewById(R.id.passwordResetText);
        confirmPasswordResetText = findViewById(R.id.passwordConfirmResetText);
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        passwordResetInput = findViewById(R.id.passwordResetInput);
        passwordConfirmResetInput = findViewById(R.id.passwordConfirmResetInput);

        // Hiển thị email từ Intent
        String email1 = getIntent().getStringExtra("email");
        email.setText(email1);

        // Xử lý sự kiện nút "Go back"
        backReset.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        // Xử lý sự kiện nút "Reset Password"
        resetPasswordBtn.setOnClickListener(v -> {
            String password = passwordResetText.getText().toString().trim();
            String confirmPassword = confirmPasswordResetText.getText().toString().trim();

            // Xóa lỗi cũ
            passwordResetInput.setError(null);
            passwordConfirmResetInput.setError(null);

            // Kiểm tra lỗi
            boolean hasError = false;

            if (password.isEmpty()) {
                passwordResetInput.setError("Password is required");
                passwordResetText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(passwordResetText, InputMethodManager.SHOW_IMPLICIT);
                hasError = true;
            }
            if (confirmPassword.isEmpty()) {
                passwordConfirmResetInput.setError("Confirm password is required");
                confirmPasswordResetText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(confirmPasswordResetText, InputMethodManager.SHOW_IMPLICIT);
                hasError = true;
            }
            if (!password.equals(confirmPassword)) {
                passwordConfirmResetInput.setError("Passwords do not match");
                confirmPasswordResetText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(confirmPasswordResetText, InputMethodManager.SHOW_IMPLICIT);
                hasError = true;
            }

            // Chỉ cập nhật nếu không có lỗi
            if (!hasError) {
                Boolean check = dbHelper.updatePassword(email1, password);
                if (check) {
                    passwordResetText.setText("");
                    confirmPasswordResetText.setText("");
                    Toast.makeText(ResetPasswordActivity.this, "Reset Password Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Error in reset password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}