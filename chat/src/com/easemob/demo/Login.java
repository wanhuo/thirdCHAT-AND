package com.easemob.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.easemob.EaseMob;
import com.easemob.chat.callbacks.LoginCallBack;
import com.easemob.chat.domain.EMUserBase;
import com.easemob.demo.domain.DemoUser;
import com.easemob.exceptions.EMAuthenticationException;
import com.easemob.exceptions.EMNetworkUnconnectedException;
import com.easemob.exceptions.EMResourceNotExistException;
import com.easemob.exceptions.EaseMobException;
import com.easemob.ui.activity.AlertDialog;

public class Login extends Activity {
    private static final String TAG = Login.class.getSimpleName();

    private EditText usernameEditText;
    private EditText passwordEditText;
    private ProgressDialog progressDialog;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(Gl.getUserName() != null && Gl.getPassword() != null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        } else {        
            usernameEditText = (EditText) findViewById(R.id.username);
            passwordEditText = (EditText) findViewById(R.id.password);
            String userName = Gl.getUserName();
            if (userName != null) {
                usernameEditText.setText(userName);
            }
        }
	}

	public void login(View view) {    
	    final String userName = usernameEditText.getText().toString();	    
	    final String password = passwordEditText.getText().toString();
        if(userName.isEmpty()){
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", getString(R.string.login_input_username)));
        } else if (password.isEmpty()){
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", getString(R.string.login_input_pwd)));
        } else {
        	showLoginDialog();
            Gl.setUserName(userName);
            
    		EaseMob.init(this.getApplicationContext());
    		EaseMob.login(userName, password, new LoginCallBack() {
                @Override
                public void onSuccess(Object user) {       
                    DemoUser demoUser = ((EMUserBase)user).toType(DemoUser.class);
                    Gl.setPassword(password);
                    
                    ChatUtil.addUser(Login.this, demoUser);
                    
                    //Gl.setUserInfoSaveState(true);
                    
                    closeLogingDialog();
                    startActivity(new Intent(Login.this, MainActivity.class).putExtra("loggedin", true));
                    finish();
                }

                @Override
                public void onFailure(final EaseMobException cause) {
                   Log.e(TAG, "login: " + cause.getMessage());
                   closeLogingDialog();
                   
                   if(cause instanceof EMAuthenticationException) {
                       startActivity(new Intent(Login.this, AlertDialog.class).putExtra("msg", getString(R.string.login_failure)+": " + getString(R.string.login_failuer_pswerror)));        
                   } else if(cause instanceof EMNetworkUnconnectedException) {
                       startActivity(new Intent(Login.this, AlertDialog.class).putExtra("msg", getString(R.string.login_failure)+": " + getString(R.string.login_failuer_network_unconnected)));        
                   } else if(cause instanceof EMResourceNotExistException) {
                       startActivity(new Intent(Login.this, AlertDialog.class).putExtra("msg", getString(R.string.login_failure)+": " + getString(R.string.login_failuer_toast)));           
                   } else {
                       startActivity(new Intent(Login.this, AlertDialog.class).putExtra("msg", getString(R.string.login_failure)+": " + getString(R.string.login_failuer_toast)));        
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

        }		
	}

	public void register(View view) {
		startActivityForResult(new Intent(this, Register.class), 0);
	}

	@Override
    protected void onResume() {
        super.onResume();
        
        //may back from register activity. refresh username if necessary
        if (Gl.getUserName() != null) {
            usernameEditText.setText(Gl.getUserName());
        }
	}
       
	protected void showLoginDialog() {
		if ((!isFinishing()) && (this.progressDialog == null)) {
			this.progressDialog = new ProgressDialog(this);
		}
		this.progressDialog.setMessage(getString(R.string.logining));
		this.progressDialog.setCanceledOnTouchOutside(false);
		this.progressDialog.show();
	}
	
	protected void closeLogingDialog() {
		if (this.progressDialog != null) {
			this.progressDialog.dismiss();
		}
	}

}
