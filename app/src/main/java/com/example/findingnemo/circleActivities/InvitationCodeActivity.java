package com.example.findingnemo.circleActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findingnemo.R;
import com.example.findingnemo.googleMaps.MyNavigationActivity;
import com.example.findingnemo.modelClasses.UserModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InvitationCodeActivity extends AppCompatActivity {

    String name, email, code, oldLatitude, oldLongitude;
    Uri photoUri;
    TextView inviteCode;
    Button nextButton;
    Boolean isSharing;
    UserModel users;
    ProgressBar progressBar;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount acct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_code);

        inviteCode = findViewById(R.id.inviteCode);
        nextButton = findViewById(R.id.next_btn);
        progressBar = findViewById(R.id.progressBarRegisterBtn);

        Intent intent = getIntent();
        if(intent!=null){
            oldLatitude = intent.getStringExtra("latitudeFromGoogle");
            oldLongitude = intent.getStringExtra("longitudeFromGoogle");
            code = intent.getStringExtra("code");
            isSharing = intent.getBooleanExtra("isSharing", false);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("321727690748-eb5mvpu5b5gq1h0gcvf34e5v3kv31e9s.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        assert acct != null;
        name = acct.getDisplayName();
        email = acct.getEmail();
        photoUri = acct.getPhotoUrl();

        assert photoUri != null;

        inviteCode.setText(code);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user.getUid();

                DatabaseReference reference = FirebaseDatabase.getInstance("https://finding-nemo-3e2fd-default-rtdb.firebaseio.com/").getReference("users");

//                users = new UserModel(name, email, code, photoUri.toString(), isSharing.toString(), oldLatitude, oldLongitude, uid);

                reference.child(uid).setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Intent intent1 = new Intent(InvitationCodeActivity.this, MyNavigationActivity.class);
                            Toast.makeText(getApplicationContext(), "User Registered successfully.", Toast.LENGTH_SHORT).show();
                            startActivity(intent1);
                        } else {
                            Toast.makeText(getApplicationContext(), "Registration unsuccessful.", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

    }
}