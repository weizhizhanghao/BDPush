package com.example.think.emulatebd.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.think.emulatebd.R;
import com.example.think.emulatebd.app.PushApplication;
import com.example.think.emulatebd.common.util.SharePreferenceUtil;

public class WelcomeActivity extends Activity {

    private SharePreferenceUtil spUtil;
    private Handler handler;

    Runnable startAct = new Runnable() {
        @Override
        public void run() {
            if (!TextUtils.isEmpty(spUtil.getUserId())){
                Intent intent = new Intent(WelcomeActivity.this,
                        MainActivity.class);
                startActivity(intent);
            } else {
                startActivity(new Intent(WelcomeActivity.this,
                        FirstSetActivity.class));
            }
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        spUtil = PushApplication.getInstance().getSpUtil();
        handler = new Handler();
        handler.postDelayed(startAct, 3000);
    }
}
