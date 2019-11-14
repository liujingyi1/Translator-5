package com.android.rgk.common.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.rgk.common.db.CardDao;
import com.android.rgk.common.db.DataColumns;
import com.android.rgk.common.db.DatabaseHelper;
import com.android.rgk.common.db.bean.CardInfo;

import java.util.ArrayList;
import java.util.List;

public class CardDaoImpl implements CardDao, DataColumns.CardColumns {
    private DatabaseHelper mDatabaseHelper;

    private static final String QUERY_COLUMNS = CARD_NO + ","      //0
            + CARD_TYPE + ","                                      //1
            + TIME_STAMP + ","                                     //2
            + END_TIME + ","                                       //3
            + FILTER_TYPE;                                         //4

    public CardDaoImpl(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    public CardInfo getCard(String cardNo) {
        if (cardNo == null || cardNo.length() == 0) {
            return null;
        }
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String sql = "SELECT " + QUERY_COLUMNS + " FROM " + TABLE_CARD + " WHERE card_no=?";
        Cursor cursor = database.rawQuery(sql, new String[]{cardNo});
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        try {
            cursor.moveToFirst();
            return getCardInfo(cursor);
        } finally {
            cursor.close();
        }
    }

    private CardInfo getCardInfo(Cursor cursor) {
        CardInfo cardInfo = new CardInfo();
        cardInfo.cardNo = cursor.getString(0);
        cardInfo.cardType = cursor.getInt(1);
        cardInfo.timeStamp = cursor.getLong(2);
        cardInfo.endTime = cursor.getLong(3);
        cardInfo.filterType = cursor.getInt(4);
        return cardInfo;
    }

    @Override
    public List<CardInfo> getAllCards() {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String sql = "SELECT " + QUERY_COLUMNS + " FROM " + TABLE_CARD;
        Cursor cursor = database.rawQuery(sql, null);
        ArrayList<CardInfo> cardInfos = new ArrayList<>();
        if (cursor == null) {
            return cardInfos;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return cardInfos;
        }
        try {
            while (cursor.moveToNext()) {
                cardInfos.add(getCardInfo(cursor));
            }
        } finally {
            cursor.close();
        }

        return cardInfos;
    }

    @Override
    public long insertCard(String cardNo, int cardType, long timeStamp, long endTime, int filterType) {
        if (cardNo == null || cardNo.length() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CARD_NO, cardNo);
        values.put(CARD_TYPE, cardType);
        values.put(TIME_STAMP, timeStamp);
        values.put(END_TIME, endTime);
        values.put(FILTER_TYPE, filterType);
        long rowId = database.insert(TABLE_CARD, null, values);
        return rowId;
    }

    @Override
    public int updateCard(String cardNo, int filterType) {
        if (cardNo == null || cardNo.length() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FILTER_TYPE, filterType);
        int count = database.update(TABLE_CARD, values, CARD_NO + "=?", new String[]{cardNo});
        return count;
    }

    @Override
    public int deleteCard(String cardNo) {
        if (cardNo == null || cardNo.length() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return database.delete(TABLE_CARD, CARD_NO + "=?", new String[]{cardNo});
    }
}
