/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.chatuidemo.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.adapter.GroupAdapter;

public class GroupsActivity extends BaseActivity {
	private ListView groupListView;
	protected List<EMGroup> grouplist;
	private GroupAdapter groupAdapter;
	private InputMethodManager inputMethodManager;
	public static GroupsActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_groups);
		
		instance = this;
		inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		grouplist =	EMGroupManager.getInstance().getAllGroups();
		groupListView = (ListView)findViewById(R.id.list);
		groupAdapter = new GroupAdapter(this, 1, grouplist);
		groupListView.setAdapter(groupAdapter);
		registerForContextMenu(groupListView);
		groupListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == groupAdapter.getCount() - 1) {
					view.showContextMenu();
				} else {
					
					//进入群聊
					Intent intent = new Intent(GroupsActivity.this, ChatActivity.class);
					// it is group chat
					intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
					intent.putExtra("groupId", groupAdapter.getItem(position - 1).getGroupId());
					startActivity(intent);
				}
			}

		});
		groupListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});
	}
	
	/**
	 * 进入公开群聊列表
	 */
	public void onPublicGroups(View view){
		startActivity(new Intent(this, PublicGroupsActivity.class));
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(0,0,0,"新建群聊");
		menu.add(0,1,0,"新建匿名群");
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(item.getItemId() == 0){
			//新建群聊
			startActivity(new Intent(GroupsActivity.this, NewGroupActivity.class));
		}else{
			startActivity(new Intent(GroupsActivity.this, NewAnonymousGropActivity.class));
		}
		
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		grouplist = EMGroupManager.getInstance().getAllGroups();
		groupAdapter = new GroupAdapter(this, 1, grouplist);
		groupListView.setAdapter(groupAdapter);
		groupAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
	}
	
	/**
	 * 返回
	 * @param view
	 */
	public void back(View view){
		finish();
	}
}
