package com.example.think.emulatebd.slidingmenu;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by HuangMei on 2016/12/21.
 */

public interface SlidingActivityBase {

    public void setBehindContentView(View view, ViewGroup.LayoutParams layoutParams);

    public void setBehindContentView(View view);

    public void setBehindContentView(int layoutResID);

    public SlidingMenu getSlidingMenu();

    public void toggle();

    public void showContent();

    public void showMenu();

    public void showSecondaryMenu();

    public void setSlidingActionBarEnabled(boolean slidingActionBarEnabled);

}
