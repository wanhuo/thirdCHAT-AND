package com.easemob.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.user.EMUser;
import com.easemob.exceptions.EMNetworkUnconnectedException;
import com.easemob.exceptions.EaseMobException;
import com.easemob.ui.activity.AddGroup;
import com.easemob.ui.activity.AlertDialog;
import com.easemob.ui.activity.ChatActivity;
import com.easemob.ui.activity.ChatHistoryFragment;
import com.easemob.ui.activity.ContactsListFragment;
import com.easemob.ui.activity.ContactsListFragment.ContactsListFragmentListener;
import com.easemob.ui.activity.GroupListFragment.GroupListFragmentListener;
import com.easemob.ui.activity.GroupListFragment;
import com.easemob.user.EMUserManager;
import com.easemob.user.callbacks.GetContactsCallback;
import com.easemob.user.callbacks.LoginCallBack;
import com.easemob.user.domain.Group;

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


    public boolean wasPaused = false;

	private List<EMUser> contactList;

	private MyContactsListFragment contactFragment;
	
	private MyGroupListFragment groupFragment;

	private ChatHistoryFragment chatHistoryFragment;
	
	private NewMessageBroadcastReceiver msgReceiver; 
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        unreadLabel = (TextView) findViewById(R.id.unread_msg_number);

        instance = this;

        mTabs = new Button[3];
        mTabs[0] = (Button) findViewById(R.id.btn_conversations);
        mTabs[1] = (Button) findViewById(R.id.btn_contacts);
        mTabs[2] = (Button) findViewById(R.id.btn_settings);
        selectedTabs = new Drawable[] { getResources().getDrawable(R.drawable.tab_weixin_pressed),
                getResources().getDrawable(R.drawable.tab_find_frd_pressed),
                getResources().getDrawable(R.drawable.tab_address_pressed),
                getResources().getDrawable(R.drawable.tab_settings_pressed) };
        unSelectedTabs = new Drawable[] { getResources().getDrawable(R.drawable.tab_weixin_normal),
                getResources().getDrawable(R.drawable.tab_find_frd_normal),
                getResources().getDrawable(R.drawable.tab_address_normal),
                getResources().getDrawable(R.drawable.tab_settings_normal) };
        currentTabIndex = 0;
      

        String userName = ChatDemoApp.getInstance().getUserName();
        String password = ChatDemoApp.getInstance().getPassword();
        boolean loggedin = getIntent().getBooleanExtra("loggedin", false);
        msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        intentFilter.setPriority(3);
        registerReceiver(msgReceiver, intentFilter);
        // if already login from LoginActivity, skip the login call below
        if (!loggedin) {
            EMUserManager.getInstance().login(userName, password, 
                    new MainLoginCallback());
        } {
            //here, logined from login activity, try to get contacts if not inited;
            //@@@@ temp workaround. need to move contact db, sync to user sdk!!!
            if (!ChatDemoApp.getInstance().getInited()) {
                GetContactsCallbackImpl callback = new GetContactsCallbackImpl();
                callback.deleteNonExistingUsers = true;
                callback.setInitedAfterSuccess = true;
                Log.d(TAG, "logined, now start to get contacts in background");
                EMUserManager.getInstance().getContactsInBackground(callback);
            }
        }

        contactList = new ArrayList<EMUser>(EMUserManager.getInstance().getAllUsers().values());
        //排序
        Collections.sort(contactList, new Comparator<EMUser>() {
            @Override
            public int compare(EMUser lhs, EMUser rhs) {
                return (lhs.getHeader().compareTo(rhs.getHeader()));
            }
        });
        
        contactFragment = new MyContactsListFragment(contactList,
                new MyContactListListener());
        chatHistoryFragment = new ChatHistoryFragment();
        groupFragment = new MyGroupListFragment(Group.allGroups, new MyGroupListListener());
        fragments = new Fragment[] {chatHistoryFragment, 
        		contactFragment,
        		groupFragment,
        		new SettingFragment()};
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragments[0]).commit();
        
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
		}
    	
    }
    
    /**
     * grouplist监听事件
     * @author Administrator
     *
     */
    class MyGroupListListener implements GroupListFragmentListener{

        @Override
        public void onListItemClickListener(int position) {
            //点击新建群组按钮item
           if (position == groupFragment.groupAdapter.getCount() - 1) {
               if (EMChatManager.getInstance().isConnected()) {
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
        
    @SuppressLint("ValidFragment")
	class MyContactsListFragment extends ContactsListFragment {
		public MyContactsListFragment(List<EMUser> contactList, ContactsListFragmentListener listener) {
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
        updateUnreadLabel();

        if (wasPaused) {
            // 从后台返回后，检查网络状态。在错误信息提示栏显示“无法连接服务器信息”如果没有网络连接。
            wasPaused = false;
            if (!EMChatManager.getInstance().isConnected()) {
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
        if (msgReceiver != null) {
            try {
                unregisterReceiver(msgReceiver);
            } catch (Exception e) {
                e.printStackTrace();
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
            System.err.println("!!!! index is:" + index);
            System.err.println(" !!!! fragments size:" + fragments.length);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();

            if (index == 0) {
                ((ChatHistoryFragment) fragments[0]).userRowAdapter.notifyDataSetChanged();
            }
        }

        mTabs[currentTabIndex].setCompoundDrawablesWithIntrinsicBounds(null, unSelectedTabs[currentTabIndex], null,
                null);
        mTabs[index].setCompoundDrawablesWithIntrinsicBounds(null, selectedTabs[index], null, null);
        currentTabIndex = index;
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
                finish();
            } else if(requestCode == REQUEST_CODE_CONTACT){
            	contactList.clear();
				contactList.addAll(EMUserManager.getInstance().getAllUsers().values());
            	//排序
                Collections.sort(contactList, new Comparator<EMUser>() {
                    @Override
                    public int compare(EMUser lhs, EMUser rhs) {
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



    public int getUnreadMsgCountTotal() {
        int unreadMsgCountTotal = 0;
        unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
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

    
    private class GetContactsCallbackImpl implements GetContactsCallback {
        public boolean deleteNonExistingUsers = false;
        public boolean setInitedAfterSuccess = false;

        @Override
        public void onSuccess(final List<EMUser> contacts) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Iterator<EMUser> itor = contacts.iterator();
                    while (itor.hasNext()) {
                        EMUser user = itor.next();
                        if (user.getUsername().equals(EMUserManager.getInstance().getCurrentUserName())) {
                            itor.remove();
                        }
                    }
                    EMUserManager.getInstance().updateLocalUsers(contacts);
                    System.err.println("app get contacts callback. users:" + EMUserManager.getInstance().getAllUsers().size());

                    // Refresh UI`
                    switch (currentTabIndex) {
                    case 0:
                        ((ChatHistoryFragment) fragments[0]).userRowAdapter.notifyDataSetChanged();
                        break;
                    case 1: // when add a user these code be invoke twice?
                    	ContactsListFragment tmp = ((ContactsListFragment) fragments[1]);
                    	contactList.clear();
                    	contactList.addAll(EMUserManager.getInstance().getAllUsers().values());
                    	//排序
                        Collections.sort(contactList, new Comparator<EMUser>() {
                            @Override
                            public int compare(EMUser lhs, EMUser rhs) {
                                return lhs.getHeader().compareTo(rhs.getHeader());

                            }
                        });
                        tmp.contactAdapter.notifyDataSetChanged();
                        break;
                    case 2:
                    default:
                        break;
                    }

                    if (setInitedAfterSuccess) {
                        ChatDemoApp.getInstance().setInited(true);
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
            if (!ChatDemoApp.getInstance().getInited()) {
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
        
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msgId = intent.getStringExtra("msgid");
            String msgFrom = intent.getStringExtra("from");
            int msgType = intent.getIntExtra("type", 0);
            String msgBody = intent.getStringExtra("body");
            Log.d("main", "new message id:" + msgId + " from:" + msgFrom + " type:" + msgType + " body:" + msgBody);
            updateUnreadLabel();        
            }
    }
            
}
