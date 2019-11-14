package com.android.rgk.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.rgk.common.Constant;

public class DatabaseHelper extends SQLiteOpenHelper implements DataColumns.UserColumns,
        DataColumns.NumberColumns, DataColumns.CardColumns, DataColumns.AccessLogColumns,
        DataColumns.DoorMagnetColumns, DataColumns.PasswordColumns, DataColumns.ImageColumns,
        DataColumns.SIPAccountColumns {
    private static final String DATABASE_NAME = "doorway.db";
    private static final int DATABASE_VERSION = 3;

    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER +
            "(" + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + PERSON_ID + " TEXT NOT NULL,"
            + SERVER_PERSON_ID + " TEXT NOT NULL,"
            + USER_NAME + " TEXT,"
            + USER_AGE + " INTEGER,"
            + USER_GENDER + " INTEGER,"
            + USER_SCORE + " INTEGER,"
            + USER_EXPIRE_DATE + " INTEGER,"
            + USER_FACE_DBID + " TEXT,"
            + USER_UID + " INTEGER,"
            + PERSON_TYPE + " INTEGER DEFAULT 0);";

    private static final String CREATE_TABLE_NUMBER = "CREATE TABLE " + TABLE_NUMBER +
            "(" + NUMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ROOM + " TEXT NOT NULL,"
            + NUMBER + " TEXT NOT NULL,"
            + ORDER + " INTEGER);";

    private static final String CREATE_TABLE_SIP = "CREATE TABLE " + TABLE_SIP +
            "(" + SIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DOMAIN + " TEXT NOT NULL,"
            + USER + " TEXT NOT NULL,"
            + SIP_PASSWORD + " TEXT NOT NULL,"
            + OUTBUND + " TEXT,"
            + SIP_TYPE + " TEXT NOT NULL);";

    private static final String CREATE_TABLE_PASSWORD = "CREATE TABLE " + TABLE_PASSWORD +
            "(" + PASSWORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + PASSWORD + " TEXT NOT NULL,"
            + STARTTIME + " INTEGER,"
            + EXPIREDATE + " INTEGER);";

    private static final String CREATE_TABLE_CARD = "CREATE TABLE " + TABLE_CARD +
            "(" + CARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CARD_NO + " TEXT NOT NULL,"
            + CARD_TYPE + " INTEGER,"
            + TIME_STAMP + " INTEGER,"
            + END_TIME + " INTEGER,"
            + FILTER_TYPE + " INTEGER DEFAULT 2);";

    private static final String CREATE_TABLE_ACCESS_LOG = "CREATE TABLE " + TABLE_ACCESS_LOG +
            "(" + ACCESS_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + OPEN_TYPE + " INTEGER,"
            + OPEN_TIME + " INTEGER,"
            + LOCK_STATUS + " INTEGER,"
            + OPEN_RESULT + " INTEGER,"
            + PARAMETERS + " TEXT);";

    private static final String CREATE_TABLE_DOOR_MAGNET = "CREATE TABLE " + TABLE_DOOR_MAGNET +
            "(" + DOOR_MAGNET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + LOG_TIME + " INTEGER,"
            + SENSOR_STATUS + " INTEGER);";

    private static final String CREATE_TABLE_IMAGE = "CREATE TABLE " + TABLE_IMAGE +
            "(" + IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + IMAGE_FILE + " TEXT,"
            + IMAGE_DATE + " INTEGER);";

    protected DatabaseHelper(Context context) {
        super(context, Constant.DoorwayDatabasePath + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_USER);
        database.execSQL(CREATE_TABLE_NUMBER);
        database.execSQL(CREATE_TABLE_PASSWORD);
        database.execSQL(CREATE_TABLE_SIP);
        database.execSQL(CREATE_TABLE_CARD);
        database.execSQL(CREATE_TABLE_ACCESS_LOG);
        database.execSQL(CREATE_TABLE_DOOR_MAGNET);
        database.execSQL(CREATE_TABLE_IMAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(), "Upgrading database from version" +
                oldVersion + "to" + newVersion + ",which will destroy all the old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NUMBER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSWORD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SIP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCESS_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOOR_MAGNET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE);
        onCreate(db);
    }
}
