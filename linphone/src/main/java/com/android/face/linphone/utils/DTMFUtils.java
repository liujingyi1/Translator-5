package com.android.face.linphone.utils;

import android.os.Handler;

import com.android.face.linphone.manager.LinphoneManager;
import com.android.rgk.common.lock.LockManager;

public class DTMFUtils {
    private static final int DTMF_1 = 49;
    private static final int DTMF_2 = 50;
    private static final int DTMF_3 = 51;
    private static final int DTMF_4 = 52;
    private static final int DTMF_5 = 53;
    private static final int DTMF_6 = 54;
    private static final int DTMF_7 = 55;
    private static final int DTMF_8 = 56;
    private static final int DTMF_9 = 57;
    private static final int DTMF_STAR = 42;
    private static final int DTMF_0 = 48;
    private static final int DTMF_WELL = 35;

    private static final int CALL_TYPE_TOAST = 0;
    private static final int CALL_TYPE_VIDEO = 1;
    private static final int CALL_TYPE_SIP = 2;
    private static final int CALL_TYPE_PSTN = 3;

    public static void handleDTMFCode(String room, int dtmf) {
        //TODO David need add logic here for dtmf
        if (DTMF_WELL == dtmf) {
            LockManager.getInstance().unlockBySip(CALL_TYPE_SIP, room);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LinphoneManager.getLc().terminateCall(LinphoneManager.getLc().getCurrentCall());
                }
            }, 5000);

        }
    }
}
