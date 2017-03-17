package com.example.think.emulatebd.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.think.emulatebd.R;
import com.example.think.emulatebd.wheel.adapters.AbstractWheelTextAdapter;

/**
 * Created by HuangMei on 2016/12/26.
 */

public class SexAdapter extends AbstractWheelTextAdapter {

    public static final String SEXS[] = new String[]{"美女", "帅哥", "人妖"};
    public static final int FLAGS[] = new int[]{R.drawable.female, R.drawable.male,
            R.drawable.nomale};

    public SexAdapter(Context context) {
        super(context, R.layout.sex_select_layout, NO_RESOURCE);
        setItemTextResource(R.id.sex_name);
    }

    @Override
    public View getItem(int index, View convertView, ViewGroup parent) {
        View view = super.getItem(index, convertView, parent);
        ImageView img = (ImageView)view.findViewById(R.id.flag);
        img.setImageResource(FLAGS[index]);
        return view;
    }

    @Override
    public int getItemsCount() {
        return SEXS.length;
    }

    @Override
    protected CharSequence getItemText(int index) {
        return SEXS[index];
    }
}
