package com.example.chat_callvideoapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_callvideoapp.ActivityScreen.StoriesActivity;
import com.example.chat_callvideoapp.MainActivity;
import com.example.chat_callvideoapp.Model.Status;
import com.example.chat_callvideoapp.Model.User;
import com.example.chat_callvideoapp.Model.UserStatus;
import com.example.chat_callvideoapp.R;
import com.example.chat_callvideoapp.databinding.ItemStatusBinding;
import com.example.chat_callvideoapp.databinding.ItemStoriesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.MyViewHolder> {
    Context mContext;
    List<UserStatus> list;
    public StoriesAdapter(Context mContext, List<UserStatus> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_stories,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UserStatus userStatus = list.get(position);
        if (userStatus.getStatusList() != null && userStatus.getStatusList().size() > 0) {
            Status lastStatus = userStatus.getStatusList().get(userStatus.getStatusList().size() - 1);

            holder.binding.count.setText(String.valueOf(userStatus.getStatusList().size()));

            Glide.with(mContext).load(userStatus.getProfileImage())
                    .placeholder(R.drawable.avatar)
                    .into(holder.binding.image);

            if (lastStatus != null && lastStatus.getImageUrl() != null) {
                Glide.with(mContext).load(lastStatus.getImageUrl()).into(holder.binding.itemStories);
            } else {
                holder.binding.itemStories.setImageResource(R.drawable.image_placeholder);
            }
        } else {
            holder.binding.count.setText("0");
            holder.binding.image.setImageResource(R.drawable.image_placeholder);
            holder.binding.itemStories.setImageResource(R.drawable.image_placeholder);
        }
        holder.binding.circularStatusView.setPortionsCount(userStatus.getStatusList().size());
        holder.binding.circularStatusView.setPortionsColor(Color.BLUE);
        holder.binding.circularStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<MyStory> myStories = new ArrayList<>();
                for (Status status : userStatus.getStatusList()){
                    myStories.add(new MyStory(status.getImageUrl()));
                }
                holder.binding.circularStatusView.setPortionsColor(Color.GRAY);
                new StoryView.Builder(((StoriesActivity)mContext).getSupportFragmentManager())
                        .setStoriesList(myStories)
                        .setStoryDuration(5000)
                        .setTitleText(userStatus.getName())
                        .setSubtitleText("")
                        .setTitleLogoUrl(userStatus.getProfileImage())
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {
                            }

                            @Override
                            public void onTitleIconClickListener(int position) {
                            }
                        })
                        .build()
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ItemStoriesBinding binding;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemStoriesBinding.bind(itemView);
        }
    }
}
