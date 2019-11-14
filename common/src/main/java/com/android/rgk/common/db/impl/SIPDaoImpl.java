package com.android.rgk.common.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.android.rgk.common.db.DataColumns;
import com.android.rgk.common.db.DatabaseHelper;
import com.android.rgk.common.db.SIPDao;
import com.android.rgk.common.db.bean.SIPAccountInfo;

public class SIPDaoImpl implements SIPDao, DataColumns.SIPAccountColumns {
    private DatabaseHelper mDatabaseHelper;

    private static final String QUERY_COLUMNS = DOMAIN + ","
            + USER + ","
            + SIP_PASSWORD + ","
            + OUTBUND + ","
            + SIP_TYPE;

    public SIPDaoImpl(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    public int insertSIP(SIPAccountInfo info) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DOMAIN, info.domain);
        values.put(USER, info.user);
        values.put(SIP_PASSWORD, info.password);
        values.put(OUTBUND, info.outbund);
        values.put(SIP_TYPE, info.type);
        return (int) database.insert(TABLE_SIP, null, values);
    }

    @Override
    public int deleteSIPByType(String type) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return database.delete(TABLE_SIP, SIP_TYPE + "=?", new String[]{type});
    }

    @Override
    public SIPAccountInfo getAccountInfoByType(String type) {
        if (TextUtils.isEmpty(type)) {
            return null;
        }

        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();

        String sql = "SELECT " + QUERY_COLUMNS + " FROM number WHERE " + SIP_TYPE + "=? ";

        Cursor cursor = database.rawQuery(sql, new String[]{type});
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        try {
            cursor.moveToFirst();
            return getAccountInfoByCursor(cursor);
        } finally {
            cursor.close();
        }
    }

    private SIPAccountInfo getAccountInfoByCursor(Cursor cursor) {
        SIPAccountInfo accout = new SIPAccountInfo();
        accout.domain = cursor.getString(0);
        accout.user = cursor.getString(1);
        accout.password = cursor.getString(2);
        accout.outbund = cursor.getString(3);
        accout.type = cursor.getString(4);
        return accout;
    }
}
