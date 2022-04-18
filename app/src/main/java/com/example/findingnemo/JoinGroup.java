package com.example.findingnemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

public class JoinGroup extends AppCompatActivity {

    Pinview pinview;
    Button join;
    DatabaseReference reference, currentReference;
    FirebaseUser user;
    String join_user_id, current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        pinview = findViewById(R.id.joinPin);
        join = findViewById(R.id.button);

        user = FirebaseAuth.getInstance().getCurrentUser();
        current_user_id = user.getUid();

        reference = FirebaseDatabase.getInstance().getReference("users");
        currentReference = reference.child(user.getUid());

        currentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = reference.orderByChild("circleNode").equalTo(pinview.getValue());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            User user1 = null;
                            for(DataSnapshot ds: snapshot.getChildren()){
                                user1 = ds.getValue(User.class);
                                join_user_id = user1.userId;

                                DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("users")
                                        .child(join_user_id).child("GroupMembers");

                                GroupJoin groupJoin = new GroupJoin(current_user_id);
                                GroupJoin groupJoin1 = new GroupJoin(join_user_id);

                                groupReference.child(user.getUid()).setValue(groupJoin)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(getApplicationContext(), "Joined Successfully!", Toast.LENGTH_SHORT).show();
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
}