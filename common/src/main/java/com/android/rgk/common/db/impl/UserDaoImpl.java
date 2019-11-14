package com.android.rgk.common.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.android.rgk.common.db.DataColumns;
import com.android.rgk.common.db.DatabaseHelper;
import com.android.rgk.common.db.UserDao;
import com.android.rgk.common.db.bean.User;

import java.util.ArrayList;

public class UserDaoImpl implements UserDao, DataColumns.UserColumns {
    private DatabaseHelper mDatabaseHelper;

    private static final String QUERY_COLUMNS = USER_ID + ","  //0
            + USER_NAME + ","                                      //1
            + PERSON_ID + ","                                      //2
            + SERVER_PERSON_ID + ","                              //3
            + USER_EXPIRE_DATE + ","                              //4
            + USER_FACE_DBID + ","                                //5
            + USER_UID + ","                                       //6
            + PERSON_TYPE;                                         //7

    public UserDaoImpl(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    public ArrayList<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String sql = "SELECT " + QUERY_COLUMNS + " FROM " + TABLE_USER;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor == null) {
            return users;
        }
        try {
            while (cursor.moveToNext()) {
                users.add(getUser(cursor));
            }
        } finally {
            cursor.close();
        }

        return users;
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT " + QUERY_COLUMNS + " FROM users WHERE " + USER_ID + "=" + id;
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        try {
            cursor.moveToFirst();
            return getUser(cursor);
        } finally {
            cursor.close();
        }
    }

    @Override
    public User getUserByPersonId(int personId) {
        String sql = "SELECT " + QUERY_COLUMNS + " FROM users WHERE " + PERSON_ID + "=" + personId;
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        try {
            cursor.moveToFirst();
            return getUser(cursor);
        } finally {
            cursor.close();
        }
    }

    @Override
    public User getUserByServerPersonId(String serverPersonId) {
        if (serverPersonId == null || serverPersonId.length() == 0) {
            return null;
        }
        String sql = "SELECT " + QUERY_COLUMNS + " FROM users WHERE " + SERVER_PERSON_ID + "=?";
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, new String[]{serverPersonId});
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        try {
            cursor.moveToFirst();
            return getUser(cursor);
        } finally {
            cursor.close();
        }
    }

    @Override
    public User getUserByFaceId(int faceId) {
        String sql = "SELECT " + QUERY_COLUMNS + " FROM users WHERE " + SERVER_PERSON_ID + "=" + faceId;
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        try {
            cursor.moveToFirst();
            return getUser(cursor);
        } finally {
            cursor.close();
        }
    }

    private User getUser(Cursor cursor) {
        User user = new User();
        user.userId = cursor.getString(0);
        user.name = cursor.getString(1);
        user.personId = cursor.getString(2);
        user.serverPersonId = cursor.getString(3);
        user.expireDate = cursor.getLong(4);
        user.faceDBID = cursor.getString(5);
        user.uid = cursor.getInt(6);
        user.personType = cursor.getInt(7);
        return user;
    }

    @Override
    public int insert(User user) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return (int) database.insert(TABLE_USER, null, buildValues(user));
    }

    @Override
    public int update(User user) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return (int) database.update(TABLE_USER, buildValues(user),
                PERSON_ID + "=?", new String[]{user.getPersonId()});
    }

    private ContentValues buildValues(User user) {
        ContentValues values = new ContentValues();
        if (user.name != null) {
            values.put(USER_NAME, user.name);
        }
        if (user.personId != null) {
            values.put(PERSON_ID, user.personId);
        }
        if (user.serverPersonId != null) {
            values.put(SERVER_PERSON_ID, user.serverPersonId);
        }
        values.put(PERSON_TYPE, user.personType);
        if (user.age != null) {
            values.put(USER_AGE, user.age);
        }
        if (user.gender != null) {
            values.put(USER_AGE, user.gender);
        }
        if (user.score != null) {
            values.put(USER_AGE, user.score);
        }
        if (user.expireDate > 0) {
            values.put(USER_EXPIRE_DATE, user.expireDate);
        }
        if (user.faceDBID != null) {
            values.put(USER_FACE_DBID, user.faceDBID);
        }
        if (user.uid > 0) {
            values.put(USER_UID, user.uid);
        }
        return values;
    }

    @Override
    public int deleteUserByPersonId(int personId) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return database.delete(TABLE_USER, PERSON_ID + "=" + personId, null);
    }

    @Override
    public int deleteUserByServerPersonId(String serverPersonId) {
        if (serverPersonId == null || serverPersonId.length() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return database.delete(TABLE_USER, SERVER_PERSON_ID + "=?", new String[]{serverPersonId});
    }

    @Override
    public int deleteUserByFaceId(int faceId) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        return database.delete(TABLE_USER, SERVER_PERSON_ID + "=" + faceId, null);
    }

    @Override
    public int deleteAllUsers() {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM users", null);
        if (cursor == null) {
            return 0;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return 0;
        }
        int count = cursor.getCount();
        try {
            database.execSQL("delete from " + TABLE_USER);
            database.execSQL("update sqlite_sequence set seq = 0 where name =  '" + TABLE_USER + "'");
        } catch (SQLException e) {
            e.printStackTrace();
            count = 0;
        }
        return count;
    }
}
