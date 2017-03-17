package com.example.think.emulatebd.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.print.PageRange;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.think.emulatebd.R;
import com.example.think.emulatebd.app.PushApplication;
import com.example.think.emulatebd.wheel.adapters.AbstractWheelAdapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuangMei on 2016/12/26.
 */

public class HeadAdapter extends AbstractWheelAdapter{

    final int IMAGE_WIDTH = 50;
    final int IMAGE_HIGHT = 50;

    private final int items[] = PushApplication.heads;

    private List<SoftReference<Bitmap>> images;
    private LayoutInflater inflater;
    private Context context;

    public HeadAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        images = new ArrayList<>(items.length);
        for (int id : items){
            images.add(new SoftReference<>(loadImage(id)));
        }
    }

    private Bitmap loadImage(int id){
        Bitmap bitmap = BitmapFactory
                .decodeResource(context.getResources(), id);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH,
                IMAGE_HIGHT, true);
        bitmap.recycle();
        return scaled;
    }

    @Override
    public int getItemsCount() {
        return items.length;
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
        if (cachedView == null){
            cachedView = inflater.inflate(R.layout.head_select_layout, null);
        }
        ImageView img = (ImageView) cachedView.findViewById(R.id.head);
        SoftReference<Bitmap> bitmapSoftReference = images.get(index);
        Bitmap bitmap = bitmapSoftReference.get();
        if (bitmap == null){
            bitmap = loadImage(items[index]);
            images.set(index, new SoftReference<Bitmap>(bitmap));
        }
        img.setImageBitmap(bitmap);
        return cachedView;
    }
}
