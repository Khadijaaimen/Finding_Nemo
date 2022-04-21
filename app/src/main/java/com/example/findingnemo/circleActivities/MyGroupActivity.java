package com.example.findingnemo.circleActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.findingnemo.modelClasses.MembersAdapter;
import com.example.findingnemo.R;
import com.example.findingnemo.modelClasses.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_group);

        recyclerView = findViewById(R.id.recyclerview);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        nameList = new ArrayList<>();
        manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        userReference = FirebaseDatabase.getInstance().getReference("users");
        reference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("GroupMembers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nameList.clear();

                if(snapshot.exists()){
                    for(DataSnapshot ds: snapshot.getChildren()){
                        groupMemberId = ds.child("groupMemberId").getValue(String.class);
                        userReference.child(groupMemberId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                userModel = snapshot.getValue(UserModel.class);
                                nameList.add(userModel);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        adapter = new MembersAdapter(nameList, getApplicationContext());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}