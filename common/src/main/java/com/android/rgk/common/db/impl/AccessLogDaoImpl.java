package com.android.rgk.common.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.rgk.common.db.AccessLogDao;
import com.android.rgk.common.db.DataColumns;
import com.android.rgk.common.db.DatabaseHelper;
import com.android.rgk.common.db.bean.AccessLog;

import java.util.ArrayList;
import java.util.List;

public class AccessLogDaoImpl implements AccessLogDao, DataColumns.AccessLogColumns {
    private DatabaseHelper mDatabaseHelper;

    private static final String QUERY_COLUMNS = OPEN_TYPE + ","  //0
            + OPEN_TIME + ","                                        //1
            + LOCK_STATUS + ","                                      //2
            + OPEN_RESULT + ","                                      //3
            + PARAMETERS;                                            //4

    public AccessLogDaoImpl(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    public List<AccessLog> getAllAccessLogs() {
        ArrayList<AccessLog> accessLogs = new ArrayList<>();
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String sql = "SELECT " + QUERY_COLUMNS + " FROM " + TABLE_ACCESS_LOG;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor == null) {
            return accessLogs;
        }
        if (cursor.getCount() == 0) {
            return accessLogs;
        }
        try {
            while (cursor.moveToNext()) {
                accessLogs.add(getAccessLog(cursor));
            }
        } finally {
            cursor.close();
        }
        return accessLogs;
    }

    private AccessLog getAccessLog(Cursor cursor) {
        AccessLog accessLog = new AccessLog();
        accessLog.openType = cursor.getInt(0);
        accessLog.openTime = cursor.getLong(1);
        accessLog.lockStatus = cursor.getInt(2);
        accessLog.openResult = cursor.getInt(3);
        accessLog.parameters = cursor.getString(4);
        return accessLog;
    }

    @Override
    public List<AccessLog> getAccessLogs(int type) {
        ArrayList<AccessLog> accessLogs = new ArrayList<>();
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String sql = "SELECT " + QUERY_COLUMNS + " FROM " + TABLE_ACCESS_LOG + " WHERE open_type=" + type;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor == null) {
            return accessLogs;
        }
        if (cursor.getCount() == 0) {
            return accessLogs;
        }
        try {
            while (cursor.moveToNext()) {
                accessLogs.add(getAccessLog(cursor));
            }
        } finally {
            cursor.close();
        }
        return accessLogs;
    }

    @Override
    public long insertAccessLog(AccessLog accessLog) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(OPEN_TYPE, accessLog.openType);
        values.put(OPEN_TIME, accessLog.openTime);
        values.put(LOCK_STATUS, accessLog.lockStatus);
        values.put(OPEN_RESULT, accessLog.openResult);
        values.put(PARAMETERS, accessLog.parameters);
        return database.insert(TABLE_ACCESS_LOG, null, values);
    }

    @Override
    public int deleteAllAccessLog() {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return database.delete(TABLE_ACCESS_LOG, null, null);
    }
}
