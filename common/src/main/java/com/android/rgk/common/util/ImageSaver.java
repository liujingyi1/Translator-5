package com.android.rgk.common.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class ImageSaver implements Callable<Object> {
    private static final String TAG = "ImageSaver";
    private Bitmap mBitmap;
    private List<Rect> mRectFList;
    private String mDir;

    public ImageSaver(Bitmap bitmap, List<Rect> rectFList, String dir) {
        mBitmap = bitmap;
        mRectFList = rectFList;
        mDir = dir;
    }

    @Override
    public Object call() throws Exception {
        LogUtil.d(TAG, "call mDir=" + mDir);
        try {
            File dir = new File(mDir);
            if (!dir.exists()) {
                boolean success = dir.mkdirs();
                if (!success) {
                    return null;
                }
            }
            String fileName = generateFileName();
            FileOutputStream fos = new FileOutputStream(mDir + fileName + ".jpg");
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            Bitmap bmp;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            for (int i = 0; i < mRectFList.size(); i++) {
                Rect rect = mRectFList.get(i);
                bmp = BitmapUtil.clipBitmap(mBitmap, rect);
                fos = new FileOutputStream(mDir + fileName + "-" + (i + 1) + ".jpg");
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                bmp.recycle();
            }
            mBitmap.recycle();
            mBitmap = null;
        } catch (IOException e) {
            LogUtil.w(TAG, "ImageSaver.call:" + LogUtil.getStackTraceString(e));
        }

        return null;
    }

    private String generateFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        String prefix = dateFormat.format(new Date());
        return "IMG_" + prefix;
    }
}
