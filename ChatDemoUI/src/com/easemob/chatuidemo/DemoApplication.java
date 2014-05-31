package com.easemob.chatuidemo;



import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chatuidemo.db.DbOpenHelper;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.User;
import com.easemob.chatuidemo.utils.PreferenceUtils;

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
         
         int pid = android.os.Process.myPid();
         String processAppName = getAppName(pid);
         if (processAppName == null || processAppName.equals("")) {
             //workaround for baidu location sdk 3.3
             //百度定位sdk3.3，定位服务运行在一个单独的进程，每次定位服务启动的时候，都会调用application::onCreate 创建新的进程。
             //但环信的sdk只需要在主进程中初始化一次。 这个特殊处理是，如果从pid 找不到对应的processInfo processName，
             //则此application::onCreate 是被service 调用的，直接返回
             return;
         }
         
        applicationContext = this;
        instance = this;
        // 初始化易聊SDK,一定要先调用init()
        Log.d("EMChat Demo", "initialize EMChat SDK");
        EMChat.getInstance().init(applicationContext);
        // debugmode设为true后，就能看到sdk打印的log了
        EMChat.getInstance().setDebugMode(true);
        
        //获取到EMChatOptions对象
        EMChatOptions options = EMChatManager.getInstance().getChatOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        //设置收到消息是否有新消息通知，默认为true
        options.setNotificationEnable(PreferenceUtils.getInstance(applicationContext).getSettingMsgNotification());
        //设置收到消息是否有声音提示，默认为true
        options.setNoticeBySound(PreferenceUtils.getInstance(applicationContext).getSettingMsgSound());
        //设置收到消息是否震动 默认为true
        options.setNoticedByVibrate(PreferenceUtils.getInstance(applicationContext).getSettingMsgVibrate());
        //设置语音消息播放是否设置为扬声器播放 默认为true
        options.setUseSpeaker(PreferenceUtils.getInstance(applicationContext).getSettingMsgSpeaker());

               
        if (getUserName() != null && contactList == null) {
            UserDao dao = new UserDao(applicationContext);
            // 获取本地好友user list到内存,方便以后获取好友list
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
	 * 获取当前登陆用户名
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
		//先调用sdk logout，在清理app中自己的数据
		EMChatManager.getInstance().logout();
		DbOpenHelper.getInstance(applicationContext).closeDB();
		// reset password to null
		setPassword(null);
		setContactList(null);
		
	}
	
	private String getAppName(int pID)
	{
	    String processName = null;
	    ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
	    List l = am.getRunningAppProcesses();
	    Iterator i = l.iterator();
	    PackageManager pm = this.getPackageManager();
	    while(i.hasNext()) 
	    {
	          ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo)(i.next());
	          try 
	          { 
	              if(info.pid == pID)
	              {
	                  CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
	                  //Log.d("Process", "Id: "+ info.pid +" ProcessName: "+ info.processName +"  Label: "+c.toString());
	                  //processName = c.toString();
	                  processName = info.processName;
	                  return processName;
	              }
	          }
	          catch(Exception e) 
	          {
	                //Log.d("Process", "Error>> :"+ e.toString());
	          }
	   }
	    return processName;
	}
}
