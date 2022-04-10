package com.example.findingnemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    LinearLayout buttonGoogle;
    FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;
    ProgressBar progressBar;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseUser acct;
    AuthCredential credential;
    String intentFrom;

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;


    @Override
    public void onStart() {
        super.onStart();
        buttonGoogle.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        acct = FirebaseAuth.getInstance().getCurrentUser();
        if (isNetwork(getApplicationContext())) {
            if (acct != null) {
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String userCode = snapshot.child("code").getValue(String.class);
                        intentFrom = "onStart";

                        Intent intent = new Intent(MainActivity.this, MyNavigation.class);
                        intent.putExtra("userCode", userCode);
                        intent.putExtra("intentFrom",intentFrom);

                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } else {
                progressBar.setVisibility(View.GONE);
                buttonGoogle.setEnabled(true);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to your internet", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBarSignBtn);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("321727690748-eb5mvpu5b5gq1h0gcvf34e5v3kv31e9s.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        buttonGoogle = findViewById(R.id.googleSignin);
        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetwork(getApplicationContext())) {
                    signIn();
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect to your internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.clearDefaultAccountAndReconnect();
        }
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public boolean isNetwork(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();

        fAuth.fetchSignInMethodsForEmail(Objects.requireNonNull(account.getEmail())).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if(task.isSuccessful()){
                    onStart();
                } else{
                    mAuth.signInWithEmailAndPassword(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(MainActivity.this, InvitationCode.class);
                                Random r = new Random();
                                int n = 100000 + r.nextInt(900000);
                                String code = String.valueOf(n);

                                intentFrom = "google";

                                intent.putExtra("code", code);
                                intent.putExtra("isSharing", false);
                                intent.putExtra("intentFrom", intentFrom);

                                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("code").setValue(code);

                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                progressBar.setVisibility(View.GONE);

                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("Are you sure you want to exit?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                        finish();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }
}
