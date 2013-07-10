package com.easemob.demo;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.easemob.chat.EMDuplicateResourceException;
import com.easemob.chat.EMNetworkUnconnectedException;
import com.easemob.chat.EMUser;
import com.easemob.chat.EaseMob;
import com.easemob.chat.EaseMobException;
import com.easemob.chat.callbacks.CreateAccountCallBack;
import com.easemob.chat.domain.EMUserBase;
import com.easemob.ui.activity.AlertDialog;

public class Register extends Activity {
    private static final String TAG = Register.class.getSimpleName();

	private EditText userNameEditText;
	private EditText passwordEditText;
	private EditText confirmPasswordEditText;
    private ProgressDialog progressDialog;

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
	}
	
	public void register(View view){
	    final String username = userNameEditText.getText().toString();
	    final String password = passwordEditText.getText().toString();
	    final String confirmpassword = confirmPasswordEditText.getText().toString();
        if(username.isEmpty()){
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", getString(R.string.regist_input_account)));
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

                    ChatDemoApp.setUserName(username);
                    
                    Log.d("register", "create user successful: " + user.getName());                    
                    startActivityForResult(new Intent(getContext(), AlertDialog.class).putExtra("msg", getString(R.string.register_success)), REQUEST_CODE_REG_CONFIRM);
                }

                @Override
                public void onFailure(EaseMobException cause) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    
                    if(cause instanceof EMDuplicateResourceException) {
                        Log.w(TAG, cause.getMessage());
                        if(cause.getMessage().contains("Creating company failed")) {
                            startActivity(new Intent(Register.this, AlertDialog.class).putExtra("msg", getString(R.string.register_failure)+"：" + getString(R.string.register_failure_company_exist)));
                         } else {
                            startActivity(new Intent(Register.this, AlertDialog.class).putExtra("msg", getString(R.string.register_failure)+"：" + getString(R.string.register_failure_user_exist)));
                        }
                        return;
                    }
                    
                    if(cause instanceof EMNetworkUnconnectedException) {
                        Log.w(TAG, "网络连接不可用，请稍后重试");
                        startActivity(new Intent(Register.this, AlertDialog.class).putExtra("msg", getString(R.string.register_failure)+"：" + getString(R.string.login_failuer_network_unconnected)));        
                        return;
                    }
                    
                    Log.e(TAG, cause.getMessage()); 
                    startActivity(new Intent(Register.this, AlertDialog.class).putExtra("msg", getString(R.string.register_failure)+"：" + cause.getMessage()));                 
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
	
}
