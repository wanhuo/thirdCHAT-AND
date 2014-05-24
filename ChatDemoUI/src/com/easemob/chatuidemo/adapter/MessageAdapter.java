package com.easemob.chatuidemo.adapter;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.LocationMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.AlertDialog;
import com.easemob.chatuidemo.activity.BaiduMapActivity;
import com.easemob.chatuidemo.activity.ChatActivity;
import com.easemob.chatuidemo.activity.ContextMenu;
import com.easemob.chatuidemo.activity.ShowBigImage;
import com.easemob.chatuidemo.task.LoadImageTask;
import com.easemob.chatuidemo.utils.ImageCache;
import com.easemob.chatuidemo.utils.SmileUtils;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.DateUtils;
import com.easemob.util.EMLog;
import com.easemob.util.LatLng;

public class MessageAdapter extends BaseAdapter {

	private final static String TAG = "msg";

	private static final int MESSAGE_TYPE_RECV_TXT = 0;
	private static final int MESSAGE_TYPE_SENT_TXT = 1;
	private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
	private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
	private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
	private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
	private static final int MESSAGE_TYPE_SENT_VOICE = 6;
	private static final int MESSAGE_TYPE_RECV_VOICE = 7;

	public static final String IMAGE_DIR = "chat/image/";
	public static final String VOICE_DIR = "chat/audio/";

	private String username;
	private LayoutInflater inflater;
	private Activity activity;
	private Bitmap headBp;
	private Bitmap otherHead;

	// reference to conversation object in chatsdk
	private EMConversation conversation;

	// if it is single chat, we will delete pic,audio files on service once
	// downloaded
	// for groupchat, leave for server side scripts to clean up old files
	private int chatType = ChatActivity.CHATTYPE_SINGLE;

	private Context context;

	public MessageAdapter(Context context, String username, int chatType) {
		this.username = username;
		this.chatType = chatType;
		this.context = context;
		inflater = LayoutInflater.from(context);
		activity = (Activity) context;
		this.conversation = EMChatManager.getInstance().getConversation(username);
	}

	// public void setUser(String user) {
	// this.user = user;
	// }

	public int getCount() {
		return conversation.getMsgCount();
	}

	public EMMessage getItem(int position) {
		return conversation.getMessage(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		EMMessage message = conversation.getMessage(position);
		if (message.getType() == EMMessage.Type.TXT) {
			return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
		}
		if (message.getType() == EMMessage.Type.IMAGE) {
			return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE : MESSAGE_TYPE_SENT_IMAGE;

		}
		if (message.getType() == EMMessage.Type.LOCATION) {
			return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION : MESSAGE_TYPE_SENT_LOCATION;
		}
		if (message.getType() == EMMessage.Type.VOICE) {
			return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE : MESSAGE_TYPE_SENT_VOICE;

		}

		return -1;// invalid
	}

	public int getViewTypeCount() {
		return 8;
	}

	private View createViewByMessage(EMMessage message, int position) {
		switch (message.getType()) {
		case LOCATION:
			return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_location, null) : inflater.inflate(
					R.layout.row_sent_location, null);
		case IMAGE:
			return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_picture, null) : inflater.inflate(
					R.layout.row_sent_picture, null);

		case VOICE:
			return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice, null) : inflater.inflate(
					R.layout.row_sent_voice, null);

		default:
			return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_message, null) : inflater.inflate(
					R.layout.row_sent_message, null);
		}
	}

	

	@SuppressLint("NewApi")
	public View getView(final int position, View convertView, ViewGroup parent) {
		final EMMessage message = getItem(position);
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = createViewByMessage(message, position);
			if (message.getType() == EMMessage.Type.IMAGE) {
				try {
					holder.iv = ((ImageView) convertView.findViewById(R.id.iv_sendPicture));
					holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
					holder.tv = (TextView) convertView.findViewById(R.id.percentage);
					holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
					holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
					holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
				} catch (Exception e) {
				}
			} else if (message.getType() == EMMessage.Type.TXT) {
				try {
					holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
					holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
					holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
					holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
					holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
				} catch (Exception e) {
				}
			} else if (message.getType() == EMMessage.Type.VOICE) {
				try {
					holder.iv = ((ImageView) convertView.findViewById(R.id.iv_voice));
					holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
					holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
					holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
					holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
					holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
					holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
				} catch (Exception e) {
				}
			} else if (message.getType() == EMMessage.Type.LOCATION) {
				try {
					holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
					holder.tv = (TextView) convertView.findViewById(R.id.tv_location);
					holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
					holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
					holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
				} catch (Exception e) {
				}
			}

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// 如果是发送的消息，显示已读textview
		if (message.direct == EMMessage.Direct.SEND) {
			holder.tv_ack = (TextView) convertView.findViewById(R.id.tv_ack);
			if (holder.tv_ack != null) {
				if (message.isAcked) {
					holder.tv_ack.setVisibility(View.VISIBLE);
				} else {
					holder.tv_ack.setVisibility(View.INVISIBLE);
				}
			}
		}else {
			//如果是文本或者地图消息，显示的时候给对方发送已读回执
			if ((message.getType() == Type.TXT || message.getType() == Type.LOCATION) && !message.isAcked) {
				try {
					// 发送已读回执
					message.isAcked = true;
					EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		switch (message.getType()) {
		// 根据消息type显示item
		case IMAGE:
			handleImageMessage(message, holder, position, convertView);
			break;
		case TXT:
			handleTextMessage(message, holder, position);
			break;
		case LOCATION:
			handleLocationMessage(message, holder, position, convertView);
			break;
		case VOICE:
			handleVoiceMessage(message, holder, position, convertView);
			break;
		default:
			// not supported
		}

		if (message.direct == EMMessage.Direct.SEND) {
			View statusView = convertView.findViewById(R.id.msg_status);

			statusView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					// 显示重发消息的自定义alertdialog
					Intent intent = new Intent(activity, AlertDialog.class);
					intent.putExtra("msg", activity.getString(R.string.confirm_resend));
					intent.putExtra("title", activity.getString(R.string.resend));
					intent.putExtra("cancel", true);
					intent.putExtra("position", position);
					if (message.getType() == EMMessage.Type.TXT)
						activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_TEXT);
					else if (message.getType() == EMMessage.Type.VOICE)
						activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VOICE);
					else if (message.getType() == EMMessage.Type.IMAGE)
						activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PICTURE);
					else if (message.getType() == EMMessage.Type.LOCATION)
						activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_LOCATION);
					else if (message.getType() == EMMessage.Type.FILE)
						activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_RESEND_NET_DISK);
					else if (message.getType() == EMMessage.Type.VIDEO)
						activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VIDEO);

				}
			});

		}

		TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);

		if (position == 0) {
			timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
			timestamp.setVisibility(View.VISIBLE);
		} else {
			if (DateUtils.isCloseEnough(message.getMsgTime(), conversation.getMessage(position - 1).getMsgTime())) {
				timestamp.setVisibility(View.GONE);
			} else {
				timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
				timestamp.setVisibility(View.VISIBLE);
			}
		}

		return convertView;
	}

	/**
	 * 图片消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 * @param convertView
	 */
	private void handleImageMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
		holder.pb.setTag(position);
		holder.iv.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				activity.startActivityForResult(
						(new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
								EMMessage.Type.IMAGE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
				return true;
			}
		});

		if (message.direct == EMMessage.Direct.RECEIVE) {
			System.err.println("it is receive msg");
			if (message.status == EMMessage.Status.INPROGRESS) {
				System.err.println("!!!! back receive");
				holder.iv.setImageResource(R.drawable.default_image);
				showDownloadImageProgress(message, holder);
				// downloadImage(message, holder);
			} else {
				System.err.println("!!!! not back receive, show image directly");
				holder.pb.setVisibility(View.GONE);
				holder.tv.setVisibility(View.GONE);
				holder.iv.setImageResource(R.drawable.default_image);
				ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
				if (imgBody.getLocalUrl() != null) {
					String filePath = imgBody.getLocalUrl();

					String thumbnailPath = getThumbnailImagePath(filePath);
					showImageView(thumbnailPath, holder.iv, filePath, imgBody.getRemoteUrl(), message);
				}
			}
			return;
		}

		// process send message
		System.err.println("!!!! it is send msg");
		// send pic, show the pic directly
		ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
		String filePath = imgBody.getLocalUrl();
		if (new File(filePath).exists())
			showImageView(filePath, holder.iv, filePath, null, message);
		else {
			showImageView(getThumbnailImagePath(filePath), holder.iv, filePath, IMAGE_DIR, message);
		}

		switch (message.status) {
		case SUCCESS:
			holder.pb.setVisibility(View.GONE);
			holder.tv.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		case FAIL:
			holder.pb.setVisibility(View.GONE);
			holder.tv.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.VISIBLE);
			break;
		case INPROGRESS:
			// set a timer
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							holder.pb.setVisibility(View.VISIBLE);
							holder.tv.setVisibility(View.VISIBLE);
							holder.tv.setText(message.progress + "%");
							if (message.status == EMMessage.Status.SUCCESS) {
								holder.pb.setVisibility(View.GONE);
								holder.tv.setVisibility(View.GONE);
								// message.setSendingStatus(Message.SENDING_STATUS_SUCCESS);
								timer.cancel();
							} else if (message.status == EMMessage.Status.FAIL) {
								holder.pb.setVisibility(View.GONE);
								holder.tv.setVisibility(View.GONE);
								// message.setSendingStatus(Message.SENDING_STATUS_FAIL);
								// message.setProgress(0);
								holder.staus_iv.setVisibility(View.VISIBLE);
								Toast.makeText(activity,
										activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), 0)
										.show();
								timer.cancel();
							}

						}
					});

				}
			}, 0, 500);
			break;
		default:
			sendPictureMessage(message, holder);
		}
	}

	/**
	 * 语音消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 * @param convertView
	 */
	private void handleVoiceMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
		VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
		holder.tv.setText(voiceBody.getLength() + "\"");
		holder.iv.setOnClickListener(new VoicePlayClickListener(message, holder.iv, holder.iv_read_status, activity, activity, username,
				chatType));
		holder.iv.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				activity.startActivityForResult(
						(new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
								EMMessage.Type.VOICE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
				return true;
			}
		});

		if (message.direct == EMMessage.Direct.RECEIVE) {
			if (message.isAcked) {
				//隐藏语音未读标志
				holder.iv_read_status.setVisibility(View.INVISIBLE);
			} else {
				holder.iv_read_status.setVisibility(View.VISIBLE);
			}
			return;
		}

		// until here, deal with send voice msg
		switch (message.status) {
		case SUCCESS:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		case FAIL:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.VISIBLE);
			break;
		case INPROGRESS:
			break;
		default:
			sendMsgInBackground(message, holder);
		}
	}

	/**
	 * 处理位置消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 * @param convertView
	 */
	private void handleLocationMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
		TextView locationView = ((TextView) convertView.findViewById(R.id.tv_location));
		LocationMessageBody locBody = (LocationMessageBody) message.getBody();
		locationView.setText(locBody.getAddress());
		LatLng loc = new LatLng(locBody.getLatitude(), locBody.getLongitude());
		locationView.setOnClickListener(new MapClickListener(loc, locBody.getAddress()));
		locationView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				activity.startActivityForResult(
						(new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type", EMMessage.Type.LOCATION.ordinal()),
						ChatActivity.REQUEST_CODE_CONTEXT_MENU);
				return false;
			}
		});

		if (message.direct == EMMessage.Direct.RECEIVE) {
			return;
		}
		// deal with send message
		switch (message.status) {
		case SUCCESS:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		case FAIL:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.VISIBLE);
			break;
		case INPROGRESS:
			break;
		default:
			sendMsgInBackground(message, holder);
		}
	}

	/**
	 * 文本消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 */
	private void handleTextMessage(EMMessage message, ViewHolder holder, final int position) {
		TextMessageBody txtBody = (TextMessageBody) message.getBody();
		Spannable span = SmileUtils.getSmiledText(context, txtBody.getMessage());
		holder.tv.setText(span, BufferType.SPANNABLE);
		holder.tv.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				activity.startActivityForResult(
						(new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
								EMMessage.Type.TXT.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
				return true;
			}
		});
		if (message.direct == EMMessage.Direct.SEND) {
			switch (message.status) {
			case SUCCESS:
				holder.pb.setVisibility(View.GONE);
				holder.staus_iv.setVisibility(View.GONE);
				break;
			case FAIL:
				holder.pb.setVisibility(View.GONE);
				holder.staus_iv.setVisibility(View.VISIBLE);
				break;
			case INPROGRESS:
				break;
			default:
				sendMsgInBackground(message, holder);
			}
		}
	}
	
	/**
	 * 发送消息
	 * @param message
	 * @param holder
	 */
	public void sendMsgInBackground(final EMMessage message, final ViewHolder holder) {
		holder.staus_iv.setVisibility(View.GONE);
		holder.pb.setVisibility(View.VISIBLE);
		EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

			@Override
			public void onSuccess() {
				updateSendedView(message, holder);
			}

			@Override
			public void onError(int code, String error) {
				updateSendedView(message, holder);
			}

			@Override
			public void onProgress(int progress, String status) {
			}

		});
	}

	/*
	 * chat sdk will automatic download thumbnail image for the image message we
	 * need to register callback show the download progress
	 */
	private void showDownloadImageProgress(final EMMessage message, final ViewHolder holder) {
		System.err.println("!!! show download image progress");
		final ImageMessageBody msgbody = (ImageMessageBody) message.getBody();
		msgbody.setDownloadCallback(new EMCallBack() {

			@Override
			public void onSuccess() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// message.setBackReceive(false);
						holder.pb.setVisibility(View.GONE);
						holder.tv.setVisibility(View.GONE);
						notifyDataSetChanged();
					}
				});
			}

			@Override
			public void onError(int code, String message) {

			}

			@Override
			public void onProgress(final int progress, String status) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						holder.tv.setText(progress + "%");

					}
				});
			}

		});
	}

	/*
	 * send message with new sdk
	 */
	private void sendPictureMessage(final EMMessage message, final ViewHolder holder) {

		try {
			String to = message.getTo();
			/*
			 * EMMessage msg = new EMMessage(EMMessage.Type.IMAGE);
			 * msg.setReceipt(to); File imageFile = new
			 * File(message.getFilePath()); ImageMessageBody body = new
			 * ImageMessageBody(imageFile); msg.addBody(body);
			 */

			// before send, update ui
			holder.staus_iv.setVisibility(View.GONE);
			holder.pb.setVisibility(View.VISIBLE);
			holder.tv.setVisibility(View.VISIBLE);
			holder.tv.setText("0%");

			// need to check, what this flag for?
			// message.setBgSendAndShowChated(false);
			// message.setBackSend(false);
			// message.setSendingStatus(Message.SENDING_STATUS_SENDING);
			// send out msg
			EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

				@Override
				public void onSuccess() {
					Log.d(TAG, "send image message successfully");
					activity.runOnUiThread(new Runnable() {
						public void run() {
							// send success
							holder.pb.setVisibility(View.GONE);
							holder.tv.setVisibility(View.GONE);
						}
					});
				}

				@Override
				public void onError(int code, String error) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							holder.pb.setVisibility(View.GONE);
							holder.tv.setVisibility(View.GONE);
							// message.setSendingStatus(Message.SENDING_STATUS_FAIL);
							holder.staus_iv.setVisibility(View.VISIBLE);
							Toast.makeText(activity,
									activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), 0).show();
						}
					});
				}

				@Override
				public void onProgress(final int progress, String status) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							holder.tv.setText(progress + "%");
						}
					});
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新ui上消息发送状态
	 * @param message
	 * @param holder
	 */
	private void updateSendedView(final EMMessage message, final ViewHolder holder) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// message.setBackSend(false);
				// send success
				if (message.status == EMMessage.Status.SUCCESS) {
					holder.pb.setVisibility(View.GONE);
					holder.staus_iv.setVisibility(View.GONE);
				} else if (message.status == EMMessage.Status.FAIL) {
					holder.pb.setVisibility(View.GONE);
					holder.staus_iv.setVisibility(View.VISIBLE);
					Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), 0)
							.show();
				}
			}
		});
	}

	/**
	 * load image into image view
	 * 
	 * @param thumbernailPath
	 * @param iv
	 * @param position
	 * @return the image exists or not
	 */
	private boolean showImageView(final String thumbernailPath, final ImageView iv, final String localFullSizePath, String remoteDir,
			final EMMessage message) {
		String imagename = localFullSizePath.substring(localFullSizePath.lastIndexOf("/") + 1, localFullSizePath.length());
		// final String remote = remoteDir != null ? remoteDir+imagename :
		// imagename;
		final String remote = remoteDir;

		// first check if the thumbnail image already loaded into cache
		Bitmap bitmap = ImageCache.getInstance().get(thumbernailPath);
		if (bitmap != null) {
			// thumbnail image is already loaded, reuse the drawable
			iv.setImageBitmap(bitmap);
			iv.setClickable(true);
			iv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					System.err.println("image view on click");
					Intent intent = new Intent(activity, ShowBigImage.class);
					File file = new File(localFullSizePath);
					if (file.exists()) {
						Uri uri = Uri.fromFile(file);
						intent.putExtra("uri", uri);
						System.err.println("here need to check why download everytime");
					} else {
						// The local full size pic does not exist yet.
						// ShowBigImage needs to download it from the server
						// first
						intent.putExtra("remotepath", remote);
					}
					if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked) {
						message.isAcked = true;
						try {
							EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					activity.startActivity(intent);
				}
			});
			return true;
		} else {
			// TODO: For whatever reasons if the thumbernailPath does not exist
			// (it should...), shall we display a default image instead?
			if (!new File(thumbernailPath).exists()) {
				return false;
			}

			new LoadImageTask().execute(thumbernailPath, localFullSizePath, remote, chatType, iv, activity, message);
			return true;
		}

	}

	public static class ViewHolder {
		ImageView iv;
		TextView tv;
		ProgressBar pb;
		ImageView staus_iv;
		ImageView head_iv;
		TextView tv_userId;
		ImageView playBtn;
		TextView timeLength;
		TextView size;
		LinearLayout container_status_btn;
		ImageView iv_read_status;
		TextView tv_ack;
	}
	
	/*
	 * 点击地图消息listener
	 */
	class MapClickListener implements View.OnClickListener {

		LatLng location;
		String address;

		public MapClickListener(LatLng loc, String address) {
			location = loc;
			this.address = address;

		}

		@Override
		public void onClick(View v) {
			 Intent intent;
			 intent = new Intent(context, BaiduMapActivity.class);
			 intent.putExtra("latitude", location.latitude);
			 intent.putExtra("longitude", location.longitude);
			 intent.putExtra("address", address);
			 activity.startActivity(intent);
		}

	}

	public String getThumbnailImagePath(String imagePath) {
		String path = imagePath.substring(0, imagePath.lastIndexOf("/") + 1);
		path += "th" + imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
		EMLog.d("msg", "original image path:" + imagePath);
		EMLog.d("msg", "thum image path:" + path);
		return path;
	}

}