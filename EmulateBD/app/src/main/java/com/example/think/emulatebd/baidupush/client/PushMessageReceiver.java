package com.example.think.emulatebd.baidupush.client;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.example.think.emulatebd.R;
import com.example.think.emulatebd.activity.MainActivity;
import com.example.think.emulatebd.app.PushApplication;
import com.example.think.emulatebd.bean.Message;
import com.example.think.emulatebd.bean.MessageItem;
import com.example.think.emulatebd.bean.RecentItem;
import com.example.think.emulatebd.bean.User;
import com.example.think.emulatebd.common.util.L;
import com.example.think.emulatebd.common.util.NetUtil;
import com.example.think.emulatebd.common.util.SendMsgAsyncTask;
import com.example.think.emulatebd.common.util.SharePreferenceUtil;
import com.example.think.emulatebd.common.util.T;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by HuangMei on 2016/12/23.
 */

public class PushMessageReceiver extends BroadcastReceiver{
    public static final String TAG = PushMessageReceiver.class.getSimpleName();
    public static final int NOTIFY_ID = 0x000;
    public static int mNewNum = 0;// 通知栏新消息条目，我只是用了一个全局变量，
    public static final String RESPONSE = "response";
    public static ArrayList<EventHandler> ehList = new ArrayList<EventHandler>();


    public static abstract interface EventHandler{
        public abstract void onMessage(Message message);
        public abstract void onBind(String method, int errorCode, String content);
        public abstract void onNotify(String title, String content);
        public abstract void onNetChange(boolean isNetConnected);
        public void onNewFriend(User u);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        L.i("listener num = " + ehList.size());
        if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)){
            String message = intent.getExtras().getString(
                    PushConstants.EXTRA_PUSH_MESSAGE_STRING);
            L.i("onMessage:" + message);
            try {
                Message msgItem = PushApplication.getInstance().getGson()
                        .fromJson(message, Message.class);
                parseMessage(msgItem, context);
            } catch (Exception e){

            }
        } else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)){
            final String method = intent.getStringExtra(PushConstants.EXTRA_METHOD);

            final int errCode = intent
                    .getIntExtra(PushConstants.EXTRA_ERROR_CODE,
                            PushConstants.ERROR_SUCCESS);

            final String content = new String(
                    intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
            L.i("onMessage: method : " + method + ", result : " + errCode
                    + ", content : " + content);
            paraseContent(context, errCode, content);// 处理消息

            for (int i = 0; i < ehList.size(); i ++){
                ((EventHandler)ehList.get(i)).onBind(method, errCode, content);
            }
        } else if  (intent.getAction().equals(
                PushConstants.ACTION_RECEIVER_NOTIFICATION_CLICK)) {
            L.d(TAG, "intent=" + intent.toUri(0));
            String title = intent
                    .getStringExtra(PushConstants.EXTRA_NOTIFICATION_TITLE);
            String content = intent
                    .getStringExtra(PushConstants.EXTRA_NOTIFICATION_CONTENT);
            for (int i = 0; i < ehList.size(); i++)
                ((EventHandler) ehList.get(i)).onNotify(title, content);
        } else if (intent.getAction().equals(
                "android.net.conn.CONNECTIVITY_CHANGE")) {
            boolean isNetConnected = NetUtil.isNetConnected(context);
            for (int i = 0; i < ehList.size(); i++)
                ((EventHandler) ehList.get(i)).onNetChange(isNetConnected);
        }
    }

    private void parseMessage(Message message, Context context){
        Gson gson = PushApplication.getInstance().getGson();
        L.i("gson ====" + message.toString());
        String tag = message.getTag();
        String userId = message.getUser_id();
        int headId = message.getHead_id();

        if (!TextUtils.isEmpty(tag)){
            if (userId.equals(PushApplication.getInstance().getSpUtil().getUserId()))
                return;
            User u = new User(userId, message.getChannel_id(), message.getNick(),
                    headId, 0);
            PushApplication.getInstance().getUserDB().addUser(u);
            for (EventHandler handler : ehList){
                handler.onNewFriend(u);
            }
            if (!tag.equals(RESPONSE)){
                L.i("response start");
                Message item = new Message(System.currentTimeMillis(), "hi",
                        PushMessageReceiver.RESPONSE);
                new SendMsgAsyncTask(gson.toJson(item), userId).send();
                L.i("response end");
            }
        } else {
            if (PushApplication.getInstance().getSpUtil().getMsgSound()){
                PushApplication.getInstance().getMediaPlayer().start();
            }
            if (ehList.size() > 0){
                for (int i = 0; i < ehList.size(); i ++){
                    ((EventHandler) ehList.get(i)).onMessage(message);
                }
            } else {
                showNotify(message, context);
                MessageItem item = new MessageItem(
                        MessageItem.MESSAGE_TYPE_TEXT, message.getNick(),
                        System.currentTimeMillis(), message.getMessage(), headId,
                        true, 1);
                RecentItem recentItem = new RecentItem(userId, headId,
                        message.getNick(), message.getMessage(), 0,
                        System.currentTimeMillis());
                PushApplication.getInstance().getMessageDB()
                        .saveMsg(userId, item);
                PushApplication.getInstance().getRecentDB()
                        .saveRecent(recentItem);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void showNotify(Message message, Context context) {
        mNewNum --;
        PushApplication application = PushApplication.getInstance();

        int icon = R.drawable.notify_newmessage;
        CharSequence tickerText = message.getNick() + ":"
                + message.getMessage();
        long when = System.currentTimeMillis();
//        Notification notification = new Notification(icon, tickerText, when);
//        notification.flags = Notification.FLAG_NO_CLEAR;
//
//        notification.defaults |= Notification.DEFAULT_VIBRATE;
//        notification.contentView = null;

        Intent intent = new Intent(application, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(application, 0,
                intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setAutoCancel(true)
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setWhen(when)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.FLAG_NO_CLEAR)
                .setContentIntent(contentIntent)
                .setContentInfo(application.getSpUtil().getNick() + " (" + mNewNum + "条新消息)");

        Notification notification = builder.getNotification();
//        notification.setLatestEventInfo(PushApplication.getInstance(),
//                application.getSpUtil().getNick() + " (" + mNewNum + "条新消息)",
//                tickerText, contentIntent);
        application.getNotificationManager().notify(NOTIFY_ID, notification);
//        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(NOTIFY_ID, builder.build());
    }

    private void paraseContent(final Context context, int errorCode,
                               String content) {
        if (errorCode == 0){
            String appid = "";
            String channelid = "";
            String userid = "";

            try {
                JSONObject jsonContent = new JSONObject(content);
                JSONObject params = jsonContent
                        .getJSONObject("response_params");
                appid = params.getString("appid");
                channelid = params.getString("channel_id");
                userid = params.getString("user_id");
            } catch (Exception e){
                L.e(TAG, "Parse bind json infos errors:" + e);
            }
            SharePreferenceUtil util = PushApplication.getInstance()
                    .getSpUtil();
            util.setAppId(appid);
            util.setChannelId(channelid);
            util.setUserId(userid);
        } else {
            if (NetUtil.isNetConnected(context)){
                if (errorCode == 30607){
                    T.showLong(context, "账号已过期，请重新登录");
                } else {
                    T.showLong(context, "启动失败，正在重试...");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PushManager.startWork(context,
                                    PushConstants.LOGIN_TYPE_API_KEY,
                                    PushApplication.API_KEY);
                        }
                    }, 2000);
                }
            } else {
                T.showLong(context, R.string.net_error_tip);
            }
        }
    }
}
