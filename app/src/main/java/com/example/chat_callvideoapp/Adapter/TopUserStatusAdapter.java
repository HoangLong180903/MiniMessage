package com.example.chat_callvideoapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_callvideoapp.Model.User;
import com.example.chat_callvideoapp.R;
import com.example.chat_callvideoapp.databinding.ItemStatusBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TopUserStatusAdapter extends RecyclerView.Adapter<TopUserStatusAdapter.MyViewHolder> {

    Context mContext;
    List<User> list;

    private DatabaseReference presenceRef;
    String status;
    public TopUserStatusAdapter(Context mContext, List<User> list) {
        this.mContext = mContext;
        this.list = list;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        presenceRef = database.getReference("Presence");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_status, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = list.get(position);
        Glide.with(mContext).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.image);
        checkUserStatus(user.getUid(), holder.binding.imageView);
    }

    private void checkUserStatus(String userId, ImageView imageView) {
        presenceRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    status = snapshot.getValue(String.class);
                    if ("Online".equals(status)) {
                        imageView.setVisibility(View.VISIBLE);
                    } else {
                        imageView.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemStatusBinding binding;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemStatusBinding.bind(itemView);
        }
    }

}
