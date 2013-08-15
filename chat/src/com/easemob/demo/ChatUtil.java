package com.easemob.demo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.usergrid.java.client.entities.Entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.easemob.EaseMob;
import com.easemob.chat.UserUtil;
import com.easemob.chat.db.EaseMobMsgDB;
import com.easemob.chat.domain.EMUserBase;
import com.easemob.chat.domain.Group;
import com.easemob.chat.domain.Message;
import com.easemob.chat.domain.RESTDepartment;
import com.easemob.cloud.CloudOperationCallback;
import com.easemob.cloud.HttpFileManager;
import com.easemob.demo.db.Contract;
import com.easemob.demo.db.DBOpenHelper;
import com.easemob.demo.domain.DemoUser;
import com.easemob.ui.activity.ChatActivity;
import com.easemob.util.HanziToPinyin;


public class ChatUtil {
    private static final String TAG = ChatUtil.class.getSimpleName();  
    

    
    //load all users
    
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
        String myselfId = EaseMob.getCurrentUserName();
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
                //user.setFavorite(cursor.getInt(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_FAVORITE)) > 0);
                user.setPicture(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_REMOTEAVATARPATH)));
                
                
                //Load chat history
                if(EaseMobMsgDB.isTableExists(db, user.getUsername())){
                	List<Message> chatHistory= EaseMobMsgDB.findAllMessages(ctx, user.getUsername());
//                	List<Message> chatHistory= EaseMobMsgDB.findSpecifiedMessages(ctx, user.getUsername(), "0", ChatActivity.PAGE_SIZE);
                	user.setMessages(chatHistory);
                }
//                List<Message> chatHistory = loadMessageHistory(user.getId(), false);            
                
                allUsers.put(user.getUsername(), user); 
            } while (cursor.moveToNext());
        }

        cursor.close();
        
        //NOTE: do not close db otherwise we get a "database not open" exception: http://stackoverflow.com/questions/6554436/keep-getting-database-not-open-error
        //db.close();       
        
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
                //user.setFavorite(cursor.getInt(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_FAVORITE)) > 0);
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
    * Load all users whose favorite attribute is true
    * @param allUsers
    * @return List<User>
    */
    public static List<DemoUser> loadUsersWithFavorite(List<DemoUser> allUsers) {
        List<DemoUser> resultList = new ArrayList<DemoUser>();
        for(DemoUser user : allUsers) {
/*            if(user.isFavorite()) {
                resultList.add(user);
            }*/
        }
        
        return resultList;
    }
    
    /**
    * Search users based on department path
    * @param allUsers
    * @param path: department path. example: /department1/department2/department3
    * @return List<User> 
    */
    
    public static String getDepartmentName(String path){
    	return path.substring(path.lastIndexOf("/") + 1, path.length());
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
    * Search users' conversation history with provided keywords
    * @param allUsers
    * @param query the query string. Can be part of conversation history
    * @return List<User>
    */
    public static List<DemoUser> searchConversation(List<DemoUser> allUsers, String query) {
        //TODO: we only search against conversation history stored in cache at the moment. We may need to search from message history files if necessary
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
     * persist a message.
     * @param message message to log
     * @param isPushMessage if the message is push message, we save the message history to a file named PushMeg.db
     */
    /*
    public static void saveMessageHistory(Message message, boolean isPushMessage) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }

        File filepath;
        if (isPushMessage) {
            filepath = UserUtil.getPushMessagePath(message.getTo());
        } else if (message.isIncoming()) {
            filepath = UserUtil.getMessagePath(message.getFrom());
        } else {
            filepath = UserUtil.getMessagePath(message.getTo());
        }

        ObjectOutputStream output = null;
        try {
            filepath.getParentFile().mkdirs();
            // write to the last record
            // @Johnson: this need revisi. we can hold a member variable on
            // objectoutstream, instead of creating it everytime

            if (filepath.exists() & filepath.length() > 0) {
                OutputStream file = new FileOutputStream(filepath, true);
                output = new AppendObjectOutputStream(file);
            } else {
                OutputStream file = new FileOutputStream(filepath, true);
                output = new ObjectOutputStream(file);
            }
            output.writeObject(message);
            output.flush();
            output.reset();
        } catch (IOException e) {
            Log.e(TAG, "Error writing chat history", e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                }
            }
        }
    }
    */

    //TODO: only load most recent 50 messages (or 20?. we need a paged operation)
    /*
    public static List<Message> loadMessageHistory(String userName,
            boolean isPushMessage) {
        List<Message> chatHistory = new LinkedList<Message>();

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return chatHistory;
        }

        ObjectInput input = null;
        try {
            File historyFile;
            if (isPushMessage) {
                historyFile = UserUtil.getPushMessagePath(userName);
            } else {
                historyFile = UserUtil.getMessagePath(userName);
            }

            if (historyFile.exists()) {
                FileInputStream fis = new FileInputStream(historyFile);
                input = new ObjectInputStream(fis);
                Message msg = null;
                while ((msg = (Message) input.readObject()) != null) {
                	msg.setProgress(100);
                	msg.setAttachDownloaded(true);
                    chatHistory.add(msg);
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "loadChatHistory failed: ", e);
        } catch (EOFException e) {
            // do nothing to skip the error
        } catch (IOException e) {
            Log.e(TAG, "loadChatHistory failed: ", e);
        } catch (Exception e) {
            Log.e(TAG, "loadChatHisotry failed:", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                }
            }
        }

        return chatHistory;
    }
    */
    /*
    public static void deleteMessageHistory(String userName, List<Message> messagesToDelete, boolean isPushMessage) {
        System.out.println("delete msg historys");
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }

        File historyFile;
        if (isPushMessage) {
            historyFile = UserUtil.getPushMessagePath(userName);
        } else {
            historyFile = UserUtil.getMessagePath(userName);
        }

        if (!historyFile.exists()) {
            System.out.println("doesn't exists!");
            return;
        }

        File outputTempFile = UserUtil.getTempPath(historyFile);

        ObjectInput input = null;
        ObjectOutputStream output = null;

        try {
            FileInputStream fis = new FileInputStream(historyFile);
            input = new ObjectInputStream(fis);

            OutputStream os = new FileOutputStream(outputTempFile, false);
            output = new ObjectOutputStream(os);

            Message msg = null;
            while ((msg = (Message) input.readObject()) != null) {
                for (Message messageToDelete : messagesToDelete) {
                    if (msg.equals(messageToDelete)) {
                        // Delete this message
                        continue;
                    }
                }

                output.writeObject(msg);
            }

            output.flush();
            output.reset();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "loadChatHistory failed: ", e);
        } catch (EOFException e) {
            // do nothing to skip the error
        } catch (IOException e) {
            Log.e(TAG, "loadChatHistory failed: ", e);
        } catch (Exception e) {
            Log.e(TAG, "loadChatHisotry failed:", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                }
            }
        }

        // Now delete the old file and rename the temp file
        historyFile.delete();
        outputTempFile.renameTo(historyFile);
        System.err.println("delete history finished");
    }
    */
    
    public static void deleteAllMessageHistory(String userName, List<Message> messages, boolean isPushMessage) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }

        File historyFile;
        if (isPushMessage) {
            historyFile = UserUtil.getPushMessagePath(userName);
        } else {
            historyFile = UserUtil.getMessagePath(userName);
        }

        if (!historyFile.exists()) {
            return;
        }

        historyFile.delete();
        //delete pic, voice files attached in the message
        for (Message msg : messages) {
            if(msg.getType() == Message.TYPE_IMAGE || msg.getType() == Message.TYPE_VOICE) {
                if (msg.getFilePath() != null) {
                    File toDelFile = new File(msg.getFilePath());
                    if (toDelFile.exists()) {
                        toDelFile.delete();
                    }
                }
            }
        }
    }

    /**
    * If the user has been set as favorite by the current logged in user (myself). 
    * @param Context
    * @param userId 
    * @param isFavoriate 
    */
    public static void setFavorite(Context ctx, String userId, boolean isFavoriate) {
        SQLiteDatabase db = DBOpenHelper.getInstance(ctx).getWritableDatabase();        
        ContentValues values = new ContentValues();
        values.put(Contract.UserTable.COLUMN_NAME_FAVORITE, isFavoriate?1:0);
        db.update(Contract.UserTable.TABLE_NAME, values, Contract.UserTable.COLUMN_NAME_ID + " = ?",
                new String[] { String.valueOf(userId) });
    }
    
    
    //add user if doesn't exists
    public static void addOrUpdateUsers(Context ctx, List<EMUserBase> remoteContactList) {
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
                    //already have this avator on phone
                    continue;
                }
                new Thread(new Runnable() {                    
                    @Override
                    public void run() {
                        hfm.downloadThumbnailFile(picture, localFilePath, EaseMob.APPKEY, null, 60, true, new CloudOperationCallback() {

                            @Override
                            public void onProgress(int progress) {
                                Log.d("ease", "download progress: " + progress);
                            }

                            @Override
                            public void onError(String msg) {
                                Log.d("ease", "download error: " + msg);
                            }

                            @Override
                            public void onSuccess() {
                                Log.d("ease", "download complete");
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
                                // TODO Auto-generated method stub
                                
                            }
                            
                            @Override
                            public void onError(String msg) {
                                Log.d("ease", "download error: " + msg);
                            }

                            @Override
                            public void onSuccess() {
                                Log.d("ease", "download complete");
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
    * Update the user list using a Contact list retrieved from remote chat server. This will remove the local user if the same user has been deleted on 
    * the remote chat server. This will add a new local user if the user does not exist locally yet. This will update the local user to sync with the user 
    * info retrieved from the remote chat server.
    * @param Context
    * @param remoteContactList 
    */
    public static void addOrUpdateOrDeleteUsers(Context ctx, List<EMUserBase> remoteContactList) {
        SQLiteDatabase db = DBOpenHelper.getInstance(ctx).getWritableDatabase();
        Map<String, EMUserBase> allLocalUsers = loadAllUsers(ctx);
        addOrUpdateUsers(ctx, remoteContactList);
        
        String myselfId = EaseMob.getCurrentUserName();
        for(String userId : allLocalUsers.keySet()) {
            boolean found = false;           
            for(EMUserBase contact : remoteContactList) {
                if(userId.equals(contact.getUsername())) {
                    found = true;
                }
            }
            
            //This local user has been removed on remote (except myself)
            if(!found && !userId.equals(myselfId)) {
                db.delete(Contract.UserTable.TABLE_NAME, Contract.UserTable.COLUMN_NAME_ID + " = ?",
                        new String[] { String.valueOf(userId) });
            }
        }

        //db.close();       
    }
    
    //Need a field to indicate if the change is avator only
    /**
    * Update user, persist the change to DB or add the user if the user does not exist on DB.
    * @param Context
    * @param remoteContact 
    */
    public static void updateOrAddUser(Context ctx, DemoUser remoteContact) {
        SQLiteDatabase db = DBOpenHelper.getInstance(ctx).getWritableDatabase();
        
        //TODO: Refactor. avoid unnecessary db access. for example, can we load it from userlist cached in MainActivity?
        DemoUser user = loadUser(ctx, remoteContact.getUsername());
        
        if(user == null) {
            //This is a new contact added on remote, sync it to local
            addDB(remoteContact, db);
        } else {
            //Update the local user if necessary
            updateDB(remoteContact, db);
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
    
    private static void saveAvator(String userId, byte[] avator) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {   
            Log.e(TAG, "MEDIA is not MOUNTED, saveAvator failed");
            return;
        }
        
        File filepath = UserUtil.getAvatorPath(userId);
        filepath.getParentFile().mkdirs();

        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(filepath));
            os.write(avator);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }  
     
    }
    
    
    public static byte[] getFileBytes(File file) throws IOException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytes = (int) file.length();
            byte[] buffer = new byte[bytes];
            int readBytes = bis.read(buffer);
            if (readBytes != buffer.length) {
                throw new IOException("Entire file not read");
            }
            return buffer;
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }
        
}
