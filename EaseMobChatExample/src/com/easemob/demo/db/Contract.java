package com.easemob.demo.db;

import android.provider.BaseColumns;

public class Contract {

	public static abstract class UserTable implements BaseColumns {
		public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_JID = "jid";
        public static final String COLUMN_NAME_GROUP_ID = "group_id";
        public static final String COLUMN_NAME_NICK = "nick";       
        public static final String COLUMN_NAME_HEADER = "header";
		public static final String COLUMN_NAME_SEX = "sex";
		public static final String COLUMN_NAME_NOTE = "note";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_ORGAINIZATION = "organization";
        public static final String COLUMN_NAME_MOBILE = "mobile";		
        public static final String COLUMN_NAME_AVATOR_PATH = "avator_path";		
        public static final String COLUMN_NAME_WORKPHONE = "workphone"; 
        public static final String COLUMN_NAME_SIGNATURE = "signature"; 
        public static final String COLUMN_NAME_ADDRESS = "address"; 
        public static final String COLUMN_NAME_FAVORITE = "favorite";
        public static final String COLUMN_NAME_REMOTEAVATARPATH = "remoteavatarpath"; 

	}
		

	public static abstract class LinkTable implements BaseColumns {
		public static final String TABLE_NAME = "link";
		public static final String COLUMN_NAME_ROOM_ID = "roomID";
		public static final String COLUMN_NAME_REMOTE_ID = "remoteID";
	}

	public static abstract class RemoteKeyMapTable implements BaseColumns {
		public static final String TABLE_NAME = "remoteKeyMap";
		public static final String COLUMN_NAME_REMOTE_ID = "remoteID";
		public static final String COLUMN_NAME_KEY = "key";
		public static final String COLUMN_NAME_VALUE = "value";
	}

	private Contract() {

	}

}
