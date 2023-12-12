package com.example.chat_callvideoapp.ActivityScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.chat_callvideoapp.R;
import com.example.chat_callvideoapp.Utils.AndroidUtil;
import com.example.chat_callvideoapp.databinding.ActivityConfirmOtpactivityBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mukeshsolanki.OnOtpCompletionListener;
import com.mukeshsolanki.OtpView;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ConfirmOTPActivity extends AppCompatActivity {
    ActivityConfirmOtpactivityBinding binding;
    Long timeoutSeconds = 60L;
    FirebaseAuth mAuth ;
    String verificationId;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    ProgressDialog dialog;
    String strPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        action();
    }
    //set resend lại mã otp khi hết 60s
    void startResendTimer(){
        binding.tvResendOtp.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                runOnUiThread(() -> {
                    binding.tvResendOtp.setText("Gửi lại mã OTP trong: " + timeoutSeconds + "s");
                });
                if (timeoutSeconds <= 0) {
                    timeoutSeconds = 60L;
                    timer.cancel();
                    runOnUiThread(() -> {
                        binding.tvResendOtp.setEnabled(true);
                        binding.tvResendOtp.setText("Gửi lại mã OTP");
                    });
                }
            }
        }, 0, 1000);
    }


    public void action(){
        setInProgress(true);
        startResendTimer();
        mAuth = FirebaseAuth.getInstance();
        //hiện keybroad
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        binding.otpView.requestFocus();

        strPhoneNumber = getIntent().getStringExtra("phone");
        binding.tvGetPhone.setText(""+strPhoneNumber);


        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(strPhoneNumber)
                .setTimeout(60L,TimeUnit.SECONDS)
                .setActivity(ConfirmOTPActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        setInProgress(false);
                        verificationId = s;
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
        //nhập mã otp
        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,otp);
                mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            setInProgress(false);
                        }else{
                            setInProgress(true);
                            AndroidUtil.showToast(ConfirmOTPActivity.this,"Dang nhap that bai");
                        }
                    }
                });
            }
        });

        binding.btnContinueOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConfirmOTPActivity.this, ProfileActivity.class));
                finishAffinity();
            }
        });
        binding.tvResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOtp(strPhoneNumber,true);
            }
        });
    }

    //đặt trạng thái cho progress bar
    private void setInProgress(boolean inProgress){
        if (inProgress){
            binding.progressOtp.setVisibility(View.VISIBLE);
            binding.btnContinueOtp.setVisibility(View.GONE);
        }else{
            binding.progressOtp.setVisibility(View.GONE);
            binding.btnContinueOtp.setVisibility(View.VISIBLE);
        }
    }

        void sendOtp(String phoneNumber,boolean isResend) {
            startResendTimer();
            setInProgress(true);
            PhoneAuthOptions.Builder builder =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                    setInProgress(false);
                                }

                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                    AndroidUtil.showToast(ConfirmOTPActivity.this, "OTP verification failed");
                                    setInProgress(false);
                                }

                                @Override
                                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    super.onCodeSent(s, forceResendingToken);
                                    verificationId = s;
                                    resendingToken = forceResendingToken;
                                    AndroidUtil.showToast(ConfirmOTPActivity.this, "OTP sent successfully");
                                    setInProgress(false);
                                }
                            });
            if (isResend) {
                PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
            } else {
                PhoneAuthProvider.verifyPhoneNumber(builder.build());
            }
        }
}