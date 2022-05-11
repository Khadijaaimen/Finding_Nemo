package com.example.findingnemo.circleActivities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findingnemo.R;
import com.example.findingnemo.modelClasses.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersHolderView> {

    ArrayList<UserModel> nameList;
    Context context;

    public MembersAdapter(ArrayList<UserModel> nameList, Context context) {
        this.nameList = nameList;
        this.context = context;
    }

    @NonNull
    @Override
    public MembersHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        MembersHolderView  membersHolderView = new MembersHolderView(v, context, nameList);
        return membersHolderView;
    }

    @Override
    public void onBindViewHolder(@NonNull MembersHolderView holder, int position) {
        UserModel userObject = nameList.get(position);
        holder.name_txt.setText(userObject.getUserName());
        Picasso.get().load(userObject.getUri()).placeholder(R.drawable.user_icon).into(holder.circleImageView);


        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getUid())
                        .child("GroupMembers");
                reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    String memberID = snapshot.child(nameList.get(position).getUserId()).getKey();
                                    reference.child(memberID).removeValue();
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                                            .child(memberID).child("joinedGroupId");
                                    String adminID = databaseReference.child(FirebaseAuth.getInstance().getUid()).getKey();
                                    databaseReference.child(adminID).removeValue();
                                    nameList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeRemoved(position, nameList.size());
                                    return;
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
    public int getItemCount() {
        return nameList.size();
    }

    public static class MembersHolderView extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView name_txt;
        ImageButton deleteButton;
        ImageView circleImageView;
        Context c;
        ArrayList<UserModel> nameArrayList;
        FirebaseAuth auth;
        FirebaseUser user;

        public MembersHolderView(@NonNull View itemView, Context c, ArrayList<UserModel> nameArrayList) {
            super(itemView);
            this.c = c;
            this.nameArrayList = nameArrayList;

            itemView.setOnClickListener(this);

            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();

            name_txt = itemView.findViewById(R.id.member_title);
            circleImageView = itemView.findViewById(R.id.user_icon);
            deleteButton = itemView.findViewById(R.id.deleteBtn);
        }

        @Override
        public void onClick(View v) {

        }
    }

}
