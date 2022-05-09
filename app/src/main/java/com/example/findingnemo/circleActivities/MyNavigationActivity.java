package com.example.findingnemo.circleActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findingnemo.generalActivities.MainActivity;
import com.example.findingnemo.generalActivities.ProfileActivity;
import com.example.findingnemo.R;
import com.example.findingnemo.geofencing.GeofenceHelper;
import com.example.findingnemo.googleMaps.GpsTracker;
import com.example.findingnemo.modelClasses.UserModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class MyNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
        , OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    String code, intentTo, sharing, nameUser, emailUser;
    Uri photoUri;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount acct;
    GoogleMap mMap;
    FirebaseAuth auth;
    TextView name, email;
    ImageView icon;
    Button okButton, yesButton, noButton;
    Dialog dialog;
    TextView textView;
    GeofencingClient geofencingClient;
    GeofenceHelper geofenceHelper;
    Double latitudeRefresh, longitudeRefresh, latCard, longCard, geoLat, geoLong;

    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    float GEOFENCE_RADIUS = 600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_navigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("321727690748-eb5mvpu5b5gq1h0gcvf34e5v3kv31e9s.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        assert acct != null;
        nameUser = acct.getDisplayName();
        emailUser = acct.getEmail();
        photoUri = acct.getPhotoUrl();

        Intent intent = getIntent();
        if (intent != null) {
            intentTo = getIntent().getStringExtra("intentFrom");
            if (intentTo.equals("onStart")) {
                code = getIntent().getStringExtra("userCode");
                latCard = getIntent().getDoubleExtra("latitudeFromStart", 0.0);
                longCard = getIntent().getDoubleExtra("longitudeFromStart", 0.0);
                sharing = "false";
            } else {
                sharing = getIntent().getStringExtra("isSharing");
                code = getIntent().getStringExtra("code");
                latCard = getIntent().getDoubleExtra("latitudeFromGoogle", 0.0);
                longCard = getIntent().getDoubleExtra("longitudeFromGoogle", 0.0);
            }

            geoLat = getIntent().getDoubleExtra("geoLat", 0.0);
            geoLong = getIntent().getDoubleExtra("geoLong", 0.0);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance("https://finding-nemo-3e2fd-default-rtdb.firebaseio.com/").getReference("users");

        reference.child(uid).child("code").setValue(code);
        reference.child(uid).child("email").setValue(emailUser);
        reference.child(uid).child("geofenceLat").setValue(geoLat);
        reference.child(uid).child("geofenceLong").setValue(geoLong);
        reference.child(uid).child("isSharing").setValue(sharing);
        reference.child(uid).child("uri").setValue(photoUri.toString());
        reference.child(uid).child("userId").setValue(uid);
        reference.child(uid).child("userLatitude").setValue(latCard);
        reference.child(uid).child("userLongitude").setValue(longCard);
        reference.child(uid).child("userName").setValue(nameUser);

        dialog = new Dialog(MyNavigationActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_geofence_dialog);

        if (geoLat != 0 && geoLong != 0) {
            dialog.dismiss();
        } else {
            dialog.create();
            dialog.show();
        }

        textView = dialog.findViewById(R.id.geofenceText);
        noButton = dialog.findViewById(R.id.noBtn);
        yesButton = dialog.findViewById(R.id.yesBtn);
        okButton = dialog.findViewById(R.id.okBtn);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText(R.string.texts);
                yesButton.setVisibility(View.GONE);
                noButton.setVisibility(View.GONE);
                okButton.setVisibility(View.VISIBLE);
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        auth = FirebaseAuth.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        name = header.findViewById(R.id.userName);
        email = header.findViewById(R.id.userEmail);
        icon = header.findViewById(R.id.userIcon);

        name.setText(acct.getDisplayName());
        email.setText(acct.getEmail());
        Picasso.get().load(acct.getPhotoUrl()).into(icon);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
    }

    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MyNavigationActivity.this);
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
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(MyNavigationActivity.this, ProfileActivity.class);
            intent.putExtra("latitude", latCard);
            intent.putExtra("longitude", longCard);
            startActivity(intent);

        } else if (id == R.id.nav_groups) {
            Intent intent = new Intent(MyNavigationActivity.this, MyGroupActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_join) {
            Intent intent3 = new Intent(MyNavigationActivity.this, JoinGroupActivity.class);
            startActivity(intent3);

        } else if (id == R.id.nav_invite) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, "My Invitation Code is: \n" + code);
            startActivity(Intent.createChooser(i, "\bUse your invite code.\b Share Using: "));
        } else if (id == R.id.nav_logout) {
            if (acct != null) {
                FirebaseAuth.getInstance().signOut();

                mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(MyNavigationActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Session not close", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else if (id == R.id.add_geofence) {
            View parentLayout = findViewById(android.R.id.content);
            Snackbar.make(parentLayout, "Please press for a few seconds on map to add geofence.", Snackbar.LENGTH_LONG)
                    .show();
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (geoLat != 0 && geoLong != 0) {
            LatLng latLngGeofence = new LatLng(geoLat, geoLong);
            addMarker(latLngGeofence);
            addCircle(latLngGeofence, GEOFENCE_RADIUS);
            addGeofence(latLngGeofence, GEOFENCE_RADIUS);
        }

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MyNavigationActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        GpsTracker gpsTracker = new GpsTracker(MyNavigationActivity.this);
        if (gpsTracker.canGetLocation()) {
            latitudeRefresh = gpsTracker.getLatitudeFromNetwork();
            longitudeRefresh = gpsTracker.getLongitudeFromNetwork();
        } else {
            gpsTracker.showSettingsAlert();
            return;
        }

        LatLng currentLocation = new LatLng(latitudeRefresh, longitudeRefresh);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

        enableUserLocation();

        mMap.setOnMapLongClickListener(MyNavigationActivity.this);

        enableUserLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
            } else {
                //We do not have the permission..

            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //Ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                handleMapLongClick(latLng);
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("geofenceLat").setValue(latLng.latitude);
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("geofenceLong").setValue(latLng.longitude);

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            }

        } else {
            handleMapLongClick(latLng);
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("geofenceLat").setValue(latLng.latitude);
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("geofenceLong").setValue(latLng.longitude);
        }

    }

    private void handleMapLongClick(LatLng latLng) {
        mMap.clear();
        addMarker(latLng);
        addCircle(latLng, GEOFENCE_RADIUS);
        addGeofence(latLng, GEOFENCE_RADIUS);
        Toast.makeText(getApplicationContext(), "Geofence Added.", Toast.LENGTH_SHORT).show();
    }

    private void addGeofence(LatLng latLng, float radius) {

        String GEOFENCE_ID = "SOME_GEOFENCE_ID";
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "onFailure: " + errorMessage);
                    }
                });
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }
}