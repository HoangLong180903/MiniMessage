package com.example.chat_callvideoapp.ActivityScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.chat_callvideoapp.MainActivity;
import com.example.chat_callvideoapp.Model.User;
import com.example.chat_callvideoapp.R;
import com.example.chat_callvideoapp.databinding.ActivityPhoneNumberBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
public class PhoneNumberActivity extends AppCompatActivity {
    ActivityPhoneNumberBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        action();
    }


    public void action(){
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(binding.edPhoneNumber, InputMethodManager.SHOW_IMPLICIT);
        binding.edPhoneNumber.requestFocus();

        binding.progressPhoneNumber.setVisibility(View.GONE);
        //kiểm tra thông tin đăng nhập
        database = FirebaseDatabase.getInstance();
        //đăng ký vùng miền cho số điện thoại
        binding.pickerPhoneCountry.registerCarrierNumberEditText(binding.edPhoneNumber);
        binding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //kiểm tra số điện thoại hợp lý tên miền k
                if (!binding.pickerPhoneCountry.isValidFullNumber()){
                    binding.edPhoneNumber.setError("Phone number not valid");
                    return;
                }
                Intent intent = new Intent(PhoneNumberActivity.this, ConfirmOTPActivity.class);
                intent.putExtra("phone",binding.pickerPhoneCountry.getFullNumberWithPlus());
                startActivity(intent);
            }
        });
    }
}