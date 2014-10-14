package com.easemob.chatuidemo.activity;

import com.easemob.chat.EMGroupManager;
import com.easemob.chatuidemo.R;
import com.easemob.exceptions.EaseMobException;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class NewAnonymousGropActivity extends BaseActivity{
	private EditText groupNameEditText;
	private EditText introductionEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_anonymos_group);
		
		groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
		introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
	}
	
	
	/**
	 * 创建群组
	 * @param v
	 */
	public void save(View v) {
		String name = groupNameEditText.getText().toString();
		final String groupName = groupNameEditText.getText().toString().trim();
		final String desc = introductionEditText.getText().toString();
		if (TextUtils.isEmpty(name)) {
			Intent intent = new Intent(this, AlertDialog.class);
			intent.putExtra("msg", "群组名称不能为空");
			startActivity(intent);
		} else {
			new Thread(new Runnable() {
				public void run() {
					try {
						//创建匿名群
						//demo把群主昵称写死成我是群主，可以调用EMGroupManager.getInstance().getRandomString(9)生成nick
						EMGroupManager.getInstance().createAnonymousGroup(groupName, "我是群主", desc, null, 200);
						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(NewAnonymousGropActivity.this, "创建匿名群成功", 0).show();
								finish();
							}
						});
					} catch (EaseMobException e) {
						e.printStackTrace();
						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(NewAnonymousGropActivity.this, "创建群组失败，请检查网络或稍候重试", 1).show();
							}
						});
					}
				}
			}).start();
		}
	}
	
	public void back(View view){
		finish();
	}
}
