package com.android.rgk.common;

import android.os.Environment;

import com.android.rgk.common.lock.LockManager;

import java.io.File;

public class Constant {
    //注册头像的存储路径
    public static final String ImagePath;
    public static final String FeatureDatabasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/face_recognition_db";
    public static final String DoorwayDatabasePath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/door_way_db/";

    static {
        File file = new File(Constant.FeatureDatabasePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(Constant.DoorwayDatabasePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        String dir = LockManager.getInstance().getSdcardPath();
        if (dir != null && dir.length() > 0) {
            dir += "/face_recognition_pic/";
        } else {
            dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/face_recognition_pic/";
        }
        ImagePath = dir;

        file = new File(ImagePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    //后置摄像头绘制左右翻转
    public static final boolean backCameraLeftRightReverse = false;
    //特殊设备摄像头绘制左右翻转
    public static boolean specialCameraLeftRightReverse = false;
    //特殊设备摄像头绘制上下翻转
    public static boolean specialCameraTopDownReverse = false;
    //设置非全屏
    public static boolean specialPreviewSize = false;
    //设置特殊识别角度
    public static boolean specialAngle = false;

    //是否启动双目摄像头人脸活体识别
    public static boolean enableMutilCamera = true;

    //启动双目时可见光为后置，红外为前置
    public static boolean enableColorBack = true;

    public static final String SPECIAL_CAMERA_LEFT_RIGHT_REVERSE = "SPECIALCAMERALEFTRIGHTREVERSE";
    public static final String SPECIAL_CAMERA_TOP_DOWN_REVERSE = "SPECIALCAMERATOPDOWNREVERSE";
    public static final String SPECIAL_PREVIEW_SIZE = "SPECIALPREVIEWSIZE";
    public static final String SPECIAL_PREVIEW_SIZE_SCALBIT = "SPECIALPREVIEWSIZESCALBIT";
    public static final String SPECIAL_ANGLE = "SPECIALANGLE";
    public static final String SPECIAL_ANGLE_ANGLE = "SPECIALANGLEANGLE";

    public static int SDKOrientation;
}
