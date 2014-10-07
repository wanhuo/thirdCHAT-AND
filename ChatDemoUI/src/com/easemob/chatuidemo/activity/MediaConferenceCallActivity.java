package com.easemob.chatuidemo.activity;

import org.jivesoftware.smack.XMPPException;

import com.easemob.chat.EMChatManager;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.R.color;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class MediaConferenceCallActivity extends BaseActivity{
	private TextView tokenSatus = null;
	private Button requireToken = null;
	private boolean isTalkTokenGranted = false;
	private String confId = null;
	private String confName = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media_conference_call);
		
		tokenSatus = (TextView) findViewById(R.id.token_status);
		requireToken = (Button) findViewById(R.id.talk);
		
		confId = getIntent().getStringExtra("confId");
		confName = getIntent().getStringExtra("confName");
		
		requireToken.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					if(!isTalkTokenGranted){
						requireToken.setBackgroundColor(color.gray_pressed);
						requireTalkToken();
					}
					return true;
				}else if(event.getAction() == MotionEvent.ACTION_UP){
					requireToken.setBackgroundColor(color.gray_normal);
					requireToken.requestLayout();
				    releaseTalkToken();
					return true;
				}
				return false;
			}
			
		});
	}
	
	private void requireTalkToken(){
		new Thread(){
			@Override
			public void run(){
				try {
					EMChatManager.getInstance().requireTalkToken(confId);
					isTalkTokenGranted = true;
					MediaConferenceCallActivity.this.runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							tokenSatus.setText("talk is granted!");
						}
						
					});
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					MediaConferenceCallActivity.this.runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							tokenSatus.setText("failed to require the talk token, please try later...");
						}
						
					});
				}
			}
		}.start();
	}
	
	@Override
	public void onDestroy(){
		exitRoom();
		super.onDestroy();
	}
	
	private void releaseTalkToken(){
		new Thread(){
			@Override
			public void run(){
				EMChatManager.getInstance().releaseTalkToken(confId);
				isTalkTokenGranted = false;
				MediaConferenceCallActivity.this.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						tokenSatus.setText("token is released!");
					}
					
				});
			}
		}.start();
	}
	
	private void exitRoom(){
		EMChatManager.getInstance().exitMediaConferenceRoom(confId);
	}
}
