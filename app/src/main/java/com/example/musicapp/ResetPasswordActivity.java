package com.example.musicapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.musicapp.dbhelper.DatabaseHelper;

public class ResetPasswordActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private EditText passwordResetText,confirmPasswordResetText;
    private DatabaseHelper dbHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        TextView email = findViewById(R.id.email);
        TextView backReset = findViewById(R.id.backReset);
        passwordResetText= findViewById(R.id.passwordResetText);
        confirmPasswordResetText= findViewById(R.id.passwordConfirmResetText);
        Button resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        backReset = findViewById(R.id.backReset);
        String email1= getIntent().getStringExtra("email");
        email.setText(email1);
        dbHelper = new DatabaseHelper(this);
        backReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(ResetPasswordActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password= passwordResetText.getText().toString();
                String confirmPassword= confirmPasswordResetText.getText().toString();
                if(password.isEmpty()){
                    passwordResetText.setError("password is missing");
                    passwordResetText.setFocusable(true);
                }
                else if(confirmPassword.isEmpty()){
                    confirmPasswordResetText.setError("confirm password is missing");
                }
                else if(!password.equals(confirmPassword)){
                    confirmPasswordResetText.setError("password does not match");
                }
                boolean check= dbHelper.updatePassword(email1,password);
                if(check){
                    passwordResetText.setText("");
                    confirmPasswordResetText.setText("");
                    Toast.makeText(ResetPasswordActivity.this, "Reset Password Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent= new Intent(ResetPasswordActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(ResetPasswordActivity.this, "Error in reset password", Toast.LENGTH_SHORT).show();

                }
            }
        });




    }

}