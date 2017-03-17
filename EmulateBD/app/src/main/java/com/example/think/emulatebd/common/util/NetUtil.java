package com.example.think.emulatebd.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by HuangMei on 2016/12/7.
 */

public class NetUtil {
    public static boolean isNetConnected(Context context){
        boolean isNetConnected;
        //获得网络连接服务
        ConnectivityManager connectivityManager = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()){
            isNetConnected = true;
        } else {
            L.i("没有可用网络");
            isNetConnected = false;
        }
        return isNetConnected;
    }
}
