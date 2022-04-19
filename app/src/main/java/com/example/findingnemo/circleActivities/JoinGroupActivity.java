package com.example.findingnemo.circleActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.findingnemo.googleMaps.MyNavigationActivity;
import com.example.findingnemo.R;
import com.example.findingnemo.modelClasses.UserModel;
import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class JoinGroupActivity extends AppCompatActivity {

    Pinview pinview;
    Button join;
    DatabaseReference reference, currentReference, groupReference;
    FirebaseUser user;
    String join_user_id, current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        pinview = findViewById(R.id.pinView);
        pinview.showCursor(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pinview.setFocusable(View.FOCUSABLE);
        }
        join = findViewById(R.id.joinBtn);

        user = FirebaseAuth.getInstance().getCurrentUser();
        current_user_id = user.getUid();

        reference = FirebaseDatabase.getInstance().getReference("users");
        currentReference = reference.child(user.getUid());

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = reference.orderByChild("code").equalTo(pinview.getValue());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            UserModel user1 = null;
                            for(DataSnapshot ds: snapshot.getChildren()){
                                String name = ds.child("userName").getValue(String.class);
                                String email = ds.child("email").getValue(String.class);
                                String code = ds.child("code").getValue(String.class);
                                String isSharing = ds.child("isSharing").getValue(String.class);
                                String lat = ds.child("latitude").getValue(String.class);
                                String lng = ds.child("longitude").getValue(String.class);
                                String uri = ds.child("uri").getValue(String.class);
                                String userId = ds.child("userId").getValue(String.class);
                                user1 = new UserModel(name, email, code, uri, isSharing, lat, lng, userId);
                                join_user_id = user1.userId;

                                groupReference = FirebaseDatabase.getInstance().getReference("users")
                                        .child(join_user_id).child("GroupMembers");

                                GroupJoinModel groupJoin = new GroupJoinModel(current_user_id);
                                GroupJoinModel groupJoin1 = new GroupJoinModel(join_user_id);

                                groupReference.child(user.getUid()).setValue(groupJoin)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent intent = new Intent(JoinGroupActivity.this, MyNavigationActivity.class);
                                            Toast.makeText(getApplicationContext(), "Joined Successfully!", Toast.LENGTH_SHORT).show();
                                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            startActivity(intent);
                                        }
                                    }
                                });

                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Code is invalid.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(JoinGroupActivity.this, MyNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}