package com.example.findingnemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

public class SignUpActivity extends AppCompatActivity {

    EditText nName, nEmail, nPassword, nPassword2;
    Button nRegisterBtn;
    TextView nClickLogin;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nName = findViewById(R.id.editName);
        nEmail = findViewById(R.id.editEmailAddress);
        nPassword = findViewById(R.id.editPassword);
        nPassword2 = findViewById(R.id.editRePassword);
        nRegisterBtn = findViewById(R.id.signUpBtn);
        nClickLogin = findViewById(R.id.alreadyCreatedAccount);

        progressBar = findViewById(R.id.progressBarSignup);
        firebaseAuth = FirebaseAuth.getInstance();

        nRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);

                String name = nName.getText().toString().toLowerCase().trim();
                String email = nEmail.getText().toString().trim();
                String password = nPassword.getText().toString().trim();
                String password2 = nPassword2.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    nEmail.setError("* Required Field!");
                    nEmail.setBackgroundColor(R.color.red);
                    return;
                }

                if(TextUtils.isEmpty(name)){
                    nName.setError("* Required Field!");
                    nName.setBackgroundColor(R.color.red);
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    nPassword.setError("* Required Field!");
                    nPassword.setBackgroundColor(R.color.red);
                    return;
                } else if(password.length() < 8){
                    nPassword.setError("Password must have more than 6 characters!");
                    return;
                }

                if(TextUtils.isEmpty(password2)){
                    nPassword2.setError("* Required Field!");
                    nPassword2.setBackgroundColor(R.color.red);
                    return;
                } else if(password2.length() < 8){
                    nPassword2.setError("Password must have more than 8 characters!");
                    return;
                }

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SignUpActivity.this, "User Added", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        } else{
                            Toast.makeText(SignUpActivity.this, "Error:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}