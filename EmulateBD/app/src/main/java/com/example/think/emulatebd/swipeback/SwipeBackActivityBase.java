package com.example.think.emulatebd.swipeback;

/**
 * Created by HuangMei on 2016/12/23.
 */

public interface SwipeBackActivityBase {

    public abstract SwipeBackLayout getSwipeBackLayout();

    public abstract void setSwipeBackEnable(boolean enable);

    public abstract void scrollToFinishActivity();
}
