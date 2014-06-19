package com.easemob.chatuidemo.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.chat.EMGroupInfo;
import com.easemob.chat.EMGroupManager;
import com.easemob.chatuidemo.R;
import com.easemob.exceptions.EaseMobException;

public class PublicGroupsActivity extends Activity {
	private ProgressBar pb;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_public_groups);

		pb = (ProgressBar) findViewById(R.id.progressBar);
		listView = (ListView) findViewById(R.id.list);

		new Thread(new Runnable() {
			public void run() {
				try {
					// 从服务器获取所用公开的群聊
					final List<EMGroupInfo> groupsList = EMGroupManager.getInstance().getAllPublicGroupsFromServer();
					runOnUiThread(new Runnable() {
						public void run() {
							pb.setVisibility(View.INVISIBLE);
							listView.setAdapter(new GroupsAdapter(PublicGroupsActivity.this, 1, groupsList));
							
							//设置item点击事件
							listView.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									startActivity(new Intent(PublicGroupsActivity.this, GroupSimpleDetailActivity.class).
											putExtra("groupinfo", groupsList.get(position)));
								}
							});
						}
					});
				} catch (EaseMobException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							pb.setVisibility(View.INVISIBLE);

						}
					});
				}
			}
		}).start();

	}

	private class GroupsAdapter extends ArrayAdapter<EMGroupInfo> {

		private LayoutInflater inflater;

		public GroupsAdapter(Context context, int res, List<EMGroupInfo> groups) {
			super(context, res, groups);
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_group, null);
			}

			((TextView) convertView.findViewById(R.id.name)).setText(getItem(position).getGroupName());

			return convertView;
		}
	}
	
	public void back(View view){
		finish();
	}
}
