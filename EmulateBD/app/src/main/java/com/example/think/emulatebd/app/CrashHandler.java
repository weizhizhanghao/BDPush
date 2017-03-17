package com.example.think.emulatebd.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.view.WindowManager;

import com.example.think.emulatebd.R;
import com.example.think.emulatebd.common.util.L;
import com.example.think.emulatebd.common.util.T;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by HuangMei on 2016/12/26.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler INSTANCE;
    private Context mContext;

    public CrashHandler() {
    }

    public static CrashHandler getInstance(){
        if (INSTANCE == null)
            INSTANCE = new CrashHandler();
        return INSTANCE;
    }

    public void init(Context context){
        mContext = context;

        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void uncaughtException(Thread thread, Throwable ex){
        if (!handleException(ex) && mDefaultHandler != null);{
            mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    public boolean handleException(Throwable throwable){
        if (throwable == null || mContext == null){
            return false;
        }
        final String crashReport = getCrashReport(mContext, throwable);
        new Thread(){
            @Override
            public void run() {
                Looper.prepare();;
                File file = save2File(crashReport);
                sendAppCrashReport(mContext, crashReport, file);
                Looper.loop();
            }
        }.start();
        return true;
    }

    private File save2File(String crashReport){

        String fileName = "crash" + System.currentTimeMillis() + ".txt";
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED
        )){
            try {
                File dir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + "crash");
                if (!dir.exists())
                    dir.mkdir();
                File file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(crashReport.toString().getBytes());
                fos.close();
            } catch (Exception e){
                L.i("save2File error:" + e.getMessage());
            }
        }
        return null;
    }

    private void sendAppCrashReport(final  Context context,
                                    final String crashReport, final File file){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.app_error);
        builder.setMessage(R.string.app_error_message);
        builder.setPositiveButton(R.string.submit_report, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    String[] tos = {"2643253851@qq.com"};
                    intent.putExtra(Intent.EXTRA_EMAIL, tos);
                    intent.putExtra(Intent.EXTRA_SUBJECT,
                            "推聊Android客户端 - 错误报告");
                    if (file != null){
                        intent.putExtra(Intent.EXTRA_STREAM,
                                Uri.fromFile(file));
                        intent.putExtra(Intent.EXTRA_TEXT,
                                "请将此错误报告发送给我，以便我尽快修复此问题，谢谢合作！\n");
                    } else {
                        intent.putExtra(Intent.EXTRA_TEXT,
                                "请将此错误报告发送给我，以便我尽快修复此问题，谢谢合作！\n"
                                        + crashReport);
                    }
                    intent.setType("text./plain");
                    intent.setType("message/rfc882");
                    Intent.createChooser(intent, "Choose Email Client");
                    context.startActivity(intent);
                }catch (Exception e){
                    T.showLong(context, "There are no email clients installed.");
                } finally {
                    dialog.dismiss();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    private String getCrashReport(Context context, Throwable throwable){
        PackageInfo packageInfo = getPackageInfo(context);
        StringBuffer exceptionStr = new StringBuffer();
        exceptionStr.append("Version:" + packageInfo.versionName + "(" +
            packageInfo.versionCode + ")\n" );
        exceptionStr.append("Android:" + android.os.Build.VERSION.RELEASE
                + "(" + android.os.Build.MODEL + ")\n");
        exceptionStr.append("Exception:" + throwable.getMessage() + "\n");
        StackTraceElement[] elements = throwable.getStackTrace();
        for (int i = 0; i < elements.length; i ++){
            exceptionStr.append(elements[i].toString() + "\n");
        }
        return exceptionStr.toString();
    }

    private PackageInfo getPackageInfo(Context context){
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e){
            L.i("getPackageInfo err= " + e.getMessage());
        }

        if (info == null){
            info = new PackageInfo();
        }
        return info;
    }

}
