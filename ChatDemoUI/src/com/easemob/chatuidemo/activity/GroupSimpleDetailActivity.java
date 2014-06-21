package com.easemob.chatuidemo.activity;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupInfo;
import com.easemob.chat.EMGroupManager;
import com.easemob.chatuidemo.R;
import com.easemob.exceptions.EaseMobException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GroupSimpleDetailActivity extends Activity {
	private Button btn_add_group;
	private TextView tv_admin;
	private TextView tv_name;
	private TextView tv_introduction;
	private String groupid;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_simle_details);
		tv_name = (TextView) findViewById(R.id.name);
		tv_admin = (TextView) findViewById(R.id.tv_admin);
		btn_add_group = (Button) findViewById(R.id.btn_add_to_group);
		tv_introduction = (TextView) findViewById(R.id.tv_introduction);
		progressBar = (ProgressBar) findViewById(R.id.loading);

		EMGroupInfo groupInfo = (EMGroupInfo) getIntent().getSerializableExtra("groupinfo");
		String groupname = groupInfo.getGroupName();
		groupid = groupInfo.getGroupId();
		
		tv_name.setText(groupname);
		
		
		new Thread(new Runnable() {
			public void run() {
				//从服务器获取详情
				try {
					final EMGroup group = EMGroupManager.getInstance().getGroupFromServer(groupid);
					runOnUiThread(new Runnable() {
						public void run() {
							progressBar.setVisibility(View.INVISIBLE);
							if(!group.getMembers().contains(EMChatManager.getInstance().getCurrentUser()))
								btn_add_group.setEnabled(true);
							tv_name.setText(group.getGroupName());
							tv_admin.setText(group.getOwner());
							tv_introduction.setText(group.getDescription());
						}
					});
				} catch (final EaseMobException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							progressBar.setVisibility(View.INVISIBLE);
							Toast.makeText(GroupSimpleDetailActivity.this, "获取群聊信息失败: "+e.getMessage(), 1).show();
						}
					});
				}
				
			}
		}).start();
		
	}
	
	//加入群聊
	public void addToGroup(View view){
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("正在发送请求...");
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					EMGroupManager.getInstance().joinGroup(groupid);
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(GroupSimpleDetailActivity.this, "加入群聊成功", 0).show();
							btn_add_group.setEnabled(false);
						}
					});
				} catch (final EaseMobException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(GroupSimpleDetailActivity.this, "加入群聊失败："+e.getMessage(), 0).show();
						}
					});
				}
			}
		}).start();
	}
	
	public void back(View view){
		finish();
	}
}
