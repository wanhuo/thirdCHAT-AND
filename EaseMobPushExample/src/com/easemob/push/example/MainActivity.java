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
    private static MainActivity instance = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        instance = this;
        Button btnStopPush = (Button)findViewById(R.id.btn_stop_push);
        btnStopPush.setOnClickListener(this);
        
        Button btnResumemPush = (Button)findViewById(R.id.btn_resume_push);
        btnResumemPush.setOnClickListener(this);
        
        tvDebugOutput = (TextView)findViewById(R.id.tv_debug_msg);
        tvDebugOutput.setMovementMethod(new ScrollingMovementMethod());
        tvDebugOutput.append("\n\r");
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_stop_push:
            EaseMobPush.stopPush(this);
            break;
        case R.id.btn_resume_push:
            EaseMobPush.resumePush(this);
            break;
        }
    }
    
    public static void printDebugMsg(String msg) {
        instance.tvDebugOutput.append(msg);
        instance.tvDebugOutput.append("\n\r");
    }

}
