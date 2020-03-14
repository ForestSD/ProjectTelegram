package com.example.projecttelegram;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

@SuppressLint({"MissingFirebaseInstanceTokenRefresh", "Registered"})
public class MyFireBaseMessageService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Bundle bundle = new Bundle();
        bundle.putString("msgBody", remoteMessage.getNotification().getBody());

        Intent new_intent = new Intent();
        new_intent.setAction("ACTION_STRING_ACTIVITY");
        new_intent.putExtra("msg", bundle);

        sendBroadcast(new_intent);

    }
}


