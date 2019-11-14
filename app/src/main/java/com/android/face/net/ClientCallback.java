package com.android.face.net;

import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.client.mqtt.IClientCallback;
import com.android.face.faceapi.readsense.YMFaceTrackManager;
import com.android.face.linphone.utils.LinphoneLogInUtils;
import com.android.face.mcu.McuManager;
import com.android.face.ota.OtaManager;
import com.android.face.ota.utils.Constants;
import com.android.face.ota.utils.UpdateInfo;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.db.bean.CardInfo;
import com.android.rgk.common.db.bean.User;
import com.android.rgk.common.lock.LockManager;
import com.android.rgk.common.net.MqttManager;
import com.android.rgk.common.net.MqttTopics;
import com.android.rgk.common.util.FileUtil;
import com.android.rgk.common.util.LogUtil;

import java.util.List;

public class ClientCallback extends IClientCallback.Stub {
    private static final String TAG = "ClientCallback";

    private static final String TYPE_0 = "0";
    private static final String TYPE_1 = "1";

    private static final int RESPONSE_CODE_SUCCESS = 0;
    private static final int RESPONSE_CODE_FAIL = 1;

    private MqttManager mqttManager;

    public ClientCallback() {
        mqttManager = MqttManager.getInstance();
    }

    @Override
    public void dispatch(String topic, String content) throws RemoteException {
        LogUtil.i(TAG, "dispatch() topic=" + topic + ",content=" + content);
        if (content == null || content.length() == 0) {
            return;
        }
        switch (topic) {
            case "/mqtt/connected/": {
                McuManager.request();
                break;
            }
            case MqttTopics.DISPATCH_FILTER_ITEM: {
                JSONObject jsonObject = JSON.parseObject(content);
                updateCard(jsonObject);
                response(jsonObject.getString("seqNo"), RESPONSE_CODE_SUCCESS);
                break;
            }

            case MqttTopics.DISPATCH_FILTER_LIST: {
                JSONObject jsonObject = JSON.parseObject(content);
                JSONArray jsonArray = jsonObject.getJSONArray("filterItem");
                int size = jsonArray.size();
                JSONObject jsonObject1;
                for (int i = 0; i < size; i++) {
                    jsonObject1 = jsonArray.getJSONObject(i);
                    updateCard(jsonObject1);
                }
                break;
            }

            case MqttTopics.SIP_ITEM: {
                DataOperator dataOperator = DataOperator.getInstance();
                JSONObject jsonObject = JSON.parseObject(content);
                String roomNo = jsonObject.getString("roomNo");
                String phone = jsonObject.getString("phone");
                String type = jsonObject.getString("type");
                dataOperator.deleteByRoom(roomNo);
                if (TYPE_0.equals(type)) {
                    String[] number = phone.split(";");
                    for (int i = 0; i < number.length; i++) {
                        dataOperator.insertNumber(roomNo, number[i], i);
                    }
                }
                response(jsonObject.getString("seqNo"), RESPONSE_CODE_SUCCESS);
                break;
            }

            case MqttTopics.SIP_ITEM_LIST: {
                JSONObject jsonObject = JSON.parseObject(content);
                JSONArray jsonArray = jsonObject.getJSONArray("sipItem");
                int size = jsonArray.size();
                DataOperator dataOperator = DataOperator.getInstance();
                JSONObject jsonObject1;
                for (int i = 0; i < size; i++) {
                    jsonObject1 = jsonArray.getJSONObject(i);
                    String roomNo = jsonObject1.getString("roomNo");
                    String phone = jsonObject1.getString("phone");
                    String type = jsonObject1.getString("type");
                    dataOperator.deleteByRoom(roomNo);
                    if (TYPE_0.equals(type)) {
                        String[] number = phone.split(";");
                        for (int j = 0; j < number.length; j++) {
                            dataOperator.insertNumber(roomNo, number[j], j);
                        }
                    }
                }
                break;
            }

            case MqttTopics.SIP_CONFIG_INFO: {
                JSONObject jsonObject = JSON.parseObject(content);
                String domain = jsonObject.getString("domain");
                String user = jsonObject.getString("user");
                String password = jsonObject.getString("password");
                String outbund = jsonObject.getString("outbund");
                LinphoneLogInUtils.genericLogIn(user, password, null, domain);
                break;
            }

            case MqttTopics.DISPATCH_OTP: {
                DataOperator dataOperator = DataOperator.getInstance();
                JSONObject jsonObject = JSON.parseObject(content);
                String password = jsonObject.getString("password");
                long startTime = jsonObject.getLongValue("startTime");
                long expireDate = jsonObject.getLongValue("expireDate");
                String isOTP = jsonObject.getString("isOTP");
                dataOperator.deleteOverduePassword(System.currentTimeMillis());
                dataOperator.insertPassword(password, startTime, expireDate);
                break;
            }

            case MqttTopics.UPGRADE_REQUEST: {
                JSONObject obj = JSON.parseObject(content);
                String url = obj.getString(Constants.OTA_DOWNLOAD_URL);
                String md5 = obj.getString(Constants.OTA_MD5);
                String fileType = obj.getString(Constants.OTA_FILE_TYPE);
                String version = obj.getString(Constants.OTA_VERSION);
                String fileId = obj.getString(Constants.OTA_FILE_ID);
                String crc = obj.getString(Constants.OTA_CRC);
                LogUtil.d("fangjuan", "url=" + url + ",md5=" + md5 + ",fileType=" + fileType + ",version=" + version
                        + ",fileID=" + fileId + ",crc=" + crc);
                UpdateInfo info = new UpdateInfo(null, url, version, md5, fileType, fileId, crc);
                if (updateCheck(info)) {
                    OtaManager.getInstance().startUpdateActivity(info);
                }
                break;
            }

            case MqttTopics.DEVICE_CONTROL_REQUEST: {
                JSONObject jsonObject = JSON.parseObject(content);
                String type = jsonObject.getString("type");
                if (TYPE_1.equals(type)) {
                    String detail = getJsonString("1001");
                    boolean success = LockManager.getInstance().unlock(LockManager.UNLOCK_TYPE_APP, detail);
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("status", success ? 0 : 1);
                    MqttManager.getInstance().response(jsonObject1.toJSONString());
                }
                break;
            }

            case MqttTopics.FACE_ADD: {
                FileUtil.saveLog("faceAdd.txt", content);
                JSONObject jsonPrent = JSON.parseObject(content);
                JSONObject jsonObject = jsonPrent.getJSONObject("data");
                int faceId = jsonObject.getIntValue("faceId");
                int uid = jsonObject.getIntValue("uid");
                String faceContent = jsonObject.getString("faceContent");
                long expireDate = jsonObject.getLongValue("expireDate");
                String faceDBID = jsonObject.getString("faceDBID");
                McuManager.saveLastTimeStamp(McuManager.KEY_FACE_TIMESTAMP, System.currentTimeMillis());
                float[] features = null;
                if (faceContent != null && faceContent.contains("face_feature") && faceContent.contains("faces")) {
                        /*FaceFeature faceFeature = FaceFeature.toFaceFeature(faceContent);
                        features = faceFeature.getFaceFeature(0);*/
                    JSONObject faceContainerObject = JSON.parseObject(faceContent);
                    JSONArray faceJsonArray = faceContainerObject.getJSONArray("faces");
                    JSONObject faceObject = faceJsonArray.getJSONObject(0);
                    String faceFeatureStr = faceObject.getString("face_feature");
                    List<Float> list = JSON.parseArray(faceFeatureStr, Float.class);
                    features = getFaceFeature(list);
                } else {
                    //List<Float> list = JSON.parseArray(faceContent, Float.class);
                    //features = getFaceFeature(list);
                    features = base64ToFea(faceContent);
                }
                registerFace(features, faceId, expireDate, faceDBID, uid);
                response(jsonPrent.getString("seqNo"), RESPONSE_CODE_SUCCESS);
                break;
            }

            case MqttTopics.FACE_DELETE: {
                JSONObject jsonPrent = JSON.parseObject(content);
                JSONObject jsonObject = jsonPrent.getJSONObject("data");
                String faceId = jsonObject.getString("faceId");
                DataOperator dataOperator = DataOperator.getInstance();
                User user = dataOperator.getUserByServerPersonId(faceId);
                YMFaceTrackManager.getRSFaceRecognition().personDelete(Integer.valueOf(user.personId));
                dataOperator.deleteUserByServerPersonId(faceId);
                response(jsonPrent.getString("seqNo"), RESPONSE_CODE_SUCCESS);
                break;
            }

            case MqttTopics.TIME_CALIBRATE: {
                JSONObject jsonObject = JSON.parseObject(content);
                long when = jsonObject.getLongValue("timeStamp");
                LockManager.getInstance().setTime(when);
                break;
            }

            case MqttTopics.DISPATCH_SIP_ITEM: {
                JSONObject jsonPrent = JSON.parseObject(content);
                JSONObject jsonObject = jsonPrent.getJSONObject("data");
                String roomNo = jsonObject.getString("roomNo");
                String phone = jsonObject.getString("sip");
                McuManager.saveLastTimeStamp(McuManager.KEY_SIP_TIMESTAMP, jsonObject.getLong("timeStamp"));
                DataOperator dataOperator = DataOperator.getInstance();
                dataOperator.deleteByRoom(roomNo);
                String[] number = phone.split(";");
                for (int j = 0; j < number.length; j++) {
                    if (number[j].isEmpty()) {
                        continue;
                    }
                    dataOperator.insertNumber(roomNo, number[j], j);
                }
                response(jsonPrent.getString("seqNo"), RESPONSE_CODE_SUCCESS);
                break;
            }

            case MqttTopics.DISPATCH_AD_URL: {
                break;
            }

            case MqttTopics.DISPATCH_DOWNLOAD_URL: {
                break;
            }

            case MqttTopics.DISPATCH_GROUP_CODES: {
                break;
            }

            case MqttTopics.DISPATCH_READING_HEAD_PROGRAM_URL: {
                break;
            }
        }
    }

    private void updateCard(JSONObject jo) {
        CardInfo ci = new CardInfo();
        JSONObject jsonObject = jo.getJSONObject("data");
        ci.cardNo = jsonObject.getString("cardNo");
        ci.cardType = jsonObject.getIntValue("cardType");
        ci.timeStamp = jsonObject.getIntValue("timeStamp");
        McuManager.saveLastTimeStamp(McuManager.KEY_FILTER_TIMESTAMP, ci.timeStamp);
        ci.filterType = jsonObject.getIntValue("filterType");
        ci.endTime = jsonObject.getLongValue("endTime");
        DataOperator dataOperator = DataOperator.getInstance();
        dataOperator.deleteCard(ci.cardNo);
        if (ci.filterType == 0 || ci.filterType == 2) {
            dataOperator.insertCard(ci.cardNo, ci.cardType, ci.timeStamp, ci.endTime, ci.filterType);
        }
        McuManager.getInstance().addCardInfo(ci);
    }

    private boolean updateCheck(UpdateInfo info) {
        String current = android.os.Build.VERSION.RELEASE;
        Log.i("fangjuan", "current version=" + current);
        if (!current.equals(info.getVersion())) {
            return true;
        }
        return false;
    }

    private float[] base64ToFea(String s) {
        byte[] b = Base64.decode(s, 0);
        int len = b.length / 4;
        float[] floats = new float[len];
        for (int i = 0; i < len; i++) {
            int l;
            l = b[i * 4 + 0];
            l &= 0xff;
            l |= ((long) b[i * 4 + 1] << 8);
            l &= 0xffff;
            l |= ((long) b[i * 4 + 2] << 16);
            l &= 0xffffff;
            l |= ((long) b[i * 4 + 3] << 24);
            floats[i] = Float.intBitsToFloat(l);
        }
        return floats;
    }

    private float[] getFaceFeature(List<Float> faceFeature) {
        float[] features = new float[faceFeature.size()];
        for (int i = 0; i < features.length; i++) {
            features[i] = faceFeature.get(i).floatValue();
        }
        return features;
    }

    private void registerFace(float[] features, int serverPersonId,
                              long expireDate, String faceDBID, int uid) {
        if (features != null) {
            int personId = YMFaceTrackManager.getRSFaceRecognition().personCreate(features);
            LogUtil.i(TAG, "registerFace personId=" + personId);
            User user = new User();
            user.personId = String.valueOf(personId);
            //user.name = "test name";
            user.serverPersonId = String.valueOf(serverPersonId);
            user.expireDate = expireDate;
            user.faceDBID = faceDBID;
            user.uid = uid;
            DataOperator.getInstance().insert(user);
        }
    }

    private String getJsonString(String uuid) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        return jsonObject.toJSONString();
    }

    private void response(String seqNo, int statusCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("seqNo", seqNo);
        jsonObject.put("statusCode", statusCode);
        mqttManager.response(jsonObject.toJSONString());
    }
}
