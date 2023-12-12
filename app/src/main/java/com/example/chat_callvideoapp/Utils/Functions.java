package com.example.chat_callvideoapp.Utils;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.example.chat_callvideoapp.Model.User;
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.Collections;

public class Functions {

    public static void startService(String userIdCall, String userName2, Activity activity){
        Application application = activity.getApplication(); // Android's application context
        long appID = 670113675;   // yourAppID
        String appSign = "5bb2559578458dd3704daf58be21bfd647fb109af78c4ef3fa7fb7a949379013";   // yourAppSign
        String userID = userIdCall; // yourUserID, userID should only contain numbers, English characters, and '_'.
        String userName = userName2;   // yourUserName

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
//        callInvitationConfig.notifyWhenAppRunningInBackgroundOrQuit = true;
        ZegoNotificationConfig notificationConfig = new ZegoNotificationConfig();
        notificationConfig.sound = "zego_uikit_sound_call";
        notificationConfig.channelID = "CallInvitation";
        notificationConfig.channelName = "CallInvitation";
        ZegoUIKitPrebuiltCallInvitationService.init(application, appID, appSign, userID, userName,callInvitationConfig);
    }

    public static void setVideoCall(String targetUserID, String targetUserName, com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton videoCall) {
        videoCall.setIsVideoCall(true);
        videoCall.setResourceID("zego_uikit_call");
        videoCall.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserID,targetUserName)));
    }

    public static void setVoiceCall(String targetUserID, String targetUserName, com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton voiceCall) {
        voiceCall.setIsVideoCall(false);
        voiceCall.setResourceID("zego_uikit_call");
        voiceCall.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserID,targetUserName)));
    }

    public static void passUserModelAsIntent(Intent intent, User model){
        intent.putExtra("username",model.getName());
        intent.putExtra("phone",model.getPhoneNumber());
        intent.putExtra("userId",model.getUid());
        intent.putExtra("profileImage",model.getProfileImage());
        intent.putExtra("FCMtoken",model.getToken());
    }

    public static User getUserModelFromIntent(Intent intent){
        User model = new User();
        model.setName(intent.getStringExtra("username"));
        model.setPhoneNumber(intent.getStringExtra("phone"));
        model.setUid(intent.getStringExtra("userId"));
        model.setProfileImage(intent.getStringExtra("profileImage"));
        model.setToken(intent.getStringExtra("FCMtoken"));
        return model;
    }

}
