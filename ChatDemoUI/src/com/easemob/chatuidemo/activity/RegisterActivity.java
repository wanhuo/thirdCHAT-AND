package com.easemob.chatuidemo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.chat.EMChatConfig;
import com.easemob.chat.EMChatManager;
import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;
import com.easemob.exceptions.EMNetworkUnconnectedException;
import com.easemob.exceptions.EaseMobException;

/**
 * 注册页
 *
 */
public class RegisterActivity extends Activity{
	private EditText userNameEditText;
	private EditText passwordEditText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		userNameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
	}
	
	
	/**
	 * 注册
	 * @param view
	 */
	public void register(View view){
		final String username = userNameEditText.getText().toString();
		final String pwd = passwordEditText.getText().toString();
		if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)){
			final ProgressDialog pd = new ProgressDialog(this);
			pd.setMessage("正在注册...");
			pd.show();
			final String appkey = EMChatConfig.getInstance().APPKEY;
			new Thread(new Runnable() {
				public void run() {
					try {
						//调用sdk注册方法
						EMChatManager.getInstance().createAccountOnServer(appkey + "_" + username, pwd);
						runOnUiThread(new Runnable() {
							public void run() {
								pd.dismiss();
								//保存用户名
								DemoApplication.getInstance().setUserName(username);
								Toast.makeText(getApplicationContext(), "注册成功", 0).show();
								finish();
							}
						});
					} catch (final Exception e) {
						runOnUiThread(new Runnable() {
							public void run() {
								pd.dismiss();
								if(e!=null&&e.getMessage()!=null)
								{
									String errorMsg=e.getMessage();
									if(errorMsg.indexOf("EMNetworkUnconnectedException")!=-1)
									{
										Toast.makeText(getApplicationContext(), "网络异常，请检查网络！", 0).show();
									}else if(errorMsg.indexOf("conflict")!=-1)
									{
										Toast.makeText(getApplicationContext(), "用户已存在！", 0).show();
									}else{
										Toast.makeText(getApplicationContext(), "注册失败: " + e.getMessage(), 1).show();
									}
									
								}else{
									Toast.makeText(getApplicationContext(), "注册失败: 未知异常", 1).show();
									
								}
							}
						});
					}
				}
			}).start();
			
			
		}
	}
	
	public void back(View view){
		finish();
	}
	
}
