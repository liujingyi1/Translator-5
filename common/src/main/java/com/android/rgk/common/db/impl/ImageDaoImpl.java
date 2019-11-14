package com.android.rgk.common.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.rgk.common.db.DataColumns;
import com.android.rgk.common.db.DatabaseHelper;
import com.android.rgk.common.db.ImageDao;
import com.android.rgk.common.db.bean.Image;

import java.util.ArrayList;
import java.util.List;

public class ImageDaoImpl implements ImageDao, DataColumns.ImageColumns {
    private static final String QUERY_COLUMNS = IMAGE_ID + ","  //0
            + IMAGE_FILE + ","                                      //1
            + IMAGE_DATE;                                           //2

    private DatabaseHelper mDatabaseHelper;

    public ImageDaoImpl(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    public List<Image> getImages(int start, int length) {
        if (start < 0 || length <= 0) {
            return null;
        }
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String sql = "SELECT _id,file,date FROM image LIMIT " + start + "," + length;
        Cursor cursor = database.rawQuery(sql, null);
        ArrayList<Image> list = new ArrayList();
        if (cursor == null) {
            return list;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return list;
        }
        try {
            Image image = null;
            while (cursor.moveToNext()) {
                image = new Image();
                image.id = cursor.getLong(0);
                image.file = cursor.getString(1);
                image.date = cursor.getLong(2);
                list.add(image);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    @Override
    public long insertImage(String fileName, long date) {
        if (fileName == null || fileName.length() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(IMAGE_FILE, fileName);
        values.put(IMAGE_DATE, date);
        long rowId = database.insert(TABLE_IMAGE, null, values);
        return rowId;
    }

    @Override
    public int deleteImageByIds(List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        StringBuilder sb = new StringBuilder();
        sb.append("_id IN (");
        for (long id : ids) {
            sb.append(id);
            sb.append(',');
        }
        String where = sb.substring(0, sb.length() - 1);
        where += ")";
        return database.delete(TABLE_IMAGE, where, null);
    }
}
