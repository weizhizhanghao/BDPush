package com.example.think.emulatebd.xlistview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.think.emulatebd.R;

/**
 * Created by HuangMei on 2016/12/12.
 */

public class XListViewFooter extends LinearLayout{
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_LOADING = 2;

    private Context mContext;
    private View mContentView;
    private View mProgressBar;
    private TextView mHintView;

    public XListViewFooter(Context context) {
        super(context);
        initView(context);
    }

    public void setState(int state){
        mHintView.setVisibility(INVISIBLE);
        mProgressBar.setVisibility(INVISIBLE);
        mHintView.setVisibility(INVISIBLE);
        if (state == STATE_READY){
            mHintView.setVisibility(VISIBLE);
            mHintView.setText(R.string.xlistview_footer_hint_ready);
        } else if (state == STATE_LOADING){
            mProgressBar.setVisibility(VISIBLE);
        } else {
            mHintView.setVisibility(VISIBLE);
            mHintView.setText(R.string.xlistview_footer_hint_normal);
        }
    }

    public void setBottomMargin(int height){
        if (height < 0)
            return;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
        lp.bottomMargin = height;
        mContentView.setLayoutParams(lp);
    }

    public int getBottomMargin(){
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
        return lp.bottomMargin;
    }

    public void normal(){
        mHintView.setVisibility(VISIBLE);
        mProgressBar.setVisibility(GONE);
    }

    public void loading(){
        mHintView.setVisibility(GONE);
        mProgressBar.setVisibility(VISIBLE);
    }

    public void hide(){
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
        lp.height = 0;
        mContentView.setLayoutParams(lp);
    }

    public void show(){
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mContentView.setLayoutParams(lp);
    }

    private void initView(Context context){
        mContext = context;
        LinearLayout moreView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.xlistview_footer, null);
        addView(moreView);
        moreView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mContentView = moreView.findViewById(R.id.xlistview_footer_content);
        mProgressBar = moreView.findViewById(R.id.xlistview_footer_progressbar);
        mHintView = (TextView) moreView.findViewById(R.id.xlistview_footer_hint_textview);
    }
}
