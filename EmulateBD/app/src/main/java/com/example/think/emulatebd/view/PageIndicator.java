package com.example.think.emulatebd.view;

import android.support.v4.view.ViewPager;

/**
 * Created by HuangMei on 2016/12/5.
 */

public interface PageIndicator extends ViewPager.OnPageChangeListener {

    void setViewPager(ViewPager viewPager);

    void setViewPager(ViewPager viewPager, int initialPosition);

    void setCurrentItem(int item);

    void setOnPageChangeListener(ViewPager.OnPageChangeListener listener);

    void notifyDataSetChanged();
}
