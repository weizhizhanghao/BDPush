package com.example.think.emulatebd.wheel.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by HuangMei on 2016/12/12.
 */

public interface WheelViewAdapter {

    public int getItemsCount();

    public View getItem(int index, View convertView, ViewGroup parent);

    public View getEmptyItem(View convertView, ViewGroup parent);

    public void registerDataSetObserver(DataSetObserver observer);

    void ungeristerDataSetObserver(DataSetObserver observer);

}
