package com.easemob.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMSessionManager;
import com.easemob.chat.EaseMobChat;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.MessageListener;
import com.easemob.demo.domain.DemoUser;
import com.easemob.user.domain.EMUserBase;
import com.easemob.user.domain.Group;
import com.easemob.user.domain.Message;
import com.easemob.user.domain.MessageFactory;
import com.easemob.exceptions.EMNetworkUnconnectedException;
import com.easemob.exceptions.EaseMobException;
import com.easemob.group.EaseMobGroupManager;
import com.easemob.notify.EMNotificationService;
import com.easemob.ui.activity.AddGroup;
import com.easemob.ui.activity.AlertDialog;
import com.easemob.ui.activity.ChatActivity;
import com.easemob.ui.activity.ChatHistoryFragment;
import com.easemob.ui.activity.ContactsListFragment;
import com.easemob.ui.activity.ContactsListFragment.ContactsListFragmentListener;
import com.easemob.ui.activity.GroupListFragment;
import com.easemob.ui.activity.GroupListFragment.GroupListFragmentListener;
import com.easemob.user.EMUserManager;
import com.easemob.user.EaseMobUser;
import com.easemob.user.EaseMobUserConfig;
import com.easemob.user.callbacks.GetContactsCallback;
import com.easemob.user.callbacks.LoginCallBack;
import com.easemob.user.db.EaseMobMsgDB;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_LOGOUT = 1;
    private static final int REQUEST_CODE_EXIT = 2;
	private static final int REQUEST_CODE_CONTACT = 3;
	private static final int REQUEST_CODE_GROUP = 4;
    private static final int REQUEST_CODE_GROUPDETAIL = 10;

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

    //MyConnectionListener remoteConnectionListener = new MyConnectionListener();
    //MyContactListener remoteContactListener = new MyContactListener();

    public boolean wasPaused = false;

	private List<EMUserBase> contactList;

	private MyContactsListFragment contactFragment;

	private MyGroupListFragment groupFragment;
	
	private ChatHistoryFragment chatHistoryFragment;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        unreadLabel = (TextView) findViewById(R.id.unread_msg_number);

        instance = this;

        mTabs = new Button[4];
        mTabs[0] = (Button) findViewById(R.id.btn_conversations);
        mTabs[1] = (Button) findViewById(R.id.btn_contacts);
        mTabs[2] = (Button) findViewById(R.id.btn_groups);
        mTabs[3] = (Button) findViewById(R.id.btn_settings);
        selectedTabs = new Drawable[] { getResources().getDrawable(R.drawable.tab_weixin_pressed),
                getResources().getDrawable(R.drawable.tab_find_frd_pressed),
                getResources().getDrawable(R.drawable.tab_address_pressed),
                getResources().getDrawable(R.drawable.tab_settings_pressed) };
        unSelectedTabs = new Drawable[] { getResources().getDrawable(R.drawable.tab_weixin_normal),
                getResources().getDrawable(R.drawable.tab_find_frd_normal),
                getResources().getDrawable(R.drawable.tab_address_normal),
                getResources().getDrawable(R.drawable.tab_settings_normal) };
        currentTabIndex = 0;
      

        /******************************* EaseMob SDK Start ***********************************/
        /***** Use EaseMob SDK. Step 1: EaseMob.init() and EaseMob.login. ********************/
        String userName = Gl.getUserName();
        String password = Gl.getPassword();
        boolean loggedin = getIntent().getBooleanExtra("loggedin", false);
        EMChatManager.getInstance().addMessageReciverListener(new AppMessageListener());
        // if already login from LoginActivity, skip the login call below
        if (!loggedin) {
            EMUserManager.getInstance().login(userName, password, 
                    new MainLoginCallback());
        } {
            //here, logined from login activity, try to get contacts if not inited;
            //@@@@ temp workaround. need to move contact db, sync to user sdk!!!
            if (!Gl.getInited()) {
                GetContactsCallbackImpl callback = new GetContactsCallbackImpl();
                callback.deleteNonExistingUsers = true;
                callback.setInitedAfterSuccess = true;
                Log.d(TAG, "logined, now start to get contacts in background");
                EMUserManager.getInstance().getContactsInBackground(callback);
            }
        }
        /****** Use EaseMob SDK. Step 2: Register receivers to receive chat message **********/
        // Register receiver on EaseMobService for receiving chat message
        /*
        IntentFilter chatIntentFilter = new IntentFilter(EaseMobService.getBroadcastChatAction());
        chatIntentFilter.setPriority(3);
        registerReceiver(chatBroadcastReceiver, chatIntentFilter);
        */
        isChatBroadcastReceiverRegistered = true;

        // Register receiver for receiving group invited broadcast
        /*
        IntentFilter groupIntentFilter = new IntentFilter(EaseMobService.getBroadcastGroupInvitedAction());
        registerReceiver(groupInvitedReceiver, groupIntentFilter);
        */

        // Register receiver for receiving group deleted broadcast
        /*
        IntentFilter groupDelIntentFilter = new IntentFilter(EaseMobService.getBroadcastGroupChangedAction());
        registerReceiver(groupChangedReceiver, groupDelIntentFilter);
        */

        /****** Use EaseMob SDK. Step 3: Register listeners to receive contact and connection event *******/
        //!!!!!!@@@@ Johnson, need to check below two listeners for sdk2.0
        // Register receiver for contact changed event.
        /*
        EaseMob.getInstance().addContactListener(remoteContactListener);
        // Register receiver for connection status event.
        EaseMob.getInstance().addConnectionListener(remoteConnectionListener);
        */

        /********************************** EaseMob SDK End ******************************************/

        
        // Load all available users from local DB. This also load users' chat history
        allUsers = ChatUtil.loadAllUsers(this);
        
        //@@@ need to find a way to hold and set all users, singleton
        //EaseMobUser.setAllUsers(allUsers);
        EMUserManager.getInstance().setAllUsers(allUsers);
        
        // Load all available groups from local DB. 
        Group.allGroups = EaseMobMsgDB.loadGroups(this);
        
        contactList = new ArrayList<EMUserBase>(allUsers.values());
        //排序
        Collections.sort(contactList, new Comparator<EMUserBase>() {
            @Override
            public int compare(EMUserBase lhs, EMUserBase rhs) {
                return (lhs.getHeader().compareTo(rhs.getHeader()));
            }
        });
        
        contactFragment = new MyContactsListFragment(contactList, new MyContactListListener());
        groupFragment = new MyGroupListFragment(Group.allGroups, null/*new MyGroupListListener()*/);
        chatHistoryFragment = new ChatHistoryFragment();
        fragments = new Fragment[] {chatHistoryFragment, 
        		contactFragment,
        		groupFragment,
        		new SettingFragment()};
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragments[0]).commit();
        
        //until here, notify SDK that the UI is inited
        //!!!!!???? for sdk 2.0? do we still need the app init flag????
        //EaseMobUser.setApplicationInited(true);
    }
    
    /**
     * contactlist 监听事件
     * @author Administrator
     *
     */
    class MyContactListListener implements ContactsListFragmentListener{

		@Override
		public void onListItemClickListener(int position) {

            startActivity(new Intent(MainActivity.this, ChatActivity.class).
                    putExtra("userId", contactFragment.contactAdapter.getItem(position).getUsername()));
//			Toast.makeText(MainActivity.this, "第position"+"被点击", 1).show();
			//点击进入会话页面
			//startActivity(new Intent(MainActivity.this, ChatActivity.class).putExtra("userId", contactList.get(position).getUsername()));
		}
    	
    }
    
    /**
     * grouplist监听事件
     * @author Administrator
     *
     */
    /*
    class MyGroupListListener implements GroupListFragmentListener{

		@Override
		public void onListItemClickListener(int position) {
			//点击新建群组按钮item
		   if (position == groupFragment.groupAdapter.getCount() - 1) {
               if (EaseMobService.isConnected()) {
                   startActivityForResult(new Intent(MainActivity.this, AddGroup.class),REQUEST_CODE_GROUP);
               } else {
                   startActivity(new Intent(MainActivity.this, AlertDialog.class).putExtra(
                           "msg", MainActivity.this.getString(R.string.network_unavailable)));
               }
           } else {//点击普通item进入群组聊天页面
               Intent intent = new Intent(MainActivity.this,ChatActivity.class);
               intent.putExtra("isChat", false);
               //it is group chat
               intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
               intent.putExtra("position", position - 1);
               intent.putExtra("groupId", groupFragment.groupAdapter.getItem(position-1).getGroupId());
               startActivityForResult(intent, REQUEST_CODE_GROUP);
           }
			
		}
    	
    }
    */
    
	//MyContactsListFragment继承了SDK中的ContactsListFragment（即联系人页面），做了以下改动：
    //1. ContactsListFragment缺省包含一个不可见的标题栏，标题栏右侧还有“添加好友”按钮。在MyContactsListFragment中我们将标题栏设为可见
    //2. 重载“添加好友”按钮的处理。
    @SuppressLint("ValidFragment")
	class MyContactsListFragment extends ContactsListFragment {
		public MyContactsListFragment(List<EMUserBase> contactList, ContactsListFragmentListener listener) {
			super(contactList, listener);
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			//显示标题栏
			titleLayout.setVisibility(View.VISIBLE);
			//设置标题
			title.setText("好友");
			//添加好友
			addContactBtn.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					startActivityForResult(new Intent(getActivity(), AddContact.class), REQUEST_CODE_CONTACT);
					
				}
			});
		}
    }
    
    //MyGroupListFragment继承了SDK中的GroupListFragment（即群组页面），做了以下改动：
    //1. ContactsListFragment缺省包含一个不可见的标题栏，标题栏右侧还有“添加群组（添加公开群组）”按钮。在MyContactsListFragment中我们将标题栏设为可见
    //2. 重载“添加群组”按钮的处理。
    @SuppressLint("ValidFragment")
	class MyGroupListFragment extends GroupListFragment {

		public MyGroupListFragment(List<Group> grouplist, GroupListFragmentListener listener) {
			super(grouplist, listener);
		}
    	
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			//显示标题栏
            titleLayout.setVisibility(View.VISIBLE);
            //设置标题
            title.setText("群组");
		}		
    }

    @Override
    protected void onResume() {
        super.onResume();
        processMsgNotification();
        updateUnreadLabel();

        if (wasPaused) {
            // 从后台返回后，检查网络状态。在错误信息提示栏显示“无法连接服务器信息”如果没有网络连接。
            wasPaused = false;
            if (!EMSessionManager.getInstance(this.getApplicationContext()).isConnected()) {
                ChatHistoryFragment fragment1 = (ChatHistoryFragment) fragments[0];
                fragment1.errorItem.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasPaused = true;
    }

    @Override
    public void onDestroy() {
        //EaseMob.setApplicationInited(false);
        //EaseMob.getInstance().removeContactListener(remoteContactListener);
        //EaseMob.getInstance().removeConnectionListener(remoteConnectionListener);

        /*
        if (chatBroadcastReceiver != null && isChatBroadcastReceiverRegistered) {
            try {
                unregisterReceiver(chatBroadcastReceiver);
                isChatBroadcastReceiverRegistered = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        if (groupInvitedReceiver != null) {
            try {
                unregisterReceiver(groupInvitedReceiver);
            } catch (Exception e) {
            }
        }
        if (groupChangedReceiver != null) {
            try {
                unregisterReceiver(groupChangedReceiver);
            } catch (Exception e) {
            }
        }

        super.onDestroy();
    }
        

    public static class SettingFragment extends Fragment{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return inflater.inflate(R.layout.main_tab_settings, container,false);
		}
	}

    public void onTabClicked(View view) {
        int index = 0;
        switch (view.getId()) {
        case R.id.btn_contacts:
            index = 1;
            break;
        case R.id.btn_groups:
        	index = 2;
        	break;
        case R.id.btn_settings:
            index = 3;
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
                ((ChatHistoryFragment) fragments[0]).userRowAdapter.notifyDataSetChanged();
            }
        }
        // mTabs[currentTabIndex].setBackgroundDrawable(null);
        mTabs[currentTabIndex].setCompoundDrawablesWithIntrinsicBounds(null, unSelectedTabs[currentTabIndex], null,
                null);
        // mTabs[index].setBackgroundDrawable(getResources().getDrawable(
        // R.drawable.tab_bg));
        mTabs[index].setCompoundDrawablesWithIntrinsicBounds(null, selectedTabs[index], null, null);
        currentTabIndex = index;
        // }
    }

    public void logout(View view) {
        startActivityForResult(new Intent(this, LogoutActivity.class), REQUEST_CODE_LOGOUT);
    }

    public void onAddContact(View view) {
        startActivity(new Intent(this, AddContact.class));
    }

    // Result of all activities.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
           if (requestCode == REQUEST_CODE_EXIT) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                Editor editor = sp.edit();
                editor.putBoolean("isShowSplash", true);
                editor.commit();
                //EaseMobService.autoRestart = false;
                //stopService(new Intent(this, EaseMobService.class));
                finish();
            } else if(requestCode == REQUEST_CODE_GROUP){
            	//更新群组列表
				groupFragment.groupAdapter.notifyDataSetChanged();
            } else if(requestCode == REQUEST_CODE_CONTACT){
            	contactList.clear();
				contactList.addAll(allUsers.values());
            	//排序
                Collections.sort(contactList, new Comparator<EMUserBase>() {
                    @Override
                    public int compare(EMUserBase lhs, EMUserBase rhs) {
                        return lhs.getHeader().compareTo(rhs.getHeader());

                    }
                });
                //更新好友列表
				contactFragment.contactAdapter.notifyDataSetChanged();
            } else {
                updateUnreadLabel();
            }
        }
    }

    private boolean isChatBroadcastReceiverRegistered = false;
    /*
    private BroadcastReceiver chatBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "main activity received msg:" + intent.getStringExtra(EaseMobService.MESSAGE));
            EMUserBase tmpUser = null;
            String groupId = intent.getStringExtra("GROUP");
            if (groupId != null) {
                Log.d(TAG, "received msg from group:" + groupId);
                Group group = Group.getGroupById(groupId);
                if (group == null) {
                    Log.d(TAG, "ignore msg from unknow group:" + groupId);
                    return;
                }
                tmpUser = group;
                Log.d(TAG, "set groupuser to:" + group.getName());
            } else {
                String from = intent.getStringExtra("FROM");
                tmpUser = MainActivity.allUsers.get(from);
                if (tmpUser == null) {
                    Log.e(TAG, "receive msg error, cant find user with ID:" + from);
                    abortBroadcast();
                    return;
                }
            }
            
            //Update the group or user object with the latest message received
            int rowId;
            Message message = MessageFactory.createMsgFromIntent(intent);
            
            //Save to db
            rowId = tmpUser.addMessage(message, true);
            message.setRowId(rowId + "");
            message.setBackReceive(true);
            
            //Refresh UnreadLabel:
            updateUnreadLabel();
            
            //Refresh ChatHistoryFragment
            if (currentTabIndex == 0) {
                ChatHistoryFragment chatHistoryFragment = (ChatHistoryFragment) fragments[0];
                chatHistoryFragment.refresh();
            }

            abortBroadcast();
        }
    };
    */

    private BroadcastReceiver groupInvitedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String groupId = intent.getStringExtra("groupId");
            String nick = intent.getStringExtra("nick");

            // @ZW： plz fix this
            if (groupId != null) {
                Group group = Group.getGroupById(groupId);
                Message msg = new Message();
                msg.setBody("你被" + nick + "邀请加入" + group.getName());
                msg.setTime(System.currentTimeMillis());
                msg.setFrom("xitong");
                group.addMessage(msg, true);
                updateUnreadLabel();
                ChatHistoryFragment tmp = (ChatHistoryFragment) fragments[0];
                if (tmp != null) {
                    tmp.userRowAdapter.notifyDataSetChanged();
                }

                GroupListFragment groupFragment = (GroupListFragment) fragments[2];
                if (groupFragment != null && groupFragment.groupAdapter != null) {
                    	groupFragment.groupAdapter.notifyDataSetChanged();
                }
            }

        }
    };

    private BroadcastReceiver groupChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // refresh chat history and group list view when group has been
            // deleted
            String groupId = intent.getStringExtra("groupId");
            if (groupId != null) {
                updateUnreadLabel();
                ChatHistoryFragment tmp = (ChatHistoryFragment) fragments[0];
                if (tmp != null) {
                    tmp.userRowAdapter.notifyDataSetChanged();
                }

                GroupListFragment groupFragment = (GroupListFragment) fragments[2];
                if (groupFragment != null && groupFragment.groupAdapter != null) {
                    	groupFragment.groupAdapter.notifyDataSetChanged();
                }
            }

        }
    };

    public int getUnreadMsgCountTotal() {
        int unreadMsgCountTotal = 0;
        for (EMUserBase user : allUsers.values()) {
            unreadMsgCountTotal += user.getUnreadMsgCount();
        }
        // also add unread count in groups
        for (EMUserBase group : Group.allGroups) {
            unreadMsgCountTotal += group.getUnreadMsgCount();
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

    /*
    private class MyConnectionListener implements ConnectionListener {
        @Override
        public void onConnected() {
            runOnUiThread(new Runnable() {
                public void run() {
                    ChatHistoryFragment fragment1 = (ChatHistoryFragment) fragments[0];
                    fragment1.errorItem.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, instance.getResources().getString(R.string.network_connected), 0)
                            .show();
                }
            });

            // process the notification message if there are any
            processMsgNotification();
            Log.d(TAG, "onconnected, try to retrive offline msg if any");
            try {
                //retrieve offline chat msg
                EMChat.retrieveOfflineMsg();
                //retrieve offline group msg
                EaseMobGroupManager.getInstance().retrieveOfflineMsg();
            } catch (Exception oe) {
                oe.printStackTrace();
            }

            // initialize the whole contact list only once
            if (!Gl.getInited()) {
                GetContactsCallbackImpl callback = new GetContactsCallbackImpl();
                callback.deleteNonExistingUsers = true;
                callback.setInitedAfterSuccess = true;
                EMUser.getContactsInBackground(callback);
            }
        }

        @Override
        public void onDisConnected(String errorString) {
            runOnUiThread(new Runnable() {
                public void run() {
                    ChatHistoryFragment fragment1 = (ChatHistoryFragment) fragments[0];
                    fragment1.errorItem.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onReConnected() {
            runOnUiThread(new Runnable() {
                public void run() {
                    ChatHistoryFragment fragment1 = (ChatHistoryFragment) fragments[0];
                    fragment1.errorItem.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onReConnecting() {
        }

        @Override
        public void onConnecting(String progress) {
        }

    }
    */

    /*
    private class MyContactListener implements ContactListener {
        @Override
        public void onContactUpdated(String[] userIdList) {
            GetContactsCallbackImpl callback = new GetContactsCallbackImpl();
            callback.deleteNonExistingUsers = true;
            EMUser.getContactsInBackground(userIdList, callback);
        }

        @Override
        public void onContactAdded(String[] userIdList) {
            GetContactsCallbackImpl callback = new GetContactsCallbackImpl();
            callback.deleteNonExistingUsers = true;
            EMUser.getContactsInBackground(userIdList, callback);
        }

        @Override
        public void onContactDeleted(String[] userIdList) {
            System.out.println("onContactDeleted");
            }
    }
    */

    private class GetContactsCallbackImpl implements GetContactsCallback {
        public boolean deleteNonExistingUsers = false;
        public boolean setInitedAfterSuccess = false;

        @Override
        public void onSuccess(final List<EMUserBase> contacts) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Iterator<EMUserBase> itor = contacts.iterator();
                    while (itor.hasNext()) {
                        EMUserBase user = itor.next();
                        if (user.getUsername().equals(EMUserManager.getInstance().getCurrentUserName())) {
                            itor.remove();
                        }
                    }
                    ChatUtil.updateUsers(MainActivity.this, contacts, deleteNonExistingUsers);

                    allUsers = ChatUtil.loadAllUsers(MainActivity.this);

                    
                    EMUserManager.getInstance().setAllUsers(allUsers);
                    System.err.println("app get contacts callback. users:" + allUsers.size());

                    // Refresh UI`
                    switch (currentTabIndex) {
                    case 0:
                        ((ChatHistoryFragment) fragments[0]).userRowAdapter.notifyDataSetChanged();
                        break;
                    case 1: // when add a user these code be invoke twice?
                    	ContactsListFragment tmp = ((ContactsListFragment) fragments[1]);
                    	contactList.clear();
                    	contactList.addAll(allUsers.values());
                    	//排序
                        Collections.sort(contactList, new Comparator<EMUserBase>() {
                            @Override
                            public int compare(EMUserBase lhs, EMUserBase rhs) {
                                return lhs.getHeader().compareTo(rhs.getHeader());

                            }
                        });
//                        tmp.contactAdapter = new ContactAdapter(getApplicationContext(), R.layout.row_contact,
//                                tmp.list, tmp.sidebar);
//                        tmp.contactListView.setAdapter(tmp.contactAdapter);
                        tmp.contactAdapter.notifyDataSetChanged();
                        break;
                    case 2:
                        // Johnson: we moved annoucements to second level page.
                        // do we still need the notify data changes
                        // ((TabFragment3)
                        // fragments[2]).adapter.notifyDataSetChanged();
                    default:
                        break;
                    }

                    if (setInitedAfterSuccess) {
                        Gl.setInited(true);
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
        Exception e = new Exception("!!!! check how to deal with notification in sdk2.0");
        e.printStackTrace();
        /*
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            com.easemob.user.Message notificationMsg;
            while ((notificationMsg = EMNotificationService.getInstance().getNextNotificationMsg()) != null) {
                Message message = MessageFactory.createMsgFromNotification(notificationMsg);
                EMUserBase tmpUser = MainActivity.allUsers.get(notificationMsg.getFrom());
                tmpUser.addMessage(message, true);
                updateUnreadLabel();
                notificationManager.cancel(notificationMsg.getNotificationID());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
    

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //隐藏软键盘
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

    
    private class MainLoginCallback implements LoginCallBack {
        @Override
        public void onSuccess(Object user) {
         // initialize the whole contact list only once
            if (!Gl.getInited()) {
                GetContactsCallbackImpl callback = new GetContactsCallbackImpl();
                callback.deleteNonExistingUsers = true;
                callback.setInitedAfterSuccess = true;
                Log.d(TAG, "logined, now start to get contacts in background");
                EMUserManager.getInstance().getContactsInBackground(callback);
            }
            
        }

        @Override
        public void onFailure(EaseMobException cause) {
        }

        @Override
        public void onProgress(String progress) {
        }
    }
    
    
    private class AppMessageListener implements MessageListener {

        @Override
        public void onMessageReceived(final EMMessage emMessage) {
            Log.d("chatdemo", "received msg:" + emMessage.toString());
            
            runOnUiThread(new Runnable() {
                public void run() {
            //@todo deal with group msg later
            EMUserBase tmpUser = null;
            String from = emMessage.getFrom();
            tmpUser = MainActivity.allUsers.get(from);
            if (tmpUser == null) {
                Log.e(TAG, "receive msg error, cant find user with name:" + from);
                return;
            }
            
            int rowId;
            Message message = MessageFactory.createMsgFromEMMsg(emMessage);
            
            //@@@@ todo. move db operation to chatsdk
            //Save to db
            rowId = tmpUser.addMessage(message, true);
            message.setRowId(rowId + "");
            message.setBackReceive(true);
            
            //Refresh UnreadLabel:
            updateUnreadLabel();
            
            //Refresh ChatHistoryFragment
            if (currentTabIndex == 0) {
                ChatHistoryFragment chatHistoryFragment = (ChatHistoryFragment) fragments[0];
                chatHistoryFragment.refresh();
            }
                }
            });//end of run ui thread

        }    
    }
}
