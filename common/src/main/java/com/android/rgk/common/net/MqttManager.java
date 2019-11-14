package com.android.rgk.common.net;

import android.os.IBinder;
import android.os.RemoteException;

import com.android.client.mqtt.IClientCallback;
import com.android.rgk.common.util.LogUtil;
import com.android.server.mqtt.IMqttService;

import java.lang.reflect.Method;
import java.util.List;

public class MqttManager {
    private static final String TAG = "MqttManager";

    private IMqttService mMqttService;
    private static MqttManager mInstance;

    public static MqttManager getInstance() {
        if (mInstance == null) {
            mInstance = new MqttManager();
        }
        return mInstance;
    }

    private MqttManager() {
        initMqttService();
    }

    public void uploadAccessLog(String content) {
        publish(MqttTopics.UPLOAD_ACCESS_LOG, content);
    }

    public void uploadDoorSensor(String content) {
        publish(MqttTopics.UPLOAD_DOOR_SENSOR, content);
    }

    public void uploadDeviceInfo(String content) {
        publish(MqttTopics.UPLOAD_DEVICE_INFO, content);
    }

    public void uploadDeviceEvent(String content) {
        publish(MqttTopics.UPLOAD_DEVICE_EVENT, content);
    }

    public void filterRequest(String content) {
        publish(MqttTopics.FILTER_REQUEST, content);
    }

    public void response(String content) {
        publish(MqttTopics.RESPONSE, content);
    }

    public void uploadDeviceInfoRequest(String content) {
        publish(MqttTopics.UPLOAD_DEVICE_INFO_REQUEST, content);
    }

    public void sipRequest(String content) {
        publish(MqttTopics.SIP_REQUEST, content);
    }

    public void keepAlive(String content) {
        publish(MqttTopics.KEEP_ALIVE, content);
    }

    public void faceRequest(String content) {
        publish(MqttTopics.FACE_REQUEST, content);
    }

    public void captureUpload(String content) {
        publish(MqttTopics.CAPTURE_UPLOAD, content);
    }

    public void readerUpgradeResultUpload(String content) {
        publish(MqttTopics.READER_UPGRADE_RESULT_UPLOAD, content);
    }

    private IMqttService initMqttService() {
        if (mMqttService != null) {
            return mMqttService;
        }

        try {
            Class clzServiceManager = Class.forName("android.os.ServiceManager");
            Method clzServiceManager$getService = clzServiceManager
                    .getDeclaredMethod("getService", String.class);
            Object oRemoteService = clzServiceManager$getService
                    .invoke(null, "mqtt_service");
            IBinder iBinder = (IBinder) oRemoteService;

            mMqttService = IMqttService.Stub.asInterface(iBinder);
        } catch (Exception e) {
            e.printStackTrace();
            mMqttService = null;
            LogUtil.i(TAG, "call forbroone error:" + LogUtil.getStackTraceString(e));
        }

        return mMqttService;
    }

    public static void destroy() {
        mInstance.onDestroy();
        mInstance = null;
    }

    private void onDestroy() {
        mMqttService = null;
    }

    public void publish(String topic, String content) {
        if (mMqttService == null) {
            return;
        }
        try {
            mMqttService.publish(topic, content);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void subscribeTopic(String topic, IClientCallback client) {
        if (mMqttService == null) {
            return;
        }
        try {
            mMqttService.subscribeTopic(topic, client);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void subscribeTopics(List<String> topics, IClientCallback client) {
        if (mMqttService == null) {
            return;
        }
        try {
            mMqttService.subscribeTopics(topics, client);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void config(String host, String username, String password) {
        if (mMqttService == null) {
            return;
        }
        try {
            mMqttService.config(host, username, password);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return mMqttService.isConnected();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
