package com.android.rgk.common.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Size;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.rgk.common.camera.ui.PreviewSurfaceView;
import com.android.rgk.common.util.BitmapUtil;
import com.android.rgk.common.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class CameraController implements CameraDevice.PreviewCallback, CameraDevice.StateCallback {
    private static final String TAG = "CameraController";
    private Activity mActivity;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private Handler mThreadHandler;
    private Looper mThreadLooper;
    private static final int MSG_SCREENSHOT = 10;
    private static final int MSG_CAPTURE = 11;
    private volatile boolean screenShotWorking = false;

    public final static int CAMERA_ID_RGB = 0;
    public final static int CAMERA_ID_INFRARED = 1;
    private SparseArray<CameraDevice> mCameraDevices = new SparseArray<>(2);

    private ArrayList<PreviewCallback> mRGBPreviewCallbacks = new ArrayList<>();
    private ArrayList<PreviewCallback> mInfraredPreviewCallbacks = new ArrayList<>();

    private SparseArray<ArrayList<CameraStateCallback>> mCameraStateCallbacks = new SparseArray<>();

    private ArrayList<ImageBase64Callback> imageBase64Callbacks = new ArrayList<>();
    private boolean mFullCapture = false;
    private boolean mFaceCapture = false;

    private CameraContext mCameraContext;
    private static CameraController mInstance;

    private int mMaxWidth;
    private int mMaxHeight;

    private CameraController(CameraContext context) {
        if (context == null) {
            throw new IllegalArgumentException("The CameraContext object must be not null.");
        }
        mCameraContext = context;
        mActivity = context.getActivity();
        /*Resources resources = mActivity.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        mMaxWidth = dm.widthPixels;
        mMaxHeight = dm.heightPixels;*/
        getScreenSize();
        startThread();
    }

    private void getScreenSize() {
        WindowManager windowManager =
                (WindowManager) mActivity.getSystemService(Context.
                        WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        if (Build.VERSION.SDK_INT >= 19) {
            // 可能有虚拟按键的情况
            display.getRealSize(outPoint);
        } else {
            // 不可能有虚拟按键
            display.getSize(outPoint);
        }
        mMaxWidth = outPoint.x;
        mMaxHeight = outPoint.y;
    }

    public static void init(CameraContext context) {
        if (mInstance == null) {
            mInstance = new CameraController(context);
        }
    }

    public static CameraController getInstance() {
        return mInstance;
    }

    private void startThread() {
        HandlerThread thread = new HandlerThread("camera_controller");
        thread.start();
        mThreadLooper = thread.getLooper();
        mThreadHandler = new Handler(mThreadLooper) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_SCREENSHOT: {
                        screenShotWorking = true;
                        byte[] data = (byte[]) msg.obj;
                        CameraConfig cameraConfig = CameraConfig.getInstance(CAMERA_ID_RGB);
                        int imageFormat = cameraConfig.getPreviewFormat();
                        Point size = cameraConfig.getPreviewSize();
                        String base64Str = BitmapUtil.getJpegBase64FromYuvByte(data, size.x, size.y, imageFormat);
                        synchronized (imageBase64Callbacks) {
                            for (ImageBase64Callback imageBase64Callback : imageBase64Callbacks) {
                                imageBase64Callback.onResult(base64Str, null);
                            }
                            imageBase64Callbacks.clear();
                        }
                        screenShotWorking = false;
                        break;
                    }

                    case MSG_CAPTURE: {
                        screenShotWorking = true;
                        CaptureArgs captureArgs = (CaptureArgs) msg.obj;
                        if (captureArgs.bitmap == null) {
                            synchronized (imageBase64Callbacks) {
                                for (ImageBase64Callback imageBase64Callback : imageBase64Callbacks) {
                                    imageBase64Callback.onResult("", "");
                                }
                                imageBase64Callbacks.clear();
                                mFullCapture = false;
                                mFaceCapture = false;
                            }
                        } else {
                            String faceCaptureBase64 = "";
                            String fullCaptureBase64 = "";
                            if (mFullCapture) {
                                fullCaptureBase64 = BitmapUtil.bitmapToBase64(captureArgs.bitmap);
                            }
                            if (mFaceCapture && captureArgs.rectList != null && captureArgs.rectList.size() > 0) {
                                Bitmap faceBitmap = BitmapUtil.clipBitmap(captureArgs.bitmap,
                                        captureArgs.rectList.get(captureArgs.maxIndex));
                                faceCaptureBase64 = BitmapUtil.bitmapToBase64(faceBitmap);
                                faceBitmap.recycle();
                                faceBitmap = null;
                            }
                            captureArgs.bitmap.recycle();
                            captureArgs.bitmap = null;
                            synchronized (imageBase64Callbacks) {
                                for (ImageBase64Callback imageBase64Callback : imageBase64Callbacks) {
                                    imageBase64Callback.onResult(fullCaptureBase64, faceCaptureBase64);
                                }
                                imageBase64Callbacks.clear();
                                mFullCapture = false;
                                mFaceCapture = false;
                            }
                            screenShotWorking = false;
                        }
                        break;
                    }
                }
            }
        };
    }

    private void stopThread() {
        if (mThreadLooper != null) {
            mThreadLooper.quitSafely();
            mThreadLooper = null;
        }
    }

    public void openCamera(CameraStateCallback callback) {
        int n = Camera.getNumberOfCameras();
        for (int i = 0; i < n; i++) {
            openCamera(i, callback);
        }
    }

    public void openCamera(int cameraId, final CameraStateCallback callback) {
        ArrayList<CameraStateCallback> stateCallbacks = mCameraStateCallbacks.get(cameraId);
        if (callback != null) {
            if (stateCallbacks == null) {
                stateCallbacks = new ArrayList();
                mCameraStateCallbacks.put(cameraId, stateCallbacks);
            }
            stateCallbacks.add(callback);
        }

        CameraDevice camera = mCameraDevices.get(cameraId);
        if (camera != null) {
            if (callback != null) {
                callback.onStateChange(camera, camera.getState(), camera.getState(), cameraId);
            }
            return;
        }
        if (mCameraContext.getCameraView(cameraId) == null) {
            LogUtil.i(TAG, "Must have a relevant surface view.");
            return;
        }

        final CameraDevice cameraDevice = new CameraDevice(cameraId, mCameraContext.getCameraView(cameraId), this);
        cameraDevice.setPreviewCallback(this);
        mCameraDevices.put(cameraId, cameraDevice);
        mThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                cameraDevice.openCamera(CameraController.this);
            }
        });
    }

    private CameraDevice mCameraDevice;
    private CameraStateCallback mCameraStateCallback;

    public void openCameraExt(int cameraId, PreviewSurfaceView previewSurfaceView,
                              CameraStateCallback callback) {
        LogUtil.i(TAG, "openCameraExt cameraId=" + cameraId);
        mCameraStateCallback = callback;
        CameraDevice device = mCameraDevices.get(cameraId);
        LogUtil.i(TAG, "openCameraExt device=" + device);
        if (device == null || device.getState() == CameraDevice.State.CLOSE) {
            if (mCameraDevice == null) {
                mCameraDevice = new CameraDevice(cameraId, previewSurfaceView, this);
                mCameraDevice.setPreviewCallback(this);
                mThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCameraDevice.openCamera(CameraController.this);
                    }
                });
            } else {
                mThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCameraDevice.openCamera(CameraController.this);
                    }
                });
            }
        } else {
            Toast.makeText(mActivity, "相机已经是打开，请先关闭再打开。", Toast.LENGTH_SHORT).show();
        }
    }

    public void closeCameraExt() {
        LogUtil.i(TAG, "closeCameraExt");
        if (mCameraDevice != null) {
            mCameraDevice.closeCamera();
            mCameraDevice = null;
        }
        mCameraStateCallback = null;
    }

    public void startPreview() {
        int size = mCameraDevices.size();
        for (int i = 0; i < size; i++) {
            mCameraDevices.valueAt(i).startPreview();
        }
    }

    public void startPreview(int cameraId) {
        mCameraDevices.get(cameraId).startPreview();
    }

    public void stopPreview() {
        int size = mCameraDevices.size();
        for (int i = 0; i < size; i++) {
            mCameraDevices.valueAt(i).stopPreview();
        }
    }

    public void stopPreview(int cameraId) {
        mCameraDevices.get(cameraId).stopPreview();
    }

    public void closeCamera() {
        int size = mCameraDevices.size();
        for (int i = 0; i < size; i++) {
            mCameraDevices.valueAt(i).closeCamera();
        }
        mCameraDevices.clear();
        mRGBPreviewCallbacks.clear();
        mInfraredPreviewCallbacks.clear();

        size = mCameraStateCallbacks.size();
        for (int i = 0; i < size; i++) {
            ArrayList list = mCameraStateCallbacks.get(i);
            if (list != null) {
                list.clear();
            }
        }
        mCameraStateCallbacks.clear();
    }

    public void closeCamera(int cameraId) {
        mCameraDevices.get(cameraId).closeCamera();
        mCameraDevices.remove(cameraId);
        if (cameraId == CAMERA_ID_RGB) {
            mRGBPreviewCallbacks.clear();
        } else if (cameraId == CAMERA_ID_INFRARED) {
            mInfraredPreviewCallbacks.clear();
        }
    }

    public static void destroy() {
        if (mInstance != null) {
            mInstance.stopThread();
            mInstance = null;
        }
    }

    public boolean containStateCallback(int cameraId, CameraStateCallback cameraStateCallback) {
        if (mCameraStateCallbacks.get(cameraId) == null) {
            return false;
        }
        return mCameraStateCallbacks.get(cameraId).contains(cameraStateCallback);
    }

    public int getRotation() {
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        return rotation;
    }

    public int getOrientation() {
        return mActivity.getResources().getConfiguration().orientation;
    }

    public CameraDevice getCameraDevice(int cameraId) {
        if (mCameraDevice != null && cameraId == mCameraDevice.getCameraId()) {
            return mCameraDevice;
        }
        return mCameraDevices.get(cameraId);
    }

    public synchronized void addRGBPreviewCallback(PreviewCallback callback) {
        mRGBPreviewCallbacks.add(callback);
    }

    public synchronized void addInfraredPreviewCallback(PreviewCallback callback) {
        mInfraredPreviewCallbacks.add(callback);
    }

    public synchronized void removeRGBPreviewCallback(PreviewCallback callback) {
        mRGBPreviewCallbacks.remove(callback);
    }

    public synchronized void removeInfraredPreviewCallback(PreviewCallback callback) {
        mInfraredPreviewCallbacks.remove(callback);
    }

    private synchronized void notifyRGBPreviewCallback(byte[] data) {
        /*if (imageBase64Callbacks.size() > 0 && !screenShotWorking) {
            byte[] copy = new byte[data.length];
            System.arraycopy(data, 0, copy, 0, copy.length);
            mThreadHandler.obtainMessage(MSG_SCREENSHOT, copy).sendToTarget();
        }*/
        for (PreviewCallback callback : mRGBPreviewCallbacks) {
            /*byte[] dataCopy = new byte[data.length];
            System.arraycopy(data, 0, dataCopy, 0, data.length);*/
            callback.onPreviewFrame(data);
        }
    }

    private synchronized void notifyInfraredPreviewCallback(byte[] data) {
        for (PreviewCallback callback : mInfraredPreviewCallbacks) {
            callback.onPreviewFrame(data);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, int cameraId) {
        //Log.d("sqm", "onPreviewFrame ==============id:"+cameraId);
        if (cameraId == CAMERA_ID_RGB) {
            notifyRGBPreviewCallback(data);
        } else if (cameraId == CAMERA_ID_INFRARED) {
            notifyInfraredPreviewCallback(data);
        }
    }

    public void showCameraView(int cameraId) {
        Size size = getMaxCameraViewSize();
        CameraDevice cameraDevice = getCameraDevice(cameraId);
        if (cameraDevice != null) {
            cameraDevice.changeCameraViewSize(size);
        }
    }

    public void hideCameraView(int cameraId) {
        Size size = getMinCameraViewSize();
        CameraDevice cameraDevice = getCameraDevice(cameraId);
        if (cameraDevice != null) {
            cameraDevice.changeCameraViewSize(size);
        }
    }

    public void showVideoView(int cameraId) {
        Size size = getVideoViewSize();
        CameraDevice cameraDevice = getCameraDevice(cameraId);
        if (cameraDevice != null) {
            cameraDevice.changeCameraViewSize(size);
        }
    }

    public Size getMaxCameraViewSize() {
        return new Size(mMaxWidth, mMaxHeight);
    }

    public Size getMinCameraViewSize() {
        return new Size(1, 1);
    }

    public Size getVideoViewSize() {
        return new Size(240, 320);
    }

    public void getRgbImageBase64(ImageBase64Callback callback, boolean fullCapture, boolean faceCapture) {
        synchronized (imageBase64Callbacks) {
            imageBase64Callbacks.add(callback);
            mFullCapture = fullCapture;
            mFaceCapture = faceCapture;
        }
    }

    public int getCaptureRequestSize() {
        synchronized (imageBase64Callbacks) {
            return imageBase64Callbacks.size();
        }
    }

    public void capture(Bitmap bmp, List<Rect> rectList, int maxIndex) {
        if (imageBase64Callbacks.size() > 0 && !screenShotWorking) {
            CaptureArgs captureArgs = new CaptureArgs(bmp, rectList, maxIndex);
            mThreadHandler.obtainMessage(MSG_CAPTURE, captureArgs).sendToTarget();
        }
    }

    @Override
    public void onStateChange(CameraDevice cameraDevice, CameraDevice.State oldState, CameraDevice.State newState, int cameraId) {
        ArrayList<CameraStateCallback> stateCallbacks = mCameraStateCallbacks.get(cameraId);
        if (stateCallbacks != null) {
            for (CameraStateCallback stateCallback : stateCallbacks) {
                stateCallback.onStateChange(cameraDevice, oldState, newState, cameraId);
            }
        }
        if (mCameraStateCallback != null) {
            mCameraStateCallback.onStateChange(cameraDevice, oldState, newState, cameraId);
        }
    }

    public interface CameraContext {
        PreviewSurfaceView getCameraView(int cameraId);

        Activity getActivity();
    }

    public interface PreviewCallback {
        void onPreviewFrame(byte[] data);
    }

    public interface CameraStateCallback {
        void onStateChange(CameraDevice cameraDevice, CameraDevice.State oldState, CameraDevice.State newState, int cameraId);
    }

    public interface ImageBase64Callback {
        void onResult(String fullCaptureBase64, String faceCaptureBase64);
    }

    class CaptureArgs {
        Bitmap bitmap;
        List<Rect> rectList;
        int maxIndex;

        public CaptureArgs(Bitmap bitmap, List<Rect> rectList, int maxIndex) {
            this.bitmap = bitmap;
            this.rectList = rectList;
            this.maxIndex = maxIndex;
        }
    }
}
