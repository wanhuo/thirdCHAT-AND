package com.easemob.chatuidemo.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chatuidemo.Constant;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.domain.User;

/**
 * 简单的好友Adapter实现
 *
 */
public class ContactAdapter extends ArrayAdapter<User>{

	private LayoutInflater layoutInflater;
	private EditText query;
	private ImageButton clearSearch;

	public ContactAdapter(Context context, int textViewResourceId, List<User> objects) {
		super(context, textViewResourceId, objects);
		layoutInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? 0 : 1;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0) {//搜索框
			if(convertView == null){
				convertView = layoutInflater.inflate(R.layout.search_bar, null);
				query = (EditText) convertView.findViewById(R.id.query);
				clearSearch = (ImageButton) convertView.findViewById(R.id.search_clear);
				query.addTextChangedListener(new TextWatcher() {
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						getFilter().filter(s);
						if (s.length() > 0) {
							clearSearch.setVisibility(View.VISIBLE);
						} else {
							clearSearch.setVisibility(View.INVISIBLE);
						}
					}
	
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}
	
					public void afterTextChanged(Editable s) {
					}
				});
				clearSearch.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
						if (((Activity) getContext()).getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
							manager.hideSoftInputFromWindow(((Activity) getContext()).getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
						//清楚搜索框文字
						query.getText().clear();
					}
				});
			}
		}else{
			if(convertView == null){
				convertView = layoutInflater.inflate(R.layout.row_contact, null);
			}
			
			ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
			TextView unreadMsgView = (TextView) convertView.findViewById(R.id.unread_msg_number);
			TextView nameTextview = (TextView) convertView.findViewById(R.id.name);
			User user = getItem(position);
			//设置nick，demo里不涉及到完整user，用username代替nick显示
			String username = user.getUsername();
			
			//显示新的朋友item
			if(username.equals(Constant.NEW_FRIENDS_USERNAME)){
				nameTextview.setText(user.getNick());
				avatar.setImageResource(R.drawable.new_friends_icon);
				if(user.getUnreadMsgCount() > 0){
					unreadMsgView.setVisibility(View.VISIBLE);
					unreadMsgView.setText(user.getUnreadMsgCount()+"");
				}else{
					unreadMsgView.setVisibility(View.INVISIBLE);
				}
			}else{
				nameTextview.setText(username);
				unreadMsgView.setVisibility(View.INVISIBLE);
				avatar.setImageResource(R.drawable.default_avatar);
			}
		}
		
		return convertView;
	}
	
	@Override
	public User getItem(int position) {
		return position == 0 ? new User() : super.getItem(position - 1);
	}
	
	@Override
	public int getCount() {
		//有搜索框，cout+1
		return super.getCount() + 1;
	}
	
}
