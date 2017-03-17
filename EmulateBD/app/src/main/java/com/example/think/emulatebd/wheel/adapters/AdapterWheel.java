package com.example.think.emulatebd.wheel.adapters;

import android.content.Context;

import com.example.think.emulatebd.wheel.WheelAdapter;

/**
 * Created by HuangMei on 2016/12/14.
 */

public class AdapterWheel extends AbstractWheelTextAdapter{

    private WheelAdapter adapter;

    public AdapterWheel(Context context, WheelAdapter adapter) {
        super(context);
        this.adapter = adapter;
    }

    public WheelAdapter getAdapter(){
        return adapter;
    }

    @Override
    protected CharSequence getItemText(int index) {
        return adapter.getItem(index);
    }

    @Override
    public int getItemsCount() {
        return adapter.getItemsCount();
    }
}
