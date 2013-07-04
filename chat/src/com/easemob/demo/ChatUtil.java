package com.easemob.demo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.easemob.chat.UserUtil;
import com.easemob.chat.db.EaseMobMsgDB;
import com.easemob.chat.domain.EMUserBase;
import com.easemob.chat.domain.Group;
import com.easemob.chat.domain.Message;
import com.easemob.cloud.CloudOperationCallback;
import com.easemob.cloud.HttpFileManager;
import com.easemob.ui.activity.ChatActivity;
import com.easemob.ui.util.AppendObjectOutputStream;
import com.easemob.util.HanziToPinyin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;


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
        String myselfId = UserUtil.getCurrentUserId();
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
                //user.setNote(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_NOTE)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_EMAIL)));
                user.setDepartment(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_ORGAINIZATION)));
                user.setMobile(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_MOBILE)));
                user.setWorkPhone(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_WORKPHONE)));
                user.setAddress(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_ADDRESS)));
                user.setSignature(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_SIGNATURE)));
                //user.setFavorite(cursor.getInt(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_FAVORITE)) > 0);
                user.setPicture(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_REMOTEAVATARPATH)));
                
                
                //Load chat history
                if(EaseMobMsgDB.isTableExists(db, user.getUsername())){
                	List<Message> chatHistory= EaseMobMsgDB.findAllMessages(ctx, user.getUsername());
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
                user.setNote(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_NOTE)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_EMAIL)));
                user.setDepartment(cursor.getString(cursor.getColumnIndex(Contract.UserTable.COLUMN_NAME_ORGAINIZATION)));
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
    * Load all users who has recent chat. 
    * @param allUsers
    * @return List<User> sorted in order of last chat time
    */
    public static List<EMUserBase> loadUsersWithRecentChat(List<EMUserBase> allUsers,Context context) {
        List<EMUserBase> resultList = new ArrayList<EMUserBase>();
        for(EMUserBase user : allUsers) {
            if(user.getMessages().size() >0) {
                resultList.add(user);
            }
        }
        
        //TODO:
        //create group adapter to also show conversation from groupchat in chats list fragment
/*        for (Group group : Group.allGroups) {
            GroupToUserAdapter ga = new GroupToUserAdapter(group, context);
            if (ga.getMessages().size() > 0) {
                resultList.add(ga);
            }
        }*/
        
        //Sort User by last chat time
        Collections.sort( resultList, new Comparator<EMUserBase>() {
            @Override
            public int compare( final EMUserBase user1,
                                final EMUserBase user2 ) {
                if(user2.getMessages().size() == 0 && user1.getMessages().size() == 0) {
                    return 0;
                } else if(user2.getMessages().size() == 0 && user1.getMessages().size() > 0) {
                    return -1;
                } else if(user2.getMessages().size() > 0 && user1.getMessages().size() == 0) {
                    return 1;
                }

                Message user2LastMessage = user2.getMessages().get(user2.getMessages().size() -1);
                Message user1LastMessage = user1.getMessages().get(user1.getMessages().size() -1);
               
                return user2LastMessage.getTime().compareTo(user1LastMessage.getTime());
            }
            
        } );
        return resultList;
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
    public static List<EMUserBase> loadUsersWithDepartment(List<EMUserBase> allUsers, String path) {
        List<EMUserBase> resultList = new ArrayList<EMUserBase>();
        
        //mock data. 组织结构返回空
        if("/".equals(path)) {
            return resultList;
        }
        
        //mock data. /组织结构/研发部 返回user列表的第1个到第2员工
        if("/集团总公司/研发设计中心".equals(path)) {
            for(int i = 0; i< allUsers.size(); i++) {
                EMUserBase u = allUsers.get(i);
                if(0 <= i && i < 2) {
                    resultList.add(u);
                }
            }
            return resultList;
        }
        
        if("/集团总公司/研发设计中心/开发部/移动开发部/ios".equals(path)) {
            for(int i = 0; i< allUsers.size(); i++) {
                EMUserBase u = allUsers.get(i);
                if( 2 <= i) {
                    resultList.add(u);
                }
            }
            return resultList;
        }

        return resultList;
    }
    
    /**
    * Get child departments 
    * @param path: parent department. Use "/" as the root path
    * @return List<String> child departments
    */
    public static List<String> getChildDepartments(String path) {
        List<String> resultList = new ArrayList<String>();
        
      //Mock data        
        if("/".equals(path)) {
            resultList.add("/集团总公司");
            return resultList;
        }
        
        if("/集团总公司".equals(path)) {
            resultList.add("/集团总公司/财务管理中心");
            resultList.add("/集团总公司/总经理办公室");
            resultList.add("/集团总公司/运营中心");
            resultList.add("/集团总公司/营销中心");
            resultList.add("/集团总公司/研发设计中心");
            resultList.add("/集团总公司/生产管理中心");
            return resultList;
        }
        
        if("/集团总公司/财务管理中心".equals(path)) {
            resultList.add("/集团总公司/财务管理中心/财务部");
            resultList.add("/集团总公司/财务管理中心/直营监察部");
            return resultList;
        }
        
        if("/集团总公司/总经理办公室".equals(path)) {
            resultList.add("/集团总公司/总经理办公室/人力资源部");
            resultList.add("/集团总公司/总经理办公室/综合管理部");
            resultList.add("/集团总公司/总经理办公室/物业管理部");
            return resultList;
        }
        
        if("/集团总公司/运营中心".equals(path)) {
            resultList.add("/集团总公司/运营中心/信息部");
            resultList.add("/集团总公司/运营中心/工程部");
            resultList.add("/集团总公司/运营中心/物流部");
            return resultList;
        }

        if("/集团总公司/营销中心".equals(path)) {
            resultList.add("/集团总公司/营销中心/加盟部");
            resultList.add("/集团总公司/营销中心/直营部");
            resultList.add("/集团总公司/营销中心/市场部");
            resultList.add("/集团总公司/营销中心/渠道拓展部");
            resultList.add("/集团总公司/营销中心/零售支持部");
            resultList.add("/集团总公司/营销中心/商品部");
            resultList.add("/集团总公司/营销中心/多元化部");
            resultList.add("/集团总公司/营销中心/北京销售部");
            return resultList;
        }

        if("/集团总公司/营销中心/北京销售部".equals(path)) {
            resultList.add("/集团总公司/营销中心/北京销售部/东城销售部");
            resultList.add("/集团总公司/营销中心/北京销售部/西城销售部");
            resultList.add("/集团总公司/营销中心/北京销售部/海淀销售部");
            resultList.add("/集团总公司/营销中心/北京销售部/朝阳销售部");
            resultList.add("/集团总公司/营销中心/北京销售部/石景山销售部");
            resultList.add("/集团总公司/营销中心/北京销售部/通州销售部");
            resultList.add("/集团总公司/营销中心/北京销售部/昌平销售部");
            resultList.add("/集团总公司/营销中心/北京销售部/顺义销售部");
            return resultList;
        }

        if("/集团总公司/研发设计中心".equals(path)) {
            resultList.add("/集团总公司/研发设计中心/设计部");
            resultList.add("/集团总公司/研发设计中心/开发部");
            resultList.add("/集团总公司/研发设计中心/技术部");
            resultList.add("/集团总公司/研发设计中心/核价部");
            return resultList;
        }
        
        if("/集团总公司/研发设计中心/开发部".equals(path)) {
            resultList.add("/集团总公司/研发设计中心/开发部/移动开发部");
            resultList.add("/集团总公司/研发设计中心/开发部/ERP");
            return resultList;
        }
        
        if("/集团总公司/研发设计中心/开发部/移动开发部".equals(path)) {
            resultList.add("/集团总公司/研发设计中心/开发部/移动开发部/ios");
            resultList.add("/集团总公司/研发设计中心/开发部/移动开发部/android");
            return resultList;
        }

        if("/集团总公司/生产管理中心".equals(path)) {
            resultList.add("/集团总公司/生产管理中心/服装部");
            resultList.add("/集团总公司/生产管理中心/鞋品部");
            resultList.add("/集团总公司/生产管理中心/装备部");
            return resultList;
        }
        
        return resultList;
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
    

    //TODO: only load most recent 50 messages (or 20?. we need a paged operation)
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
    
    public static void deleteMessageHistory(String userName, List<Message> messagesToDelete, boolean isPushMessage) {
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
    }
    
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
                //This is a new contact added on remote, sync it to local
                DemoUser myUser = remoteContact.toType(DemoUser.class);
                addDB(myUser, db);
                
                final String picture = remoteContact.getPicture();
                if(picture == null || picture.startsWith("http://www.gravatar.com")){
                    //No need to download avatar for this user
                    continue;
                }
                final String localFilePath = UserUtil.getAvatorPath(username).getAbsolutePath();
                if (new File(localFilePath).exists()) {
                    //already have this avator on phone
                    continue;
                }
                new Thread(new Runnable() {                    
                    @Override
                    public void run() {
                        final String localFilePath = UserUtil.getAvatorPath(username).getAbsolutePath();
                        hfm.downloadFile(picture, localFilePath, DemoApp.appId, null, new CloudOperationCallback() {

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
                                if (ChatActivity.getAvatorCache().get(username) != null) {
                                    ChatActivity.getAvatorCache().remove(username);
                                }
                            }
                        });
                    }
                }).start();
            } else {
                //Sync the existing local user with the remote user if necessary
                DemoUser myUser = remoteContact.toType(DemoUser.class);
                updateDB(myUser, db);
                
                final String picture = remoteContact.getPicture();
                if(picture == null || picture.equals(localUser.getPicture())){
                    //No need to download avatar for this user
                    continue;
                }
                final String localFilePath = UserUtil.getAvatorPath(username).getAbsolutePath();
                if (new File(localFilePath).exists()) {
                    //already have this avator on phone
                    continue;
                }
				new Thread(new Runnable() {
					@Override
					public void run() {
						hfm.downloadFile(picture, localFilePath, DemoApp.appId, null, new CloudOperationCallback() {
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
        
        String myselfId = UserUtil.getCurrentUserId();
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
        if(contact.getDepartment() != null) {
            values.put(Contract.UserTable.COLUMN_NAME_ORGAINIZATION, contact.getDepartment());
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
        		values.put(Contract.UserTable.COLUMN_NAME_HEADER, HanziToPinyin.getInstance().get(contact.getNick().substring(0, 1)).get(0).target.substring(0, 1).toUpperCase());
        	}
        }
        values.put(Contract.UserTable.COLUMN_NAME_SEX, contact.getSex());
        values.put(Contract.UserTable.COLUMN_NAME_EMAIL, contact.getEmail());
        values.put(Contract.UserTable.COLUMN_NAME_ORGAINIZATION, contact.getDepartment());
        values.put(Contract.UserTable.COLUMN_NAME_MOBILE, contact.getMobile());
        values.put(Contract.UserTable.COLUMN_NAME_WORKPHONE, contact.getWorkPhone());
        values.put(Contract.UserTable.COLUMN_NAME_SIGNATURE, contact.getSignature());
        values.put(Contract.UserTable.COLUMN_NAME_ADDRESS, contact.getAddress());
        values.put(Contract.UserTable.COLUMN_NAME_REMOTEAVATARPATH, contact.getPicture());
        
        //values.put(Contract.UserTable.COLUMN_NAME_NOTE, "");  
        //TODO:Save avator
/*        if(contact.getAvatar() != null && contact.getAvatar().length != 0) {
            saveAvator(contact.getJID(), contact.getAvatar());    
        }*/
        
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
    
    
    public static void saveGroups(List<Group> groups) {
        File filepath = new File(UserUtil.getHistoryPath() + "/group.dat");
        ObjectOutputStream output = null;
        try {
            filepath.getParentFile().mkdirs();
            OutputStream file = new FileOutputStream(filepath, false);
            output = new ObjectOutputStream(file);
            output.writeObject(groups);
            output.flush();
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
    
    public static List<Group> loadGroups(Context context,List<EMUserBase> users) {
//    	List<Group> groups = null;
//    	SQLiteDatabase db = DBOpenHelper.getInstance(context).getReadableDatabase();
//    	if(db.isOpen()){
//    		Cursor cursor = db.rawQuery("select * from " + Contract.GroupsTable.TABLE_NAME,null);
//    		if(cursor.moveToFirst()){
//    			groups = new ArrayList<Group>();
//    			do{
//    				Group group = new Group(context);
//    				group.setName(cursor.getString(cursor.getColumnIndex(Contract.GroupsTable.COLUMN_NAME_GROUP_NAME)));
//    				group.setId(cursor.getInt(cursor.getColumnIndex(Contract.GroupsTable.COLUMN_NAME_ID))+"");
//    				for(User user : users){
//    					if(group.getId().equals(user.getGroupId())){
//    						group.getUsers().add(user);
//    					}
//    				}
//    				List<Message> msgs = findAllMessages(context, group.getName());
//    				group.setMessages(msgs);
//    				groups.add(group);
//    			}while(cursor.moveToNext());
//    		}
//    	}
    	
    	File filepath = new File(UserUtil.getHistoryPath() + "/group.dat");
        ObjectInputStream input = null;
        ArrayList<Group> groups = new ArrayList<Group>();
        if (!filepath.exists()) {
        	return groups;
        }
        try {
        	FileInputStream fis = new FileInputStream(filepath);
        	input = new ObjectInputStream(fis);
        	groups = (ArrayList<Group>)input.readObject();
        	//Load chat history, need to refactor to only load limited num of record during init
        	for (Group group : groups) {
                List<Message> chatHistory = loadMessageHistory(group.getName(), false);            
                group.setMessages(chatHistory);
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	try {
        	    input.close();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
    	return groups;
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
