package com.example.think.emulatebd.slidingmenu;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.think.emulatebd.R;

/**
 * Created by HuangMei on 2016/12/21.
 */

public class SlidingActivityHelper {

    private Activity mActivity;

    private SlidingMenu mSlidingMenu;

    private View mViewAbove;

    private View mViewBehind;

    private boolean mBroadcasting = false;

    private boolean mOnPostCreateCalled = false;

    private boolean mEnableSlide = true;

    public SlidingActivityHelper(Activity activity) {
        this.mActivity = activity;
    }

    public void onCreate(Bundle savedInstanceState){
        mSlidingMenu = (SlidingMenu) LayoutInflater.from(mActivity).inflate(R.layout.slidingmenumain, null);
    }

    public void onPostCreate(Bundle saveInstanceState){
        if (mViewBehind == null || mViewAbove == null){
            throw new IllegalStateException("Both setBehindContentView must be called " +
                    "in onCreate in addition to setContentView.");
        }

        mOnPostCreateCalled = true;

        mSlidingMenu.attachToActivity(mActivity,
                mEnableSlide ? SlidingMenu.SLIDING_WINDOW : SlidingMenu.SLIDING_CONTENT);

        final boolean open;
        final boolean secondary;

        if (saveInstanceState != null){
            open = saveInstanceState.getBoolean("SlidingActivityHelper.open");
            secondary = saveInstanceState.getBoolean("SlidingActivityHelper.secondary");
        } else {
            open = false;
            secondary = false;
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (open){
                    if (secondary){
                        mSlidingMenu.showSecondaryMenu(false);
                    } else {
                        mSlidingMenu.showMenu(false);
                    }
                } else {
                    mSlidingMenu.showContent(false);
                }
            }
        });
    }

    public void setSlidingActionBarEnabled(boolean slidingActionBarEnabled){
        if (mOnPostCreateCalled){
            throw new IllegalStateException("enableSlidingActionBar must be called in onCreate.");
        }
        mEnableSlide = slidingActionBarEnabled;
    }

    public View findViewById(int id){
        View v;
        if (mSlidingMenu != null){
            v = mSlidingMenu.findViewById(id);
            if (v != null){
                return v;
            }
        }
        return null;
    }

    public void onSaveInstanceState(Bundle outState){
        outState.putBoolean("SlidingActivityHelper.open", mSlidingMenu.isMenuShowing());
        outState.putBoolean("SlidingActivityHelper.secondary", mSlidingMenu.isSecondaryMenuShowing());
    }

    public void registerAboveContentView(View v, ViewGroup.LayoutParams params){
        if (!mBroadcasting)
            mViewAbove = v;
    }

    public void setContentView(View view){
        mBroadcasting = true;
        mActivity.setContentView(view);
    }

    public void setBehindContentView(View view, ViewGroup.LayoutParams layoutParams){
        mViewBehind = view;
        mSlidingMenu.setMenu(mViewBehind);
    }

    public SlidingMenu getmSlidingMenu(){
        return mSlidingMenu;
    }

    public void toggle() {
        mSlidingMenu.toggle();
    }

    public void showContent(){
        mSlidingMenu.showContent();
    }

    public void showMenu(){
        mSlidingMenu.showMenu();
    }

    public void showSecondaryMenu() {
        mSlidingMenu.showSecondaryMenu();
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mSlidingMenu.isMenuShowing()) {
            showContent();
            return true;
        }
        return false;
    }
}
