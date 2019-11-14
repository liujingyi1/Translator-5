package com.android.rgk.common.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.android.rgk.common.db.DataColumns;
import com.android.rgk.common.db.DatabaseHelper;
import com.android.rgk.common.db.NumberDao;

import java.util.ArrayList;

public class NumberDaoImpl implements NumberDao, DataColumns.NumberColumns {
    private DatabaseHelper mDatabaseHelper;

    public NumberDaoImpl(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    public ArrayList<String> getNumberByRoom(String room) {

        if (TextUtils.isEmpty(room)) {
            return null;
        }

        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();

        String sql = "SELECT " + NUMBER + " FROM number WHERE " + ROOM + "=? " +
                "ORDER BY " + ORDER;

        Cursor cursor = database.rawQuery(sql, new String[]{room});
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        ArrayList<String> numbers = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                numbers.add(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }

        return numbers;
    }

    @Override
    public int insertNumber(String room, String number, int order) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ROOM, room);
        values.put(NUMBER, number);
        values.put(ORDER, order);
        return (int) database.insert(TABLE_NUMBER, ROOM, values);
    }

    @Override
    public int deleteByRoom(String room) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return database.delete(TABLE_NUMBER, ROOM + "=?", new String[]{room});
    }

    @Override
    public String getRoomByNumber(String number) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();

        String sql = "SELECT " + ROOM + " FROM number WHERE " + NUMBER + "=?";

        Cursor cursor = database.rawQuery(sql, new String[]{number});
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        try {
            cursor.moveToFirst();
            return cursor.getString(0);
        } finally {
            cursor.close();
        }
    }

    @Override
    public ArrayList<String> getAllRooms() {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();

        String sql = "SELECT DISTINCT " + ROOM + " FROM number";

        Cursor cursor = database.rawQuery(sql, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        ArrayList<String> rooms = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                rooms.add(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }
        return rooms;
    }

    @Override
    public int deleteByRoomAndNumber(String room, String number) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return database.delete(TABLE_NUMBER, ROOM + "=? AND " + NUMBER + "=?", new String[]{room, number});
    }
}
