package com.easemob.demo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.easemob.chat.db.ContractGroup;
import com.easemob.chat.db.EaseMobMsgDB;
import com.easemob.demo.Gl;
import com.easemob.chat.db.MsgDBOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

	public static DBOpenHelper instance;
            
	private static final String USER_TABLE_CREATE = "CREATE TABLE "
			+ Contract.UserTable.TABLE_NAME + " (" 
	        + Contract.UserTable.COLUMN_NAME_ID	+ " TEXT PRIMARY KEY, " 
            + Contract.UserTable.COLUMN_NAME_NICK   + " TEXT, " 
            + Contract.UserTable.COLUMN_NAME_JID   + " TEXT, " 
            + Contract.UserTable.COLUMN_NAME_GROUP_ID + " TEXT, "
            + Contract.UserTable.COLUMN_NAME_HEADER + " TEXT, "
			+ Contract.UserTable.COLUMN_NAME_SEX + " TEXT, "
            + Contract.UserTable.COLUMN_NAME_NOTE + " TEXT, "
            + Contract.UserTable.COLUMN_NAME_EMAIL + " TEXT, "
            + Contract.UserTable.COLUMN_NAME_ORGAINIZATION + " TEXT, "
			+ Contract.UserTable.COLUMN_NAME_MOBILE + " TEXT, "
		    + Contract.UserTable.COLUMN_NAME_AVATOR_PATH + " TEXT, "
            + Contract.UserTable.COLUMN_NAME_WORKPHONE + " TEXT, "
            + Contract.UserTable.COLUMN_NAME_SIGNATURE + " TEXT, "
            + Contract.UserTable.COLUMN_NAME_ADDRESS + " TEXT, "
            + Contract.UserTable.COLUMN_NAME_FAVORITE + " INT, "
            + Contract.UserTable.COLUMN_NAME_REMOTEAVATARPATH + " TEXT); ";

	
	private static final int DATABASE_VERSION = 9;

	private DBOpenHelper(Context context) {
		super(context, EaseMobMsgDB.getDatabaseName(), null, DATABASE_VERSION);
	}

	public static DBOpenHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DBOpenHelper(context.getApplicationContext());
		}
		return instance;
	}
	
	public static void closeDB() {
	    if (instance != null) {
	        try {
	            SQLiteDatabase db = instance.getWritableDatabase();
	            db.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        instance = null;
	    }
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(USER_TABLE_CREATE);
		db.execSQL(MsgDBOpenHelper.USER_TABLE_GROUPS_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    db.execSQL("DROP TABLE IF EXISTS " + Contract.UserTable.TABLE_NAME);
	    db.execSQL("DROP TABLE IF EXISTS " + ContractGroup.GroupsTable.TABLE_NAME);
	    onCreate(db);

        Gl.setInited(false);
	}

}
