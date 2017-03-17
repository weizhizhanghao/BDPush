package com.example.think.emulatebd.adapter;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.example.think.emulatebd.view.JazzyViewPager;

import java.util.List;

/**
 * Created by HuangMei on 2016/12/27.
 */

public class FacePageAdapter extends PagerAdapter{

    private List<View> views;
    private JazzyViewPager viewPager;

    public FacePageAdapter(List<View> views, JazzyViewPager viewPager) {
        super();
        this.views = views;
        this.viewPager = viewPager;
    }

    @Override
    public int getCount() {
        if (views != null)
            return views.size();
        return 0;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager)container).removeView(views.get(position));
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    @Override
    public float getPageWidth(int position) {
        return super.getPageWidth(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public Object instantiateItem(View container, int position) {
        ((ViewPager)container).addView(views.get(position), 0);
        viewPager.setObjectForPosition(views.get(position), position);
        return views.get(position);
    }

    @Override
    public void notifyDataSetChanged() {
        // TODO Auto-generated method stub
        super.notifyDataSetChanged();
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        // TODO Auto-generated method stub
        super.restoreState(state, loader);
    }

    @Override
    public Parcelable saveState() {
        // TODO Auto-generated method stub
        return super.saveState();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setPrimaryItem(View container, int position, Object object) {
        // TODO Auto-generated method stub
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        // TODO Auto-generated method stub
        super.setPrimaryItem(container, position, object);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void startUpdate(View container) {
        // TODO Auto-generated method stub
        super.startUpdate(container);
    }

    @Override
    public void startUpdate(ViewGroup container) {
        // TODO Auto-generated method stub
        super.startUpdate(container);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
