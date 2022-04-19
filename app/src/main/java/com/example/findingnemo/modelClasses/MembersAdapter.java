package com.example.findingnemo.modelClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findingnemo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

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
        holder.name_txt.setText(userObject.userName);
        Picasso.get().load(userObject.uri).placeholder(R.drawable.user_icon).into(holder.circleImageView);
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public static class MembersHolderView extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView name_txt;
        CircleImageView circleImageView;
        View v;
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

        }

        @Override
        public void onClick(View v) {

        }
    }

}
