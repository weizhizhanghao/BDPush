package com.example.think.emulatebd.common.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.think.emulatebd.R;

/**
 * Created by HuangMei on 2016/12/7.
 */

public class DialogUtil {

    public static Dialog getLoginDialog(Activity context){
        final Dialog dialog = new Dialog(context, R.style.Dialog);
        dialog.setContentView(R.layout.first_dialog_view);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();

        int screenW = getScreenWidth(context);
        lp.width = (int)(0.6 * screenW);

        TextView titleTxtv = (TextView) dialog.findViewById(R.id.tvLoad);
        titleTxtv.setText(R.string.first_start_dialog_text);
        return dialog;
    }

    public static Dialog getCustomDialog(Activity context){
        final Dialog dialog = new Dialog(context, R.style.Dialog);
        return dialog;
    }

    /**
     * 获取自定义的对话框
    */

    public static Dialog getWinDialog(Context context){
        final Dialog dialog = new Dialog(context, R.style.Dialog);
        dialog.getWindow()
                .setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        return dialog;
    }

    public static int getScreenWidth(Activity context){
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScrrenHeight(Activity context){
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }
}
