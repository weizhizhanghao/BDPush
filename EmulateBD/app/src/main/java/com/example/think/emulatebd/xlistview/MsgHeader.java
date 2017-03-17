package com.example.think.emulatebd.xlistview;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.think.emulatebd.R;

/**
 * Created by HuangMei on 2016/12/7.
 */

public class MsgHeader extends LinearLayout {

    private LinearLayout mContainer;
    private ProgressBar mProgressBar;
    private TextView mHintTextView;
    private int mState = STATE_NORMAL;

    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;

    public MsgHeader(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.message_header, null);

        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);

        mHintTextView = (TextView) findViewById(R.id.xlistview_header_hint_textview);
        mProgressBar = (ProgressBar) findViewById(R.id.xlistview_header_progressbar);
    }

    public void setState(int state){
        if (state == mState){
            return;
        }

        if (state == STATE_REFRESHING){
            mProgressBar.setVisibility(VISIBLE);
        } else{
            mProgressBar.setVisibility(GONE);
        }

        switch (state){

            case STATE_NORMAL:
                if (mState == STATE_READY){

                }
                if (mState == STATE_REFRESHING){

                }

                mHintTextView.setVisibility(VISIBLE);
                mHintTextView.setText("显示更多消息");
                break;
            case STATE_READY:
                if (mState != STATE_READY){
                    mHintTextView.setVisibility(VISIBLE);
                    mHintTextView.setText("释放即可显示");
                }
                break;
            case STATE_REFRESHING:
                mHintTextView.setVisibility(GONE);
                break;
            default:
                break;
        }
        mState = state;
    }

    public void setVisiableHeight(int height){
        if (height < 0){
            height = 0;
        }
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisiableHeight(){
        return mContainer.getHeight();
    }
}




