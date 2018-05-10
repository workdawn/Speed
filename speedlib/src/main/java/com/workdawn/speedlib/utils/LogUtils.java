package com.workdawn.speedlib.utils;

import android.util.Log;

/**
 * Created on 2018/4/25.
 * @author workdawn
 */
public class LogUtils {
    private static boolean sShowLog = false;
    private final static String TAG = "Speed";
    private final static String PREFIX = "Speed log start >> ";
    public static void openLog(boolean showLog){
        sShowLog = showLog;
    }
    public static void d(String message) {
        if (sShowLog)
            Log.d(TAG, PREFIX + message);

    }

    public static void w(String message) {
        if (sShowLog)
            Log.w(TAG, PREFIX + message);

    }

    public static void i(String message) {
        if (sShowLog)
            Log.i(TAG, PREFIX + message);

    }

    public static void e(String message) {
        if (sShowLog)
            Log.e(TAG, PREFIX + message);

    }
}
