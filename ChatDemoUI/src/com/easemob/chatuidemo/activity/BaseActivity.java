/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.easemob.chatuidemo.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.view.View;
import android.widget.TextView.BufferType;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.NotificationCompat;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chatuidemo.utils.CommonUtils;
import com.easemob.chatuidemo.utils.SmileUtils;
import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends FragmentActivity {
    private static final int notifiId = 11;
    protected NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onresume时，取消notification显示
        EMChatManager.getInstance().activityResumed();
        // umeng
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // umeng
        MobclickAgent.onPause(this);
    }

    /**
     * 当应用在前台时，如果当前消息不是属于当前会话，在状态栏提示一下
     * 如果不需要，注释掉即可
     * @param message
     */
    protected void notifyNewMessage(EMMessage message) {

        // notification.setLatestEventInfo(this, "", "", null);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(getApplicationInfo().icon)
                .setWhen(System.currentTimeMillis()).setAutoCancel(true);
        
        String ticker = CommonUtils.getMessageDigest(message, this);
        if(message.getType() == Type.TXT)
            ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
        //设置状态栏提示
        mBuilder.setTicker(message.getFrom()+": " + ticker);

        Notification notification = mBuilder.build();
        notificationManager.notify(notifiId, notification);
        notificationManager.cancel(notifiId);
    }

    /**
     * 返回
     * 
     * @param view
     */
    public void back(View view) {
        finish();
    }

}
