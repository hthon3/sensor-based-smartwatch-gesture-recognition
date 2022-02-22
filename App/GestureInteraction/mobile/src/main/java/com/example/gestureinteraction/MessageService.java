package com.example.gestureinteraction;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MessageService extends WearableListenerService {
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/sensor_data_collection")) {
            final String message = new String(messageEvent.getData());
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("sensor_data_collection", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        } else if (messageEvent.getPath().equals("/sensor_data_classification")){
            final String message = new String(messageEvent.getData());
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("sensor_data_classification", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
    }
}