package com.android.face;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.android.face.bluetooth.BleService;
import com.android.face.linphone.LinphonePreferences;
import com.android.face.linphone.LinphoneService;
import com.android.face.linphone.manager.BluetoothManager;
import com.android.face.ota.OtaManager;
import com.android.face.permission.RequestPermissionsActivity;
import com.android.rgk.common.db.DataOperator;

import static android.content.Intent.ACTION_MAIN;

public class MainActivity extends BaseActivity {
    private boolean permission = false;
    private Handler mHandler;

    private ServiceWaitThread mThread;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            return;
        }
        permission = true;

        DataOperator.init(getApplicationContext());
        startActivity(new Intent(this, CameraActivity.class));
        OtaManager.init(getApplicationContext());
        mHandler = new Handler();
        if (LinphoneService.isReady()) {
            onServiceReady();
        } else {
            startService(new Intent(ACTION_MAIN).setClass(FaceApplication.getInstance(),
                    LinphoneService.class));
            mThread = new ServiceWaitThread();
            mThread.start();
        }

        // startService(new Intent().setClass(FaceApplication.getInstance(), BleService.class));
    }


    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!LinphoneService.isReady()) {
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onServiceReady();
                }
            });
            mThread = null;
        }
    }

    protected void onServiceReady() {
        BluetoothManager.getInstance().initBluetooth();

        if (LinphonePreferences.instance().isFirstLaunch()) {

            // add by David for open video setting default
            LinphonePreferences.instance().setInitiateVideoCall(true);
            LinphonePreferences.instance().setAutomaticallyAcceptVideoRequests(true);
            LinphonePreferences.instance().setVideoPreset("custom");
            LinphonePreferences.instance().setPreferredVideoFps(30);
            // add end

            LinphonePreferences.instance().firstLaunchSuccessful();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (permission) {
            DataOperator.destroy();
        }
        OtaManager.destroy();
    }
}
