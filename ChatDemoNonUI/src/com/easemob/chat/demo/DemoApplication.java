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
         
         //初始化易聊SDK
         Log.d("EMChat Demo", "initialize EMChat SDK");
         EaseMobChat.getInstance().init(appContext);
    }
}
