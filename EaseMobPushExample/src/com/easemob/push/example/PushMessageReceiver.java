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
    
    //below variables are for test cases to verify the received intent
    public static String notificationTitle = null;
    public static String notificationMessage = null;
    
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(EaseMobPush.PUSH_INTENT_REGISTRATION)) {
            //for registration intent, will have device registration id
            String registrationId = intent.getStringExtra(EaseMobPush.PUSH_PARAM_REGISTRATION_ID);
            Log.d(TAG, "application received push registration id:" + registrationId);
            MainActivity.printDebugMsg("application received push registration id:" + registrationId);
            
        } else if (action.equals(EaseMobPush.PUSH_INTENT_NOTIFICATION)){
            //for notification intent, will have notification id
            String notificationId = intent.getStringExtra(EaseMobPush.PUSH_PARAM_NOTIFICATION_ID);
            String title = intent.getStringExtra(EaseMobPush.PUSH_PARAM_NOTIFICATION_TITLE);
            notificationTitle = title;
            Log.d(TAG, "application received push notification, id:" + notificationId);
            MainActivity.printDebugMsg("application received push notification, id:" + notificationId + " title:" + title);
            
        } else if (action.equals(EaseMobPush.PUSH_INTENT_MESSAGE)){
            //for notification intent, will have push message
            String message = intent.getStringExtra(EaseMobPush.PUSH_PARAM_MESSAGE);
            notificationMessage = message;
            Log.d(TAG, "application received push msg:" + message);
            Toast.makeText(context, "push msg:" + message, Toast.LENGTH_LONG).show();
            MainActivity.printDebugMsg("application received push msg:" + message);
        }
    }
}
