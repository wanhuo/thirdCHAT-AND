package com.easemob.demo;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.easemob.chat.EMUser;
import com.easemob.EaseMob;
import com.easemob.chat.callbacks.CreateAccountCallBack;
import com.easemob.chat.domain.EMUserBase;
import com.easemob.demo.R;
import com.easemob.exceptions.EMDuplicateResourceException;
import com.easemob.exceptions.EMNetworkUnconnectedException;
import com.easemob.exceptions.EaseMobException;
import com.easemob.ui.activity.AlertDialog;

public class Register extends Activity {
    private static final String TAG = Register.class.getSimpleName();

	private EditText userNameEditText;
	private EditText passwordEditText;
	private EditText confirmPasswordEditText;
    private ProgressDialog progressDialog;

    //a-z, 0-9, underscore, hyphen.   Length at least 3 characters and maximum length of 15 
    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
    
    private static final int REQUEST_CODE_REG_CONFIRM = 1;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		userNameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		confirmPasswordEditText = (EditText) findViewById(R.id.confirm_password);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.registering));
		progressDialog.setCanceledOnTouchOutside(false);
	}
	
	public void register(View view){
	    final String username = userNameEditText.getText().toString();
	    final String password = passwordEditText.getText().toString();
	    final String confirmpassword = confirmPasswordEditText.getText().toString();
        if(username.isEmpty()){
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", getString(R.string.regist_input_account)));
        } else if(!validate(username)){
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", getString(R.string.regist_invalid_username)));
        } else if (password.isEmpty() && confirmpassword.isEmpty()){
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", getString(R.string.login_input_pwd)));
        } else if (!password.equals(confirmpassword)){
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", getString(R.string.regist_pwd_twice_error)));
        } else {
        	progressDialog.show();       	

            EaseMob.init(this.getApplicationContext());
		    EMUser.createAppUserInBackground(username, password, new CreateAccountCallBack() {
                @Override
                public void onSuccess(EMUserBase user) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }

                    Gl.setUserName(username);                    
                
                    startActivityForResult(new Intent(getContext(), AlertDialog.class).putExtra("msg", getString(R.string.register_success)), REQUEST_CODE_REG_CONFIRM);
                }

                @Override
                public void onFailure(EaseMobException cause) {
                    Log.e(TAG, cause.getMessage());
                    
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    
                    if(cause instanceof EMDuplicateResourceException) {
                        startActivity(new Intent(Register.this, AlertDialog.class).putExtra("msg", getString(R.string.register_failure)+": " + getString(R.string.register_failure_user_exist)));
                    } else if (cause instanceof EMNetworkUnconnectedException) {
                        startActivity(new Intent(Register.this, AlertDialog.class).putExtra("msg", getString(R.string.register_failure)+": " + getString(R.string.login_failuer_network_unconnected)));        
                    } else {                    
                        startActivity(new Intent(Register.this, AlertDialog.class).putExtra("msg", getString(R.string.register_failure)+": " + getString(R.string.login_failuer_toast)));
                    }
                }

				@Override
				public void onProgress(final String progress) {
	                runOnUiThread(new Runnable() {
	                    public void run() {
	    					progressDialog.setMessage(progress);
	                    }
	                });					
				}		    
		    });     
		    
		    progressDialog.show();
        }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if (requestCode == REQUEST_CODE_REG_CONFIRM){
				finish();
				back(null);//back to the login view
			}
	    }
	}
	
	@Override
	public void onBackPressed() {
		back(null);
	}
	
	public void back(View view){
		finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}
	
	public Context getContext() {
	    return this;
	}
		   
    public boolean validate(final String username) {  
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }
}
