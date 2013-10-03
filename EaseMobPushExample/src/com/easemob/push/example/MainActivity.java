package com.easemob.push.example;

import com.easemob.push.EaseMobPush;

import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

    private TextView tvDebugOutput;
    Button btnStopPush;
    Button btnResumePush;
    private static MainActivity instance = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        instance = this;
        btnStopPush = (Button)findViewById(R.id.btn_stop_push);
        btnStopPush.setOnClickListener(this);
        
        btnResumePush = (Button)findViewById(R.id.btn_resume_push);
        btnResumePush.setOnClickListener(this);
        
        tvDebugOutput = (TextView)findViewById(R.id.tv_debug_msg);
        tvDebugOutput.setMovementMethod(new ScrollingMovementMethod());
        tvDebugOutput.append("\n\r");
        
        btnResumePush.setEnabled(false);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_stop_push:
            EaseMobPush.stopPush(this.getApplicationContext());
            btnStopPush.setEnabled(false);
            btnResumePush.setEnabled(true);
            break;
        case R.id.btn_resume_push:
            EaseMobPush.resumePush(this.getApplicationContext());
            btnResumePush.setEnabled(false);
            btnStopPush.setEnabled(true);
            break;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    public static void printDebugMsg(String msg) {
        instance.tvDebugOutput.append(msg);
        instance.tvDebugOutput.append("\n\r");
    }

    @Override
    protected void onResume() {
        System.out.println("main activity resumed");
        super.onResume();
    }


    @Override
    protected void onPause() {
        System.out.println("main activity paused");
        super.onPause();
    }
}
