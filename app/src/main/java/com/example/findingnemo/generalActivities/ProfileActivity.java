package com.example.findingnemo.generalActivities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.findingnemo.R;
import com.example.findingnemo.circleActivities.NotificationCheckPreference;
import com.example.findingnemo.googleMaps.GpsTracker;
import com.example.findingnemo.googleMaps.MapsActivity2;
import com.example.findingnemo.modelClasses.Location;
import com.example.findingnemo.modelClasses.UploadImage;
import com.example.findingnemo.modelClasses.UserModelClass;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    TextView latitudes, longitudes, boldName;
    TextInputLayout mNameEditText, mEmailEditText, lastLatEditText, lastLongEditText, shareLocationEditText;
    String lat, lng, oldLatitude, oldLongitude, latRefresh, longRefresh;
    ImageView addImage;
    FirebaseAuth firebaseAuth;
    Button logoutBtn, refreshButton, locBtn, updateLocButton, navigateBtn, shareLocationBtn, sendBtn;
    GpsTracker gpsTracker;
    Double latitude, longitude, latitudeRefresh, longitudeRefresh;
    FirebaseUser currentUser;
    String time, newLatitude, newLongitude, id;
    Boolean isButtonClicked = false;
    FrameLayout frameLayout;
    EditText editText;
    Boolean visibility_Flag;
    LinearLayout linearLayout;
    String FCM_API = "https://fcm.googleapis.com/fcm/send";
    String serverKey =
            "key=" + "AAAABlmn-f8:APA91bFdofVriN4LhIoa_yHSFlu6OtgTZvm1HV1oUCF5gDRccmCuEJJ0vsZMgVFUpJcJYBqbUIV8lQdeEVtewMLgNbVGRoWdPiO_tgnsWQ-SYgXojXKv0qxalCkAGrGNWk2_eDvnc4F0";
    String contentType = "application/json";
    RequestQueue mRequestQueue;
    ImageButton backButton;

    String personName, personEmail;
    UserModelClass userModelClass;
    GoogleSignInAccount acct;
    GoogleSignInClient mGoogleSignInClient;
    DatabaseReference reference;
    StorageReference storageReference, fileReference;
    List<Location> userLocations;
    Location locations;
    RelativeLayout relativeLayout;
    Uri imageUri;
    ProgressBar progressBar;
    Boolean isUploaded = false;

    public static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        boldName = findViewById(R.id.nameBoldProfile);
        logoutBtn = findViewById(R.id.logoutButton);
        latitudes = findViewById(R.id.latProfile);
        longitudes = findViewById(R.id.longProfile);
        refreshButton = findViewById(R.id.refreshLocation);
        locBtn = findViewById(R.id.lastLocation);
        updateLocButton = findViewById(R.id.updateLocation);
        navigateBtn = findViewById(R.id.navigateLocation);
        shareLocationBtn = findViewById(R.id.shareButton);
        sendBtn = findViewById(R.id.send);
        frameLayout = findViewById(R.id.layout);
        mNameEditText = findViewById(R.id.layout1);
        editText = findViewById(R.id.title);
        mEmailEditText = findViewById(R.id.layout2);
        linearLayout = findViewById(R.id.linearLayout);
        backButton = findViewById(R.id.backBtn);

        lastLongEditText = findViewById(R.id.layout8);
        lastLatEditText = findViewById(R.id.layout9);
        shareLocationEditText = findViewById(R.id.layout11);
//        relativeLayout = findViewById(R.id.anotherRelativeLayout);
        progressBar = findViewById(R.id.progressBarIcon);

        firebaseAuth = FirebaseAuth.getInstance();
        userModelClass = new UserModelClass();

        Intent intent = getIntent();

        Double latDouble = intent.getDoubleExtra("latitude", 0.0);
        Double longDouble = intent.getDoubleExtra("longitude", 0.0);

        oldLatitude = String.valueOf(latDouble);
        oldLongitude = String.valueOf(longDouble);

        Toast.makeText(ProfileActivity.this, "Please press refresh button to get the current location", Toast.LENGTH_LONG).show();

        currentUser = firebaseAuth.getCurrentUser();
        locations = new Location();
        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        visibility_Flag = false;
        mRequestQueue = Volley.newRequestQueue(this);
        FirebaseMessaging.getInstance().subscribeToTopic("news");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("27273984511-ljcd4cm9ccae3e758e9fl37d57sq5me3.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        reference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("userUploads");

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        navigateBtn.setEnabled(false);
        reference.child(id).child("locations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    navigateBtn.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gpsTracker = new GpsTracker(ProfileActivity.this);
        if (gpsTracker.canGetLocation()) {
            latitudeRefresh = gpsTracker.getLatitudeFromNetwork();
            longitudeRefresh = gpsTracker.getLongitudeFromNetwork();
            newLatitude = String.valueOf(latitudeRefresh);
            newLongitude = String.valueOf(longitudeRefresh);
        } else {
            gpsTracker.showSettingsAlert();
        }

        progressBar.setVisibility(View.VISIBLE);
        reference.child(id).child("uri").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String image = snapshot.getValue(String.class);
                    addImage = findViewById(R.id.imageAddImage);
                    Picasso.get().load(image).into(addImage);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showAllUserData();
                progressBar.setVisibility(View.GONE);
            }
        });

        showAllUserData();

        locBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (visibility_Flag) {
                    linearLayout.setVisibility(View.GONE);
                    visibility_Flag = false;
                } else {
                    linearLayout.setVisibility(View.VISIBLE);
                    visibility_Flag = true;
                }
            }
        });

        userLocations = new ArrayList<Location>();

        reference.child(id).child("locations").child("0").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String latitudeString = null, longitudeString = null;
                if (snapshot.exists()) {
                    if (userLocations.size() < 4) {
                        for (DataSnapshot dss : snapshot.getChildren()) {
                            latitudeString = dss.child("latitude").getValue().toString();
                            longitudeString = dss.child("longitude").getValue().toString();
                            String time = dss.child("time").getValue().toString();
                            userLocations.add(userLocations.size(), new Location(latitudeString, longitudeString, time));
                        }
                        lastLongEditText.getEditText().setText(longitudeString);
                        lastLatEditText.getEditText().setText(latitudeString);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        updateLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker = new GpsTracker(ProfileActivity.this);
                if (gpsTracker.canGetLocation()) {
                    latitude = gpsTracker.getLatitudeFromNetwork();
                    longitude = gpsTracker.getLongitudeFromNetwork();
                    lat = String.valueOf(latitude);
                    lng = String.valueOf(longitude);
                    lastLongEditText.getEditText().setText(lng);
                    lastLatEditText.getEditText().setText(lat);
                    locations.setLatitude(lat);
                    locations.setLongitude(lng);
                    oldLongitude = lng;
                    oldLatitude = lat;

                    navigateBtn.setEnabled(true);
                    Date date = new Date();
                    time = DateFormat.getDateTimeInstance().format(date);

                    if (userLocations.size() < 4) {
                        userLocations.add(userLocations.size(), new Location(lat, lng, time));
                        reference.child(id).child("locations").setValue(Arrays.asList(userLocations));
                    } else {
                        userLocations.add(userLocations.size(), new Location(lat, lng, time));
                        userLocations.remove(0);
                        reference.child(id).child("locations").setValue(Arrays.asList(userLocations));
                    }
                } else {
                    gpsTracker.showSettingsAlert();
                }
//                showAllUserData();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isButtonClicked = true;
                gpsTracker = new GpsTracker(ProfileActivity.this);
                if (gpsTracker.canGetLocation()) {
                    latitudeRefresh = gpsTracker.getLatitudeFromNetwork();
                    longitudeRefresh = gpsTracker.getLongitudeFromNetwork();
                    latRefresh = String.valueOf(latitudeRefresh);
                    longRefresh = String.valueOf(longitudeRefresh);
                } else {
                    gpsTracker.showSettingsAlert();
                }
                showAllUserData();
            }
        });


        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(ProfileActivity.this, MapsActivity2.class);
                Bundle b = new Bundle();
                b.putDouble("lat1", Double.parseDouble(oldLatitude));
                b.putDouble("long1", Double.parseDouble(oldLongitude));
                b.putDouble("lat2", latitudeRefresh);
                b.putDouble("long2", longitudeRefresh);

                if (userLocations.get(0) != null) {
                    b.putDouble("loc1Lat", Double.parseDouble(userLocations.get(0).getLatitude()));
                    b.putDouble("loc1Lng", Double.parseDouble(userLocations.get(0).getLongitude()));
                }
                if (userLocations.size() == 2) {
                    b.putDouble("loc2Lat", Double.parseDouble(userLocations.get(1).getLatitude()));
                    b.putDouble("loc2Lng", Double.parseDouble(userLocations.get(1).getLongitude()));
                }
                if (userLocations.size() == 3) {
                    b.putDouble("loc3Lat", Double.parseDouble(userLocations.get(2).getLatitude()));
                    b.putDouble("loc3Lng", Double.parseDouble(userLocations.get(2).getLongitude()));
                }

                intent2.putExtras(b);
                startActivity(intent2);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        shareLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibility_Flag) {
                    frameLayout.setVisibility(View.GONE);
                    editText.setVisibility(View.GONE);
                    visibility_Flag = false;
                } else {
                    frameLayout.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.VISIBLE);
                    visibility_Flag = true;
                }
                shareLocationEditText.getEditText().setText("My Current Location: " + "\n" + "Latitude: " + newLatitude + "," + "\n" + "Longitude: " + newLongitude);
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });

//        relativeLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openFileChooser();
//            }
//        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (imageUri != null) {
            fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            DatabaseReference imageStore = FirebaseDatabase.getInstance().getReference("users").child(id).child("uri");

                            UploadImage uploadImage = new UploadImage(uri.toString());
                            imageStore.setValue(uploadImage);
                            isUploaded = true;

                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No file Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            progressBar.setVisibility(View.VISIBLE);
            imageUri = data.getData();
            addImage = findViewById(R.id.imageAddImage);
            addImage.setImageURI(imageUri);
            uploadFile();
        }
    }

    private void sendNotification() {
        if (editText.getText().toString().isEmpty()) {
            Toast.makeText(ProfileActivity.this, "Please Enter Title", Toast.LENGTH_SHORT).show();
        } else if (shareLocationEditText.getEditText().getText().toString().isEmpty()) {
            Toast.makeText(ProfileActivity.this, "Please Enter Message", Toast.LENGTH_SHORT).show();
        }

        String Message = newLatitude + "," + newLongitude;

        String Title = editText.getText().toString();
        JSONObject json = new JSONObject();
        try {
            json.put("to", "topic/news");
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", Title);
            notificationObj.put("body", Message);

            json.put("data", notificationObj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, FCM_API,
                    json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(ProfileActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    shareLocationEditText.getEditText().setText("My Current Location: " + "\n" + "Latitude: " + newLatitude + "," + "\n" + "Longitude: " + newLongitude);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ProfileActivity.this, "Didn't Work", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content_type", contentType);
                    header.put("authorization", serverKey);
                    return header;
                }
            };
            mRequestQueue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void signOut() {
        if (acct != null) {
            firebaseAuth.signOut();

            mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Session not close", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showAllUserData() {

        if (acct != null) {
            personName = acct.getDisplayName();
            personEmail = acct.getEmail();

            userModelClass.setEmail(personEmail);
            userModelClass.setName(personName);
            userModelClass.setLatitude(oldLatitude);
            userModelClass.setLongitude(oldLongitude);

            mNameEditText.getEditText().setText(personName);
            mEmailEditText.getEditText().setText(personEmail);
            boldName.setText(personName);
            if (isButtonClicked) {
                latitudes.setText(latRefresh);
                longitudes.setText(longRefresh);
            } else {
                latitudes.setText(newLatitude);
                longitudes.setText(newLongitude);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationCheckPreference.setNotificationSent(getApplicationContext(), false);
    }
}

