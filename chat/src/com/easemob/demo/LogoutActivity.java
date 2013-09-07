package com.easemob.demo;

import com.easemob.EaseMob;
import com.easemob.chat.db.EaseMobMsgDB;
import com.easemob.chat.db.MsgDBOpenHelper;
import com.easemob.chat.domain.Group;
import com.easemob.demo.db.DBOpenHelper;

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
        //clearn up global variables
        MainActivity.allUsers.clear();
        Group.allGroups.clear();
        
        //close contact db
        DBOpenHelper.closeDB();

        //reset password to null
        Gl.setPassword(null);
        //reset inited so that the new user can retrieve contact list after login 
        Gl.setInited(false);
        
        EaseMob.logout();
        finish();
        MainActivity.instance.finish();
//        setResult(RESULT_OK);
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
