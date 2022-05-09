package com.example.findingnemo.circleActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

public class
JoinGroupActivity extends AppCompatActivity {

    Pinview pinview;
    Button join;
    DatabaseReference reference, groupReference;
    FirebaseUser user;
    String join_user_id, current_user_id, name, email;
    Double updateLat, updateLong;

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

        FirebaseDatabase.getInstance().getReference("users").child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                if(ds.exists()) {
                    name = ds.child("userName").getValue(String.class);
                    email = ds.child("email").getValue(String.class);
                    updateLat = ds.child("updated_latitude").getValue(Double.class);
                    updateLong = ds.child("updated_longitude").getValue(Double.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        reference = FirebaseDatabase.getInstance().getReference("users");

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = reference.orderByChild("code").equalTo(pinview.getValue());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot ds: snapshot.getChildren()){
                                join_user_id = ds.child("userId").getValue(String.class);

                                reference.child(current_user_id).child("joinedGroupId").child(join_user_id).child("adminId").setValue(join_user_id);
                                groupReference = FirebaseDatabase.getInstance().getReference("users")
                                        .child(join_user_id).child("GroupMembers");

                                groupReference.child(user.getUid()).child("userName").setValue(name);
                                groupReference.child(user.getUid()).child("email").setValue(email);
                                groupReference.child(user.getUid()).child("groupMemberId").setValue(current_user_id);
                                groupReference.child(user.getUid()).child("updated_latitude").setValue(updateLat);
                                groupReference.child(user.getUid()).child("updated_longitude").setValue(updateLong).
                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent intent = new Intent(JoinGroupActivity.this, MyNavigationActivity.class);
                                            Toast.makeText(getApplicationContext(), "Joined Successfully!", Toast.LENGTH_SHORT).show();
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
        super.onBackPressed();
        JoinGroupActivity.this.finish();
    }
}