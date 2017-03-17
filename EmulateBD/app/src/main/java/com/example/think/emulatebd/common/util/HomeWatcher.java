package com.example.think.emulatebd.common.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by HuangMei on 2016/12/7.
 */

public class HomeWatcher {

    static final String TAG = "HomeWatcher";
    private Context mContext;
    private IntentFilter mFilter;
    private OnHomePressedListener mListener;
    private InnerReceiver mReceiver;

    public interface OnHomePressedListener{
        public void onHomePressed();
        public void onHomeLongPressed();
    }

    public HomeWatcher(Context Context) {
        this.mContext = Context;
        mReceiver = new InnerReceiver();
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public void setOnHomePressedListener(OnHomePressedListener listener){
        this.mListener = listener;
    }

    /**
     * 开始监听，注册广播
     */
    public void startWatch(){
        if (mReceiver != null){
            mContext.registerReceiver(mReceiver, mFilter);
        }
    }

    /**
     * 停止监听，注销广播
     */
    public void stopWatch(){
        if (mReceiver != null){
            mContext.unregisterReceiver(mReceiver);
        }
    }

    /**
     * 广播接收者
     */
    class InnerReceiver extends BroadcastReceiver{

        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null){
                    L.i(TAG, "action:" + action + ",reason" + reason);

                    if (mListener != null){
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)){
                            mListener.onHomePressed();
                        } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                            mListener.onHomeLongPressed();
                        }
                    }
                }
            }
        }
    }

}
