package com.example.think.emulatebd.swipeback;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import com.example.think.emulatebd.R;

/**
 * Created by HuangMei on 2016/12/23.
 */

public class SwipeBackActivityHelper {
    private Activity mActivity;
    private SwipeBackLayout mSwipeBackLayout;

    public SwipeBackActivityHelper(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @SuppressWarnings("deprecation")
    public void onActivtyCreate() {
        mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        mActivity.getWindow().getDecorView().setBackgroundDrawable(null);
        mSwipeBackLayout = (SwipeBackLayout) LayoutInflater.from(mActivity)
                .inflate(R.layout.swipeback_layout,
                        null);
    }

    public void onPostCreate(){
        mSwipeBackLayout.attachToActivity(mActivity);
    }

    public View findViewById(int id){
        if (mSwipeBackLayout != null){
            return mSwipeBackLayout.findViewById(id);
        }
        return null;
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }
}
