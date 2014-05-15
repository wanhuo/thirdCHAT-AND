package com.easemob.chatuidemo.domain;


public class User {
	private String username;
	private String nick;
	private int unreadMsgCount;
	
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public int getUnreadMsgCount() {
		return unreadMsgCount;
	}
	public void setUnreadMsgCount(int unreadMsgCount) {
		this.unreadMsgCount = unreadMsgCount;
	}
	
	@Override
	public int hashCode() {
		return 17*getUsername().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		 if(o  == null || !(o instanceof User)){
	            return false;
	        }
		return getUsername().equals(((User)o).getUsername());
	}
	
}
