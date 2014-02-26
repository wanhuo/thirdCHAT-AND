package com.easemob.demo;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;


import com.easemob.user.EMUser;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EaseMobChatConfig;
import com.easemob.cloud.CloudOperationCallback;
import com.easemob.cloud.HttpFileManager;
import com.easemob.user.AvatorUtils;
import com.easemob.user.EMUserDB;
import com.easemob.user.EMUserManager;
import com.easemob.user.EaseMobUserConfig;
import com.easemob.user.UserUtil;


public class ChatUtil {
    private static final String TAG = ChatUtil.class.getSimpleName();  
    
    /**
    * Load all users, this also load users' chat history. This does not include the current user (myself).
    * @param ctx Contexts
    * @return Map<String, User>
    */
    //public static Map<String, EMUser> loadAllUsers(Context ctx) {
    //    return EMUserDB.getInstance().loadAllUsers();
    //}
    
    public static EMUser loadUser(Context ctx, String userId) {
        return EMUserDB.getInstance().loadUser(userId);
    }       
    
    /**
    * Search users 
    * @param allUsers
    * @param query the query string. Can be part of user name (user id or user nick), location, phone number etc
    * @return List<User>
    */
    public static List<EMUser> searchUsers(List<EMUser> allUsers, String query) {
        //TODO: we only search against user name at the moment
        List<EMUser> resultList = new ArrayList<EMUser>();
        for(EMUser user : allUsers) {
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
    public static List<EMUser> searchChatHistory(List<EMUser> allUsers, String query) {
        //TODO: we only search against chat history stored in cache at the moment. We may need to search from message history files if necessary
        List<EMUser> resultList = new ArrayList<EMUser>();
        for(EMUser user : allUsers) {
            //List<EMMessage> history = user.getMessages();
            List<EMMessage> history = EMChatManager.getInstance().getConversation(user.getUsername()).getMessages();
            for(EMMessage m : history) {
                if(m.body!=null && m.body.toString().contains(query)) {
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
    public static void updateUsers(Context ctx, List<EMUser> remoteContactList, boolean removeNonExistingUser) {
        updateUsers(ctx, remoteContactList);
        
        if (removeNonExistingUser) {
            Map<String, EMUser> allLocalUsers = EMUserManager.getInstance().getAllUsers();
            String myselfId = EMUserManager.getInstance().getCurrentUserName();
            
            for (String userId : allLocalUsers.keySet()) {
                boolean found = false;
                for (EMUser contact : remoteContactList) {
                    if (userId.equals(contact.getUsername())) {
                        found = true;
                    }
                }

                // This local user has been removed on remote (except myself)
                if (!found && !userId.equals(myselfId)) {
                    Log.d("db", "remove local user which doesn't exists on server");
                    EMUserDB.getInstance().deleteUser(userId);
                }
            }
        }       
    }
    
    //update or add user (if the user does not exist in db yet)
    private static void updateUsers(Context ctx, List<EMUser> remoteContactList) {        
        final HttpFileManager hfm = new HttpFileManager(EaseMobUserConfig.getInstance().applicationContext,
                EaseMobChatConfig.getInstance().EASEMOB_STORAGE_URL);
        
        Map<String, EMUser> allLocalUsers = EMUserManager.getInstance().getAllUsers();
        for(EMUser remoteContact : remoteContactList) {
            //userName is the primary key            
        	final String username = remoteContact.getUsername();   
        	EMUser localUser = allLocalUsers.get(username);
            
            if(localUser == null) {
            	//DemoUser myUser = remoteContact.toType(DemoUser.class);
                EMUserDB.getInstance().saveUser(remoteContact);
                
                final String picture = remoteContact.getAvatorPath();
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
                        hfm.downloadThumbnailFile(picture, localFilePath, EaseMobUserConfig.getInstance().APPKEY, null, 60, true, new CloudOperationCallback() {

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
                                if (AvatorUtils.getAvatorCache().get("th"+username) != null) {
                                    AvatorUtils.getAvatorCache().remove("th"+username);
                                }
                            }
                        });
                    }
                }).start();
            } else {
                //Sync the existing local user with the remote user if necessary
                EMUserDB.getInstance().updateUser(remoteContact);
                final String localFilePath = UserUtil.getThumbAvatorPath(username).getAbsolutePath();
                
                final String picture = remoteContact.getAvatorPath();
                if(picture == null || picture.equals(localUser.getAvatorPath()) && new File(localFilePath).exists()){
                    //No need to download avatar for this user
                    continue;
                }
                new Thread(new Runnable() {
                    
                    @Override
                    public void run() {
                        hfm.downloadThumbnailFile(picture, localFilePath, EaseMobUserConfig.getInstance().APPKEY, null, new CloudOperationCallback() {
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
                                if (AvatorUtils.getAvatorCache().get(username) != null) {
                                    AvatorUtils.getAvatorCache().remove(username);
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
    public static void updateUser(Context ctx, EMUser user) {
        EMUserDB.getInstance().updateUser(user);
    }
        
    /**
    * Add a user to DB
    * @param Context
    * @param userId 
    */
    public static void addUser(Context ctx, EMUser contact) {
        EMUserDB.getInstance().saveUser(contact);
    }
    
    /**
    * Delete a user from DB
    * @param Context
    * @param userId 
    */
    
    public static void deleteUser(Context ctx, String userId) {
        EMUserDB.getInstance().deleteUser(userId);
    }  
}
