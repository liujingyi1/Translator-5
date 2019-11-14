package com.android.rgk.common.db.bean;

import com.alibaba.fastjson.JSON;
import com.android.rgk.common.util.LogUtil;

public class CardInfo {
    private static final String TAG = "CardInfo";
    public String cardNo;
    public int cardType;
    public long endTime;
    public int filterType;
    public long timeStamp;

    public static final int TYPE_WHITE = 1;
    public static final int TYPE_BLACK = 2;

    public static CardInfo toCardInfo(String jsonString) {
        LogUtil.d(TAG, "toCardInfo:" + jsonString);
        return JSON.parseObject(jsonString, CardInfo.class);
    }

    @Override
    public String toString() {
        return cardNo + "," + cardType + "," + timeStamp + "," + endTime + "," + filterType;
    }
}
