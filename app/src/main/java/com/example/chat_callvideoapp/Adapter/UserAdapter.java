package com.example.chat_callvideoapp.Adapter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_callvideoapp.ActivityScreen.ChatActivity;
import com.example.chat_callvideoapp.MainActivity;
import com.example.chat_callvideoapp.Model.User;
import com.example.chat_callvideoapp.R;
import com.example.chat_callvideoapp.Utils.Functions;
import com.example.chat_callvideoapp.databinding.ItemConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {
    Context mContext;
    List<User> list;
    String senderId , senderRoom  ;
    FirebaseDatabase database;
    public UserAdapter(Context mContext, List<User> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_conversation,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = list.get(position);
        senderId = FirebaseAuth.getInstance().getUid();
        senderRoom = senderId + user.getUid();
        database = FirebaseDatabase.getInstance();
        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.binding.itemTvSendingTime.setText(dateFormat.format(new Date(time)));
                            holder.binding.itemTvLastestMessage.setText(lastMsg);
                        } else {
                            holder.binding.itemTvLastestMessage.setText("Tap to chat");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.itemTvNameConversation.setText(""+user.getName());
        Glide.with(mContext).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.itemImgConversation);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                Functions.passUserModelAsIntent(intent,user);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public final static class MyViewHolder extends RecyclerView.ViewHolder{
        ItemConversationBinding binding;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemConversationBinding.bind(itemView);
        }
    }

}
