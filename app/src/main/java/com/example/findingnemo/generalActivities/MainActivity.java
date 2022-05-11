package com.example.findingnemo.generalActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.findingnemo.R;
import com.example.findingnemo.circleActivities.MembersAdapter;
import com.example.findingnemo.circleActivities.NotificationCheckPreference;
import com.example.findingnemo.circleActivities.Util;
import com.example.findingnemo.geofencing.GeofenceLocationService;
import com.example.findingnemo.googleMaps.GpsTracker;
import com.example.findingnemo.circleActivities.MyNavigationActivity;
import com.example.findingnemo.modelClasses.UpdatingLocations;
import com.example.findingnemo.modelClasses.UserModel;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.ArrayList;
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
    PermissionManager permission;
    GpsTracker gpsTracker;
    Double latitudeRefresh, longitudeRefresh;

    private static final int MY_FINE_LOCATION_REQUEST = 99;
    private static final int MY_BACKGROUND_LOCATION_REQUEST = 100;

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
                permission = new PermissionManager() {
                };
                permission.checkAndRequestPermissions(this);

                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String userCode = snapshot.child("code").getValue(String.class);
                                Double latCard = snapshot.child("userLatitude").getValue(Double.class);
                                Double longCard = snapshot.child("userLongitude").getValue(Double.class);
                                Double latGeo = snapshot.child("geofenceLat").getValue(Double.class);
                                Double longGeo = snapshot.child("geofenceLong").getValue(Double.class);
                                intentFrom = "onStart";

                                Intent intent = new Intent(MainActivity.this, MyNavigationActivity.class);
                                intent.putExtra("userCode", userCode);
                                intent.putExtra("intentFrom", intentFrom);

                                intent.putExtra("latitudeFromStart", latCard);
                                intent.putExtra("longitudeFromStart", longCard);
                                intent.putExtra("geoLat", latGeo);
                                intent.putExtra("geoLong", longGeo);

                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

//                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestIdToken("27273984511-ljcd4cm9ccae3e758e9fl37d57sq5me3.apps.googleusercontent.com")
//                        .requestEmail()
//                        .build();
//
//                mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
//                if (acct != null) {
//                    mAuth.signOut();
//
//                    mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                Toast.makeText(getApplicationContext(), "Signed out from google", Toast.LENGTH_SHORT).show();
//                                startActivity(intent);
//                                finish();
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Session not closed", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                }

            } else {
                progressBar.setVisibility(View.GONE);
                buttonGoogle.setEnabled(true);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to your internet", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBarSignBtn);
        mAuth = FirebaseAuth.getInstance();

        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

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

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String userCode = snapshot.child("code").getValue(String.class);
                                        Double latCard = snapshot.child("userLatitude").getValue(Double.class);
                                        Double longCard = snapshot.child("userLongitude").getValue(Double.class);
                                        Double latGeo = snapshot.child("geofenceLat").getValue(Double.class);
                                        Double longGeo = snapshot.child("geofenceLong").getValue(Double.class);
                                        intentFrom = "onStart";

                                        Intent intent = new Intent(MainActivity.this, MyNavigationActivity.class);
                                        intent.putExtra("userCode", userCode);
                                        intent.putExtra("intentFrom", intentFrom);

                                        intent.putExtra("latitudeFromStart", latCard);
                                        intent.putExtra("longitudeFromStart", longCard);
                                        intent.putExtra("geoLat", latGeo);
                                        intent.putExtra("geoLong", longGeo);

                                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(MainActivity.this, MyNavigationActivity.class);

                                        Random r = new Random();
                                        int n = 100000 + r.nextInt(900000);
                                        String code = String.valueOf(n);

                                        intentFrom = "google";
                                        gpsTracker = new GpsTracker(MainActivity.this);
                                        if (gpsTracker.canGetLocation()) {
                                            latitudeRefresh = gpsTracker.getLatitudeFromNetwork();
                                            longitudeRefresh = gpsTracker.getLongitudeFromNetwork();
                                        } else {
                                            gpsTracker.showSettingsAlert();
                                        }

                                        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("code").setValue(code);
                                        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("userLatitude").setValue(latitudeRefresh);
                                        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("userLongitude").setValue(longitudeRefresh);
                                        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("geofenceLat").setValue(0);
                                        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("geofenceLong").setValue(0);

                                        intent.putExtra("latitudeFromGoogle", latitudeRefresh);
                                        intent.putExtra("longitudeFromGoogle", longitudeRefresh);

                                        intent.putExtra("code", code);
                                        intent.putExtra("isSharing", "false");
                                        intent.putExtra("intentFrom", intentFrom);
                                        intent.putExtra("geoLat", 0);
                                        intent.putExtra("geoLong", 0);

                                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_BACKGROUND_LOCATION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_FINE_LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestBackgroundLocationPermission();
                    }
                }

            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
            return;
        }
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
                        NotificationCheckPreference.setNotificationSent(getApplicationContext(), false);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationCheckPreference.setNotificationSent(getApplicationContext(), false);
    }
}

