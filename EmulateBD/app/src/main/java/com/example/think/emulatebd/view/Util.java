package com.example.think.emulatebd.view;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by HuangMei on 2016/12/5.
 */

public class Util {
    public static int dpToPx(Resources res, int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }
}
