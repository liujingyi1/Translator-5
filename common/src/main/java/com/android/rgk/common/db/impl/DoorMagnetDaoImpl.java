package com.android.rgk.common.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.rgk.common.db.DataColumns;
import com.android.rgk.common.db.DatabaseHelper;
import com.android.rgk.common.db.DoorMagnetDao;
import com.android.rgk.common.db.bean.DoorMagnet;

import java.util.ArrayList;
import java.util.List;

public class DoorMagnetDaoImpl implements DoorMagnetDao, DataColumns.DoorMagnetColumns {
    private DatabaseHelper mDatabaseHelper;

    private static final String QUERY_COLUMNS = LOG_TIME + ","  //0
            + SENSOR_STATUS;                                        //1

    public DoorMagnetDaoImpl(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    public List<DoorMagnet> getAllDoorMagnets() {
        ArrayList<DoorMagnet> doorMagnets = new ArrayList<>();
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String sql = "SELECT " + QUERY_COLUMNS + " FROM " + TABLE_DOOR_MAGNET;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor == null) {
            return doorMagnets;
        }
        if (cursor.getCount() == 0) {
            return doorMagnets;
        }
        try {
            while (cursor.moveToNext()) {
                doorMagnets.add(getDoorMagnet(cursor));
            }
        } finally {
            cursor.close();
        }
        return doorMagnets;
    }

    private DoorMagnet getDoorMagnet(Cursor cursor) {
        DoorMagnet doorMagnet = new DoorMagnet();
        doorMagnet.logTime = cursor.getLong(0);
        doorMagnet.sensorStatus = cursor.getInt(1);
        return doorMagnet;
    }

    @Override
    public long insertDoorMagnet(long time, int status) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LOG_TIME, time);
        values.put(SENSOR_STATUS, status);
        return database.insert(TABLE_DOOR_MAGNET, null, values);
    }

    @Override
    public int deleteAllDoorMagnets() {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return database.delete(TABLE_DOOR_MAGNET, null, null);
    }
}
