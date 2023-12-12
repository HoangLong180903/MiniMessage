package com.example.chat_callvideoapp.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_callvideoapp.Model.Message;
import com.example.chat_callvideoapp.Model.User;
import com.example.chat_callvideoapp.R;
import com.example.chat_callvideoapp.databinding.DialogRecallBinding;
import com.example.chat_callvideoapp.databinding.ItemReceiveBinding;
import com.example.chat_callvideoapp.databinding.ItemReceiveGroupBinding;
import com.example.chat_callvideoapp.databinding.ItemSentBinding;
import com.example.chat_callvideoapp.databinding.ItemSentGroupBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GroupMessagesAdapter extends RecyclerView.Adapter{

    Context mContext;
    List<Message> list;

    final int ITEM_SENT = 1;
    final  int ITEM_RECEIVE = 2;



    public GroupMessagesAdapter(Context mContext, List<Message> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_group, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_receive_group, parent, false);
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

        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(mContext)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(mContext, config, (pos) -> {
            if (pos < 0)
                return false;
            if(holder.getClass() == SentViewHolder.class) {
                SentViewHolder viewHolder = (SentViewHolder)holder;
                viewHolder.binding.itemSentIconFeeling.setImageResource(reactions[pos]);
                viewHolder.binding.itemSentIconFeeling.setVisibility(View.VISIBLE);
            } else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
                viewHolder.binding.itemReceiveIcon.setImageResource(reactions[pos]);
                viewHolder.binding.itemReceiveIcon.setVisibility(View.VISIBLE);
            }

            message.setFellingIcon(pos);
            FirebaseDatabase.getInstance().getReference()
                    .child("public")
                    .child(message.getMessageId()).setValue(message);
            return true; // true is closing popup, false is requesting a new selection
        });


        if(holder.getClass() == SentViewHolder.class) {
            SentViewHolder viewHolder = (SentViewHolder)holder;

            if(message.getMessageText().equals("photo")) {
                viewHolder.binding.itemSendPhotoImage.setVisibility(View.VISIBLE);
                viewHolder.binding.itemSentTextview.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(message.getMessageImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(viewHolder.binding.itemSendPhotoImage);
            }

            FirebaseDatabase.getInstance()
                    .getReference().child("Users")
                    .child(message.getSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.itemSentPerson.setText(user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });

            viewHolder.binding.itemSentTextview.setText(message.getMessageText());

            if(message.getFellingIcon() >= 0) {
                viewHolder.binding.itemSentIconFeeling.setImageResource(reactions[message.getFellingIcon()]);
                viewHolder.binding.itemSentIconFeeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.itemSentIconFeeling.setVisibility(View.GONE);
            }

            viewHolder.binding.itemSentTextview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

            viewHolder.binding.itemSendPhotoImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
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
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
            if(message.getMessageText().equals("photo")) {
                viewHolder.binding.itemReceivePhotoImage.setVisibility(View.VISIBLE);
                viewHolder.binding.itemReceiveTextview.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(message.getMessageImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(viewHolder.binding.itemReceivePhotoImage);
            }
            FirebaseDatabase.getInstance()
                    .getReference().child("Users")
                    .child(message.getSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.itemReceivePerson.setText(""+user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            viewHolder.binding.itemReceiveTextview.setText(message.getMessageText());

            if(message.getFellingIcon() >= 0) {
                viewHolder.binding.itemReceiveIcon.setImageResource(reactions[message.getFellingIcon()]);
                viewHolder.binding.itemReceiveIcon.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.itemReceiveIcon.setVisibility(View.GONE);
            }

            viewHolder.binding.itemReceiveTextview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

            viewHolder.binding.itemReceivePhotoImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
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
       ItemSentGroupBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentGroupBinding.bind(itemView);
        }
    }

    public static final class ReceiverViewHolder extends RecyclerView.ViewHolder{
        ItemReceiveGroupBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveGroupBinding.bind(itemView);
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
                        .child("public")
                        .child(message.getMessageId()).setValue(message);
                dialog.dismiss();
            }
        });

        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference()
                        .child("public")
                        .child(message.getMessageId()).setValue(null);
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
