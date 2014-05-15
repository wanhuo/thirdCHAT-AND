package com.easemob.chatuidemo.domain;

public class InviteMessage {
	private String from;
	//时间
	private long time;
	//添加理由
	private String reason;
	
	//未验证，已同意等状态
	private InviteMesageStatus status;
	
	//是不是我发的好友申请
	private boolean isInviteFromMe;

	private int id;
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}


	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public InviteMesageStatus getStatus() {
		return status;
	}

	public void setStatus(InviteMesageStatus status) {
		this.status = status;
	}

	public boolean isInviteFromMe() {
		return isInviteFromMe;
	}

	public void setInviteFromMe(boolean isInviteFromMe) {
		this.isInviteFromMe = isInviteFromMe;
	}

	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}



	public enum InviteMesageStatus{
		/**已添加*/
		AGREED,
		/**未验证*/
		NO_VALIDATION,
		/**已忽略*/
		IGNORED
	}
	
}



