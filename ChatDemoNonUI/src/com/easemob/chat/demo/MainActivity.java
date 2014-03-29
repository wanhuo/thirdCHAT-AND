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
        
        //注册message receiver， 接收聊天消息
        msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        registerReceiver(msgReceiver, intentFilter);
    }

    
    @Override
    protected void onResume() {
        super.onResume();
        showLoginProgressDialog();
        //登录到聊天服务器。此处使用了一个测试账号，用户名是test1，密码是123456。
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
        //登出聊天服务器
        EMChatManager.getInstance().logout();
    }
    
    @Override
    public void onDestroy() {
        //反注册接收聊天消息的message receiver
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
     * 发送消息。本demo是发送消息给测试机器人（其账号为"bot"）。该测试机器人接收到消息后会把接收的消息原封不动的自动发送回来
     * @param view
     */
    public void onSendTxtMsg(View view) {
        try {
            //创建一个消息
            EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
            //设置消息的接收方
            msg.setReceipt("bot");
            //设置消息内容。本消息类型为文本消息。
            TextMessageBody body = new TextMessageBody(tvMsg.getText().toString());
            msg.addBody(body);
        
            //下面的code 展示了如果添加扩展属性 
            msg.setAttribute("extStringAttr", "String Test Value");
            msg.setAttribute("extBoolTrue", true);
            msg.setAttribute("extBoolFalse", false);
            msg.setAttribute("extIntAttr", 100);
            
            //发送消息
            EMChatManager.getInstance().sendMessage(msg);
            Log.d("EMChat Demo", "消息发送成功:" + msg.toString());
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "消息发送失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 接收消息的BroadcastReceiver
     *
     */
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //消息id
            String msgId = intent.getStringExtra("msgid");
            //消息发送方
            String msgFrom = intent.getStringExtra("from");
            //消息类型
            int msgType = intent.getIntExtra("type", 0);
            //消息内容
            String msgBody = intent.getStringExtra("body");
            
            Log.d("EMChat Demo", "new message id:" + msgId + " from:" + msgFrom + " type:" + msgType + " body:" + msgBody);
            tvReceivedMsg.append("from:" + msgFrom + " body:" + msgBody + " \n");
            
            //从SDK 根据消息ID 可以获得消息对象
            EMMessage message = EMChatManager.getInstance().getMessage(msgId);
            
        }
    }

}
