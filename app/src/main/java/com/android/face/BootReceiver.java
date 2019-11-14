package com.android.face;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.rgk.common.util.LogUtil;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.i(TAG, "onReceive: " + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            startFaceRecognizeActivity(context);
        }
    }

    private void startFaceRecognizeActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
