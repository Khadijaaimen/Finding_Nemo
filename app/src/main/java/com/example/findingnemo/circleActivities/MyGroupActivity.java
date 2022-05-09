package com.example.findingnemo.circleActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.findingnemo.R;
import com.example.findingnemo.modelClasses.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyGroupActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager manager;

    FirebaseUser user;
    FirebaseAuth auth;
    UserModel userModel;
    ArrayList<UserModel> nameList;
    DatabaseReference reference, userReference;
    String groupMemberId;
    EditText circleName;
    ImageButton editButton;
    TextInputLayout latitudeEdit, longitudeEdit;
    ProgressBar progressBar;
    CardView cardView;
    StorageReference storageReference, fileReference;
    Uri imageUri;
    Boolean isChecked = false;
    RelativeLayout relativeLayout;
    ImageView home, groupIcon, addMember, check;
    String code;
    ArrayList<String> idArrayList = new ArrayList<>();

    public static final int PICK_IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_group);

        recyclerView = findViewById(R.id.recyclerview);
        cardView = findViewById(R.id.imageCardView);
        relativeLayout = findViewById(R.id.groupIconLayout);
        progressBar = findViewById(R.id.progressBarIcon);
        circleName = findViewById(R.id.groupNameTextView);
        editButton = findViewById(R.id.editBtn);
        latitudeEdit = findViewById(R.id.geofenceLat);
        longitudeEdit = findViewById(R.id.geofenceLong);
        home = findViewById(R.id.homeBtn);
        groupIcon = findViewById(R.id.nav_header_view_profilePic);
        addMember = findViewById(R.id.addMemberImage);
        check = findViewById(R.id.checkBtn);

        progressBar.setVisibility(View.VISIBLE);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("groupUploads");

        nameList = new ArrayList<>();
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        userReference = FirebaseDatabase.getInstance().getReference("users");

        if (!isChecked)
            userReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isChecked) {
                        Double geolat = snapshot.child("geofenceLat").getValue(Double.class);
                        assert geolat != null;
                        String lat = geolat.toString();
                        latitudeEdit.getEditText().setText(lat);
                        Double geolong = snapshot.child("geofenceLong").getValue(Double.class);
                        String lng = geolong.toString();
                        longitudeEdit.getEditText().setText(lng);
                        code = snapshot.child("code").getValue(String.class);

                        if (snapshot.child("groupIcon").exists()) {
                            String uri = snapshot.child("groupIcon").getValue(String.class);
                            Picasso.get().load(uri).into(groupIcon);
                        } else {
                            groupIcon.setPadding(20, 20, 20, 20);
                            Picasso.get().load(R.drawable.groups).into(groupIcon);
                        }
                        progressBar.setVisibility(View.GONE);

                        if (snapshot.child("groupName").exists()) {
                            String getName = snapshot.child("groupName").getValue(String.class);
                            circleName.setText(getName);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, "My Invitation Code is: \n" + code);
                startActivity(Intent.createChooser(i, "\bUse your invite code.\b Share Using: "));
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        circleName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleName.setText("");
                circleName.setClickable(true);
                circleName.requestFocus();
                circleName.setCursorVisible(true);
                editButton.setVisibility(View.GONE);
                circleName.setHint("Type Name");
                check.setVisibility(View.VISIBLE);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleName.setText("");
                circleName.setClickable(true);
                circleName.requestFocus();
                circleName.setCursorVisible(true);
                circleName.setHint("Type Name");
                editButton.setVisibility(View.GONE);
                check.setVisibility(View.VISIBLE);
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = String.valueOf(circleName.getText());
                if (name.isEmpty()) {
                    circleName.setError("Name is required");
                } else {
                    circleName.setError("");
                    FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("groupName").setValue(name);
                    circleName.setText(name);
                    check.setVisibility(View.GONE);
                    editButton.setVisibility(View.VISIBLE);
                }
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("GroupMembers");

        if (!isChecked)
            gettingMemberInfo();

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }

    private void gettingMemberInfo() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isChecked) {
                    nameList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            groupMemberId = ds.getKey();
                            idArrayList.add(groupMemberId);
                        }
                        showMemberInfo();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMemberInfo() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!isChecked) {
                    for (int i = 0; i < idArrayList.size(); i++) {
                        String name = snapshot.child(idArrayList.get(i)).child("userName").getValue(String.class);
                        String email = snapshot.child(idArrayList.get(i)).child("email").getValue(String.class);
                        String code = snapshot.child(idArrayList.get(i)).child("code").getValue(String.class);
                        String isSharing = snapshot.child(idArrayList.get(i)).child("isSharing").getValue(String.class);
                        Double lat = snapshot.child(idArrayList.get(i)).child("userLatitude").getValue(Double.class);
                        Double lng = snapshot.child(idArrayList.get(i)).child("userLongitude").getValue(Double.class);
                        String uri = snapshot.child(idArrayList.get(i)).child("uri").getValue(String.class);
                        String userId = snapshot.child(idArrayList.get(i)).child("userId").getValue(String.class);
                        Double geoLat = snapshot.child(idArrayList.get(i)).child("geofenceLat").getValue(Double.class);
                        Double geoLong = snapshot.child(idArrayList.get(i)).child("geofenceLong").getValue(Double.class);
                        userModel = new UserModel(userId, name, email, code, uri, isSharing, lat, lng, geoLat, geoLong);
                        nameList.add(userModel);
                    }
                    adapter = new MembersAdapter(nameList, getApplicationContext());
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    isChecked = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                            DatabaseReference imageStore;
                            imageStore = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("groupIcon");
                            imageStore.setValue(uri.toString());
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
            imageUri = data.getData();
            groupIcon.setImageURI(imageUri);
            uploadFile();
        }
    }
}