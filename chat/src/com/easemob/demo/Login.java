package com.easemob.demo;

import com.easemob.demo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class Login extends Activity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
	}

	public void login(View view) {
	    DemoApp.setCompanyName("金生丽水");
        DemoApp.setCompanyKey("10000");
        DemoApp.setUserName(usernameEditText.getText().toString());
        DemoApp.setPassword(passwordEditText.getText().toString());
        finish();
        startActivity(new Intent(this, MainActivity.class));
	}
}
