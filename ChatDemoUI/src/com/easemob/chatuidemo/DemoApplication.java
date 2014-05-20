package com.easemob.chatuidemo;



import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.User;

public class DemoApplication extends Application {

    public static Context applicationContext;
	private static DemoApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";
	private String userName = null;
	// login password
	private static final String PREF_PWD = "pwd";
	private String password = null;
	private Map<String,User> contactList;
	
	
    @Override
    public void onCreate() {             
         super.onCreate();
         applicationContext = this;
         instance = this;
         
         //初始化易聊SDK,一定要先调用init()
         Log.d("EMChat Demo", "initialize EMChat SDK");
         EMChat.getInstance().init(applicationContext);
         //debugmode设为true后，就能看到sdk打印的log了
         EMChat.getInstance().setDebugMode(true);
         //默认添加好友时，是不需要验证的，改成需要验证
         EMChatOptions options = new EMChatOptions();
         options.setAcceptInvitationAlways(false);
         EMChatManager.getInstance().setChatOptions(options);
         
         
         if(getUserName() != null && contactList == null){
        	 UserDao dao = new UserDao(applicationContext);
        	 //获取本地好友user list到内存,方便以后获取好友list
        	 contactList = dao.getContactList();
         }
    }
    
    
    public static DemoApplication getInstance() {
		return instance;
	}
    
    
    
    /**
     * 获取内存中好友user list
     * @return
     */
    public Map<String,User> getContactList() {
		return contactList;
	}

    /**
     * 设置好友user list到内存中
     * @param contactList
     */
	public void setContactList(Map<String,User> contactList) {
		this.contactList = contactList;
	}


	/**
	 * 获取用户名
	 * 
	 * @return
	 */
	public String getUserName() {
		if (userName == null) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(applicationContext);
			userName = preferences.getString(PREF_USERNAME, null);
		}
		return userName;
	}

	/**
	 * 获取密码
	 * 
	 * @return
	 */
	public String getPassword() {
		if (password == null) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(applicationContext);
			password = preferences.getString(PREF_PWD, null);
		}
		return password;
	}

	/**
	 * 设置用户名
	 * 
	 * @param user
	 */
	public void setUserName(String username) {
		if (username != null) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(applicationContext);
			SharedPreferences.Editor editor = preferences.edit();
			if (editor.putString(PREF_USERNAME, username).commit()) {
				userName = username;
			}
		}
	}

	/**
	 * 设置密码
	 * 
	 * @param pwd
	 */
	public void setPassword(String pwd) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(applicationContext);
		SharedPreferences.Editor editor = preferences.edit();
		if (editor.putString(PREF_PWD, pwd).commit()) {
			password = pwd;
		}
	}
	
	/**
	 * 退出登录,清空数据
	 */
	public void logout() {
		// reset password to null
		setPassword(null);
		setContactList(null);
		// 退出sdk
		EMChatManager.getInstance().logout();
		
	}
}
