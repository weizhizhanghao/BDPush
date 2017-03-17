package com.example.think.emulatebd.common.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by HuangMei on 2016/12/7.
 * Toast
 */

public class T {

    private static Toast toast;

    //短时间显示toast
    public static void showShort(Context context, CharSequence message){
        if (null == toast){
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    public static void showShort(Context context, int message){
        if (null == toast){
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    //长时间显示toast
    public static void showLong(Context context, CharSequence message){
        if (null == toast){
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    public static void showLong(Context context, int message){
        if (null == toast){
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    //自定义显示时长的Toast
    public static void show(Context context, CharSequence message, int duration){
        if (null == toast){
            toast = Toast.makeText(context, message, duration);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    public static void show(Context context, int message, int duration){
        if (null == toast){
            toast = Toast.makeText(context, message, duration);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    //hide the toast ,if any
    public static void hideToast(){
        if (null != toast){
            toast.cancel();
        }
    }
}
