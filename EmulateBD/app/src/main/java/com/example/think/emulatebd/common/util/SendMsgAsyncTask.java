package com.example.think.emulatebd.common.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;

import com.example.think.emulatebd.R;
import com.example.think.emulatebd.app.PushApplication;
import com.example.think.emulatebd.baidupush.server.BaiduPush;

/**
 * Created by HuangMei on 2016/12/7.
 */

public class SendMsgAsyncTask {
    private BaiduPush mBaiduPush;
    private String mMessage;
    private Handler mHandler;
    private MyAsyncTask mTask;
    private String mUserId;
    private OnSendScuessListener mListener;

    public interface OnSendScuessListener {
        void sendScuess();
    }

    public void setOnSendScuessListener(OnSendScuessListener listener) {
        this.mListener = listener;
    }

    Runnable reSend = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            L.i("resend msg...");
            send();//重发
        }
    };

    public SendMsgAsyncTask(String jsonMsg,String useId) {
        // TODO Auto-generated constructor stub
        mBaiduPush = PushApplication.getInstance().getBaiduPush();
        mMessage = jsonMsg;
        mUserId = useId;
        mHandler = new Handler();
    }

    public void send() {
        if (NetUtil.isNetConnected(PushApplication.getInstance())) {//如果网络可用
            mTask = new MyAsyncTask();
            mTask.execute();
        } else {
            T.showLong(PushApplication.getInstance(), R.string.net_error_tip);
        }
    }

    public void stop() {
        if (mTask != null)
            mTask.cancel(true);
    }

    class MyAsyncTask extends AsyncTask<Void, Void, String>{

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            L.i("Send msg result:" + s);
            if (s.contains(BaiduPush.SEND_MSG_ERROR)){
                mHandler.postDelayed(reSend, 100);
            } else {
                if (mListener != null){
                    mListener.sendScuess();
                }
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "";
            if (TextUtils.isEmpty(mUserId)){
                result = mBaiduPush.PushMessage(mMessage);
            } else {
                result = mBaiduPush.PushMessage(mMessage, mUserId);
            }
            return result;
        }
    }
}
