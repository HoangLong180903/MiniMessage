package com.example.chat_callvideoapp.ActivityScreen;

import static com.example.chat_callvideoapp.Utils.FirebaseUtil.currentUserId;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chat_callvideoapp.Adapter.MessagesAdapter;
import com.example.chat_callvideoapp.Model.Message;
import com.example.chat_callvideoapp.Model.User;
import com.example.chat_callvideoapp.R;
import com.example.chat_callvideoapp.Utils.Functions;
import com.example.chat_callvideoapp.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessagesAdapter adapter;
    List<Message> list;
    String senderRoom, receiverRoom;
    String receiverUid, senderUid;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String token;

    FirebaseAuth mAuth;
    User user;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseFirestore.getInstance().collection("users").document(currentUserId());
        action();
        actionOfChat();

    }

    private void action() {
        //ẩn keybroad
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.edChating, InputMethodManager.SHOW_FORCED);
        }
        View view = binding.getRoot();
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                imm.hideSoftInputFromWindow(binding.edChating.getWindowToken(), 0);
                return false;
            }
        });

        //init firebase
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        list = new ArrayList<>();
        user = Functions.getUserModelFromIntent(getIntent());
        database.getReference().child("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User user = snapshot1.getValue(User.class);
                            //danh sách user đăng nhập
                            if (user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                Functions.startService(user.getUid(), user.getName(), ChatActivity.this);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
        Functions.setVoiceCall(user.getUid(), user.getName(), binding.iconPhoneCall);
        Functions.setVideoCall(user.getUid(), user.getName(), binding.iconVideoCall);
        //icon video call
        mAuth = FirebaseAuth.getInstance();
        binding.tvTopChat.setText("" + user.getName());
        Glide.with(this).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(binding.chatProfile);
        //===//
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        database.getReference().child("Presence").child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String status = snapshot.getValue(String.class);
                            if (!status.isEmpty()) {
                                if (status.equals("Offline")) {
                                    binding.tvStatus.setText(status);
                                    binding.tvStatus.setVisibility(View.VISIBLE);
                                } else {
                                    binding.tvStatus.setText(status);
                                    binding.tvStatus.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        adapter = new MessagesAdapter(this, list, senderRoom, receiverRoom);
        binding.rcChat.setLayoutManager(new LinearLayoutManager(this));
        binding.rcChat.setAdapter(adapter);

        //get data
        database.getReference().child("Chats")
                .child(senderRoom)
                .child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            list.add(message);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        // icon send data
        binding.imageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ẩn bàn phím
                imm.hideSoftInputFromWindow(binding.edChating.getWindowToken(), 0);
                String messageTxt = binding.edChating.getText().toString();


                Date date = new Date();
                Message message = new Message(messageTxt, senderUid, date.getTime());
                binding.edChating.setText("");

                //lấy mã gửi tin nhắn
                String randomKey = database.getReference().push().getKey();

                //set thời gian gửi , tin nhắn gửi mới nhất
                HashMap<String, Object> lastMgsObj = new HashMap<>();
                lastMgsObj.put("lastMsg", message.getMessageText());
                lastMgsObj.put("lastMsgTime", date.getTime());
                database.getReference().child("Chats").child(senderRoom).updateChildren(lastMgsObj);
                database.getReference().child("Chats").child(receiverRoom).updateChildren(lastMgsObj);
                database.getReference().child("Chats")
                        .child(senderRoom)
                        .child("Messages")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("Chats")
                                        .child(receiverRoom)
                                        .child("Messages")
                                        .child(randomKey)
                                        .setValue(message)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                sendNotification(user.getName(), message.getMessageText(), token);
                                            }
                                        });
                            }
                        });
            }
        });
    }

    private void actionOfChat() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);
//        gallery picker
        binding.iconPhotoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_GET_CONTENT);
                mIntent.setType("image/*");
                startActivityForResult(mIntent, 25);
            }
        });
//        camera picker
        binding.iconCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });
        final Handler handler = new Handler();
        binding.edChating.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("Presence")
                        .child(senderUid)
                        .setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("Presence")
                            .child(senderUid)
                            .setValue("Online");
                }
            };
        });
        binding.iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void sendNotification(String name, String message, String token) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to", token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
                    , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("notificaton",""+error.getLocalizedMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    String key = "Key=AAAAzCFOfys:APA91bE0zt9-aHqk3_s7EU7RIBjFcCDmteRzc0RGaa7jyWHnkZVpEBSlAKKO34Zu3ntJi0s6AruTHh3_du8fahM1ZWVSZMVG7747_n_Lv7a_7FtP-8VCNPYTFaC78aTNO06b_49eefuw";
                    map.put("Content-Type", "application/json");
                    map.put("Authorization", key);
                    return map;
                }
            };
            queue.add(request);
        } catch (Exception ex) {
        }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 25){
            if (resultCode == RESULT_OK) {
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("Chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();
                                        String messageTxt = binding.edChating.getText().toString();
                                        Date date = new Date();

                                        Message message = new Message(messageTxt, senderUid, date.getTime());
                                        message.setMessageText("photo");
                                        message.setMessageImageUrl(filePath);
                                        binding.edChating.setText("");

                                        //lấy mã gửi tin nhắn
                                        String randomKey = database.getReference().push().getKey();

                                        //set thời gian gửi , tin nhắn gửi mới nhất
                                        HashMap<String, Object> lastMgsObj = new HashMap<>();
                                        lastMgsObj.put("lastMsg", message.getMessageText());
                                        lastMgsObj.put("lastMsgTime", date.getTime());
                                        database.getReference().child("Chats").child(senderRoom).updateChildren(lastMgsObj);
                                        database.getReference().child("Chats").child(receiverRoom).updateChildren(lastMgsObj);
                                        database.getReference().child("Chats")
                                                .child(senderRoom)
                                                .child("Messages")
                                                .child(randomKey)
                                                .setValue(message)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        database.getReference().child("Chats")
                                                                .child(receiverRoom)
                                                                .child("Messages")
                                                                .child(randomKey)
                                                                .setValue(message)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {

                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            checkSelfPermission();
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            uploadImageToFirebaseStorage();
        }
    }

    public void checkSelfPermission(){
        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(ChatActivity.this,new String[]{
                    Manifest.permission.CAMERA
            },100);
        }
    }

    private void uploadImageToFirebaseStorage() {
        Calendar calendar = Calendar.getInstance();
        StorageReference reference = storage.getReference().child("Chats").child(calendar.getTimeInMillis() + "");
        dialog.show();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] data = outputStream.toByteArray();
        reference.putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();
                            String messageTxt = binding.edChating.getText().toString();
                            Date date = new Date();

                            Message message = new Message(messageTxt, senderUid, date.getTime());
                            message.setMessageText("camera");
                            message.setMessageImageUrl(filePath);
                            binding.edChating.setText("");

                            //lấy mã gửi tin nhắn
                            String randomKey = database.getReference().push().getKey();

                            //set thời gian gửi , tin nhắn gửi mới nhất
                            HashMap<String, Object> lastMgsObj = new HashMap<>();
                            lastMgsObj.put("lastMsg", message.getMessageText());
                            lastMgsObj.put("lastMsgTime", date.getTime());
                            database.getReference().child("Chats").child(senderRoom).updateChildren(lastMgsObj);
                            database.getReference().child("Chats").child(receiverRoom).updateChildren(lastMgsObj);
                            database.getReference().child("Chats")
                                    .child(senderRoom)
                                    .child("Messages")
                                    .child(randomKey)
                                    .setValue(message)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("Chats")
                                                    .child(receiverRoom)
                                                    .child("Messages")
                                                    .child(randomKey)
                                                    .setValue(message)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {

                                                        }
                                                    });
                                        }
                                    });
                        }
                    });
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        ZegoUIKitPrebuiltCallInvitationService.unInit();
        database.getReference().child("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User user = snapshot1.getValue(User.class);
                            //danh sách user đăng nhập
                            if (user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                Functions.startService(user.getUid(), user.getName(), ChatActivity.this);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
        Functions.setVoiceCall(user.getUid(), user.getName(), binding.iconPhoneCall);
        Functions.setVideoCall(user.getUid(), user.getName(), binding.iconVideoCall);
    }
}