package com.easemob.chatuidemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.easemob.chatuidemo.R;

public class ExitGroupDialog extends Activity{
    private TextView text;
    private Button exitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logout_actionsheet);
        
        text = (TextView) findViewById(R.id.tv_text);
        exitBtn = (Button) findViewById(R.id.btn_exit);
        
        text.setText(R.string.exit_group_hint);
        String toast = getIntent().getStringExtra("deleteToast");
        if(toast != null)
        	text.setText(toast);
        exitBtn.setText(R.string.exit_group);
    }
    
    public void logout(View view){
    	setResult(RESULT_OK);
        finish();
        
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
