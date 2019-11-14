package com.android.rgk.common.lock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.RemoteException;

import com.alibaba.fastjson.JSONObject;
import com.android.internal.lock.ILockManager;
import com.android.internal.lock.IStateCallback;
import com.android.rgk.common.camera.CameraController;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.db.bean.AccessLog;
import com.android.rgk.common.net.MqttManager;
import com.android.rgk.common.util.BitmapUtil;

public class LockManager {
    public static final int UNLOCK_TYPE_NFC = 0;
    public static final int UNLOCK_TYPE_FACE = 1;
    public static final int UNLOCK_TYPE_BLUETOOTH = 2;
    public static final int UNLOCK_TYPE_PASSWORD = 3;
    public static final int UNLOCK_TYPE_SIP = 4;
    public static final int UNLOCK_TYPE_INNER = 5;
    public static final int UNLOCK_TYPE_APP = 6;

    private ILockManager mLockManager;
    private static LockManager mInstance;

    private static Object lock = new Object();

    private Context mContext;

    @SuppressLint("WrongConstant")
    private LockManager(Context context) {
        mContext = context;
        mLockManager = (ILockManager) context.getSystemService("lock");
    }

    public static void init(Context context) {
        synchronized (lock) {
            if (mInstance == null) {
                mInstance = new LockManager(context);
            }
        }
    }

    public static LockManager getInstance() {
        return mInstance;
    }

    public void setStateCallback(IStateCallback iStateCallback) {
        if (mLockManager == null) {
            return;
        }
        try {
            mLockManager.setStateCallback(iStateCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean unlock(int type, String openDetails) {
        boolean success = setGpioHigh();
        if (!success) {
            return false;
        }
        long time = System.currentTimeMillis();
        int lockStatus = success ? 0 : 1;

        insertAccessLog(type, openDetails, time, lockStatus);

        uploadAccessLog(type, openDetails, time, lockStatus);
        return true;
    }

    private void uploadAccessLog(int type, String openDetails, long time, int lockStatus) {
        JSONObject jsonObject = new JSONObject();
        String device = LockManager.getInstance().readNvStr(1);
        jsonObject.put("deviceNo", device);
        jsonObject.put("openType", type);
        jsonObject.put("openTime", time);
        jsonObject.put("lockStatus", lockStatus);
        jsonObject.put("openResult", 0);
        jsonObject.put("parameters", openDetails);
        String jsonString = jsonObject.toJSONString();
        MqttManager.getInstance().uploadAccessLog(jsonString);
    }

    private void insertAccessLog(int type, String openDetails, long time, int lockStatus) {
        AccessLog accessLog = new AccessLog();
        accessLog.openType = type;
        accessLog.openTime = time;
        accessLog.lockStatus = lockStatus;
        accessLog.openResult = 0;
        accessLog.parameters = openDetails;
        DataOperator.getInstance().insertAccessLog(accessLog);
    }

    public boolean unlockByFace(final String faceId, final String credentialNo, final int similarity,
                                Bitmap bitmap, Rect faceRect) {
        boolean success = setGpioHigh();
        if (!success) {
            return false;
        }
        final long time = System.currentTimeMillis();
        final int lockStatus = success ? 0 : 1;
        Bitmap faceBitmap = BitmapUtil.clipBitmap(bitmap, faceRect);
        String faceCaptureBase64 = BitmapUtil.bitmapToBase64(faceBitmap);
        String fullCaptureBase64 = BitmapUtil.bitmapToBase64(bitmap);
        String openDetail = getJsonString(faceId, faceCaptureBase64,
                fullCaptureBase64, credentialNo, similarity);
        insertAccessLog(UNLOCK_TYPE_FACE, openDetail, time, lockStatus);
        uploadAccessLog(UNLOCK_TYPE_FACE, openDetail, time, lockStatus);
        faceBitmap.recycle();
        faceBitmap = null;
        return true;
    }

    private static String getJsonString(String faceId, String faceCapture, String doorCapture,
                                        String credentialNo, int similarity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("faceId", faceId);
        jsonObject.put("faceCapture", faceCapture);
        jsonObject.put("doorCapture", doorCapture);
        jsonObject.put("credentialNo", credentialNo);
        jsonObject.put("similarity", similarity);
        return jsonObject.toJSONString();
    }

    public boolean unlockBySip(final int callType, final String roomNo) {
        boolean success = setGpioHigh();
        if (!success) {
            return false;
        }
        final long time = System.currentTimeMillis();
        final int lockStatus = success ? 0 : 1;
        CameraController.getInstance().getRgbImageBase64(new CameraController.ImageBase64Callback() {
            @Override
            public void onResult(String fullCaptureBase64, String faceCaptureBase64) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", callType);
                jsonObject.put("roomNo", roomNo);
                jsonObject.put("faceCapture", faceCaptureBase64);
                String openDetails = jsonObject.toJSONString();
                insertAccessLog(UNLOCK_TYPE_SIP, openDetails, time, lockStatus);
                uploadAccessLog(UNLOCK_TYPE_SIP, openDetails, time, lockStatus);
            }
        }, false, true);
        return true;
    }

    public synchronized void setGpioLow() {
        if (mLockManager == null) {
            return;
        }
        try {
            mLockManager.setGpioLow();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean setGpioHigh() {
        if (mLockManager == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = mLockManager.setGpioHigh();
        } catch (RemoteException e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    public synchronized String getGpioValue() {
        if (mLockManager == null) {
            return null;
        }
        try {
            return mLockManager.getGpioValue();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized String getNfcValue() {
        if (mLockManager == null) {
            return null;
        }
        try {
            return mLockManager.getNfcValue();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void setNfcLight(String brightness) {
        if (mLockManager == null) {
            return;
        }
        try {
            mLockManager.setNfcLight(brightness);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public synchronized int writeNvStr(int id, String str) {
        if (mLockManager == null) {
            return -1;
        }
        int ret = -1;
        try {
            ret = mLockManager.writeNvStr(id, str);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public synchronized String readNvStr(int id) {
        if (mLockManager == null) {
            return null;
        }
        String str = null;
        try {
            str = mLockManager.readNvStr(id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return str;
    }

    public synchronized void setLightValue(int val) {
        if (mLockManager == null) {
            return;
        }
        try {
            mLockManager.setLightValue(val);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public synchronized int getLightValue() {
        if (mLockManager == null) {
            return 0;
        }
        int val = 0;
        try {
            val = mLockManager.getLightValue();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return val;
    }

    public synchronized void setTime(long when) {
        if (mLockManager == null) {
            return;
        }
        try {
            mLockManager.setTime(when);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public synchronized String getSdcardPath() {
        if (mLockManager == null) {
            return null;
        }
        String path = null;
        try {
            path = mLockManager.getSdcardPath();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return path;
    }

    public synchronized boolean installPackage(String otaPackagePath) {
        if (mLockManager == null) {
            return false;
        }
        boolean success = false;
        try {
            success = mLockManager.installPackage(otaPackagePath);
        } catch (RemoteException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public synchronized void writeSerialport(byte[] bytes) {
        if (mLockManager == null) {
            return;
        }
        try {
            mLockManager.writeSerialport(bytes);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public byte[] readSerialport() {
        if (mLockManager == null) {
            return null;
        }
        try {
            return mLockManager.readSerialport();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getEthernet() {
        if (mLockManager == null) {
            return "";
        }
        try {
            return mLockManager.getEthernet();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean setEthernet(String str) {
        if (mLockManager == null) {
            return false;
        }
        try {
            return mLockManager.setEthernet(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
