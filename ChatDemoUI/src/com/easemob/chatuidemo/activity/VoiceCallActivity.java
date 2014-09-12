package com.easemob.chatuidemo.activity;

import com.easemob.chatuidemo.R;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class VoiceCallActivity extends BaseActivity implements OnClickListener{
	private LinearLayout comingBtnContainer;
	private Button hangupBtn;
	private Button refuseBtn;
	private Button answerBtn;
	private ImageView muteImage;
	private ImageView handsFreeImage;
	
	private boolean isMuteState;
	private boolean isHandsfreeState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voice_call);
		
		
		comingBtnContainer = (LinearLayout) findViewById(R.id.ll_coming_call); 
		refuseBtn = (Button) findViewById(R.id.btn_refuse_call);
		answerBtn = (Button) findViewById(R.id.btn_answer_call);
		hangupBtn = (Button) findViewById(R.id.btn_hangup_call);
		muteImage = (ImageView) findViewById(R.id.iv_mute);
		handsFreeImage = (ImageView) findViewById(R.id.iv_handsfree);
		
		refuseBtn.setOnClickListener(this);
		answerBtn.setOnClickListener(this);
		hangupBtn.setOnClickListener(this);
		muteImage.setOnClickListener(this);
		handsFreeImage.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_refuse_call: //挂断响铃
			finish();
			break;
			
		case R.id.btn_answer_call: //接听
			comingBtnContainer.setVisibility(View.INVISIBLE);
			hangupBtn.setVisibility(View.VISIBLE);
			break;

		case R.id.btn_hangup_call: //挂断
			finish();
			break;
			
		case R.id.iv_mute:  //静音开关
			if(isMuteState){
				//关闭静音开关
				muteImage.setImageResource(R.drawable.icon_mute_normal);
				isMuteState = false;
			}else{
				muteImage.setImageResource(R.drawable.icon_mute_on);
				isMuteState = true;
			}
			break;
		case R.id.iv_handsfree: //免提开关
			if(isHandsfreeState){
				//关闭静音开关
				handsFreeImage.setImageResource(R.drawable.icon_speaker_normal);
				isHandsfreeState = false;
			}else{
				handsFreeImage.setImageResource(R.drawable.icon_speaker_on);
				isHandsfreeState = true;
			}
			break;
		default:
			break;
		}
	}
	
	
	
	
}
