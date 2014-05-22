package com.easemob.chatuidemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;

public class SettingsFragment extends Fragment{
	private Button logoutBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_conversation_settings, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		logoutBtn = (Button) getView().findViewById(R.id.btn_logout);
		logoutBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DemoApplication.getInstance().logout();
				// 重新显示登陆页面
				((MainActivity)getActivity()).finish();
				startActivity(new Intent(getActivity(), LoginActivity.class));
			}
		});
	}
	
	
}
