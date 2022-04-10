package com.example.findingnemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText mEmail, mPassword;
    Button mLogin;
    TextView mClickSignup;
    ProgressBar mProgressBar;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.editEmail);
        mPassword = findViewById(R.id.editPasswords);
        mLogin = findViewById(R.id.loginBtn);
        mClickSignup = findViewById(R.id.notCreatedAccount);
        mProgressBar = findViewById(R.id.progressBarLogin);

        fAuth = FirebaseAuth.getInstance();

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Required Field!");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Required Field!");
                    return;
                } else if (password.length() < 8) {
                    mPassword.setError("Password must have more than 6 characters!");
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);

//                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(LoginActivity.this, "User Logged In!", Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(getApplicationContext(), GroupChoice.class));
//                        } else {
//                            Toast.makeText(LoginActivity.this, "Error:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                            mProgressBar.setVisibility(View.GONE);
//                        }
//                    }
//                });
            }
        });

        mClickSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }
}
