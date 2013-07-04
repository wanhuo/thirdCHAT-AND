package com.easemob.demo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.easemob.chat.EaseMob;


public final class DemoApp extends Application {

	private static final String VERSION = "20004";
	private static final String PREF_FILE_NAME = "EaseMob";
	public static final String appId = "qixin_10000";
	public static String qixin_cache = Environment.getExternalStorageDirectory() + "/Android/data/com.easemob.qixin/";
	public static String image_cache = qixin_cache + "imageCache";
	private static Context sContext;
	private static SharedPreferences sDefaultSharedPreferences; // settings from preference activity 

    public static DemoApp instance = null;
    
    public static boolean isUpdateFail = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		init(this.getApplicationContext());
	}
	
	public void onTerminate() {
        super.onTerminate();
    }
	
	public static DemoApp getInstance() {
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
		EaseMob.applicationContext = context;
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
	
    //company name
    public static final String PREF_COMPANYNAME = "companyName";
    private static String companyName = null;
    
    /**
     * @param userName
     */
    public static void setCompanyName(String company){
        //REVISIT: Can we set channel_username to null?
        if (company != null && !company.equals(companyName)) {
            SharedPreferences.Editor editor = sDefaultSharedPreferences.edit();
            if (editor.putString(PREF_COMPANYNAME, company).commit()) {
            	companyName = company;
            }
        }
    }
    
    /**
     * @return
     */
    public static String getCompanyName(){
        if (companyName == null) {
            companyName = sDefaultSharedPreferences.getString(PREF_COMPANYNAME, null); 
        }
        return companyName;
    }	
    
    
  //company name
    public static final String PREF_COMPANYKEY = "companyKey";
    private static String companyKey = null;
    
    /**
     * @param userName
     */
    public static void setCompanyKey(String company){
        //REVISIT: Can we set channel_username to null?
        if (company != null && !company.equals(companyKey)) {
            SharedPreferences.Editor editor = sDefaultSharedPreferences.edit();
            if (editor.putString(PREF_COMPANYKEY, company).commit()) {
                companyKey = company;
            }
        }
    }
    
    /**
     * @return
     */
    public static String getCompanyKey(){
        if (companyKey == null) {
            companyKey = sDefaultSharedPreferences.getString(PREF_COMPANYKEY, null); 
        }
        return companyKey;
    }
    
  //login user name
    public static final String PREF_USERNAME = "username";
    private static String userName = null;
    
    /**
     * @param userName
     */
    public static void setUserName(String user){
        System.err.println("set user name:" + user);
        if (user != null && !user.equals(userName)) {
            SharedPreferences.Editor editor = sDefaultSharedPreferences.edit();
            if (editor.putString(PREF_USERNAME, user).commit()) {
                System.err.println("!!!!!!! set user name to:" + user);
                userName = user;
            }
        }
    }
    
    /**
     * @return
     */
    public static String getUserName(){
        if (userName == null) {
            userName = sDefaultSharedPreferences.getString(PREF_USERNAME, null); 
        }
        return userName;
    }
    
    
  //login password
    public static final String PREF_PWD = "pwd";
    private static String password = null;
    
    /**
     * @param password
     */
    public static void setPassword(String pwd){
        SharedPreferences.Editor editor = sDefaultSharedPreferences.edit();
        if (editor.putString(PREF_PWD, pwd).commit()) {
            password = pwd;
        }
    }
    
    /**
     * @return
     */
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