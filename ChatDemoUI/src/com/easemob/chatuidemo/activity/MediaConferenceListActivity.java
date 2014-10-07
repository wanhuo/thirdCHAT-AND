package com.easemob.chatuidemo.activity;

import java.util.List;

import org.jivesoftware.smack.XMPPException;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.core.EMConferenceIQ.EMConferenceRoom;
import com.easemob.chatuidemo.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MediaConferenceListActivity extends BaseActivity{
	private ListView listView;
	private List<EMConferenceRoom> confList = null;
	private MediaConferenceAdapter adapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media_conference);
		listView = (ListView)findViewById(R.id.list);
		
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("loading media conference rooms");
		pd.setCancelable(false);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(){
			@Override
			public void run(){
				try {
					confList = EMChatManager.getInstance().getMediaConferenceRooms();
					
					MediaConferenceListActivity.this.runOnUiThread(new Runnable(){
						@Override
						public void run(){
							adapter = new MediaConferenceAdapter(MediaConferenceListActivity.this,1,confList);
							listView.setAdapter(adapter);
							listView.setOnItemClickListener(new OnItemClickListener(){

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									// TODO Auto-generated method stub
									final String confId = confList.get(position).getConferenceID();
									final String confName = confList.get(position).getConferenceName();
									
									Log.i("", "the selected conf id : " + confId);
									pd.setMessage("joining conference room : " + confName);
									pd.setCancelable(false);
									pd.setCanceledOnTouchOutside(false);
									pd.show();
									new Thread(){
										@Override
										public void run(){
											try {
												EMChatManager.getInstance().joinMediaConferenceRoom(confId);
												MediaConferenceListActivity.this.runOnUiThread(new Runnable(){
													@Override
													public void run(){
														pd.dismiss();
												        Toast.makeText(getApplicationContext(), "加入成功", 0).show();
												        Intent intent = new Intent(MediaConferenceListActivity.this, MediaConferenceCallActivity.class);
												        intent.putExtra("confId", confId);
												        intent.putExtra("confName", confName);
												        startActivity(intent);
													}
												});
											} catch (XMPPException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
												final String msg = e.getMessage();
												MediaConferenceListActivity.this.runOnUiThread(new Runnable(){
													@Override
													public void run(){
														pd.dismiss();
												        Toast.makeText(getApplicationContext(), "加入失败: " + msg, 0).show();
													}
												});
											}	
										}
									}.start();
								}
								
							});
							adapter.notifyDataSetChanged();
							pd.dismiss();	
						}
					});
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					final String msg = e.getMessage();
					MediaConferenceListActivity.this.runOnUiThread(new Runnable(){
						@Override
						public void run(){
							pd.setMessage("faild to load media rooms");
							pd.dismiss();
							Toast.makeText(getApplicationContext(), "获取会议失败: " + msg, 0).show();
						}
					});
				}
			}
		}.start();
	}
	
	@Override
	protected void onStart(){
		super.onStart();
	}
}

class MediaConferenceAdapter extends ArrayAdapter<EMConferenceRoom> {

	private LayoutInflater inflater;

	public MediaConferenceAdapter(Context context, int res, List<EMConferenceRoom> groups) {
		super(context, res, groups);
		this.inflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.row_media_conference_room, null);
		}
		
		((TextView)convertView.findViewById(R.id.name)).setText(getItem(position).getConferenceName());
		return convertView;
	}

	@Override
	public int getCount() {
		return super.getCount();
	}

}