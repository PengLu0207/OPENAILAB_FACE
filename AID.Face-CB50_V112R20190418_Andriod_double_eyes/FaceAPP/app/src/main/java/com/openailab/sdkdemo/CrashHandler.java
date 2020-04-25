package com.openailab.sdkdemo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;


/**
 *
 */

public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    // CrashHandler
    private static CrashHandler INSTANCE = new CrashHandler();

    // Context
    private MainActivity activity;

    // UncaughtException
    private UncaughtExceptionHandler mDefaultHandler;


    private Map<String, String> infos = new HashMap<String, String>();


    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private StringBuilder sb;
    private String logItemSectionSplitor = ", ";


    private String errorStr;

    // CrashHandler
    private CrashHandler() {
    }

    // CrashHandler
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(MainActivity activity) {
        this.activity = activity;
        // UncaughtException
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        //  CrashHandler
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
//        if (!handleException(ex) && mDefaultHandler != null) {
//            mDefaultHandler.uncaughtException(thread, ex);
//        } else {


        Log.d("zheng", "CrashHandler error:" + errorStr);

        //System.exit(0);
//        Log.d("zheng", "000");
//        Log.d("zheng", activity + "");
        //释放底层资源
        try {
            activity.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.d("zheng", "111");
        Intent intent = new Intent();
        intent.setClass(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
//        Log.d("zheng", "222");
        // System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
//        Log.d("zheng", "CrashHandler error:ok");
//        }
    }


    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }


        saveCrashInfo2File(ex);
        return true;
    }


    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);

            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";

                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }

        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }


    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();

        String result = writer.toString();
        sb.append(result);
        Log.e("zheng", sb.toString());
        errorStr = sb.toString();

        return null;
    }


}
