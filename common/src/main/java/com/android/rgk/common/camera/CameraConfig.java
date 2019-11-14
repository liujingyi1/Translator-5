package com.android.rgk.common.camera;

import android.graphics.Point;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;

public class CameraConfig {
    private Point mPreviewSize;
    private int mFps;
    private int mOrientationDegree;
    private int mPreviewFormat;
    private String mFlashMode;
    private String mWhiteBalance;
    private String mSceneMode;
    private int mFacing;

    private static Object lock = new Object();

    private static SparseArray<CameraConfig> mCameraConfigSparseArray = new SparseArray<>(2);

    public static final ArrayList<String> VIDEO_SIZES = new ArrayList<>();

    static {
        VIDEO_SIZES.add("256x144"); //QCIF
        VIDEO_SIZES.add("320x240"); //QVGA
        VIDEO_SIZES.add("512x288"); //CIF
        VIDEO_SIZES.add("640x480"); //VGA
//        VIDEO_SIZES.add("1280x720"); //720P
    }

    public static final HashMap<String, String> VIDEO_SIZE_MAP = new HashMap<>();

    static {
        VIDEO_SIZE_MAP.put("256x144", "QCIF");
        VIDEO_SIZE_MAP.put("320x240", "QVGA");
        VIDEO_SIZE_MAP.put("512x288", "CIF");
        VIDEO_SIZE_MAP.put("640x480", "VGA");
        //VIDEO_SIZE_MAP.put("640x480", "720P");
    }

    private CameraConfig() {

    }

    public static CameraConfig getInstance(int cameraId) {
        synchronized (lock) {
            if (mCameraConfigSparseArray.get(cameraId) == null) {
                mCameraConfigSparseArray.put(cameraId, new CameraConfig());
            }
        }

        return mCameraConfigSparseArray.get(cameraId);
    }

    public int getFacing() {
        return mFacing;
    }

    public void setFacing(int facing) {
        mFacing = facing;
    }

    public Point getPreviewSize() {
        return mPreviewSize;
    }

    public void setPreviewSize(Point previewSize) {
        mPreviewSize = previewSize;
    }

    public String getVideoSize() {
        String previewSizeStr = mPreviewSize.x + "x" + mPreviewSize.y;
        return VIDEO_SIZE_MAP.get(previewSizeStr);
    }

    public int getFps() {
        return mFps;
    }

    public void setFps(int fps) {
        mFps = fps;
    }

    public int getOrientationDegree() {
        return mOrientationDegree;
    }

    public void setOrientationDegree(int orientationDegree) {
        mOrientationDegree = orientationDegree;
    }

    public int getPreviewFormat() {
        return mPreviewFormat;
    }

    public void setPreviewFormat(int previewFormat) {
        mPreviewFormat = previewFormat;
    }

    public String getFlashMode() {
        return mFlashMode;
    }

    public void setFlashMode(String flashMode) {
        mFlashMode = flashMode;
    }

    public String getWhiteBalance() {
        return mWhiteBalance;
    }

    public void setWhiteBalance(String whiteBalance) {
        mWhiteBalance = whiteBalance;
    }

    public String getSceneMode() {
        return mSceneMode;
    }

    public void setSceneMode(String sceneMode) {
        mSceneMode = sceneMode;
    }
}
