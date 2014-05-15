package com.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chatuidemo.Constant;
import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.User;
import com.easemob.exceptions.EaseMobException;

/**
 * 登陆页面
 * 
 */
public class LoginActivity extends Activity {
	private EditText usernameEditText;
	private EditText passwordEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		//如果用户名密码都有，直接进入主页面
		if (DemoApplication.getInstance().getUserName() != null && DemoApplication.getInstance().getPassword() != null) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
		
	}

	/**
	 * 登陆
	 * 
	 * @param view
	 */
	public void login(View view) {
		final String username = usernameEditText.getText().toString();
		final String password = passwordEditText.getText().toString();
		
		
		if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)){
			final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
			pd.setCanceledOnTouchOutside(false);
			pd.setMessage("正在登陆...");
			pd.show();
			//调用sdk登陆方法登陆聊天服务器
			EMChatManager.getInstance().login(username, password, new EMCallBack() {
				
				@Override
				public void onSuccess() {
					//登陆成功，保存用户名密码
					DemoApplication.getInstance().setUserName(username);
					DemoApplication.getInstance().setPassword(password);
					runOnUiThread(new Runnable() {
						public void run() {
							pd.setMessage("正在获取好友列表");
						}
					});
					try {
						//demo中简单的处理成每次登陆都去获取好友username，开发者自己根据情况而定
						List<String> usernames = EMChatManager.getInstance().getContactUserNames();
						Map<String,User> userlist = new HashMap<String, User>();
						for(String username : usernames){
							User user = new User();
							user.setUsername(username);
							userlist.put(username, user);
						}
						//添加user"新的朋友"
						User newFriends = new User();
						newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
						newFriends.setNick("新的朋友");
						userlist.put(Constant.NEW_FRIENDS_USERNAME,newFriends);
						//存入内存
						DemoApplication.getInstance().setContactList(userlist);
						UserDao dao = new UserDao(LoginActivity.this);
						//存入db
						List<User> users = new ArrayList<User>(userlist.values());
						dao.saveContactList(users);
					} catch (Exception e) {
					}
					pd.dismiss();
					//进入主页面
					startActivity(new Intent(LoginActivity.this, MainActivity.class));
					finish();
				}
				
				@Override
				public void onProgress(int progress, String status) {
					
				}
				
				@Override
				public void onError(int code, final String message) {
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getApplicationContext(), "登录失败: " + message, 0).show();
							
						}
					});
				}
			});
		}
	}

	/**
	 * 注册
	 * 
	 * @param view
	 */
	public void register(View view) {
		 startActivityForResult(new Intent(this, RegisterActivity.class), 0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if (DemoApplication.getInstance().getUserName() != null) {
			usernameEditText.setText(DemoApplication.getInstance().getUserName());
		}
	}
}
