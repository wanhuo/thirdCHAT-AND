package com.easemob.demo;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.user.EaseMobUser;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public final class ChatDemoApp extends Application {

	private static final String VERSION = "20004";
	public static String chatdemo_cache = Environment.getExternalStorageDirectory() + "/Android/data/com.easemob.chatdemo/";
	public static String image_cache = chatdemo_cache + "imageCache";
	private Context appContext;

	//login user name
    public final String PREF_USERNAME = "username";
    private String userName = null;
	
    //login password
    private static final String PREF_PWD = "pwd";
    private String password = null;
	
    public static ChatDemoApp instance = null;
    
    private static final String PREF_INITED = "easemob.contact.inited";
    private static Boolean inited = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		appContext = this.getApplicationContext();
		Log.d("app", "initialize EaseMob Chat Service");
        EMChat.getInstance().setDebugMode(true);
        
        //初始化  chat sdk
        EMChat.getInstance().init(this.getApplicationContext());
        //初始化  user sdk
        EaseMobUser.getInstance().init(this.getApplicationContext());
        
        //根据营业需要，设置chat 相关配置
        EMChatOptions chatOptions = new EMChatOptions();
        chatOptions.setUseEncryption(true);
        EMChatManager.getInstance().setChatOptions(chatOptions);
	}
	
	public void onTerminate() {
        super.onTerminate();
    }
	
	public static ChatDemoApp getInstance() {
        return instance;
    }
	
	public static String getVersion() {
		return VERSION;
	}
    
    public void setUserName(String user){
       if (user != null && !user.equals(userName)) {
           SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
           SharedPreferences.Editor editor = preferences.edit();
           if (editor.putString(PREF_USERNAME, user).commit()) {
                userName = user;
           }
        }
    }
    
    public String getUserName(){
        if (userName == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
            userName = preferences.getString(PREF_USERNAME, null); 
        }
        return userName;
    }
        
    public void setPassword(String pwd){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = preferences.edit();
        if (editor.putString(PREF_PWD, pwd).commit()) {
            password = pwd;
        }
    }
    
    public String getPassword(){
        if (password == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
            password = preferences.getString(PREF_PWD, null); 
        }
        
        return password;
    }
    
    public void setInited(boolean init){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = preferences.edit();
        if (editor.putBoolean(PREF_INITED, init).commit()) {
            inited = init;
        }
    }
    
    public boolean getInited(){
        if (inited == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
            inited = preferences.getBoolean(PREF_INITED, false); 
        }    
        return inited;
    }
    
}