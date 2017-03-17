package com.example.think.emulatebd.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.example.think.emulatebd.R;
import com.example.think.emulatebd.adapter.HeadAdapter;
import com.example.think.emulatebd.adapter.SexAdapter;
import com.example.think.emulatebd.app.PushApplication;
import com.example.think.emulatebd.baidupush.client.PushMessageReceiver;
import com.example.think.emulatebd.bean.Message;
import com.example.think.emulatebd.bean.User;
import com.example.think.emulatebd.common.util.DialogUtil;
import com.example.think.emulatebd.common.util.NetUtil;
import com.example.think.emulatebd.common.util.SendMsgAsyncTask;
import com.example.think.emulatebd.common.util.SharePreferenceUtil;
import com.example.think.emulatebd.common.util.T;
import com.example.think.emulatebd.db.UserDB;
import com.example.think.emulatebd.wheel.WheelView;
import com.google.gson.Gson;

/**
 * Created by HuangMei on 2016/12/26.
 */

public class FirstSetActivity extends Activity  implements View.OnClickListener,
        PushMessageReceiver.EventHandler {

    private static final int LOGIN_OUT_TIME = 0;
    private Button mFirstStartBtn;
    private WheelView mHeadWheel;
    private WheelView mSexWheel;
    private EditText mNickEt;
    private PushApplication mApplication;
    private SharePreferenceUtil mSpUtil;
    private Gson mGson;
    private UserDB mUserDB;
    private View mNetErrorView;
    private TextView mTitle;
    private LoginOutTimeProcess mLoginOutTimeProcess;
    private Dialog mConnectServerDialog;
    private SendMsgAsyncTask task;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case LOGIN_OUT_TIME:
                    if (mLoginOutTimeProcess != null
                            && mLoginOutTimeProcess.running){
                        mLoginOutTimeProcess.stop();
                    }
                    if (mConnectServerDialog != null
                            && mConnectServerDialog.isShowing()){
                        mConnectServerDialog.dismiss();
                    }
                    if (task != null){
                        task.stop();
                    }
                    T.showShort(FirstSetActivity.this, "登录超时，请重试");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_set_layout);
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NetUtil.isNetConnected(this)){
            mNetErrorView.setVisibility(View.VISIBLE);
        } else {
            mNetErrorView.setVisibility(View.GONE);
        }
    }

    private void initData(){
        mApplication = PushApplication.getInstance();
        mLoginOutTimeProcess = new LoginOutTimeProcess();
        mSpUtil = mApplication.getSpUtil();
        mGson = mApplication.getGson();
        mUserDB = mApplication.getUserDB();
        PushMessageReceiver.ehList.add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (task != null)
            task.stop();
        PushMessageReceiver.ehList.remove(this);
    }

    private void initView(){
        mNetErrorView = findViewById(R.id.net_status_bar_top);
        mTitle = (TextView) findViewById(R.id.ivTitleName);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.first_start_title);
        mNetErrorView.setOnClickListener(this);
        mFirstStartBtn = (Button) findViewById(R.id.first_start_btn);
        mFirstStartBtn.setOnClickListener(this);
        mNickEt = (EditText) findViewById(R.id.nick_ed);
        mNickEt.setText(mSpUtil.getNick());
        mHeadWheel = (WheelView) findViewById(R.id.acount_head);
        mSexWheel = (WheelView) findViewById(R.id.acount_sex);
        mHeadWheel.setViewAdapter(new HeadAdapter(this));
        mSexWheel.setViewAdapter(new SexAdapter(this));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.first_start_btn:
                if (!NetUtil.isNetConnected(this)){
                    T.showLong(this, R.string.net_error_tip);
                    return;
                }
                String nick = mNickEt.getText().toString();
                if (TextUtils.isEmpty(nick)){
                    T.showShort(getApplicationContext(), R.string.first_start_dialog_text);
                    return;
                }
                mSpUtil.setNick(nick);
                mSpUtil.setHeadIcon(mHeadWheel.getCurrentItem());
                mSpUtil.setTag(SexAdapter.SEXS[mSexWheel.getCurrentItem()]);
                PushManager.startWork(getApplicationContext(),
                        PushConstants.LOGIN_TYPE_API_KEY,
                        PushApplication.API_KEY);
                mConnectServerDialog = DialogUtil.getLoginDialog(this);
                mConnectServerDialog.show();
                mConnectServerDialog.setCancelable(false);
                if (mLoginOutTimeProcess != null && !mLoginOutTimeProcess.running)
                    mLoginOutTimeProcess.start();
                break;
            case R.id.net_status_bar_info_top:
                startActivity(new Intent(
                        Settings.ACTION_WIFI_SETTINGS
                ));
                break;
            default:
                break;
        }

    }

    @Override
    public void onMessage(Message message) {

    }

    @Override
    public void onBind(String method, int errorCode, String content) {
        if (errorCode == 0){
            User u = new User(mSpUtil.getUserId(), mSpUtil.getChannelId(),
                    mSpUtil.getNick(), mSpUtil.getHeadIcon(), 0);
            mUserDB.addUser(u);
            Message msgItem = new Message(System.currentTimeMillis(), "hi", mSpUtil.getTag());
            task = new SendMsgAsyncTask(mGson.toJson(msgItem), "");
            task.setOnSendScuessListener(new SendMsgAsyncTask.OnSendScuessListener() {
                @Override
                public void sendScuess() {
                    startActivity(new Intent(FirstSetActivity.this,
                            MainActivity.class));
                    if (mConnectServerDialog != null
                            && mConnectServerDialog.isShowing()){
                        mConnectServerDialog.dismiss();
                    }
                    if (mLoginOutTimeProcess != null
                            && mLoginOutTimeProcess.running){
                        mLoginOutTimeProcess.stop();
                    }
                    T.showShort(mApplication, R.string.first_start_scuess);
                    finish();
                }
            });
            task.send();
        }
    }

    @Override
    public void onNotify(String title, String content) {

    }

    @Override
    public void onNetChange(boolean isNetConnected) {
        if (!isNetConnected){
            T.showShort(this, R.string.net_error_tip);
            mNetErrorView.setVisibility(View.VISIBLE);
        } else {
            mNetErrorView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNewFriend(User u) {

    }

    class LoginOutTimeProcess implements Runnable{
        public boolean running = false;
        private long startTime =0L;
        private Thread thread = null;

        public LoginOutTimeProcess() {
        }

        @Override
        public void run() {
            while (true){
                if (!this.running){
                    return;
                }
                if (System.currentTimeMillis() - this.startTime > 20 * 1000L){
                    mHandler.sendEmptyMessage(LOGIN_OUT_TIME);
                }
                try{
                    Thread.sleep(10L);
                }catch (Exception ex){

                }
            }
        }

        public void start(){
            try{
                this.thread = new Thread(this);
                this.running = true;
                this.startTime = System.currentTimeMillis();
                this.thread.start();
            } finally {

            }
        }

        public void stop(){
            try {
                this.running = false;
                this.thread = null;
                this.startTime = 0L;
            }finally {

            }
        }
    }
}
