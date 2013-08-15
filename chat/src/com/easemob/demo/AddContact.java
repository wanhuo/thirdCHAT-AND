package com.easemob.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.EaseMob;
import com.easemob.chat.EMUser;
import com.easemob.chat.callbacks.AddContactCallback;
import com.easemob.chat.callbacks.GetContactCallback;
import com.easemob.chat.domain.EMUserBase;
import com.easemob.demo.domain.DemoUser;
import com.easemob.exceptions.EMNetworkUnconnectedException;
import com.easemob.exceptions.EaseMobException;
import com.easemob.ui.activity.AlertDialog;
import com.easemob.util.ImageUtils;

public class AddContact extends Activity {
    private static final String TAG = AddContact.class.getSimpleName();

	private EditText editText;
	private ProgressDialog progressDialog = null;
	private AddContact instance;
	private LinearLayout searchedUser;
	private Button saveBtn;
	private TextView textVName;
	private DemoUser contact;
	private String userName;
	private InputMethodManager inputMethodManager;
	private ImageView avatar;
	private TextView prompt;
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
			inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			switch (msg.what) {
			case 1: //found user successfully
				if(msg.obj == null){
					startActivity(new Intent(AddContact.this,AlertDialog.class).putExtra("msg", getString(R.string.addcontact_find_user_failed)));
				} else {
					final DemoUser user = (DemoUser) msg.obj;
					
					searchedUser.setVisibility(View.VISIBLE);
					searchedUser.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                          //click into the user details acticity
                        }
                    });
					textVName.setText(user.getNick());
					//set avator,use EaseMob.APPKEY to download image
    				ImageUtils.setThumbAvatorBitmap(user.getUsername(), user.getPicture(), avatar, AddContact.this);
    					
				}
				break;
			case 0:
				startActivity(new Intent(AddContact.this,AlertDialog.class).putExtra("msg", getString(R.string.addcontact_find_user_failed)));
				break;
			case 3:
				startActivity(new Intent(AddContact.this,AlertDialog.class).putExtra("msg", getString(R.string.addcontact_add_user_failed)));
				
				break;
			case 4: //added user successfully
				progressDialog.dismiss();
				setResult(RESULT_OK);
				finish();
				break;
			default:
				break;
			}
		}
		
	};
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);
		editText = (EditText) findViewById(R.id.edit_note);
		searchedUser = (LinearLayout) findViewById(R.id.ll_user);
		textVName = (TextView) findViewById(R.id.name);
		saveBtn = (Button) findViewById(R.id.save);
		avatar = (ImageView) findViewById(R.id.avatar);
		prompt = (TextView) findViewById(R.id.tv_prompt);
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
		try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(getPackageName(),  PackageManager.GET_META_DATA);
            Bundle data = info.metaData;
            boolean b = data.getBoolean(EaseMob.CONFIG_EASEMOB_ACCEPT_INVITATION_ALWAYS);
            if(b){
                prompt.setText("当前为自动添加好友模式，如果对方在线，添加会自动成为好友并且添加到你的好友列表里");
            }
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//		inputMethodManager.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS );
//		searchedUser.setOnClickListener(new UserOnclickListener());
		instance = this;
	}
	
	/**
	 * search contact
	 * @param v
	 */
	public void save(View v) {
		String name = editText.getText().toString();
		String saveText = saveBtn.getText().toString();
		if (getString(R.string.button_search).equals(saveText)) {
			userName = name;
			if (TextUtils.isEmpty(name)) {
				startActivity(new Intent(this, AlertDialog.class).putExtra("msg", getString(R.string.addcontact_input_contact_id)));
				return;
			}

			progressDialog = new ProgressDialog(this);
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage(getString(R.string.searching_user));
			progressDialog.show();
			
			// check if user exists
			EMUser.getContactInBackground(userName, new GetContactCallback() {
				@Override
				public void onSuccess(EMUserBase contact) {
					progressDialog.dismiss();
					if (contact == null) {
						startActivity(new Intent(instance, AlertDialog.class).putExtra("msg",getString(R.string.addcontact_user_not_exist)));
						return;
					}
					
					DemoUser user = contact.toType(DemoUser.class);
					final Message msg = Message.obtain();
					msg.what = 1;
					msg.obj = user;
					handler.sendMessage(msg);
				}

				@Override
				public void onFailure(EaseMobException cause) {
					Log.e("contact", "error when adding new contact:" + cause.toString());
						                
	                if(cause instanceof EMNetworkUnconnectedException) {
	                    startActivity(new Intent(AddContact.this, AlertDialog.class).putExtra("msg", getString(R.string.network_unavailable)));
                        progressDialog.dismiss();
                        return;
                    }
                    
                    startActivity(new Intent(instance, AlertDialog.class).putExtra("msg", getString(R.string.addcontact_user_not_exist)));
                    progressDialog.dismiss();
				}
			});
		} else {

		}
	}	
	
	/**
	 * add contact
	 * @param v
	 */
	public void add(View v){
		if(progressDialog == null) {
			progressDialog = new ProgressDialog(this);
		}
		progressDialog.setMessage(getString(R.string.adding_contact));
		progressDialog.show();
		if (MainActivity.allUsers.containsKey(userName)) {
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", getString(R.string.addcontact_user_exist_in_contacts)));
			progressDialog.dismiss();
			return;
		}

        EMUser.addContactInBackground(userName, "", new AddContactCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("contact", "add contact success:" + contact);
                        // ChatUtil.updateOrAddUser(instance, contact);
                        progressDialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(EaseMobException cause) {
                Log.e("contact", "error when adding new contact:" + cause.toString());
                
                if(cause instanceof EMNetworkUnconnectedException) {
                    startActivity(new Intent(AddContact.this, AlertDialog.class).putExtra("msg", getString(R.string.network_unavailable)));
                    progressDialog.dismiss();
                    return;
                }
                
                 progressDialog.dismiss();
            }
        });
		
	}

	public void cancel(View v) {
		finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}
}
