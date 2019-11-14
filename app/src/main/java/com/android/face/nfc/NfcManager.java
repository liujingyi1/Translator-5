package com.android.face.nfc;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.android.rgk.common.lock.LockManager;

public class NfcManager {
    private boolean mNfcReading = false;
    private boolean mNfcNear = false;
    private Thread mNfcThread = new Thread() {
        public void run() {
            while (mNfcReading) {
                readNfc();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
            }
        }
    };

    public NfcManager() {

    }

    public void startReadNfc() {
        if (mNfcReading) {
            return;
        }
        mNfcReading = true;
        mNfcThread.start();
    }

    public void stopReadNfc() {
        mNfcReading = false;
    }

    private void readNfc() {
        LockManager lockManager = LockManager.getInstance();
        String str = lockManager.getNfcValue();
        if (null != str) {
            if (!mNfcNear) {
                mNfcNear = true;
                String[] card_list = str.split(";");
                Log.d("NfcManager", "readNfc Num: " + card_list.length + ", str: " + str);
                for (String card : card_list) {
                    if (Fk.canOpenDoor(card)) {
                        String detail = getJsonString(Fk.mCardID, 1, 1);
                        lockManager.unlock(LockManager.UNLOCK_TYPE_NFC, detail);
                        lockManager.setNfcLight("255");
                        break;
                    }
                }
            }
        } else {
            mNfcNear = false;
        }
    }

    private String getJsonString(String cardNo, int cardType, int cardIssueType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cardNo", cardNo);
        jsonObject.put("cardType", cardType);
        jsonObject.put("cardIssueType", cardIssueType);
        return jsonObject.toJSONString();
    }
}
