package com.easemob.demo;

import com.easemob.user.domain.Group;
import com.easemob.demo.db.DBOpenHelper;
import com.easemob.user.EaseMobUser;

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
        
        EaseMobUser.getUserManager().logout();
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
