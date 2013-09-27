package com.easemob.push.example;

import com.easemob.push.EaseMobPush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/*
 * this receiver class is used to receive push messages from EaseMob SDK
 */
public class PushMessageReceiver extends BroadcastReceiver {

    private final static String TAG = "pushMsgReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        int type = intent.getIntExtra(EaseMobPush.PUSH_INTENT_TYPE, 0);
        switch (type) {
        case EaseMobPush.PUSH_TYPE_REGISTRATION:
            //for registration intent, will have device registration id
            String registrationId = intent.getStringExtra(EaseMobPush.PUSH_REGISTRATION_ID);
            Log.d(TAG, "application received push registration id:" + registrationId);
            MainActivity.printDebugMsg("application received push registration id:" + registrationId);
            break;
        case EaseMobPush.PUSH_TYPE_NOTIFICATION:
            //for notification intent, will have notification id
            String notificationId = intent.getStringExtra(EaseMobPush.PUSH_NOTIFICATION_ID);
            Log.d(TAG, "application received push notification, id:" + notificationId);
            MainActivity.printDebugMsg("application received push notification, id:" + notificationId);
            break;
        case EaseMobPush.PUSH_TYPE_MESSAGE:
            //for notification intent, will have push message
            String message = intent.getStringExtra(EaseMobPush.PUSH_MESSAGE);
            Log.d(TAG, "application received push msg:" + message);
            Toast.makeText(context, "push msg:" + message, Toast.LENGTH_LONG).show();
            MainActivity.printDebugMsg("application received push msg:" + message);
            break;
        }
    }
}
