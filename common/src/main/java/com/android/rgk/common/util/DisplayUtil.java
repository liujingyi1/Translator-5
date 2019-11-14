package com.android.rgk.common.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Display工具类
 */
public class DisplayUtil {

    public DisplayUtil() {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        @SuppressLint("WrongConstant")
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        windowManager.getDefaultDisplay().getRealMetrics(dm);
        return dm;
    }

    public static DisplayMetrics printDisplayInfo(Context context) {
        DisplayMetrics dm = getDisplayMetrics(context);
        StringBuilder sb = new StringBuilder();
        sb.append("_______  显示信息:  ");
        sb.append("\ndensity         :").append(dm.density);
        sb.append("\ndensityDpi      :").append(dm.densityDpi);
        sb.append("\nheightPixels    :").append(dm.heightPixels);
        sb.append("\nwidthPixels     :").append(dm.widthPixels);
        sb.append("\nscaledDensity   :").append(dm.scaledDensity);
        sb.append("\nxdpi            :").append(dm.xdpi);
        sb.append("\nydpi            :").append(dm.ydpi);
        //DLog.i("ContentValues", sb.toString());
        return dm;
    }

    public static int dip2px(Context c, float dpValue) {
        float scale = getDisplayMetrics(c).density;
        return (int) (dpValue * scale + 0.5F);
    }

    public static int px2dip(Context c, float pxValue) {
        float scale = getDisplayMetrics(c).density;
        return (int) (pxValue / scale + 0.5F);
    }

    public static int px2sp(Context c, float pxValue) {
        float fontScale = getDisplayMetrics(c).scaledDensity;
        return (int) (pxValue / fontScale + 0.5F);
    }

    public static int sp2px(Context c, float spValue) {
        float fontScale = getDisplayMetrics(c).scaledDensity;
        return (int) (spValue * fontScale + 0.5F);
    }

    public static int getScreenWidthPixels(Context c) {
        return getDisplayMetrics(c).widthPixels;
    }

    public static int getScreenHeightPixels(Context c) {
        return getDisplayMetrics(c).heightPixels;
    }

    @TargetApi(17)
    public static int getScreenRealH(Context context) {
        @SuppressLint("WrongConstant")
        WindowManager winMgr = (WindowManager) context.getSystemService("window");
        Display display = winMgr.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        int h;
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealMetrics(dm);
            h = dm.heightPixels;
        } else {
            try {
                Method method = Class.forName("android.view.Display").getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
                h = dm.heightPixels;
            } catch (Exception var6) {
                display.getMetrics(dm);
                h = dm.heightPixels;
            }
        }

        return h;
    }

    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;

        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return statusBarHeight;
    }

    public static int getNavigationBarrHeight(Context c) {
        Resources resources = c.getResources();
        int identifier = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelOffset(identifier);
    }

    public static String getDensity(Context ctx) {
        String densityStr = null;
        int density = ctx.getResources().getDisplayMetrics().densityDpi;
        switch (density) {
            case 120:
                densityStr = "LDPI";
                break;
            case 160:
                densityStr = "MDPI";
                break;
            case 213:
                densityStr = "TVDPI";
                break;
            case 240:
                densityStr = "HDPI";
                break;
            case 320:
                densityStr = "XHDPI";
                break;
            case 400:
                densityStr = "XMHDPI";
                break;
            case 480:
                densityStr = "XXHDPI";
                break;
            case 640:
                densityStr = "XXXHDPI";
        }

        return densityStr;
    }
}
