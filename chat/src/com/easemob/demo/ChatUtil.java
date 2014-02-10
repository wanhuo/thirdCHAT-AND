package com.easemob.demo;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.easemob.EaseMob;
import com.easemob.chat.UserUtil;
import com.easemob.chat.db.EaseMobMsgDB;
import com.easemob.chat.domain.EMUserBase;
import com.easemob.chat.domain.Message;
import com.easemob.cloud.CloudOperationCallback;
import com.easemob.cloud.HttpFileManager;
import com.easemob.core.EaseMobConfig;
import com.easemob.demo.db.Contract;
import com.easemob.demo.db.DBOpenHelper;
import com.easemob.demo.domain.DemoUser;
import com.easemob.ui.activity.ChatActivity;
import com.easemob.util.HanziToPinyin;


public class ChatUtil {
    private static final String TAG = ChatUtil.class.getSimpleName();  
    
    /**
    * Load all users, this also load users' chat history. This does not include the current user (myself).
    * @param ctx Contexts
    * @return Map<String, User>
    */
    public static Map<String, EMUserBase> loadAllUsers(Context ctx) {
        Map<String, EMUserBase> allUsers = new HashMap<String, EMUserBase>();
        
        SQLiteDatabase db = DBOpenHelper.getInstance(ctx).getWritableDatabase();
        Cursor cursor = db.query(Contract.UserTable.TABLE_NAME,
                new String[] {
                        Contract.UserTable.COLUMN_NAME_ID,
                        Contract.UserTable.COLUMN_NAME_JID,
                        Contract.UserTable.COLUMN_NAME_NICK,
                        Contract.UserTable.COLUMN_NAME_HEADER,
                        Contract.UserTable.COLUMN_NAME_SEX,
                        Contract.UserTable.COLUMN_NAME_NOTE,
                        Contract.UserTable.COLUMN_NAME_EMAIL,
                        Contract.UserTable.COLUMN_NAME_ORGAINIZATION,
                        Contract.UserTable.COLUMN_NAME_MOBILE,
                        Contract.UserTable.COLUMN_NAME_WORKPHONE,
                        Contract.UserTable.COLUMN_NAME_ADDRESS,
                        Contract.UserTable.COLUMN_NAME_SIGNATURE,
                        Contract.UserTable.COLUMN_NAME_FAVORITE,
                        Contract.UserTable.COLUMN_NAME_REMOTEAVATARPATH}, null, null,
                null, null, Contract.UserTable.COLUMN_NAME_ID + " COLLATE LOCALIZED ASC");
        String myselfId = EaseMobConfig.getCurrentUserName();
        if (cursor.moveToFirst()) {
            do {
                //Do not include "myself" 
                if(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_ID)).equals(myselfId)) {
                    continue;
                }
                DemoUser user = new DemoUser();
                user.setUsername(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_ID)));
                user.setJid(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_JID)));
                user.setNick(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_NICK)));
                user.setHeader(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_HEADER)));
                user.setSex(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_SEX)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_EMAIL)));
                user.setMobile(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_MOBILE)));
                user.setWorkPhone(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_WORKPHONE)));
                user.setAddress(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_ADDRESS)));
                user.setSignature(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_SIGNATURE)));
                user.setPicture(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_REMOTEAVATARPATH)));
                
                
                //Load chat history
                if(EaseMobMsgDB.isTableExists(db, user.getUsername())){
                	List<Message> chatHistory= EaseMobMsgDB.findAllMessages(ctx, user.getUsername());
//                	List<Message> chatHistory= EaseMobMsgDB.findSpecifiedMessages(ctx, user.getUsername(), "0", ChatActivity.PAGE_SIZE);
                	user.setMessages(chatHistory);
                }          
                
                allUsers.put(user.getUsername(), user); 
            } while (cursor.moveToNext());
        }

        cursor.close();

        return allUsers;
    }
    
    public static DemoUser loadUser(Context ctx, String userId) {
    	DemoUser user = null;
        
        SQLiteDatabase db = DBOpenHelper.getInstance(ctx).getWritableDatabase();
        Cursor cursor = db.query(Contract.UserTable.TABLE_NAME,
                new String[] {
                        Contract.UserTable.COLUMN_NAME_ID,
                        Contract.UserTable.COLUMN_NAME_JID,
                        Contract.UserTable.COLUMN_NAME_NICK,
                        Contract.UserTable.COLUMN_NAME_HEADER,
                        Contract.UserTable.COLUMN_NAME_SEX,
                        Contract.UserTable.COLUMN_NAME_NOTE,
                        Contract.UserTable.COLUMN_NAME_EMAIL,
                        Contract.UserTable.COLUMN_NAME_ORGAINIZATION,
                        Contract.UserTable.COLUMN_NAME_MOBILE,
                        Contract.UserTable.COLUMN_NAME_WORKPHONE,
                        Contract.UserTable.COLUMN_NAME_ADDRESS,
                        Contract.UserTable.COLUMN_NAME_SIGNATURE,
                        Contract.UserTable.COLUMN_NAME_FAVORITE,
                        Contract.UserTable.COLUMN_NAME_REMOTEAVATARPATH}, 
                        Contract.UserTable.COLUMN_NAME_ID + "=?", 
                        new String[] { String.valueOf(userId) },
                        null, null, null);
        
        if (cursor != null) {
            if(cursor.moveToFirst()) {
        
                user = new DemoUser();
                user.setUsername(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_ID)));
                user.setJid(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_JID)));
                user.setNick(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_NICK)));
                user.setHeader(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_HEADER)));
                user.setSex(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_SEX)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_EMAIL)));
                user.setMobile(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_MOBILE)));
                user.setWorkPhone(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_WORKPHONE)));
                user.setAddress(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_ADDRESS)));
                user.setSignature(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_SIGNATURE)));
                user.setPicture(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_REMOTEAVATARPATH)));
            //Load chat history
/*            List<Message> chatHistory = loadChatHistory(user.getId());            
            user.setMessages(chatHistory);  */     
            }
        
            cursor.close();
        }
        
        return user;
    }       
    
    /**
    * Search users 
    * @param allUsers
    * @param query the query string. Can be part of user name (user id or user nick), location, phone number etc
    * @return List<User>
    */
    public static List<DemoUser> searchUsers(List<DemoUser> allUsers, String query) {
        //TODO: we only search against user name at the moment
        List<DemoUser> resultList = new ArrayList<DemoUser>();
        for(DemoUser user : allUsers) {
            if(user.getUsername().contains(query) || (user.getNick() != null && user.getNick().contains(query))) {
                resultList.add(user);
            }
        }
        
        return resultList;
    }
    
    /**
    * Search users' chat history with provided keywords
    * @param allUsers
    * @param query the query string. Can be part of chat history
    * @return List<DemoUser>
    */
    public static List<DemoUser> searchChatHistory(List<DemoUser> allUsers, String query) {
        //TODO: we only search against chat history stored in cache at the moment. We may need to search from message history files if necessary
        List<DemoUser> resultList = new ArrayList<DemoUser>();
        for(DemoUser user : allUsers) {
            List<Message> history = user.getMessages();
            for(Message m : history) {
                if(m.getBody()!=null && m.getBody().contains(query)) {
                    resultList.add(user);
                    break;
                }
            }
        }
        return resultList;        
    }
    
    /**
    * Update the user list using a contact list retrieved from remote server. 
    * @param Context
    * @param List<EMUserBase> remoteContactList
    * @param removeNonExistingUser Remove the local user from db if the user is not listed in remoteContactList, which means the user has been deleted on the remote server.
    */
    public static void updateUsers(Context ctx, List<EMUserBase> remoteContactList, boolean removeNonExistingUser) {
        updateUsers(ctx, remoteContactList);
        
        if (removeNonExistingUser) {
            SQLiteDatabase db = DBOpenHelper.getInstance(ctx).getWritableDatabase();
            Map<String, EMUserBase> allLocalUsers = loadAllUsers(ctx);
            String myselfId = EaseMobConfig.getCurrentUserName();
            for (String userId : allLocalUsers.keySet()) {
                boolean found = false;
                for (EMUserBase contact : remoteContactList) {
                    if (userId.equals(contact.getUsername())) {
                        found = true;
                    }
                }

                // This local user has been removed on remote (except myself)
                if (!found && !userId.equals(myselfId)) {
                    db.delete(Contract.UserTable.TABLE_NAME, Contract.UserTable.COLUMN_NAME_ID + " = ?",
                            new String[] { String.valueOf(userId) });
                }
            }
        }

        //db.close();       
    }
    
    //update or add user (if the user does not exist in db yet)
    private static void updateUsers(Context ctx, List<EMUserBase> remoteContactList) {
        SQLiteDatabase db = DBOpenHelper.getInstance(ctx).getWritableDatabase();
        
        final HttpFileManager hfm = new HttpFileManager();
        
        Map<String, EMUserBase> allLocalUsers = loadAllUsers(ctx);
        for(EMUserBase remoteContact : remoteContactList) {
            //userName is the primary key            
        	final String username = remoteContact.getUsername();   
        	EMUserBase localUser = allLocalUsers.get(username);
            
            if(localUser == null) {
            	DemoUser myUser = remoteContact.toType(DemoUser.class);
                addDB(myUser, db);
                
                final String picture = remoteContact.getPicture();
                if(picture == null || picture.startsWith("http://www.gravatar.com")){
                    //No need to download avatar for this user
                    continue;
                }
                final String localFilePath = UserUtil.getThumbAvatorPath(username).getAbsolutePath();
                if (new File(localFilePath).exists()) {
                    //already have this avator locally
                    continue;
                }
                new Thread(new Runnable() {                    
                    @Override
                    public void run() {
                        hfm.downloadThumbnailFile(picture, localFilePath, EaseMob.APPKEY, null, 60, true, new CloudOperationCallback() {

                            @Override
                            public void onProgress(int progress) {
                            }

                            @Override
                            public void onError(String msg) {
                                Log.e(TAG, "downloadThumbnailFile failed: " + msg);
                            }

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "downloadThumbnailFile succeed");
                                if (ChatActivity.getAvatorCache().get("th"+username) != null) {
                                    ChatActivity.getAvatorCache().remove("th"+username);
                                }
                            }
                        });
                    }
                }).start();
            } else {
                //Sync the existing local user with the remote user if necessary
            	DemoUser myUser = remoteContact.toType(DemoUser.class);
                updateDB(myUser, db);
                final String localFilePath = UserUtil.getThumbAvatorPath(username).getAbsolutePath();
                
                final String picture = remoteContact.getPicture();
                if(picture == null || picture.equals(localUser.getPicture()) && new File(localFilePath).exists()){
                    //No need to download avatar for this user
                    continue;
                }
                new Thread(new Runnable() {
                    
                    @Override
                    public void run() {
                        hfm.downloadThumbnailFile(picture, localFilePath, EaseMob.APPKEY, null, new CloudOperationCallback() {
                            @Override
                            public void onProgress(int progress) {
                            }
                            
                            @Override
                            public void onError(String msg) {
                                Log.e(TAG, "downloadThumbnailFile failed: " + msg);
                            }

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "downloadThumbnailFile succeed");
                                if (ChatActivity.getAvatorCache().get(username) != null) {
                                    ChatActivity.getAvatorCache().remove(username);
                                }
                            }
                        });
                        
                    }
                }).start();
            }
        } 
    }   
        
    /**
    * Update user, persist the change to DB. This is for updating currently logged in user, i.e., myself. 
    * @param Context
    * @param user 
    */
    public static void updateUser(Context ctx, DemoUser user) {
        SQLiteDatabase db = DBOpenHelper.getInstance(ctx).getWritableDatabase();
     
        updateDB(user, db); 
    }
    
    private static void updateDB(DemoUser contact, SQLiteDatabase db) {
        boolean updateDb = false;
        ContentValues values = new ContentValues();
        if(contact.getJid() != null) {
            values.put(Contract.UserTable.COLUMN_NAME_JID, contact.getJid());
            updateDb =true;
        }
        if(contact.getNick() != null) {
            values.put(Contract.UserTable.COLUMN_NAME_NICK, contact.getNick());
            updateDb =true;
        }
        if(contact.getSex() != null) {
            values.put(Contract.UserTable.COLUMN_NAME_SEX, contact.getSex());
            updateDb =true;
        }
        if(contact.getEmail() != null) {
            values.put(Contract.UserTable.COLUMN_NAME_EMAIL, contact.getEmail());
            updateDb =true;
        }
        if(contact.getAddress() != null) {
            values.put(Contract.UserTable.COLUMN_NAME_ADDRESS, contact.getAddress());
            updateDb =true;
        }
        if(contact.getMobile() != null) {
            values.put(Contract.UserTable.COLUMN_NAME_MOBILE, contact.getMobile());
            updateDb =true;
        }
        if(contact.getWorkPhone() != null) {
            values.put(Contract.UserTable.COLUMN_NAME_WORKPHONE, contact.getWorkPhone());
            updateDb =true;
        }
        if(contact.getSignature() != null) {
            values.put(Contract.UserTable.COLUMN_NAME_SIGNATURE, contact.getSignature());
            updateDb =true;
        }
        if(contact.getPicture()!= null){
        	values.put(Contract.UserTable.COLUMN_NAME_REMOTEAVATARPATH, contact.getPicture());
        	updateDb = true;
        }
              
        if(updateDb) {
            db.update(Contract.UserTable.TABLE_NAME, values, Contract.UserTable.COLUMN_NAME_ID + " = ?",
                new String[] { String.valueOf(contact.getUsername())});
        }        
    }
    
    /**
    * Add a user to DB
    * @param Context
    * @param userId 
    */
    public static void addUser(Context ctx, DemoUser contact) {
        SQLiteDatabase db = DBOpenHelper.getInstance(ctx).getWritableDatabase();
        
        //Refactor: avoid unnecessary db access
        Map<String, EMUserBase> allLocalUsers = loadAllUsers(ctx);
        EMUserBase localUser = allLocalUsers.get(contact.getUsername());
        
        if(localUser !=null) {
        	return;
        	//REVISIT: shall we update the local user in this case?
        }
        
        addDB(contact, db);
    }
    
    private static void addDB(DemoUser contact, SQLiteDatabase db) {
        
/*        if (contact.getUsername() == User.getCurrentUserId()) {
            Log.e(TAG, "contact has same id with current user:" + User.getCurrentUserId());
            return;
        }*/
        ContentValues values = new ContentValues();
        values.put(Contract.UserTable.COLUMN_NAME_ID, contact.getUsername());
        values.put(Contract.UserTable.COLUMN_NAME_JID, contact.getJid());
        values.put(Contract.UserTable.COLUMN_NAME_NICK, contact.getNick());
        if(contact.getNick() == null){
        	values.put(Contract.UserTable.COLUMN_NAME_HEADER, " ");
        }else{
        	String nick = contact.getNick();
        	if(nick == null || nick.isEmpty()){
        		values.put(Contract.UserTable.COLUMN_NAME_HEADER, "Z");
        	} else {
        	    String header = HanziToPinyin.getInstance().get(contact.getNick().substring(0, 1)).get(0).target.substring(0, 1).toUpperCase();
        	    char h = header.charAt(0);
        	    if(h >= 65)
        	        values.put(Contract.UserTable.COLUMN_NAME_HEADER, HanziToPinyin.getInstance().get(contact.getNick().substring(0, 1)).get(0).target.substring(0, 1).toUpperCase());
        	    else
        	        values.put(Contract.UserTable.COLUMN_NAME_HEADER, "Z");
        	}
        }
        values.put(Contract.UserTable.COLUMN_NAME_SEX, contact.getSex());
        values.put(Contract.UserTable.COLUMN_NAME_EMAIL, contact.getEmail());
        values.put(Contract.UserTable.COLUMN_NAME_MOBILE, contact.getMobile());
        values.put(Contract.UserTable.COLUMN_NAME_WORKPHONE, contact.getWorkPhone());
        values.put(Contract.UserTable.COLUMN_NAME_SIGNATURE, contact.getSignature());
        values.put(Contract.UserTable.COLUMN_NAME_ADDRESS, contact.getAddress());
        values.put(Contract.UserTable.COLUMN_NAME_REMOTEAVATARPATH, contact.getPicture());
        
        //values.put(Contract.UserTable.COLUMN_NAME_NOTE, "");  
        
        try {
            db.insert(Contract.UserTable.TABLE_NAME, null, values);
        } catch (Exception e) {
            //catch errors. "column id is not unique" error. 
            //if logic is correct, we should only add same user to db once, so this error shouldn't happen
            e.printStackTrace();
        }
    }

    /**
    * Delete a user from DB
    * @param Context
    * @param userId 
    */
    public static void deleteUser(Context ctx, String userId) {
        SQLiteDatabase db = DBOpenHelper.getInstance(ctx).getWritableDatabase();
        
        db.delete(Contract.UserTable.TABLE_NAME, Contract.UserTable.COLUMN_NAME_ID + " = ?",
                new String[] { String.valueOf(userId) });
    }  
}
