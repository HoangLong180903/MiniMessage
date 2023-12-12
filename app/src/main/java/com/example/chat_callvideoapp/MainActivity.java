package com.example.chat_callvideoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.chat_callvideoapp.ActivityScreen.GroupChatActivity;
import com.example.chat_callvideoapp.ActivityScreen.PhoneNumberActivity;
import com.example.chat_callvideoapp.ActivityScreen.SettingActivity;
import com.example.chat_callvideoapp.ActivityScreen.StoriesActivity;
import com.example.chat_callvideoapp.Adapter.UserAdapter;
import com.example.chat_callvideoapp.Adapter.TopUserStatusAdapter;
import com.example.chat_callvideoapp.Model.Status;
import com.example.chat_callvideoapp.Model.User;
import com.example.chat_callvideoapp.Model.UserStatus;
import com.example.chat_callvideoapp.Utils.Functions;
import com.example.chat_callvideoapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    List<User> list;
    List<User> statusUserList;
    UserAdapter adapter;
    List<UserStatus> userStatusList;
    TopUserStatusAdapter userStatusAdapter;
    User user;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean nightMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        action();
        actionNav();
    }



    public void action(){
        binding.bottomNav.setItemIconTintList(null);
        //init
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        HashMap<String , Object> map = new HashMap<>();
                        map.put("token", token);
                        database.getReference()
                                .child("Users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .updateChildren(map);
                    }
                });

        list = new ArrayList<>();
        userStatusList = new ArrayList<>();
        statusUserList = new ArrayList<>();
        database.getReference().child("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        adapter = new UserAdapter(MainActivity.this,list);
        binding.rcConversation.setAdapter(adapter);


        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    //danh sách user đăng nhập
                    if (!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                        list.add(user);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //danh sách trạng thái onl-off user
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                statusUserList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    statusUserList.add(user);
                }
                userStatusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        userStatusAdapter = new TopUserStatusAdapter(MainActivity.this,statusUserList);
        binding.rcUserStatuses.setAdapter(userStatusAdapter);


        //dark - mode setting
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode",false);
        if (nightMode){
            binding.switchButton.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        binding.switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("nightMode", false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("nightMode", true);
                }
                editor.apply();
            }
        });
    }


    public void actionNav(){
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    User userProfile = snapshot1.getValue(User.class);
                    //danh sách user đăng nhập
                    if (userProfile.getUid().equals(FirebaseAuth.getInstance().getUid())){
                        Intent mIntent = new Intent(MainActivity.this, SettingActivity.class);
                        Functions.passUserModelAsIntent(mIntent,userProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.icon_stories){
                    startActivity(new Intent(MainActivity.this, StoriesActivity.class));
                }else if (item.getItemId() == R.id.icon_group_chat){
                    startActivity(new Intent(MainActivity.this, GroupChatActivity.class));
                }
                return false;
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(currentId).setValue("Offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(currentId).setValue("Offline");
    }
}
