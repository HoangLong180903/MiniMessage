package com.example.chat_callvideoapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat_callvideoapp.Model.Message;
import com.example.chat_callvideoapp.R;
import com.example.chat_callvideoapp.databinding.DialogRecallBinding;
import com.example.chat_callvideoapp.databinding.ItemReceiveBinding;
import com.example.chat_callvideoapp.databinding.ItemSentBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter{

    Context mContext;
    List<Message> list;

    final int ITEM_SENT = 1;
    final  int ITEM_RECEIVE = 2;

    String senderRoom ;
    String receiverRoom;


    public MessagesAdapter(Context mContext, List<Message> list , String senderRoom , String receiverRoom) {
        this.mContext = mContext;
        this.list = list;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = list.get(position);
        //check người gửi , người nhận
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = list.get(position);
        RequestOptions options = new RequestOptions()
                .override(100, 100); // Kích thước mong muốn
                // Cách xử lý ảnh (có thể thay đổi tùy ý)
        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(mContext)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(mContext, config, (index) -> {
            if(index < 0)
                return false;

            if (holder.getClass() == SentViewHolder.class){
                SentViewHolder viewHolder = (SentViewHolder) holder;
                viewHolder.binding.itemSentIconFeeling.setImageResource(reactions[index]);
                viewHolder.binding.itemSentIconFeeling.setVisibility(View.VISIBLE);
            }else{
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.itemReceiveIcon.setImageResource(reactions[index]);
                viewHolder.binding.itemReceiveIcon.setVisibility(View.VISIBLE);
            }

            message.setFellingIcon(index);
            FirebaseDatabase.getInstance().getReference()
                    .child("Chats")
                    .child(senderRoom)
                    .child("Messages")
                    .child(message.getMessageId()).setValue(message);

            FirebaseDatabase.getInstance().getReference()
                    .child("Chats")
                    .child(receiverRoom)
                    .child("Messages")
                    .child(message.getMessageId()).setValue(message);

            return true;
            // true is closing popup, false is requesting a new selection
        });

        if (holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder)holder;

            if (message.getMessageText().equals("photo") || message.getMessageText().equals("camera")){
                viewHolder.binding.itemSendPhotoImage.setVisibility(View.VISIBLE);
                viewHolder.binding.itemSentTextview.setVisibility(View.GONE);
                viewHolder.binding.linearLayout.setBackgroundResource(R.drawable.bg_edittext);
                Glide.with(mContext)
                        .load(message.getMessageImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .apply(options)
                        .into(viewHolder.binding.itemSendPhotoImage);
            }

            viewHolder.binding.itemSentTextview.setText(""+message.getMessageText());
            if (message.getFellingIcon() >= 0){
                viewHolder.binding.itemSentIconFeeling.setImageResource(reactions[message.getFellingIcon()]);
                viewHolder.binding.itemSentIconFeeling.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.itemSentIconFeeling.setVisibility(View.GONE);
            }
            viewHolder.binding.itemSentTextview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            viewHolder.binding.itemSendPhotoImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    actionReCall(message , Gravity.BOTTOM);
                    return false;
                }
            });
        }else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            if (message.getMessageText().equals("photo") || message.getMessageText().equals("camera")){
                viewHolder.binding.itemReceivePhotoImage.setVisibility(View.VISIBLE);
                viewHolder.binding.itemReceiveTextview.setVisibility(View.GONE);
                viewHolder.binding.linearLayout2.setBackgroundResource(R.drawable.bg_edittext);
                Glide.with(mContext).
                        load(message.getMessageImageUrl())
                        .apply(options)
                        .placeholder(R.drawable.image_placeholder)
                        .into(viewHolder.binding.itemReceivePhotoImage);
            }
            viewHolder.binding.itemReceiveTextview.setText(""+message.getMessageText());
            if (message.getFellingIcon() >= 0){
                viewHolder.binding.itemReceiveIcon.setImageResource(reactions[message.getFellingIcon()]);
                viewHolder.binding.itemReceiveIcon.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.itemReceiveIcon.setVisibility(View.GONE);

            }
            viewHolder.binding.itemReceiveTextview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
            viewHolder.binding.itemReceivePhotoImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    actionReCall(message , Gravity.BOTTOM);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static final class SentViewHolder extends RecyclerView.ViewHolder{
       ItemSentBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    public static final class ReceiverViewHolder extends RecyclerView.ViewHolder{
        ItemReceiveBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }


    public void actionReCall(Message message , int gravity){
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_recall, null);
        DialogRecallBinding binding = DialogRecallBinding.bind(view);
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setView(binding.getRoot())
                .create();

        Window window = dialog.getWindow();
        if (window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        binding.everyone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.setMessageText("Tin nhắn đã được thu hồi");
                message.setFellingIcon(-1);
                FirebaseDatabase.getInstance().getReference()
                        .child("Chats")
                        .child(senderRoom)
                        .child("Messages")
                        .child(message.getMessageId()).setValue(message);

                FirebaseDatabase.getInstance().getReference()
                        .child("Chats")
                        .child(receiverRoom)
                        .child("Messages")
                        .child(message.getMessageId()).setValue(message);
                dialog.dismiss();
            }
        });

        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference()
                        .child("Chats")
                        .child(senderRoom)
                        .child("Messages")
                        .child(message.getMessageId()).setValue(null);

                FirebaseDatabase.getInstance().getReference()
                        .child("Chats")
                        .child(receiverRoom)
                        .child("Messages")
                        .child(message.getMessageId()).setValue(message);
                dialog.dismiss();
            }
        });

        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
