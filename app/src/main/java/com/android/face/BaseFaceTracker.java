package com.android.face;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.rgk.common.Constant;
import com.android.rgk.common.camera.CameraController;
import com.android.rgk.common.camera.CameraDevice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import readsense.api.enity.YMFace;
import readsense.api.info.RSImageRotation;

public abstract class BaseFaceTracker implements CameraController.PreviewCallback,
        CameraController.CameraStateCallback, IFaceTrackUI {
    protected CameraController mCameraController;

    private final Object lock = new Object();

    private StringBuffer fps;
    private boolean showFps = false;
    private List<Float> timeList = new ArrayList<>();
    protected int iw = 0, ih;
    private float scaleBit;
    protected boolean stop = true;//表示是否已经开始进行人脸分析

    protected int screenW;
    protected int screenH;
    private int cameraFps;
    private int cameraCount;
    private long cameraTime = 0;

    private Camera.Size mPreviewSize;

    protected Activity mActivity;

    public BaseFaceTracker(Activity activity, CameraController cameraController) {
        mActivity = activity;
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
        WindowManager windowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        screenW = displayMetrics.widthPixels;
        screenH = displayMetrics.heightPixels;
        mCameraController = cameraController;
    }

    public void onResume() {
        synchronized (lock) {
            startTrack();
        }
        CameraDevice cameraDevice = mCameraController.getCameraDevice(CameraController.CAMERA_ID_RGB);
        if (cameraDevice == null) {
            mCameraController.openCamera(CameraController.CAMERA_ID_RGB, this);
        } else {
            mPreviewSize = cameraDevice.getPreviewSize();
            if (mPreviewSize == null) {
                mCameraController.openCamera(CameraController.CAMERA_ID_RGB, this);
            } else {
                initCameraMsg();
            }
        }
        mCameraController.addRGBPreviewCallback(this);
    }

    public void onPause() {
        mCameraController.removeRGBPreviewCallback(this);
        synchronized (lock) {
            stopTrack();
        }
    }

    /**
     * 初始化
     */
    public void startTrack() {
        if (stop) {
            stop = false;
        }
    }

    public void stopTrack() {
        stop = true;
        iw = 0;//重新调用initCameraMsg的开关
    }

    /**
     * 开始追踪人脸，并绘制人脸框
     *
     * @param data
     */
    private void runTrack(byte[] data) {
        try {
            long time = System.currentTimeMillis();
            final List<YMFace> faces = analyse(data, iw, ih);
            afterAnalyse(data, iw, ih, faces);
            fps = new StringBuffer();
            if (showFps) {
                fps.append("fps = ");
                long now = System.currentTimeMillis();
                float than = now - time;
                timeList.add(than);
                if (timeList.size() >= 20) {
                    float sum = 0;
                    for (int i = 0; i < timeList.size(); i++) {
                        sum += timeList.get(i);
                    }
                    fps.append((int) (1000f * timeList.size() / sum));
                    timeList.remove(0);
                }
            }
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawAnim(faces, getDrawView(), scaleBit, CameraController.CAMERA_ID_RGB, fps.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化sfv_draw_view及YMFaceTrack的方向
     */
    private void initCameraMsg() {
        if (iw == 0 && !stop) {
            CameraDevice cameraDevice = mCameraController.getCameraDevice(CameraController.CAMERA_ID_RGB);
            int surfaceW;
            int surfaceH;
            Size cameraViewSize = cameraDevice.getCameraViewSize();
            if (cameraViewSize != null) {
                surfaceW = cameraViewSize.getWidth();
                surfaceH = cameraViewSize.getHeight();
            } else {
                SurfaceView previewView = cameraDevice.getCameraView();
                surfaceW = previewView.getWidth();
                surfaceH = previewView.getHeight();
            }

            iw = mPreviewSize.width;
            ih = mPreviewSize.height;

            int orientation = 0;
            ////注意横屏竖屏问题
            //DLog.d(getResources().getConfiguration().orientation + " : " + Configuration.ORIENTATION_PORTRAIT);
            if (screenW < screenH) {//竖屏
                scaleBit = surfaceW / (float) ih;//preview的宽高是由手机Camera的取景方向定，而Camer的取景方向水平方向。
                if (CameraController.CAMERA_ID_RGB == Camera.CameraInfo.CAMERA_FACING_FRONT) {//前置摄像头
                    orientation = RSImageRotation.RS_IMG_CLOCKWISE_ROTATE_270;
                } else {//后置摄像头
                    orientation = RSImageRotation.RS_IMG_CLOCKWISE_ROTATE_0;
                }
            } else {//横屏
                scaleBit = surfaceH / (float) ih;
                orientation = RSImageRotation.RS_IMG_CLOCKWISE_ROTATE_0;

                /*if (BaseApplication.reverse_180) {
                    orientation += 180;
                }*/
            }
            //特殊设备识别角度设置
            /*if (Constant.specialAngle) {
                orientation = SharedPrefUtils.getInt(ExApplication.getContext(), Constant.SPECIAL_ANGLE_ANGLE, 0);
            }*/
            Constant.SDKOrientation = orientation;
            SurfaceView drawView = getDrawView();
            if (drawView != null) {
                ViewGroup.LayoutParams params = drawView.getLayoutParams();
                params.width = surfaceW;
                params.height = surfaceH;
                drawView.requestLayout();
            }
        }
    }

    /**
     * 抽象的绘制人脸框的方法，需子类继承实现
     *
     * @param faces
     * @param drawView
     * @param scaleBit
     * @param cameraId
     * @param fps
     */
    protected abstract void drawAnim(List<YMFace> faces, SurfaceView drawView, float scaleBit, int cameraId, String fps);

    /**
     * 抽象的分析人脸的方法，需子类继承实现
     *
     * @param data
     * @param iw
     * @param ih
     * @return
     */
    protected abstract List<YMFace> analyse(byte[] data, int iw, int ih);

    protected abstract SurfaceView getDrawView();

    protected abstract void afterAnalyse(byte[] data, int iw, int ih, List<YMFace> ymFaces);

    protected void showShortToast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPreviewFrame(byte[] data) {
        if (stop) {
            return;
        }
        if (cameraTime == 0) {
            cameraTime = System.currentTimeMillis();
        }
        cameraCount++;
        if (System.currentTimeMillis() - cameraTime > 1000) {
            cameraFps = cameraCount;
            cameraCount = 0;
            cameraTime = 0;
        }
        if (!stop) {
            synchronized (lock) {
                runTrack(data);
            }
        }
    }

    @Override
    public void onStateChange(CameraDevice camera, CameraDevice.State oldState,
                              CameraDevice.State newState, int cameraId) {
        if (newState == CameraDevice.State.PARAMETER_READY
                || newState == CameraDevice.State.PREVIEW_START) {
            mPreviewSize = camera.getPreviewSize();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initCameraMsg();
                }
            });
        }
    }

    /**
     * 深拷贝list
     *
     * @param src
     * @param <T>
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(src);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
        List<T> dest = (List<T>) inputStream.readObject();

        if (outputStream != null) {
            outputStream.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
        return dest;
    }
}
