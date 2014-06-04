package com.easemob.chatuidemo.adapter;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContact;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.domain.User;
import com.easemob.chatuidemo.utils.SmileUtils;
import com.easemob.util.DateUtils;
import com.easemob.util.EasyUtils;

/**
 * 聊天记录adpater
 * 
 */
public class ChatHistoryAdapter extends ArrayAdapter<EMContact> {

	private LayoutInflater inflater;

	public ChatHistoryAdapter(Context context, int textViewResourceId, List<EMContact> objects) {
		super(context, textViewResourceId, objects);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.row_chat_history, parent, false);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		if (holder == null) {
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.unreadLabel = (TextView) convertView.findViewById(R.id.unread_msg_number);
			holder.message = (TextView) convertView.findViewById(R.id.message);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			holder.msgState = convertView.findViewById(R.id.msg_state);
			holder.list_item_layout=(RelativeLayout) convertView.findViewById(R.id.list_item_layout);
			convertView.setTag(holder);
		}
		if(position%2==0)
		{
			holder.list_item_layout.setBackgroundResource(R.drawable.mm_listitem);
		}else{
			holder.list_item_layout.setBackgroundResource(R.drawable.mm_listitem_grey);
		}
		
		
		EMContact user = getItem(position);
		String username = user.getUsername();
		// 获取与此用户/群组的会话
		EMConversation conversation = EMChatManager.getInstance().getConversation(username);
		holder.name.setText(user.getNick() != null ? user.getNick() : username);
		if (conversation.getUnreadMsgCount() > 0) {
			// 显示与此用户的消息未读数
			holder.unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
			holder.unreadLabel.setVisibility(View.VISIBLE);
		} else {
			holder.unreadLabel.setVisibility(View.INVISIBLE);
		}

		if (conversation.getMsgCount() != 0) {
			// 把最后一条消息的内容作为item的message内容
			EMMessage lastMessage = conversation.getLastMessage();
			holder.message.setText(SmileUtils.getSmiledText(getContext(), getMessageDigest(lastMessage, (this.getContext()))),
					BufferType.SPANNABLE);

			holder.time.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
			if (lastMessage.direct == EMMessage.Direct.SEND && lastMessage.status == EMMessage.Status.FAIL) {
				holder.msgState.setVisibility(View.VISIBLE);
			} else {
				holder.msgState.setVisibility(View.GONE);
			}
		}

		return convertView;
	}

	/**
	 * 根据消息内容和消息类型获取消息内容提示
	 * 
	 * @param message
	 * @param context
	 * @return
	 */
	private String getMessageDigest(EMMessage message, Context context) {
		String digest = "";
		switch (message.getType()) {
		case LOCATION: // 位置消息
			if (message.direct == EMMessage.Direct.RECEIVE) {
				digest = EasyUtils.getAppResourceString(context, "location_recv");
				digest = String.format(digest, message.getFrom());
				return digest;
			} else {
				digest = EasyUtils.getAppResourceString(context, "location_prefix");
			}
			break;
		case IMAGE: // 图片消息
			ImageMessageBody imageBody = (ImageMessageBody) message.getBody();
			digest = EasyUtils.getAppResourceString(context, "picture") + imageBody.getFileName();
			break;
		case VOICE:// 语音消息
			digest = EasyUtils.getAppResourceString(context, "voice");
			break;
		case VIDEO: // 视频消息
			digest = EasyUtils.getAppResourceString(context, "video");
			break;
		case TXT: // 文本消息
			TextMessageBody txtBody = (TextMessageBody) message.getBody();
			digest = txtBody.getMessage();
			break;
		default:
			System.err.println("error, unknow type");
			return "";
		}

		return digest;
	}

	private static class ViewHolder {
		/** 和谁的聊天记录 */
		TextView name;
		/** 消息未读数 */
		TextView unreadLabel;
		/** 最后一条消息的内容 */
		TextView message;
		/** 最后一条消息的时间 */
		TextView time;
		/** 用户头像 */
		ImageView avatar;
		/** 最后一条消息的发送状态 */
		View msgState;
		/**整个list中每一行总布局*/
		RelativeLayout list_item_layout;
		
		
	}
}
