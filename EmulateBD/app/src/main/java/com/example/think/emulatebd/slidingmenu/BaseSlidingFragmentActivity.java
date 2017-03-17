package com.example.think.emulatebd.slidingmenu;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

public class BaseSlidingFragmentActivity extends FragmentActivity implements SlidingActivityBase  {

    private SlidingActivityHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SlidingActivityHelper(this);
        mHelper.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate(savedInstanceState);
    }

    @Override
    public View findViewById(int id) {
        View view = super.findViewById(id);
        if (view != null){
            return view;
        }
        return super.findViewById(id);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mHelper.onSaveInstanceState(outState);
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(getLayoutInflater().inflate(layoutResID, null));
    }

    @Override
    public void setContentView(View v) {
        setContentView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        mHelper.registerAboveContentView(view, params);
    }

    @Override
    public void setBehindContentView(View view, ViewGroup.LayoutParams layoutParams) {
        mHelper.setBehindContentView(view, layoutParams);
    }

    @Override
    public void setBehindContentView(View view) {
        setBehindContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setBehindContentView(int layoutResID) {
        setBehindContentView(getLayoutInflater().inflate(layoutResID, null));
    }

    @Override
    public SlidingMenu getSlidingMenu() {
        return mHelper.getmSlidingMenu();
    }

    @Override
    public void toggle() {
        mHelper.toggle();
    }

    @Override
    public void showContent() {
        mHelper.showContent();
    }

    @Override
    public void showMenu() {
        mHelper.showMenu();
    }

    @Override
    public void showSecondaryMenu() {
        mHelper.showSecondaryMenu();
    }

    @Override
    public void setSlidingActionBarEnabled(boolean slidingActionBarEnabled) {
        mHelper.setSlidingActionBarEnabled(slidingActionBarEnabled);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean b = mHelper.onKeyUp(keyCode, event);
        if (b)
            return b;
        return super.onKeyUp(keyCode, event);
    }
}
