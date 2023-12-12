package com.example.chat_callvideoapp.OnBroadingFragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chat_callvideoapp.ActivityScreen.PhoneNumberActivity;
import com.example.chat_callvideoapp.MainActivity;
import com.example.chat_callvideoapp.R;

public class PageThree extends Fragment {
    private Button btnGetStart;
    Animation btnAnim;
    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.onbroading_3,container,false);
        btnGetStart = view.findViewById(R.id.page3_btn_get_start);
        btnAnim = AnimationUtils.loadAnimation(getContext(),R.anim.button_animation);
        btnGetStart.setAnimation(btnAnim);
        btnGetStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), PhoneNumberActivity.class));
            }
        });
        return view;
    }
}
