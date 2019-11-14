package com.android.rgk.common.util;

import android.util.Log;

import com.android.rgk.common.BuildConfig;

public class LogUtil {
    public static final String TAG = "face/";

    private static final boolean DEBUG = BuildConfig.LOG_DEBUG;

    public static final void v(String tag, String msg) {
        Log.v(TAG + tag, msg);
    }

    public static final void i(String tag, String msg) {
        Log.i(TAG + tag, msg);
    }

    public static final void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(TAG + tag, msg);
        }
    }

    public static final void w(String tag, String msg) {
        Log.w(TAG + tag, msg);
    }

    public static final void e(String tag, String msg) {
        Log.e(TAG + tag, msg);
    }

    public static final String getStackTraceString(Throwable e) {
        return Log.getStackTraceString(e);
    }
}
