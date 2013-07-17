package com.easemob.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMNetworkUnconnectedException;
import com.easemob.chat.EMUser;
import com.easemob.chat.EaseMob;
import com.easemob.chat.EaseMobException;
import com.easemob.chat.EaseMobService;
import com.easemob.chat.UserUtil;
import com.easemob.chat.callbacks.ConnectionListener;
import com.easemob.chat.callbacks.ContactListener;
import com.easemob.chat.callbacks.GetContactsCallback;
import com.easemob.chat.callbacks.UpdateAccountCallBack;
import com.easemob.chat.domain.EMUserBase;
import com.easemob.chat.domain.Group;
import com.easemob.chat.domain.Message;
import com.easemob.chat.domain.MessageFactory;
import com.easemob.demo.R;
import com.easemob.ui.activity.ChatActivity;
import com.easemob.ui.activity.ChatHistoryFragment;
import com.easemob.ui.activity.ContactGroupListFragment;
import com.easemob.ui.activity.ContactListFragment;
import com.easemob.ui.adapter.ContactAdapter;
import com.easemob.ui.adapter.ContactPagerAdapter;
import com.easemob.ui.adapter.RowAdapter;
import com.easemob.ui.widget.Sidebar;


public class MainActivity extends FragmentActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	public static MainActivity instance = null;
	private Button[] mTabs;
	private Drawable[] selectedTabs;
	private Drawable[] unSelectedTabs;
	private int currentTabIndex;

	private Fragment[] fragments;
	private TextView unreadLabel;
	InputMethodManager inputManager = null;  
	
	public static boolean isChat = false;
	
	public static Map<String, EMUserBase> allUsers;
	

	MyConnectionListener remoteConnectionListener = new MyConnectionListener();
	MyContactListener remoteContactListener = new MyContactListener();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
		instance = this;


		mTabs = new Button[2];
		mTabs[0] = (Button) findViewById(R.id.btn_conversations);
		mTabs[1] = (Button) findViewById(R.id.btn_contacts);
		selectedTabs = new Drawable[] { 
				getResources().getDrawable(R.drawable.tab_weixin_pressed), 
				getResources().getDrawable(R.drawable.tab_find_frd_pressed),
				getResources().getDrawable(R.drawable.tab_address_pressed),
				getResources().getDrawable(R.drawable.tab_settings_pressed) };
		unSelectedTabs = new Drawable[] {
				getResources().getDrawable(R.drawable.tab_weixin_normal),
				getResources().getDrawable(R.drawable.tab_find_frd_normal),
				getResources().getDrawable(R.drawable.tab_address_normal), 
				getResources().getDrawable(R.drawable.tab_settings_normal) };
		currentTabIndex = 0;
		fragments = new Fragment[] { 
				new ChatHistoryFragment(),
				new ContactGroupListFragment()};
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragments[0]).commit();

		
		/************************************ EaseMob SDK Start ******************************************/
		
		/***** Use EaseMob SDK. Step 1: Call EaseMob.init() and EaseMob.login ***************************/
        String userName = ChatDemoApplication.getUserName();
        String password = ChatDemoApplication.getPassword();
        
		EaseMob.init(this.getApplicationContext(), userName, password);
		EaseMob.login();
		
		/***** Use EaseMob SDK. Step 2: Register receivers to receive chat message and push message *****/
        IntentFilter chatIntentFilter = new IntentFilter(EaseMobService.BROADCAST_CHAT_ACTION);
        chatIntentFilter.setPriority(3);
        registerReceiver(chatBroadcastReceiver, chatIntentFilter);
        isChatBroadcastReceiverRegistered = true;

 		/***** Use EaseMob SDK. Step 3: Register listeners to receive contact and connection event *******/
        // Register receiver for contact changed event.
       	EaseMob.addContactListener(remoteContactListener);
        // Register receiver for connection status event.
       	EaseMob.addConnectionListener(remoteConnectionListener);
       	
		/********************************** EaseMob SDK End    *******************************************/
        
       	
		// Load all available users from local DB. This also load users' chat history
		allUsers = ChatUtil.loadAllUsers(this);
		EMUser.setAllUsers(allUsers);
	}

	@Override
	protected void onResume() {
		super.onResume();
        processMsgNotification();
		updateUnreadLabel();
		if(isChat){
			FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
			trx.hide(fragments[currentTabIndex]);
			if (!fragments[0].isAdded()) {
				trx.add(R.id.fragment_container, fragments[0]);
			}
			trx.show(fragments[0]).commit();
			
			mTabs[currentTabIndex].setCompoundDrawablesWithIntrinsicBounds(null, unSelectedTabs[currentTabIndex], null, null);
			mTabs[0].setCompoundDrawablesWithIntrinsicBounds(null, selectedTabs[0], null, null);
			currentTabIndex = 0;
			isChat =false;
		}
		
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		EaseMob.removeContactListener(remoteContactListener);
		EaseMob.removeConnectionListener(remoteConnectionListener);

		if (chatBroadcastReceiver != null && isChatBroadcastReceiverRegistered) {
		    try {
			    unregisterReceiver(chatBroadcastReceiver);
			    isChatBroadcastReceiverRegistered = false;
		    } catch (Exception e) {
		        e.printStackTrace(); 
		    }
		}

	}


	public void onTabClicked(View view) {
		int index = 0;
		switch (view.getId()) {
		case R.id.btn_contacts:
			index = 1;
			break;
		default:
			break;
		}
		if (currentTabIndex != index) {

			FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
			trx.hide(fragments[currentTabIndex]);
			if (!fragments[index].isAdded()) {
				trx.add(R.id.fragment_container, fragments[index]);
			}
			trx.show(fragments[index]).commit();

			if (index == 0) {
				((ChatHistoryFragment) fragments[0]).rowAdapter.notifyDataSetChanged();
			}
		}
		mTabs[currentTabIndex].setCompoundDrawablesWithIntrinsicBounds(null, unSelectedTabs[currentTabIndex], null, null);
		mTabs[index].setCompoundDrawablesWithIntrinsicBounds(null, selectedTabs[index], null, null);
		currentTabIndex = index;
	}

	public void onAddContact(View view) {
		startActivity(new Intent(this, AddContact.class));
	}
	
	
	private boolean isChatBroadcastReceiverRegistered = false;
	private BroadcastReceiver chatBroadcastReceiver = new BroadcastReceiver() {
	    
	    @Override
		public void onReceive(Context context, Intent intent) {
			/*
			 * 
			 * the new message is not handled by previous handlers
			 */

			Log.d(TAG, "received msg:" + intent.getStringExtra(EaseMobService.MESSAGE));
			EMUserBase tmpUser = null;
			
			    String from = intent.getStringExtra("FROM");
	            tmpUser = MainActivity.allUsers.get(from);
	            if (tmpUser == null) {
	                Log.e(TAG, "receive msg error, cant find user with ID:" + from);
	                System.err.println("usres:" +  allUsers.keySet().toArray().toString());
	                abortBroadcast();
	                return;
	            }
			
					
			
			int rowId;
			Message message = MessageFactory.createMsgFromIntent(intent);
			tmpUser.setTableName(tmpUser.getUsername());
            
			rowId = tmpUser.addMessage(message, true);
			message.setRowId(rowId+"");
			message.setBackReceive(true);
			updateUnreadLabel();
			if (currentTabIndex == 0) {
				ChatHistoryFragment tmp = (ChatHistoryFragment) fragments[0];
				tmp.rowAdapter = new RowAdapter(getApplicationContext(), R.layout.row_weixin, 
				        ChatUtil.loadUsersWithRecentChat(
				                new ArrayList<EMUserBase>(allUsers.values()), context));
				tmp.listView.setAdapter(tmp.rowAdapter);
				tmp.rowAdapter.notifyDataSetChanged();
			}
			abortBroadcast();
		}
	};

	public int getUnreadMsgCountTotal() {
		int unreadMsgCountTotal = 0;
		for (EMUserBase user : allUsers.values()) {
			unreadMsgCountTotal += user.getUnreadMsgCount();
		}
		return unreadMsgCountTotal;
	}

	private void updateUnreadLabel() {
		int count = getUnreadMsgCountTotal();
		if (count > 0) {
			unreadLabel.setText(String.valueOf(count));
			unreadLabel.setVisibility(View.VISIBLE);
		} else {
			unreadLabel.setVisibility(View.INVISIBLE);
		}
	}

	private void updateUserinfo() {
        Log.d("###", "Update User Info");
        final DemoUser user = ChatUtil.loadUser(this, EaseMob.getCurrentUserName());

        EMUser.updateContactInBackground(user, new UpdateAccountCallBack() {
            @Override
            public void onSuccess(EMUserBase updatedUser) {
                 ChatUtil.updateUser(MainActivity.this, user);
            }

            @Override
            public void onFailure(EaseMobException cause) {
                Log.e(TAG, "Error updating contact on remote server", cause);
                Log.d("###", "update failed");
                if(cause instanceof EMNetworkUnconnectedException) {
                }
            }
        });
    }
	
	private class MyConnectionListener implements ConnectionListener {
		@Override
		public void onConnected() {
			runOnUiThread(new Runnable() {
				public void run() {
					ChatHistoryFragment fragment1 = (ChatHistoryFragment) fragments[0]; 
					fragment1.errorItem.setVisibility(View.GONE);				
					Toast.makeText(MainActivity.this, instance.getResources().getString(R.string.network_connected), Toast.LENGTH_LONG).show();
				}
			});	
			 
			// process the notification message if there are any
			processMsgNotification();

			// initialize the whole contact list only once
			if (!ChatDemoApplication.getInited()) {
			    GetContactsCallbackImpl callback = new GetContactsCallbackImpl();
	            callback.addUserMode = true;	
	            callback.setInitedAfterSuccess = true;
			    EMUser.getContactsInBackground(callback);
			}
		}

		@Override
		public void onDisConnected() {
			runOnUiThread(new Runnable() {
				public void run() {
					ChatHistoryFragment fragment1 = (ChatHistoryFragment) fragments[0]; 
					fragment1.errorItem.setVisibility(View.VISIBLE);					
					Toast.makeText(MainActivity.this, instance.getResources().getString(R.string.network_unavailable), Toast.LENGTH_LONG).show();
				}
			});			
		}

		@Override
		public void onReConnected() {
			runOnUiThread(new Runnable() {
				public void run() {
					ChatHistoryFragment fragment1 = (ChatHistoryFragment) fragments[0]; 
					fragment1.errorItem.setVisibility(View.GONE);
					Toast.makeText(MainActivity.this, "网络重新连接成功", Toast.LENGTH_LONG).show();
				}
			});
		}

		@Override
		public void onReConnecting() {
			// TODO Auto-generated method stub
		}

		@Override
		public void onConnecting(String progress) {
			runOnUiThread(new Runnable() {
				public void run() {
					
				}
			});			
		}
	}

	private class MyContactListener implements ContactListener {
		@Override
		public void onContactUpdated(String[] userIdList) {
	        GetContactsCallbackImpl callback = new GetContactsCallbackImpl();
	        callback.addUserMode = true;
	        EMUser.getContactsInBackground(userIdList, callback);
		}

		@Override
		public void onContactAdded(String[] userIdList) {
	        GetContactsCallbackImpl callback = new GetContactsCallbackImpl();
	        callback.addUserMode = true;
	        EMUser.getContactsInBackground(userIdList, callback);
		}

		@Override
		public void onContactDeleted(String[] userIdList) {
			System.out.println("onContactDeleted");
        }
	}

	class GetContactsCallbackImpl implements GetContactsCallback {
	    
	    //if add user mode, we will add remote user to local
	    //otherwise, we will sync the contacts between local and remote 
	    public boolean addUserMode = false;
	    public boolean setInitedAfterSuccess = false;
	    
		@Override
		public void onSuccess(final List<EMUserBase> contacts) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
				    if (addUserMode) {
				        ChatUtil.addOrUpdateUsers(MainActivity.this, contacts);
				    } else {
				        //local contacts will be delete if not exists on remote server
					    ChatUtil.addOrUpdateOrDeleteUsers(MainActivity.this, contacts);
				    }
					allUsers = ChatUtil.loadAllUsers(MainActivity.this);
					EMUser.setAllUsers(allUsers);
					// Refresh UI
					switch (currentTabIndex) {
					case 0:
						((ChatHistoryFragment) fragments[0]).rowAdapter.notifyDataSetChanged();
						break;
					case 1:
						ContactGroupListFragment tmp = ((ContactGroupListFragment) fragments[1]);
						List<EMUserBase> list = new ArrayList<EMUserBase>();
                        for (EMUserBase user : EMUser.allUsers.values()) {
                            if (!(user instanceof Group)) {
                                list.add(user);
                            }
                        }
						
						Collections.sort(list, new Comparator<EMUserBase>() {
							@Override
							public int compare(EMUserBase lhs, EMUserBase rhs) {
								return ((DemoUser)lhs).getHeader().compareTo(((DemoUser)rhs).getHeader());

							}
						});
						tmp.list = list;
						if(tmp.vPager.getCurrentItem() != 0 && addUserMode)
							tmp.vPager.setCurrentItem(0);
						tmp.contactAdapter = new ContactAdapter(
						        getApplicationContext(), R.layout.row_contact, tmp.list,tmp.sidebar);
						tmp.contactListView.setAdapter(tmp.contactAdapter);
						tmp.contactAdapter.notifyDataSetChanged();
						break;
					default:
						break;
					}
					
					if(setInitedAfterSuccess) {
		                ChatDemoApplication.setInited(true);
					}
				}
			});
		}

		@Override
		public void onFailure(EaseMobException cause) {
			if (cause instanceof EMNetworkUnconnectedException) {
                Log.w(TAG, "GetContactsCallback failed: 网络连接不可用，请稍后重试: " + cause);
				return;
			}

			Log.e(TAG, "GetContactsCallback failed: " + cause.getMessage());
		}
	}

	private void processMsgNotification() {
		try {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			com.easemob.chat.Message notificationMsg;
			while ((notificationMsg = EMChat.getNextNotificationMsg()) != null) {
				Message message = MessageFactory.createMsgFromNotification(notificationMsg);
				EMUserBase tmpUser = MainActivity.allUsers.get(notificationMsg.getFrom());
				tmpUser.setTableName(tmpUser.getUsername());
				tmpUser.addMessage(message, true);
				updateUnreadLabel();
				notificationManager.cancel(notificationMsg.getNotificationID());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override  
	 public boolean onTouchEvent(MotionEvent event) {  
	  // TODO Auto-generated method stub  
	  if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){  
	     if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){  
	       inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);  
	     }  
	  }  
	  return super.onTouchEvent(event);  
	 }  

}
