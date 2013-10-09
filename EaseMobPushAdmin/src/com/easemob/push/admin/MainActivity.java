package com.easemob.push.admin;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity{

    private static MainActivity instance = null;
    private EditText tvAppKey;
    private EditText tvMsg;
    
    private PubSubPubClient client;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvAppKey = (EditText)findViewById(R.id.et_appkey);
        tvAppKey.setText("pushtest1");
        tvMsg = (EditText)findViewById(R.id.et_msg);
        tvMsg.setText("push test");
        instance = this;
        
        client = new PubSubPubClient();
    }
    
    
    public void onCreateNode(View v) {
        new SendTask().execute("create");
    }
    
    public void onDeleteNode(View v) {
        new SendTask().execute("delete");
    }
    
    public void onListNode(View v) {
        new SendTask().execute("list");
    }
    
    public void onSendMessage(View v) {
        new SendTask().execute("push");
    }
    
    public void onSendNotification(View v) {
        new SendTask().execute("notification");
    }
    
    private class SendTask extends AsyncTask<String, Void, Boolean> {
        
        protected Boolean doInBackground(String... args) {
            String appKey = tvAppKey.getText().toString();
            String message = tvMsg.getText().toString();
            String type = args[0];
            boolean result = false;
            if (type.equals("notification")) {
                result = client.publishNotification(appKey, message);
            } else if (type.equals("push")) {
                result = client.publishMsg(appKey, message);
            } else if (type.equals("create")) {
                result = client.createPubSubNode(appKey);
            } else if (type.equals("delete")) {
                result = client.deletePubSubNode(appKey);
            } else if (type.equals("list")) {
                result = client.listPubSubNode(appKey);
            } 
            return new Boolean(result);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(instance,  "request exec succfully", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(instance,  "request failed. check logs for detail", Toast.LENGTH_LONG).show();
            }
        }
    }
    
}
