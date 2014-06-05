package com.easemob.chatuidemo.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView.CommaTokenizer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.ConnectionListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMNotifier;
import com.easemob.chat.GroupChangeListener;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.TextMessageBody;
import com.easemob.chatuidemo.Constant;
import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.db.InviteMessgeDao;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.InviteMessage;
import com.easemob.chatuidemo.domain.InviteMessage.InviteMesageStatus;
import com.easemob.chatuidemo.domain.User;
import com.easemob.chatuidemo.utils.CommonUtils;
import com.easemob.util.HanziToPinyin;

public class MainActivity extends FragmentActivity {

	protected static final String TAG = "MainActivity";
	// 未读消息textview
	private TextView unreadLabel;
	//未读通讯录textview
	private TextView unreadAddressLable;
	
	private Button[] mTabs;
	private ContactlistFragment contactListFragment;
	private ChatHistoryFragment chatHistoryFragment;
	private SettingsFragment settingFragment;
	private Fragment[] fragments;
	private int index;
	private RelativeLayout[] tab_containers;
	//当前fragment的index
	private int currentTabIndex;
	private NewMessageBroadcastReceiver msgReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		inviteMessgeDao = new InviteMessgeDao(this);
		userDao = new UserDao(this);
		chatHistoryFragment = new ChatHistoryFragment();
		contactListFragment = new ContactlistFragment();
		settingFragment = new SettingsFragment();
		fragments = new Fragment[] {chatHistoryFragment, contactListFragment, settingFragment };
		// 添加显示第一个fragment
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, chatHistoryFragment).
			add(R.id.fragment_container, contactListFragment).hide(contactListFragment).show(chatHistoryFragment).commit();
		
		// 注册一个接收消息的BroadcastReceiver
		msgReceiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
		intentFilter.setPriority(3);
		registerReceiver(msgReceiver, intentFilter);

		// 注册一个ack回执消息的BroadcastReceiver
		IntentFilter ackMessageIntentFilter = new IntentFilter(EMChatManager.getInstance().getAckMessageBroadcastAction());
		ackMessageIntentFilter.setPriority(3);
		registerReceiver(ackMessageReceiver, ackMessageIntentFilter);
		
		//注册一个好友请求同意好友请求等的BroadcastReceiver
		IntentFilter inviteIntentFilter = new IntentFilter(EMChatManager.getInstance().getContactInviteEventBroadcastAction());
		registerReceiver(contactInviteReceiver, inviteIntentFilter);
		
		//setContactListener监听联系人的变化等
		EMContactManager.getInstance().setContactListener(new MyContactListener());
		//注册一个监听连接状态的listener
		EMChatManager.getInstance().addConnectionListener(new MyConnectionListener());
		//注册群聊相关的listener
		EMGroupManager.getInstance().addGroupChangeListener(new MyGroupChangeListener());
	}

	/**
	 * 初始化组件
	 */
	private void initView() {
		unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
		unreadAddressLable=(TextView) findViewById(R.id.unread_address_number);
		mTabs = new Button[3];
		mTabs[0] = (Button) findViewById(R.id.btn_conversation);
		mTabs[1] = (Button) findViewById(R.id.btn_address_list);
		mTabs[2] = (Button) findViewById(R.id.btn_setting);
		//把第一个tab设为选中状态
		mTabs[0].setSelected(true);
		
	}
	
	/**
	 * button点击事件
	 * @param view
	 */
	public void onTabClicked(View view) {
		switch (view.getId()) {
		case R.id.btn_conversation:
			index = 0;
			break;
		case R.id.btn_address_list:
			index = 1;
			break;
		case R.id.btn_setting:
			index = 2;
			break;
		}
		if (currentTabIndex != index) {
			FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
			trx.hide(fragments[currentTabIndex]);
			if (!fragments[index].isAdded()) {
				trx.add(R.id.fragment_container, fragments[index]);
			}
			trx.show(fragments[index]).commit();
		}
		mTabs[currentTabIndex].setSelected(false);
		//把当前tab设为选中状态
		mTabs[index].setSelected(true);
		currentTabIndex = index;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//注销广播接收者
		try {
			unregisterReceiver(msgReceiver);
		} catch (Exception e) {}
		try {
			unregisterReceiver(ackMessageReceiver);
		} catch (Exception e) {}
		try {
			unregisterReceiver(contactInviteReceiver);
		} catch (Exception e) {}
		
	}


	/**
	 * 刷新未读消息数
	 */
	public void updateUnreadLabel() {
		int count = getUnreadMsgCountTotal();
		if (count > 0) {
			unreadLabel.setText(String.valueOf(count));
			unreadLabel.setVisibility(View.VISIBLE);
		} else {
			unreadLabel.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 刷新新的朋友消息数
	 */
	public void updateUnreadAddressLable(){
		int count=getUnreadAddressCountTotal();
		if(count>0)
		{
			unreadAddressLable.setText(String.valueOf(count));
			unreadAddressLable.setVisibility(View.VISIBLE);
		}else{
			unreadAddressLable.setVisibility(View.INVISIBLE);
		}
		
		
	}
	
	/**
	 * 获取未读新的朋友消息
	 * @return
	 */
	public int getUnreadAddressCountTotal(){
		int unreadAddressCountTotal=0;
		unreadAddressCountTotal=DemoApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME).getUnreadMsgCount();
		return unreadAddressCountTotal;
	}
	
	
	/**
	 * 获取未读消息数
	 * 
	 * @return
	 */
	public int getUnreadMsgCountTotal() {
		int unreadMsgCountTotal = 0;
		unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
		return unreadMsgCountTotal;
	}
	
	/**
	 * 新消息广播接收者
	 * 
	 * 
	 */
	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			//消息id
			String msgId = intent.getStringExtra("msgid");
			//收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
//			EMMessage message = EMChatManager.getInstance().getMessage(msgId);
			
			//刷新bottom bar消息未读数
			updateUnreadLabel();
			if(currentTabIndex == 0){
				//当前页面如果为聊天历史页面，刷新此页面
				if(chatHistoryFragment != null){
					chatHistoryFragment.refresh();
				}
			}
			//注销广播，否则在ChatActivity中会收到这个广播
			abortBroadcast();
		}
	}
	
	/**
	 * 消息回执BroadcastReceiver
	 */
	private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String msgid = intent.getStringExtra("msgid");
			String from = intent.getStringExtra("from");
			EMConversation conversation = EMChatManager.getInstance().getConversation(from);
			if(conversation != null){
				//把message设为已读
				EMMessage msg = conversation.getMessage(msgid);
				if(msg != null){
					msg.isAcked = true;
				}
			}
			abortBroadcast();
		}
	};
	
	private BroadcastReceiver contactInviteReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			//请求理由
			final String reason = intent.getStringExtra("reason");
			final boolean isResponse = intent.getBooleanExtra("isResponse", false);
			//消息发送方username
			final String from = intent.getStringExtra("username");
			
			//接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所有客户端不要重复提醒
			List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
			for(InviteMessage inviteMessage : msgs){
				if(inviteMessage.getFrom().equals(from)){
					return;
				}
			}
			
			InviteMessage msg = new InviteMessage();
			msg.setFrom(from);
			msg.setTime(System.currentTimeMillis());
			msg.setReason(reason);		
			//sdk暂时只提供同意好友请求方法，不同意选项可以参考微信增加一个忽略按钮。
			if(!isResponse){
				Log.d(TAG, from + "请求加你为好友,reason: " + reason);
				//设成未验证
				msg.setStatus(InviteMesageStatus.NO_VALIDATION);
				msg.setInviteFromMe(false);
			}else{
				Log.d(TAG, from + "同意了你的好友请求");
				//对方已同意你的请求
				msg.setStatus(InviteMesageStatus.AGREED);
				msg.setInviteFromMe(true);
			}
			//保存msg
			inviteMessgeDao.saveMessage(msg);
			//未读数加1
			User user = DemoApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME);
			user.setUnreadMsgCount(user.getUnreadMsgCount()+1);
			//提示有新消息
			EMNotifier.getInstance(getApplicationContext()).notifyOnNewMsg();
			
			//刷新bottom bar消息未读数
			updateUnreadAddressLable();
			//刷新好友页面ui
			if(currentTabIndex == 1)
				contactListFragment.refresh();
			abortBroadcast();
			
		}
		
	};
	private InviteMessgeDao inviteMessgeDao;
	private UserDao userDao;
	
	/***
	 * 联系人变化listener
	 *
	 */
	private class MyContactListener implements EMContactListener{

		@Override
		public void onContactAdded(List<String> usernameList) {
			//保存增加的联系人
			Map<String,User> localUsers = DemoApplication.getInstance().getContactList();
			Map<String, User> toAddUsers = new HashMap<String, User>();
			for(String username : usernameList){
				User user = new User();
				user.setUsername(username);
				String headerName = null;
				if (!TextUtils.isEmpty(user.getNick())) {
					headerName = user.getNick();
				} else {
					headerName = user.getUsername();
				}
				if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
					user.setHeader("");
				} else if (Character.isDigit(headerName.charAt(0))) {
					user.setHeader("#");
				} else {
					user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1))
							.get(0).target.substring(0, 1).toUpperCase());
					char header = user.getHeader().toLowerCase().charAt(0);
					if (header < 'a' || header > 'z') {
						user.setHeader("#");
					}
				} 
				//暂时有个bug，添加好友时可能会回调added方法两次
				if(!localUsers.containsKey(username)){
					userDao.saveContact(user);
				}
				toAddUsers.put(username, user);
			}
			localUsers.putAll(toAddUsers);
			//刷新ui
			if(currentTabIndex == 1)
				contactListFragment.refresh();
			
		}

		@Override
		public void onContactDeleted(List<String> usernameList) {
			//删除联系人
			Map<String,User> localUsers = DemoApplication.getInstance().getContactList();
			for(String username : usernameList){
				localUsers.remove(username);
				userDao.deleteContact(username);
			}
			//刷新ui
			if(currentTabIndex == 1)
				contactListFragment.refresh();
			updateUnreadLabel();
		}

		
	}
	
	private class MyConnectionListener implements ConnectionListener{

		@Override
		public void onConnected() {
			chatHistoryFragment.errorItem.setVisibility(View.GONE);
		}

		@Override
		public void onDisConnected(String errorString) {
			chatHistoryFragment.errorItem.setVisibility(View.VISIBLE);
			chatHistoryFragment.errorText.setText("连接不到聊天服务器");
		}

		@Override
		public void onReConnected() {
			chatHistoryFragment.errorItem.setVisibility(View.GONE);
		}

		@Override
		public void onReConnecting() {
		}

		@Override
		public void onConnecting(String progress) {
		}
		
	}
	
	/**
	 * MyGroupChangeListener
	 */
	private class MyGroupChangeListener implements GroupChangeListener {

		@Override
		public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
			//被邀请
			EMMessage msg = EMMessage.createReceiveMessage(Type.TXT);
			msg.setGroupId(groupId);
			msg.setMsgId(UUID.randomUUID().toString());
			msg.setFrom(inviter);
			msg.addBody(new TextMessageBody(inviter + "邀请你加入了群聊"));
			//保存邀请消息
			EMChatManager.getInstance().saveMessage(msg);
			//提醒新消息
			EMNotifier.getInstance(getApplicationContext()).notifyOnNewMsg();
			runOnUiThread(new Runnable() {
				public void run() {
					updateUnreadLabel();
					//刷新ui
					if(currentTabIndex == 0)
						chatHistoryFragment.refresh();
					if(CommonUtils.getTopActivity(MainActivity.this).equals(GroupsActivity.class.getName())){
						GroupsActivity.instance.onResume();
					}
				}
			});
			
		}

		@Override
		public void onInvitationAccpted(String groupId, String inviter, String reason) {
			
		}

		@Override
		public void onInvitationDeclined(String groupId, String invitee, String reason) {
			
		}


		@Override
		public void onUserRemoved(String groupId, String groupName) {
			//提示用户被T了，demo省略此步骤
			//刷新ui
			runOnUiThread(new Runnable() {
				public void run() {
					if(currentTabIndex == 0)
						chatHistoryFragment.refresh();
					if(CommonUtils.getTopActivity(MainActivity.this).equals(GroupsActivity.class.getName())){
						GroupsActivity.instance.onResume();
					}
				}
			});
		}

		@Override
		public void onGroupDestroy(String groupId, String groupName) {
			
		}

		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateUnreadLabel();
		updateUnreadAddressLable();
	}
	
}
