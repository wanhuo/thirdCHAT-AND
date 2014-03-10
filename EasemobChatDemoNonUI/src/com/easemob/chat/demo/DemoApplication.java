package com.easemob.chat.demo;



import com.easemob.chat.EaseMobChat;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class DemoApplication extends Application {

    public static Context appContext;
    @Override
    public void onCreate() {             
         super.onCreate();
         appContext = this;
         //初始化EaseMob Chat SDK
         Log.d("app", "initialize EaseMob Chat Service");
         EaseMobChat.getInstance().setDebugMode(true);
         EaseMobChat.getInstance().init(appContext);
    }
}
