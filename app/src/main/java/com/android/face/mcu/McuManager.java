package com.android.face.mcu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.face.FaceApplication;
import com.android.face.SharedPreferencesHelper;
import com.android.face.domain.Device;
import com.android.face.domain.ServerApi;
import com.android.face.linphone.utils.LinphoneLogInUtils;
import com.android.face.util.SoundEffectsUtil;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.db.bean.CardInfo;
import com.android.rgk.common.lock.LockManager;
import com.android.rgk.common.net.MqttManager;
import com.android.rgk.common.util.LogUtil;
import com.android.rgk.common.util.NetUtil;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class McuManager {
    private static final String TAG = "McuManager";
    private static McuManager mInstance;
    private static final int MSG_FEEDBACK = 1;
    private static final int MSG_UPDATETIME = 2;
    private static final int MSG_UPDATECARD = 3;
    private static final int MSG_REGISTERABLE = 4;
    private static final int MSG_SETPARAMETER = 5;
    private static final int MSG_GETPARAMETER = 6;

    private static final String KEY_0X13 = "mcu_0x13";
    private static final String KEY_0X14 = "mcu_0x14";
    private static final String KEY_0X16 = "mcu_0x16";
    private static final String KEY_0X17 = "mcu_0x17";
    private static final String KEY_REGISTERED = "mcu_registered";
    private static final String KEY_VERSION = "mcu_version";

    private static final String KEY_0X13_UPDATE = "mcu_0x13_update";
    private static final String KEY_0X14_UPDATE = "mcu_0x14_update";
    private static final String KEY_0X16_UPDATE = "mcu_0x16_update";
    private static final String KEY_0X17_UPDATE = "mcu_0x17_update";

    public static final String KEY_FILTER_TIMESTAMP = "mcu_filter_timestamp";
    public static final String KEY_SIP_TIMESTAMP = "mcu_sip_timestamp";
    public static final String KEY_FACE_TIMESTAMP = "mcu_face_timestamp";

    private ReadThread mReadThread;
    private static String mBleMac;
    private HandlerThread mWriteThread;
    private Handler mHandler;
    private List<CardInfo> mCardList;
    private byte mCardOrder;
    private NetBroadcastReceiver netBroadcastReceiver;

    public static McuManager getInstance() {
        if (mInstance == null) {
            mInstance = new McuManager();
        }
        return mInstance;
    }

    public void onCreate() {
        mCardList = new ArrayList<>();
        mReadThread = new ReadThread();
        mReadThread.start();
        mWriteThread = new HandlerThread("write-thread");
        mWriteThread.start();

        mHandler = new Handler(mWriteThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    switch (msg.what) {
                        case MSG_FEEDBACK:
                            sendData(null, (byte) msg.arg1, (byte) msg.arg2);
                            break;
                        case MSG_UPDATETIME:
                            updateTime();
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATETIME), 60 * 1000);
                            break;
                        case MSG_UPDATECARD:
                            if (mCardList.size() > 0) {
                                updateCard(mCardList.get(0));
                                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATECARD), 3000);
                            }
                            break;
                        case MSG_REGISTERABLE: {
                            JSONObject js = new JSONObject();
                            boolean b = SharedPreferencesHelper.getBoolean(KEY_REGISTERED, false);
                            js.put("status", 0);
                            js.put("code", b ? "9" : "10");
                            sendData(js, (byte) msg.arg1, (byte) msg.arg2);
                            break;
                        }
                        case MSG_SETPARAMETER: {
                            sendData(null, (byte) msg.arg1, (byte) msg.arg2);
                            String jsString = SharedPreferencesHelper.getString(KEY_0X16, null);
                            if (jsString != null) {
                                JSONObject js = JSON.parseObject(jsString);
                                MqttManager.getInstance().config(js.getString("mqttUrl"), "shhx", "7ed98d7018caf009c60008521274ceb4");
                                ServerApi.getInstance().setBaseUrl(js.getString("httpUrl"));
                                if (js.getInteger("isDHCP") != null) {
                                    JSONObject ipJs = new JSONObject();
                                    ipJs.put("isDHCP", js.getIntValue("isDHCP"));
                                    String s = js.getString("ip");
                                    ipJs.put("ip", s == null ? "" : s);
                                    s = js.getString("mask");
                                    ipJs.put("mask", s == null ? "" : s);
                                    s = js.getString("gateway");
                                    ipJs.put("gateway", s == null ? "" : s);
                                    s = js.getString("DNS");
                                    ipJs.put("DNS", s == null ? "" : s);
                                    LockManager.getInstance().setEthernet(ipJs.toString());
                                }

                                String domain = js.getString("sipUrl");
                                String username = js.getString("sipAccount");
                                String password = js.getString("sipPwd");
                                if (domain != null && username != null && password != null) {
                                    LinphoneLogInUtils.genericLogIn(username, password, null, domain);
                                }
                            }
                            break;
                        }
                        case MSG_GETPARAMETER: {
                            String jsString = SharedPreferencesHelper.getString(KEY_0X16, null);
                            JSONObject js = null;
                            if (jsString != null) {
                                js = JSON.parseObject(jsString);
                            }
                            sendData(js, (byte) msg.arg1, (byte) msg.arg2);
                            break;
                        }
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATETIME));
        mBleMac = LockManager.getInstance().readNvStr(4);
        initCard();

        netBroadcastReceiver = new NetBroadcastReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        FaceApplication.getInstance().getApplicationContext().registerReceiver(netBroadcastReceiver, filter);
    }

    class NetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            boolean hasNetwork = NetUtil.hasConnect(context);
            LogUtil.i(TAG, "NetBroadcastReceiver onReceive=" + intent.getAction());
            LogUtil.i(TAG, "NetBroadcastReceiver hasNetwork=" + hasNetwork);

            if (hasNetwork) {
                final boolean isUpdate13 = SharedPreferencesHelper.getBoolean(KEY_0X13_UPDATE, false);
                final boolean isUpdate14 = SharedPreferencesHelper.getBoolean(KEY_0X14_UPDATE, false);
                final boolean isUpdate16 = SharedPreferencesHelper.getBoolean(KEY_0X16_UPDATE, false);

                final boolean isRegister = SharedPreferencesHelper.getBoolean(KEY_REGISTERED, false);

                LogUtil.i(TAG, "NetBroadcastReceiver isRegister=" + isRegister + " isUpdate13=" + isUpdate13 + " isUpdate14=" + isUpdate14 + " isUpdate16" + isUpdate16);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("deviceNo", mBleMac);
                MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
                RequestBody body = RequestBody.create(mediaType, jsonObject.toJSONString());
                if (isRegister) {
                    if (isUpdate13 || isUpdate14 || isUpdate16) {
                        ServerApi.getInstance().canRegister(body, new ServerApi.ServerCallback() {
                            @Override
                            public void call(String msg, long code, Object result) {
                                if (code == 9) {
                                    update();
                                } else if (code == 10) {
                                    register();
                                }
                            }
                        });
                    }
                } else {
                    ServerApi.getInstance().canRegister(body, new ServerApi.ServerCallback() {
                        @Override
                        public void call(String msg, long code, Object result) {
                            if (code == 9) {
                                delete();
                            }
                        }
                    });
                }
            }
        }
    }

    public void onDestroy() {
        mReadThread.interrupt();
        mHandler = null;
        mWriteThread.quit();
        mWriteThread = null;
        mReadThread = null;
        mCardList = null;
        FaceApplication.getInstance().getApplicationContext().unregisterReceiver(netBroadcastReceiver);
    }

    public synchronized void addCardInfo(CardInfo ci) {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_UPDATECARD);
            mCardList.add(ci);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATECARD), 50);
        }
    }

    private class ReadThread extends Thread {
        private int mLen, mSize;
        private byte[] mReadBytes = new byte[1024];
        private byte[] mNextBytes;
        private byte[] mShortHeadBytes;

        @Override
        public void run() {
            super.run();
            while (!interrupted()) {
                try {
                    byte[] readBytes = mNextBytes == null ? LockManager.getInstance().readSerialport() : mNextBytes;
                    mNextBytes = null;
                    if (readBytes != null) {
                        if (mShortHeadBytes != null) {
                            byte[] newBytes = new byte[mShortHeadBytes.length + readBytes.length];
                            System.arraycopy(mShortHeadBytes, 0, newBytes, 0, mShortHeadBytes.length);
                            System.arraycopy(readBytes, 0, newBytes, mShortHeadBytes.length, readBytes.length);
                            readBytes = newBytes;
                            mShortHeadBytes = null;
                        }
                        if (mLen == 0 && readBytes.length > 0 && readBytes.length < 4 && readBytes[0] == (byte) 0xFF) {
                            mShortHeadBytes = readBytes;
                            continue;
                        }
                        if (readBytes.length >= 4 && ((readBytes[0] & 0xFF) << 8) + (readBytes[1] & 0xFF) == 0xFFFF) {
                            if (mLen > 0) {
                                LogUtil.e(TAG, "abandon A:" + mLen + ":" + ToHexString(mReadBytes, mLen));
                            }
                            mLen = 0;
                            mSize = ((readBytes[2] & 0xFF) << 8) + (readBytes[3] & 0xFF) + 2;
                        }
                        if (mSize == 0) {
                            LogUtil.e(TAG, "abandon B:" + readBytes.length + ":" + ToHexString(readBytes, readBytes.length));
                            continue;
                        }
                        for (byte readByte : readBytes) {
                            mReadBytes[mLen++] = readByte;
                            if (mLen >= 1024) {
                                LogUtil.e(TAG, "abandon C:" + mLen + ":" + ToHexString(mReadBytes, mLen));
                                mLen = 0;
                                mSize = 0;
                                break;
                            }
                        }
                        if (mLen > mSize) {
                            mNextBytes = new byte[mLen - mSize];
                            System.arraycopy(mReadBytes, mSize, mNextBytes, 0, mNextBytes.length);
                            mLen = mSize;
                        }
                        if (mLen == mSize && mLen > 0) {
                            LogUtil.d(TAG, "<<-- size:" + mLen + "," + ToHexString(mReadBytes, mLen));
                            LogUtil.d(TAG, "<<-- type:0x" + String.format("%02x", mReadBytes[4] & 0xff)
                                    .toUpperCase() + getJsonString(mReadBytes, mLen));
                            onDataReceived(mReadBytes, mLen);
                            mLen = 0;
                            mSize = 0;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onDataReceived(byte[] bytes, int len) {
        int crc16 = crc16Check(bytes, len - 2);
        if (((bytes[len - 2] & 0xFF) << 8) + (bytes[len - 1] & 0xFF) != crc16) {
            LogUtil.e(TAG, "crc16Check fail:0x" + String.format("%04x", crc16));
            return;
        }
        JSONObject js = getJSONObject(bytes, len);
        switch (bytes[4]) {
            case 0x01:
                assert js != null;
                long time = js.getLong("openTime");
                if(time > 0){
                    js.put("openTime",time*1000);
                }
                MqttManager.getInstance().uploadAccessLog(js.toString());
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FEEDBACK, bytes[4], bytes[5]));
                Integer type = js.getInteger("openType");
                if (type != null && type == 0) {
                    LockManager.getInstance().setNfcLight("255");
                    Integer result = js.getInteger("openResult");
                    if (result != null && result == 1) {
                        SoundEffectsUtil.play(SoundEffectsUtil.INVALID_CARD_ID);
                    }
                }
                break;
            case 0x02:
                assert js != null;
                MqttManager.getInstance().uploadDeviceEvent(js.toString());
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FEEDBACK, bytes[4], bytes[5]));
                break;
            case 0x05:
                assert js != null;
                Integer integer = js.getInteger("status");
                if (integer != null && integer == 0 && mCardOrder == bytes[5]) {
                    deleteTopCardInfo();
                }
                break;
            case 0x06:
                assert js != null;
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FEEDBACK, bytes[4], bytes[5]));
                break;
            case 0x08://upload MAC address
                assert js != null;
                mBleMac = js.getString("bleMac");
                LockManager.getInstance().writeNvStr(1, mBleMac);
                LockManager.getInstance().writeNvStr(4, mBleMac);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FEEDBACK, bytes[4], bytes[5]));
                break;
            case 0x10:
                SharedPreferencesHelper.setPreference(KEY_REGISTERED, true);
                register();
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FEEDBACK, bytes[4], bytes[5]));
                break;
            case 0x11:
                delete();
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FEEDBACK, bytes[4], bytes[5]));
                break;
            case 0x12:
                SoundEffectsUtil.play(SoundEffectsUtil.UNLOCK_ID);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FEEDBACK, bytes[4], bytes[5]));
                break;
            case 0x13:
                assert js != null;
                SharedPreferencesHelper.setPreference(KEY_0X13, js.toString());
                SharedPreferencesHelper.setPreference(KEY_0X13_UPDATE, true);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FEEDBACK, bytes[4], bytes[5]));
                break;
            case 0x14:
                assert js != null;
                SharedPreferencesHelper.setPreference(KEY_0X14, js.toString());
                SharedPreferencesHelper.setPreference(KEY_0X14_UPDATE, true);
                checkUpdate(0x14);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FEEDBACK, bytes[4], bytes[5]));
                break;
            case 0x15:
                mHandler.sendMessage(mHandler.obtainMessage(MSG_REGISTERABLE, bytes[4], bytes[5]));
                break;
            case 0x16:
                assert js != null;
                SharedPreferencesHelper.setPreference(KEY_0X16, js.toString());
                SharedPreferencesHelper.setPreference(KEY_0X16_UPDATE, true);
                checkUpdate(0x16);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SETPARAMETER, bytes[4], bytes[5]));
                break;
            case 0x17:
                assert js != null;
                SharedPreferencesHelper.setPreference(KEY_0X17, js.toString());
                SharedPreferencesHelper.setPreference(KEY_0X17_UPDATE, true);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FEEDBACK, bytes[4], bytes[5]));
                break;
            case 0x18:
                mHandler.sendMessage(mHandler.obtainMessage(MSG_GETPARAMETER, bytes[4], bytes[5]));
                break;
            case 0x24:
                assert js != null;
                SharedPreferencesHelper.setPreference(KEY_VERSION, js.getString("version"));
                break;
            default:
                break;
        }
    }

    private void checkUpdate(int commend) {
        boolean is16Update = SharedPreferencesHelper.getBoolean(KEY_0X16_UPDATE, false);
        boolean is14Update = SharedPreferencesHelper.getBoolean(KEY_0X14_UPDATE, false);
        boolean isUpdate = is14Update && is16Update;

        boolean isRegister = SharedPreferencesHelper.getBoolean(KEY_REGISTERED, false);
        if (isRegister && isUpdate) {
            update();
        }
    }

    private void register() {
        Device device = new Device(getJson(KEY_0X16), getJson(KEY_0X14), getJson(KEY_0X13));
        //LogUtil.e(TAG, "device:"+device);
        ServerApi.getInstance().registerDevice(device, new ServerApi.ServerCallback() {
            @Override
            public void call(String msg, long code, Object result) {
                LogUtil.i(TAG, "register msg:" + msg);
                LogUtil.i(TAG, "register code:" + code);
                LogUtil.i(TAG, "register result:" + result);
                if (code == 0) {
                    SharedPreferencesHelper.setPreference(KEY_0X13_UPDATE, false);
                    SharedPreferencesHelper.setPreference(KEY_0X14_UPDATE, false);
                    SharedPreferencesHelper.setPreference(KEY_0X16_UPDATE, false);
                }
            }
        });
    }

    private void update() {
        Device device = new Device(getJson(KEY_0X16), getJson(KEY_0X14), getJson(KEY_0X13));
        //LogUtil.e(TAG, "device:"+device);
        ServerApi.getInstance().updateDevice(device, new ServerApi.ServerCallback() {
            @Override
            public void call(String msg, long code, Object result) {
                LogUtil.i(TAG, "update msg:" + msg);
                LogUtil.i(TAG, "update code:" + code);
                LogUtil.i(TAG, "update result:" + result);
                if (code == 0) {
                    SharedPreferencesHelper.setPreference(KEY_0X13_UPDATE, false);
                    SharedPreferencesHelper.setPreference(KEY_0X14_UPDATE, false);
                    SharedPreferencesHelper.setPreference(KEY_0X16_UPDATE, false);
                }
            }
        });
    }

    private void delete() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("deviceNo", mBleMac);
        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toJSONString());
        ServerApi.getInstance().deleteDevice(body, new ServerApi.ServerCallback() {
            @Override
            public void call(String msg, long code, Object result) {
                LogUtil.i(TAG, "delete msg:" + msg);
                LogUtil.i(TAG, "delete code:" + code);
                LogUtil.i(TAG, "delete result:" + result);
            }
        });
        SharedPreferencesHelper.clear();
        resetMqtt();
        resetSip();
        deleteDB();
    }

    private void deleteDB() {
    }

    private void resetSip() {
    }

    private void resetMqtt() {
    }

    private int crc16Check(byte[] data, int len) {
        char crc = 0xffff;
        char polynomial = 0xa001;
        for (int i = 0; i < len; i++) {
            crc ^= data[i] & 0xff;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= polynomial;
                } else {
                    crc >>= 1;
                }
            }
        }
        return crc & 0xffff;
    }

    private String getJsonString(byte[] bytes, int len) {
        if (len <= 8) {
            return null;
        }
        byte[] jsonBytes = new byte[len - 8];
        System.arraycopy(bytes, 6, jsonBytes, 0, jsonBytes.length);
        return new String(jsonBytes);
    }

    private JSONObject getJSONObject(byte[] bytes, int len) {
        String s = getJsonString(bytes, len);
        if (s == null) {
            return null;
        }
        return JSON.parseObject(s);
    }

    private void sendData(JSONObject data, byte type, byte order) {
        byte[] dataBytes = (data == null) ? "".getBytes() : data.toString().getBytes();
        final byte[] bytes = new byte[dataBytes.length + 8];
        bytes[0] = (byte) 0xFF;
        bytes[1] = (byte) 0xFF;
        bytes[2] = (byte) ((dataBytes.length + 6 >> 8) & 0xFF);
        bytes[3] = (byte) ((dataBytes.length + 6) & 0xFF);
        bytes[4] = type;
        bytes[5] = order;
        System.arraycopy(dataBytes, 0, bytes, 6, dataBytes.length);
        int crc16 = crc16Check(bytes, dataBytes.length + 6);
        bytes[6 + dataBytes.length] = (byte) (crc16 >> 8 & 0xff);
        bytes[6 + dataBytes.length + 1] = (byte) (crc16 & 0xff);
        LockManager.getInstance().writeSerialport(bytes);
        LogUtil.d(TAG, "-->> size:" + bytes.length + ":" + ToHexString(bytes, bytes.length));
        LogUtil.d(TAG, "-->> type:0x" + String.format("%02x", bytes[4] & 0xff)
                .toUpperCase() + getJsonString(bytes, bytes.length));
    }

    private String ToHexString(byte[] bytes, int len) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < len; i++) {
            buffer.append(String.format("%02x", bytes[i] & 0xff).toUpperCase());
        }
        return buffer.toString();
    }

    private void updateTime() {
        JSONObject js = new JSONObject();
        js.put("timeStamp", System.currentTimeMillis() / 1000);
        sendData(js, (byte) 0x07, (byte) 0);
    }

    private synchronized void updateCard(CardInfo ci) {
        JSONObject js = new JSONObject();
        js.put("cardNo", ci.cardNo);
        js.put("timeStamp", ci.timeStamp);
        js.put("cardType", ci.cardType);
        js.put("endTime", ci.endTime);
        js.put("filterType", ci.filterType);
        sendData(js, (byte) 0x05, mCardOrder);
    }

    private void initCard() {
        List<CardInfo> list = DataOperator.getInstance().getAllCards();
        for (int i = 0; i < list.size(); i++) {
            addCardInfo(list.get(i));
        }
        //add while names for test
        String[] whiteList = {"3C874A188F2102E0", "6C1D4A188F2102E0", "6D45C1228C2102E0",
                "5B4D0C0A8F2102E0", "B7D9737D00000000"};
        for (String name : whiteList) {
            CardInfo ci = new CardInfo();
            ci.cardNo = name;
            ci.timeStamp = System.currentTimeMillis() / 1000;
            ci.cardType = 3;
            ci.endTime = System.currentTimeMillis() / 1000 + 7 * 24 * 60 * 60;
            ci.filterType = 2;
            addCardInfo(ci);
        }
    }

    private synchronized void deleteTopCardInfo() {
        mHandler.removeMessages(MSG_UPDATECARD);
        if (mCardList.size() > 0) {
            mCardOrder++;
            mCardList.remove(0);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATECARD));
        }
    }

    public void doMasterClear() {
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
        intent.putExtra("android.intent.extra.WIPE_EXTERNAL_STORAGE", false);
        FaceApplication.getInstance().getApplicationContext().sendBroadcast(intent);
    }

    private static JSONObject getJson(String key) {
        String jsStr = SharedPreferencesHelper.getString(key, "");
        JSONObject js = JSON.parseObject(jsStr);
        if (js != null) {
            return js;
        }
        return new JSONObject();
    }

    public static String getHttpUrl(String def) {
        JSONObject js = getJson(KEY_0X16);
        String s = js.getString("httpUrl");
        if (s != null) {
            return s;
        }
        return def;
    }

    public static String getMqttUrl(String def) {
        JSONObject js = getJson(KEY_0X16);
        String s = js.getString("mqttUrl");
        if (s != null) {
            return s;
        }
        return def;
    }

    public static String get0X16String(String def) {
        return SharedPreferencesHelper.getString(KEY_0X16, "");
    }

    public static void webUpdate0X16(String data) {
        SharedPreferencesHelper.setPreference(KEY_0X16, data);
        if (data != null) {
            JSONObject js = JSON.parseObject(data);
            MqttManager.getInstance().config(js.getString("mqttUrl"), "shhx", "7ed98d7018caf009c60008521274ceb4");
            ServerApi.getInstance().setBaseUrl(js.getString("httpUrl"));
            String domain = js.getString("sipUrl");
            String username = js.getString("sipAccount");
            String password = js.getString("sipPwd");
            if (domain != null && username != null && password != null) {
                LinphoneLogInUtils.genericLogIn(username, password, null, domain);
            }
        }
    }

    public String getDevcieMac() {
        return mBleMac;
    }

    public static String getCenterSip(String def) {
        JSONObject js = getJson(KEY_0X16);
        String s = js.getString("centerSip");
        if (s != null && !s.isEmpty()) {
            return s;
        }
        return def;
    }

    public static String getDoorOpenPassword(String def) {
        JSONObject js = getJson(KEY_0X14);
        String s = js.getString("doorOpenPassword");
        if (s != null) {
            return s;
        }
        return def;
    }

    public static String getBluetoothPassword(String def) {
        JSONObject js = getJson(KEY_0X14);
        String s = js.getString("bluetoothPassword");
        if (s != null) {
            return s;
        }
        return def;
    }

    public static void setBluetoothPassword(String psw) {
        JSONObject js = getJson(KEY_0X14);
        js.put("bluetoothPassword", psw);
        SharedPreferencesHelper.setPreference(KEY_0X14, js.toString());

        js = new JSONObject();
        js.put("bluetoothPassword", psw);
        getInstance().sendData(js, (byte) 0x23, (byte) 0);
    }

    public static String getVersion() {
        return SharedPreferencesHelper.getString(KEY_VERSION, "0");
    }

    public static boolean setEthernet(int isDHCP, String ip, String mask, String gateway, String DNS) {
        JSONObject js = getJson(KEY_0X16);
        js.put("isDHCP", isDHCP);
        if (isDHCP == 0) {
            js.put("ip", ip);
            js.put("mask", mask);
            js.put("gateway", gateway);
            js.put("DNS", DNS);
        }
        SharedPreferencesHelper.setPreference(KEY_0X16, js.toString());

        js = new JSONObject();
        js.put("isDHCP", isDHCP);
        if (isDHCP == 0) {
            js.put("ip", ip);
            js.put("mask", mask);
            js.put("gateway", gateway);
            js.put("DNS", DNS);
        }
        return LockManager.getInstance().setEthernet(js.toString());
    }

    public static void saveLastTimeStamp(String keyType, long timeStamp){
        if(timeStamp > SharedPreferencesHelper.getLong(keyType,1)){
            SharedPreferencesHelper.setPreference(keyType,timeStamp);
        }
    }

    public static void request(){
        JSONObject js = new JSONObject();
        js.put("deviceNo",mBleMac);
        js.put("timeStamp",SharedPreferencesHelper.getLong(KEY_FILTER_TIMESTAMP,1));
        MqttManager.getInstance().filterRequest(js.toString());

        js = new JSONObject();
        js.put("deviceNo",mBleMac);
        js.put("timeStamp",SharedPreferencesHelper.getLong(KEY_SIP_TIMESTAMP,1));
        MqttManager.getInstance().sipRequest(js.toString());

        js = new JSONObject();
        js.put("deviceNo",mBleMac);
        js.put("timeStamp",SharedPreferencesHelper.getLong(KEY_FACE_TIMESTAMP,1));
        MqttManager.getInstance().faceRequest(js.toString());
    }
}
