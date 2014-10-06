package com.easemob.chatuidemo.activity;

import java.util.List;

import org.jivesoftware.smack.XMPPException;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.core.EMConferenceIQ.EMConferenceRoom;
import com.easemob.chatuidemo.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MediaConferenceCallActivity extends BaseActivity{
	private ListView listView;
	private List<EMConferenceRoom> confList = null;
	private MediaConferenceAdapter adapter = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media_conference);
		listView = (ListView)findViewById(R.id.list);
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("loading media rooms");
		pd.show();
		new Thread(){
			@Override
			public void run(){
				try {
					confList = EMChatManager.getInstance().getMediaRooms();
					
					MediaConferenceCallActivity.this.runOnUiThread(new Runnable(){
						@Override
						public void run(){
							adapter = new MediaConferenceAdapter(MediaConferenceCallActivity.this,1,confList);
							listView.setAdapter(adapter);
							listView.setOnItemClickListener(new OnItemClickListener(){

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									// TODO Auto-generated method stub
									String confId = confList.get(position).getConferenceID();
									
									Log.i("", "the selected conf id : " + confId);
								}
								
							});
							adapter.notifyDataSetChanged();
							pd.dismiss();	
						}
					});
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					MediaConferenceCallActivity.this.runOnUiThread(new Runnable(){
						@Override
						public void run(){
							pd.setMessage("faild to load media rooms");
							pd.dismiss();
						}
					});
				}
			}
		}.start();
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