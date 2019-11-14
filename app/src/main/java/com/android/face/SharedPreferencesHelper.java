package com.android.face;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String SP_NAME_USER = "SharedPreferencesHelper";
    private static SharedPreferences mSharedPreferences;

    public static SharedPreferences getUserSp() {
        if (mSharedPreferences == null) {
            mSharedPreferences = FaceApplication.getInstance().getSharedPreferences(SP_NAME_USER, Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    public static int getInt(String key, int def) {
        return getUserSp().getInt(key, def);
    }

    public static String getString(String key, String def) {
        return getUserSp().getString(key, def);
    }

    public static boolean getBoolean(String key, boolean def) {
        return getUserSp().getBoolean(key, def);
    }

    public static long getLong(String key, long def) {
        return getUserSp().getLong(key, def);
    }

    public static void setPreference(String key, int value) {
        getUserSp().edit().putInt(key, value).commit();
    }

    public static void setPreference(String key, String value) {
        getUserSp().edit().putString(key, value).commit();
    }

    public static void setPreference(String key, boolean value) {
        getUserSp().edit().putBoolean(key, value).commit();
    }

    public static void setPreference(String key, long value) {
        getUserSp().edit().putLong(key, value).commit();
    }

    public static void clear() {
        getUserSp().edit().clear().commit();
    }
}
