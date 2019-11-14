package com.android.face.alarm;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import com.alibaba.fastjson.JSONObject;
import com.android.rgk.common.lock.LockManager;
import com.android.rgk.common.net.MqttManager;

public class AlarmController {
    private static final int MSG_TIMEOUT = 1;

    private Context mContext;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIMEOUT:
                    startAlarm(3);
                    break;
            }
        }
    };

    public AlarmController(Context context) {
        mContext = context;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F3) {
            startAlarm(1);
            return true;
        }
        return false;
    }

    public void startAlarm(int status) {
        //TODO play ringtone

        //TODO need save to database??
        uploadDeviceEvent(status);
    }

    public void stopAlarm(int status) {
        //TODO stop play ringtone

        //TODO need save to database??
        if (status == 4) {
            uploadDeviceEvent(status);
        }
    }

    public void startTimer() {
        mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, 60000);
    }

    public void stopTimer() {
        mHandler.removeMessages(MSG_TIMEOUT);
    }

    private void uploadDeviceEvent(int status) {
        JSONObject jsonObject = new JSONObject();
        String deviceId = LockManager.getInstance().readNvStr(1);
        jsonObject.put("deviceNo", deviceId);
        long time = System.currentTimeMillis();
        jsonObject.put("time", time);
        jsonObject.put("status", status);
        MqttManager.getInstance().uploadDeviceEvent(jsonObject.toJSONString());
    }
}
