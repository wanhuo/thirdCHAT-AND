package com.easemob.chat.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;

public class MainActivity extends Activity {

    private EditText tvMsg;
    private TextView tvReceivedMsg;
    
    private NewMessageBroadcastReceiver msgReceiver;
	private ProgressDialog progressDialog; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMsg = (EditText)findViewById(R.id.et_msg);
        tvReceivedMsg = (TextView)findViewById(R.id.tv_receive_msg);
        
        //注册messge receiver 接收消息
        msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        registerReceiver(msgReceiver, intentFilter);
    }

    
    @Override
    protected void onResume() {
        super.onResume();
        showLoginProgressDialog();
        //登录到聊天服务器
        EMChatManager.getInstance().login("test1", "123456", new EMCallBack() {

            @Override
            public void onError(int arg0, final String errorMsg) {
                runOnUiThread(new Runnable() {
                    public void run() {
                    	closeLoginProgressDialog();
                        Toast.makeText(MainActivity.this, "登录聊天服务器失败：" + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgress(int arg0, String arg1) {
            }

            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                    	closeLoginProgressDialog();
                        Toast.makeText(MainActivity.this, "登录聊天服务器成功", Toast.LENGTH_SHORT).show();
                    }
                });
                
            }
        });
    }

    /**
     * 显示提示dialog
     */
	private void showLoginProgressDialog() {
		if(progressDialog == null){
        	progressDialog = new ProgressDialog(this); 
        	progressDialog.setMessage("正在登陆...");
        	progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
	}
	
	/**
	 * 关闭提示dialog
	 */
	private void closeLoginProgressDialog(){
		if(progressDialog != null && progressDialog.isShowing()){
			progressDialog.dismiss();
		}
	}
    
    @Override
    protected void onPause() {
        super.onPause();
        //登出聊天服务
        EMChatManager.getInstance().logout();
    }
    
    @Override
    public void onDestroy() {
        if (msgReceiver != null) {
            try {
                unregisterReceiver(msgReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
    
    /**
     * 发送消息
     * @param view
     */
    public void onSendTxtMsg(View view) {
        try {
            EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
            //消息发送给测试机器人，bot 会把消息自动发送回来
            msg.setReceipt("bot");
            TextMessageBody body = new TextMessageBody(tvMsg.getText().toString());
            msg.addBody(body);
        
            //send out msg
            EMChatManager.getInstance().sendMessage(msg);
            Log.d("chatdemo", "消息发送成功:" + msg.toString());
        } catch (EaseMobException e) {
            Toast.makeText(MainActivity.this, "消息发送失败:" + e.getMessage(),1).show();
        }
    }
    
    /**
     * 接收消息的BroadcastReceiver
     * @author admin_new
     *
     */
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msgId = intent.getStringExtra("msgid");
            String msgFrom = intent.getStringExtra("from");
            int msgType = intent.getIntExtra("type", 0);
            String msgBody = intent.getStringExtra("body");
            Log.d("main", "new message id:" + msgId + " from:" + msgFrom + " type:" + msgType + " body:" + msgBody);
            tvReceivedMsg.append("from:" + msgFrom + " body:" + msgBody + " \n");
            }
    }

}
