package com.easemob.demo;

import com.easemob.user.EMUserManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class LogoutActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logout_actionsheet);
	}

	public void logout(View view) {
        
        //reset password to null
        ChatDemoApp.getInstance().setPassword(null);
        //reset inited so that the new user can retrieve contact list after login 
        ChatDemoApp.getInstance().setInited(false);

        //退出sdk
        EMUserManager.getInstance().logout();
        finish();
        
        //重新显示登陆页面
        MainActivity.instance.finish();
        startActivity(new Intent(this, Login.class));
	}

	public void cancel(View view) {
		finish();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}

}
