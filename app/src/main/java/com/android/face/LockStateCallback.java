package com.android.face;

import android.os.RemoteException;

import com.alibaba.fastjson.JSONObject;
import com.android.face.alarm.AlarmController;
import com.android.internal.lock.IStateCallback;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.lock.LockManager;
import com.android.rgk.common.net.MqttManager;

public class LockStateCallback extends IStateCallback.Stub {
    private AlarmController mAlarmController;

    public LockStateCallback(AlarmController alarmController) {
        mAlarmController = alarmController;
    }

    @Override
    public void onStateChange(int oldState, int newState) throws RemoteException {
        if (oldState == State.STATE_WITH_MAGNET && newState == State.STATE_WITHOUT_MAGNET) {
            uploadDoorSensor(0);
            if (mAlarmController != null) {
                mAlarmController.startTimer();
            }
        } else if (oldState == State.STATE_WITHOUT_MAGNET && newState == State.STATE_WITH_MAGNET) {
            uploadDoorSensor(1);
            if (mAlarmController != null) {
                mAlarmController.stopTimer();
            }
        }
    }

    private void uploadDoorSensor(int sensorStatus) {
        JSONObject jsonObject = new JSONObject();
        String deviceId = LockManager.getInstance().readNvStr(1);
        jsonObject.put("deviceNo", deviceId);
        long time = System.currentTimeMillis();
        jsonObject.put("logTime", time);
        jsonObject.put("sensorStatus", sensorStatus);
        MqttManager.getInstance().uploadDoorSensor(jsonObject.toJSONString());
        DataOperator.getInstance().insertDoorMagnet(time, sensorStatus);
    }

    public class State {
        public static final int STATE_INVALID = 0;
        public static final int STATE_GPIO_LOW = STATE_INVALID + 1;
        public static final int STATE_GPIO_HIGH = STATE_INVALID + 2;
        public static final int STATE_LOCK = STATE_INVALID + 3;
        public static final int STATE_UNLOCK = STATE_INVALID + 4;
        public static final int STATE_WITH_MAGNET = STATE_INVALID + 5;
        public static final int STATE_WITHOUT_MAGNET = STATE_INVALID + 6;
    }
}
