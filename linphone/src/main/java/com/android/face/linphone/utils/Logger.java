package com.android.face.linphone.utils;

import android.util.Log;

public class Logger {

    private static final String TAG = "rgk_sip";

    public static void v(String tag, String msg) {
        Log.v(TAG, "[" + tag + "]:" + msg);
    }

    public static void d(String tag, String msg) {
        Log.d(TAG, "[" + tag + "]:" + msg);
    }

    public static void i(String tag, String msg) {
        Log.i(TAG, "[" + tag + "]:" + msg);
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, "[" + tag + "]:" + msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, "[" + tag + "]:" + msg);
    }
}
