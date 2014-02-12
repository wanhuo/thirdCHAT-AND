package com.easemob.demo;

import com.easemob.chat.EaseMobChat;
import com.easemob.user.EaseMobUser;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Used to access global data like preference settings, permanent data and transient data  
 * It has a very short name 'Gl' abbreviated from Global.
 */
public final class Gl extends Application {

	private static final String VERSION = "20004";
	private static final String PREF_FILE_NAME = "EaseMob";
	public static String chatdemo_cache = Environment.getExternalStorageDirectory() + "/Android/data/com.easemob.chatdemo/";
	public static String image_cache = chatdemo_cache + "imageCache";
	private static Context sContext;
	private static SharedPreferences sDefaultSharedPreferences; // settings from preference activity 

    public static Gl instance = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		init(this.getApplicationContext());
		
		Log.d("app", "initialize EaseMob Chat Service");
        EaseMobChat.setDebugMode(true);
        EaseMobChat.init(this.getApplicationContext());
        EaseMobUser.getInstance().init(this.getApplicationContext());
	}
	
	public void onTerminate() {
        super.onTerminate();
    }
	
	public static Gl getInstance() {
        return instance;
    }
	
	public static String getVersion() {
		return VERSION;
	}
	
	/**
	 * 
	 * init
	 * @description: This method must be called in Application.onCreate() 
	 * @param context
	 */
	public static void init(Context context) {
		sContext = context;
		sDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	/**
	 * 
	 * Ct
	 * @description: get the application context, it can be used most where the Context is needed. 	
	 * @return the application context (comes from Application.getApplicationContext())
	 */
	public static Context Ct() {
		return sContext;
	}
    
	
    //login user name
    public static final String PREF_USERNAME = "username";
    private static String userName = null;
    
    public static void setUserName(String user){
       if (user != null && !user.equals(userName)) {
            SharedPreferences.Editor editor = sDefaultSharedPreferences.edit();
            if (editor.putString(PREF_USERNAME, user).commit()) {
                userName = user;
            }
        }
    }
    
    public static String getUserName(){
        if (userName == null) {
            userName = sDefaultSharedPreferences.getString(PREF_USERNAME, null); 
        }
        return userName;
    }
    
    
    //login password
    public static final String PREF_PWD = "pwd";
    private static String password = null;
    
    public static void setPassword(String pwd){
        SharedPreferences.Editor editor = sDefaultSharedPreferences.edit();
        if (editor.putString(PREF_PWD, pwd).commit()) {
            password = pwd;
        }
    }
    
    public static String getPassword(){
        if (password == null) {
            password = sDefaultSharedPreferences.getString(PREF_PWD, null); 
        }
        
        return password;
    }
    
    
    public static final String PREF_INITED = "inited";
    private static Boolean inited = null;
    
    public static void setInited(boolean init){
        SharedPreferences.Editor editor = sDefaultSharedPreferences.edit();
        if (editor.putBoolean(PREF_INITED, init).commit()) {
            inited = init;
        }
    }
    
    public static boolean getInited(){
        if (inited == null) {
            inited = sDefaultSharedPreferences.getBoolean(PREF_INITED, false); 
        }    
        return inited;
    }
    
}