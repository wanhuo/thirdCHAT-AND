package com.easemob.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import com.easemob.EaseMob;
import com.easemob.EaseMobService;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMUser;
import com.easemob.chat.callbacks.ConnectionListener;
import com.easemob.chat.callbacks.ContactListener;
import com.easemob.chat.callbacks.GetContactsCallback;
import com.easemob.chat.db.EaseMobMsgDB;
import com.easemob.chat.domain.EMUserBase;
import com.easemob.chat.domain.Group;
import com.easemob.chat.domain.Message;
import com.easemob.chat.domain.MessageFactory;
import com.easemob.exceptions.EMNetworkUnconnectedException;
import com.easemob.exceptions.EaseMobException;
import com.easemob.ui.activity.AddGroup;
import com.easemob.ui.activity.AlertDialog;
import com.easemob.ui.activity.ChatActivity;
import com.easemob.ui.activity.ChatHistoryFragment;
import com.easemob.ui.activity.ContactListFragment;
import com.easemob.ui.activity.GroupDetails;
import com.easemob.ui.adapter.ContactAdapter;
import com.easemob.ui.adapter.ContactPagerAdapter;
import com.easemob.ui.adapter.GroupAdapter;
import com.easemob.ui.adapter.RowAdapter;
import com.easemob.ui.widget.Sidebar;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_LOGOUT = 1;
    private static final int REQUEST_CODE_EXIT = 2;
    private static final int REQUEST_CODE_GROUPDETAIL = 10;

    public static MainActivity instance = null;

    public static int unreadPushMsgNum;
    private Button[] mTabs;
    private Drawable[] selectedTabs;
    private Drawable[] unSelectedTabs;
    private int currentTabIndex;

    private Fragment[] fragments;
    private TextView unreadLabel;
    InputMethodManager inputManager = null;

    public static boolean isChat = false;

    // private NewMessageBroadcastReceiver receiver;

    public static List<Message> pushMessages;

    public static Map<String, EMUserBase> allUsers;

    MyConnectionListener remoteConnectionListener = new MyConnectionListener();
    MyContactListener remoteContactListener = new MyContactListener();

    public boolean wasPaused = false;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        unreadLabel = (TextView) findViewById(R.id.unread_msg_number);

        instance = this;

        mTabs = new Button[3];
        mTabs[0] = (Button) findViewById(R.id.btn_conversations);
        mTabs[1] = (Button) findViewById(R.id.btn_contacts);
        mTabs[2] = (Button) findViewById(R.id.btn_settings);
        selectedTabs = new Drawable[] { getResources().getDrawable(R.drawable.tab_weixin_pressed),
                getResources().getDrawable(R.drawable.tab_find_frd_pressed),
                getResources().getDrawable(R.drawable.tab_settings_pressed) };
        unSelectedTabs = new Drawable[] { getResources().getDrawable(R.drawable.tab_weixin_normal),
                getResources().getDrawable(R.drawable.tab_find_frd_normal),
                getResources().getDrawable(R.drawable.tab_settings_normal) };
        currentTabIndex = 0;
        ChatHistoryFragment historyFragment = new ChatHistoryFragment();
        fragments = new Fragment[] { historyFragment, new TabFragment2(), new SettingFragment()};

        /************************************ EaseMob SDK Start ******************************************/
        /***** Use EaseMob SDK. Step 1: EaseMob.init() and EaseMob.login.login() *************************/
        String userName = Gl.getUserName();
        String password = Gl.getPassword();
        boolean loggedin = getIntent().getBooleanExtra("loggedin", false);
        // if already login from LoginActivity, skip the login in call below
        if (!loggedin) {
            EaseMob.init(this.getApplicationContext());
            EaseMob.login(userName, password);
        }
        /****** Use EaseMob SDK. Step 2: Register receivers to receive chat message and push message *****/
        // Register receiver on EaseMobService for receiving chat message
        IntentFilter chatIntentFilter = new IntentFilter(EaseMobService.BROADCAST_CHAT_ACTION);
        chatIntentFilter.setPriority(3);
        registerReceiver(chatBroadcastReceiver, chatIntentFilter);
        isChatBroadcastReceiverRegistered = true;


        // register receiver for receive group invited broadcast
        IntentFilter groupIntentFilter = new IntentFilter(EaseMobService.BROADCAST_GROUP_INVITED_ACTION);
        registerReceiver(groupInvitedReceiver, groupIntentFilter);

        // register receiver for receive group deleted broadcast
        IntentFilter groupDelIntentFilter = new IntentFilter(EaseMobService.BROADCAST_GROUP_DELETED_ACTION);
        registerReceiver(groupDeletedReceiver, groupDelIntentFilter);

        /****** Use EaseMob SDK. Step 3: Register listeners to receive contact and connection event *******/
        // Register receiver for contact changed event.
        EaseMob.addContactListener(remoteContactListener);
        // Register receiver for connection status event.
        EaseMob.addConnectionListener(remoteConnectionListener);

        /********************************** EaseMob SDK End ******************************************/

        // Load all available users from local DB. This also load users' chat history
        allUsers = ChatUtil.loadAllUsers(this);
        EMUser.setAllUsers(allUsers);
        // Load push messages from db
        pushMessages = EaseMobMsgDB.findAllMessages(this, "push_" + EaseMob.getCurrentUserName());
        // sort push messages
        Collections.sort(pushMessages, new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                long rt = rhs.getTime();
                long lt = lhs.getTime();
                if (rt == lt) {
                    return 0;
                } else if (rt > lt) {
                    return -1;
                } else {
                    return 1;
                }
                // return rhs.getTime().compareTo(lhs.getTime());
            }
        });

        Group.allGroups = EaseMobMsgDB.loadGroups(this, new ArrayList<EMUserBase>(allUsers.values()));
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragments[0]).commit();
        //until here, notify SDK that the UI is inited
        EaseMob.applicationInited = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        processMsgNotification();
        updateUnreadLabel();

        if (wasPaused) {
            // if resumed from background, need to check network connect,
            // if not connected, show to disconnect error msg
            wasPaused = false;
            if (!EaseMobService.isConnected()) {
                ChatHistoryFragment fragment1 = (ChatHistoryFragment) fragments[0];
                fragment1.errorItem.setVisibility(View.VISIBLE);
            }
        }

        if (isChat) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[0].isAdded()) {
                trx.add(R.id.fragment_container, fragments[0]);
            }
            trx.show(fragments[0]).commit();

            mTabs[currentTabIndex].setCompoundDrawablesWithIntrinsicBounds(null, unSelectedTabs[currentTabIndex], null,
                    null);
            mTabs[0].setCompoundDrawablesWithIntrinsicBounds(null, selectedTabs[0], null, null);
            currentTabIndex = 0;
            isChat = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasPaused = true;
    }

    @Override
    public void onDestroy() {
        EaseMob.applicationInited = false;
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
        if (groupInvitedReceiver != null) {
            try {
                unregisterReceiver(groupInvitedReceiver);
            } catch (Exception e) {
            }
        }
        if (groupDeletedReceiver != null) {
            try {
                unregisterReceiver(groupDeletedReceiver);
            } catch (Exception e) {
            }
        }

        super.onDestroy();
    }

    public static class TabFragment2 extends ContactListFragment {
        private GroupAdapter groupAdapter;
        private ListView groupListView;
        private ListView listView3;
        private RelativeLayout titleLayout2;
        private TextView titleText2;
        private ImageView titleLine2;
        // private InputMethodManager manager;
        private boolean isDeptInited = false;
        // private Sidebar sidebar2;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            return inflater.inflate(R.layout.main_tab_contacts, container, false);
        }

        public ListView getGroupListView() {
            return groupListView;
        }

        public GroupAdapter getGroupAdapter() {
            return groupAdapter;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                groupAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
        	super.onActivityCreated(savedInstanceState);
            manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            list = new ArrayList<EMUserBase>();
            
            List<EMUserBase> list = new ArrayList<EMUserBase>(allUsers.values());
            Collections.sort(list, new Comparator<EMUserBase>() {
                @Override
                public int compare(EMUserBase lhs, EMUserBase rhs) {
                    return lhs.getHeader().compareTo(rhs.getHeader());

                }
            });
            layoutInflater = LayoutInflater.from(getActivity());
            titleLayout1 = (RelativeLayout) getView().findViewById(R.id.title_layout1);
            titleLayout2 = (RelativeLayout) getView().findViewById(R.id.title_layout3);
            titleText1 = (TextView) getView().findViewById(R.id.title1);
            titleText2 = (TextView) getView().findViewById(R.id.title3);
            titleLine1 = (ImageView) getView().findViewById(R.id.iv_line1);
            titleLine2 = (ImageView) getView().findViewById(R.id.iv_line3);
            contactListViews = new ArrayList<View>();

            contactListViews.add(layoutInflater.inflate(R.layout.contacts_list2, null));
            contactListViews.add(layoutInflater.inflate(R.layout.contact_list, null));
            sidebar2 = (Sidebar) contactListViews.get(0).findViewById(R.id.sidebar);
            contactAdapter = new ContactAdapter(getActivity(), R.layout.row_contact, list, sidebar2);
            contactListView = (ListView) contactListViews.get(0).findViewById(R.id.list);
            groupListView = (ListView) contactListViews.get(1).findViewById(R.id.list);
            contactListView.setOnTouchListener(new ContactTouchListener());
            groupListView.setOnTouchListener(new ContactTouchListener());
            // listView3 = (ListView)
            // contactListViews.get(3).findViewById(R.id.list);
            contactListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    getActivity().startActivity(
                            new Intent(getActivity(), ChatActivity.class).putExtra("userId",
                                    contactAdapter.getItem(position).getUsername()));
                }
            });
            // registerForContextMenu(contactListView);
            // registerForContextMenu(groupListView);

            vPager = (ViewPager) getView().findViewById(R.id.vPager);
            vPager.setAdapter(new ContactPagerAdapter(contactListViews, getActivity(), contactAdapter));
            vPager.setOffscreenPageLimit(4);
            // vPager.getAdapter().notifyDataSetChanged();
            vPager.setOnPageChangeListener(new OnPageChangeListener() {

                @Override
                public void onPageSelected(int arg0) {
                    switch (arg0) {
                    case 0:
                        if (currentPagerIndex == 1) {
                            setToNormalColor(titleText2);
                            titleLine2.setVisibility(View.INVISIBLE);
                        } 
                        setToSelectedColor(titleText1);
                        titleLine1.setVisibility(View.VISIBLE);

                        break;
                    case 1:
                        if (currentPagerIndex == 0) {
                            setToNormalColor(titleText1);
                            titleLine1.setVisibility(View.INVISIBLE);
                        } 
                        setToSelectedColor(titleText2);
                        titleLine2.setVisibility(View.VISIBLE);
                        if (groupAdapter == null) {
                            // add fake data
                            groupAdapter = new GroupAdapter(getActivity(), 1, Group.allGroups);
                        }
                        groupListView.setAdapter(groupAdapter);
                        groupListView.setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (position == groupAdapter.getCount() - 1) {
                                    if (EaseMobService.isConnected()) {
                                        startActivityForResult(new Intent(getActivity(), AddGroup.class), 0);
                                    } else {
                                        startActivity(new Intent(MainActivity.instance, AlertDialog.class).putExtra(
                                                "msg", MainActivity.instance.getString(R.string.network_unavailable)));
                                    }
                                } else {
                                    startActivity(new Intent(getActivity(), GroupDetails.class).putExtra(
                                            "position", position - 1));
                                }

                            }
                        });

                        groupListView.invalidate();
                       
                        break;

                    }

                    currentPagerIndex = arg0;

                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {
                    if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                        if (getActivity().getCurrentFocus() != null)
                            manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    }

                }

                @Override
                public void onPageScrollStateChanged(int arg0) {
                }
            });
            vPager.setCurrentItem(0);

            titleLayout1.setOnClickListener(new ContactTitleClickListener(0));
            titleLayout2.setOnClickListener(new ContactTitleClickListener(1));

        }

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
        case R.id.btn_settings:
            index = 2;
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
            // 因为设成的是singtask模式，所以在切换登陆的时候必须确保main finish时才切换到loginactivity
            // 所以mainactivity finish的操作得放在logout里
            /*
             * if (requestCode == REQUEST_CODE_LOGOUT) { //finish the
             * MainActivity finish(); }else
             */if (requestCode == REQUEST_CODE_EXIT) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                Editor editor = sp.edit();
                editor.putBoolean("isShowSplash", true);
                editor.commit();
                EaseMobService.autoRestart = false;
                stopService(new Intent(this, EaseMobService.class));
                finish();
            } else {
                updateUnreadLabel();
            }
        }
    }

    private boolean isChatBroadcastReceiverRegistered = false;
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
                    // the group doesn't exist
                    // create group object here
                    Log.d(TAG, "create group obj:" + groupId);
                    group = new Group();
                    String groupName = groupId;
                    try {
                        groupName = EMChat.getChatRoomSubject(groupId);
                        Log.d(TAG, "use chatroom subject as group name:" + groupName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    group.setName(groupName);
                    group.setGroupId(groupId);
                    Group.allGroups.add(group);
                    Log.d(TAG, "create group obj, add to allgroups:" + group.getGroupId());
                    EaseMobMsgDB.saveGroup(context, group);
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
            tmpUser.setTableName(tmpUser.getUsername());
            
            //Save to db
            rowId = tmpUser.addMessage(message, true);
            message.setRowId(rowId + "");
            message.setBackReceive(true);
            
            //Refresh UI:
            updateUnreadLabel();

            abortBroadcast();
        }
    };

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
                group.setTableName(group.getName().startsWith("_") ? group.getName() : "_" + group.getName());
                group.addMessage(msg, true);
                updateUnreadLabel();
                ChatHistoryFragment tmp = (ChatHistoryFragment) fragments[0];
                if (tmp != null) {
                    tmp.rowAdapter.notifyDataSetChanged();
                }

                TabFragment2 contactGroup = (TabFragment2) fragments[1];
                if (contactGroup != null) {
                    ListView groupListView = contactGroup.getGroupListView();
                    if (groupListView != null) {
                        if (contactGroup.getGroupAdapter() != null) {
                            contactGroup.getGroupAdapter().notifyDataSetChanged();
                        }
                    }
                }
            }

        }
    };

    private BroadcastReceiver groupDeletedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // refresh chat history and group list view when group has been
            // deleted
            String groupId = intent.getStringExtra("groupId");
            if (groupId != null) {
                updateUnreadLabel();
                ChatHistoryFragment tmp = (ChatHistoryFragment) fragments[0];
                if (tmp != null) {
                    tmp.rowAdapter.notifyDataSetChanged();
                }

                TabFragment2 contactGroup = (TabFragment2) fragments[1];
                if (contactGroup != null) {
                    ListView groupListView = contactGroup.getGroupListView();
                    if (groupListView != null) {
                        contactGroup.getGroupAdapter().notifyDataSetChanged();
                    }
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
            EaseMobService.getInstance().getChatManager().retrieveOfflineMsg();

            // initialize the whole contact list only once
            if (!Gl.getInited()) {
                GetContactsCallbackImpl callback = new GetContactsCallbackImpl();
                callback.deleteNonExistingUsers = true;
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
                    // Toast.makeText(MainActivity.this,
                    // instance.getResources().getString(R.string.network_unavailable),
                    // 0).show();
                }
            });
        }

        @Override
        public void onReConnected() {
            runOnUiThread(new Runnable() {
                public void run() {
                    ChatHistoryFragment fragment1 = (ChatHistoryFragment) fragments[0];
                    fragment1.errorItem.setVisibility(View.GONE);
                    // Toast.makeText(MainActivity.this, "网络重新连接成功", 0).show();
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
            /*
             * for (String userId : userIdList) {
             * ChatUtil.deleteUser(MainActivity.this, userId); }
             * 
             * // TODO: Is this thread safe? // TODO: no need to update whole
             * list. refactor later. do we lose // unread_message_count in this
             * case? allUsers = ChatUtil.loadAllUsers(MainActivity.this); //
             * TODO: Refresh UI
             */}
    }

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
                        if (user.getUsername().equals(EaseMob.getCurrentUserName())) {
                            itor.remove();
                        }
                    }
                    ChatUtil.updateUsers(MainActivity.this, contacts, deleteNonExistingUsers);

                    allUsers = ChatUtil.loadAllUsers(MainActivity.this);

                    EMUser.setAllUsers(allUsers);

                    // Refresh UI`
                    switch (currentTabIndex) {
                    case 0:
                        ((ChatHistoryFragment) fragments[0]).rowAdapter.notifyDataSetChanged();
                        break;
                    case 1: // when add a user these code be invoke twice?
                        TabFragment2 tmp = ((TabFragment2) fragments[1]);

                        List<EMUserBase> list = new ArrayList<EMUserBase>(allUsers.values());
                        Collections.sort(list, new Comparator<EMUserBase>() {
                            @Override
                            public int compare(EMUserBase lhs, EMUserBase rhs) {
                                return lhs.getHeader().compareTo(rhs.getHeader());

                            }
                        });
                        tmp.list = list;
                        if (tmp.vPager.getCurrentItem() != 0 && deleteNonExistingUsers)
                            tmp.vPager.setCurrentItem(0);
                        tmp.contactAdapter = new ContactAdapter(getApplicationContext(), R.layout.row_contact,
                                tmp.list, tmp.sidebar);
                        tmp.contactListView.setAdapter(tmp.contactAdapter);
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
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }


}
