package com.android.rgk.common.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.android.rgk.common.db.DataColumns;
import com.android.rgk.common.db.DatabaseHelper;
import com.android.rgk.common.db.PasswordDao;

public class PasswordDaoImpl implements PasswordDao, DataColumns.PasswordColumns {
    private DatabaseHelper mDatabaseHelper;

    public PasswordDaoImpl(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    public int insertPassword(String password, long startTime, long expireDate) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PASSWORD, password);
        values.put(STARTTIME, startTime);
        values.put(EXPIREDATE, expireDate);
        return (int) database.insert(TABLE_PASSWORD, PASSWORD, values);
    }

    @Override
    public boolean checkPassword(String password, long currentTime) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }

        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();

        String sql =
                "SELECT " + PASSWORD + " FROM password WHERE " + PASSWORD + "=? " +
                        "AND " + EXPIREDATE + " >= " + currentTime + " AND " + STARTTIME + " <=" +
                        " " + currentTime;

        Cursor cursor = database.rawQuery(sql, new String[]{password});
        
        if (cursor == null) {
            return false;
        }

        if (cursor.getCount() == 0) {
            cursor.close();
            return false;
        }

        cursor.close();

        return true;
    }

    @Override
    public int deleteOverduePassword(long currentTime) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return database.delete(TABLE_PASSWORD,
                EXPIREDATE + "<" + currentTime,
                null);
    }
}
