package com.android.rgk.common.db;

import com.android.rgk.common.db.bean.CardInfo;

import java.util.List;

public interface CardDao {
    CardInfo getCard(String cardNo);

    List<CardInfo> getAllCards();

    long insertCard(String cardNo, int cardType, long timeStamp, long endTime, int filterType);

    int updateCard(String cardNo, int filterType);

    int deleteCard(String cardNo);
}
