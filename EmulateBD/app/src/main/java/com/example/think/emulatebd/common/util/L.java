package com.example.think.emulatebd.common.util;

import android.util.Log;

/**
 * Created by HuangMei on 2016/12/7.
 */

public class L {
    public static boolean isDebug = true;
    private static final  String TAG = "HM";

    public static void i(String msg){
        if (isDebug)
            Log.i(TAG, msg);
    }

    public static void d(String msg){
        if (isDebug)
            Log.d(TAG, msg);
    }

    public static void e(String msg){
        if (isDebug)
            Log.e(TAG, msg);
    }

    public static void v(String msg){
        if (isDebug)
            Log.v(TAG, msg);
    }

    //自定义TAG
    public static void i(String tag, String msg){
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg){
        if (isDebug)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg){
        if (isDebug)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg){
        if (isDebug)
            Log.v(tag, msg);
    }
}
