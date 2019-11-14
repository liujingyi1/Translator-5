package com.android.rgk.common.camera;

import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.android.rgk.common.camera.ui.PreviewSurfaceView;
import com.rgk.cameralib.CameraProxy;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.hardware.Camera.Parameters.PREVIEW_FPS_MAX_INDEX;
import static android.hardware.Camera.Parameters.PREVIEW_FPS_MIN_INDEX;

public class CameraDevice implements SurfaceHolder.Callback {
    private CameraController mController;
    private int mCameraId;
    private CameraProxy mCamera;
    private Camera.CameraInfo mCameraInfo;
    private PreviewSurfaceView mCameraView;
    private Camera.Size mPreviewSize;
    private int mOrientationDegree;

    private boolean mSurfaceCreated;
    private boolean mPendingOpen;

    private PreviewCallback mPreviewCallback;
    private StateCallback mStateCallback;
    private Callback mCallback;

    private CameraConfig mCameraConfig;

    private Size mCameraViewSize;

    public enum State {
        CLOSE,
        OPEN,
        PARAMETER_READY,
        PREVIEW_START,
        PREVIEW_STOP
    }

    private State mState = State.CLOSE;

    protected CameraDevice(int cameraId, PreviewSurfaceView cameraView, CameraController controller) {
        Log.d("sqm", "init:" + cameraId + ", " + cameraView);
        mCameraId = cameraId;
        mCameraView = cameraView;
        mCameraView.getHolder().addCallback(this);
        mController = controller;
        //if (cameraId == CameraController.CAMERA_ID_RGB) {
        mCameraConfig = CameraConfig.getInstance(cameraId);
        //}
    }

    protected void openCamera(Callback callback) {
        mCallback = callback;
        openCamera();
    }

    protected void openCamera(StateCallback callback) {
        mStateCallback = callback;
        openCamera();
    }

    protected void openCamera() {
        Log.d("sqm", "CameraDevice openCamera");
        if (!mSurfaceCreated && !mCameraView.isSurfaceCreated()) {
            mPendingOpen = true;
            return;
        }
        closeCamera();
        mCamera = CameraProxy.open(mCameraId);
        mCameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, mCameraInfo);
        setState(State.OPEN);
        try {
            mCamera.setPreviewDisplay(mCameraView.getHolder());
            setCameraDisplayOrientation();
            setCameraParameters();
            setFocusCallback();
            setState(State.PARAMETER_READY);
            startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public State getState() {
        return mState;
    }

    private void setState(State state) {
        State old = mState;
        mState = state;
        if (mStateCallback != null) {
            mStateCallback.onStateChange(this, old, mState, mCameraId);
        }
    }

    private void enableAutoFocus(Camera.Parameters parameters) {
        Log.d("sqm", "enableAutoFocus:");
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCamera.cancelAutoFocus();
    }

    private void setFocusCallback() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                mCamera.cancelAutoFocus();
            }
        });

        mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
            @Override
            public void onAutoFocusMoving(boolean b, Camera camera) {
            }
        });
    }

    protected void startPreview() {
        if (mCamera == null || mState == State.PREVIEW_START) {
            return;
        }
        mCamera.startPreview();
        mCamera.setOneShotPreviewCallback(mOneShot);
    }

    protected void stopPreview() {
        if (mCamera == null || mState == State.PREVIEW_STOP) {
            return;
        }
        mCamera.stopPreview();
        setState(State.PREVIEW_STOP);
    }

    protected synchronized void closeCamera() {
        if (mCamera != null) {
            mPendingOpen = false;
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            setState(State.CLOSE);
            mCameraView.getHolder().removeCallback(this);
        }
    }

    private void setCameraParameters() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

        // Set preview size.
        setOptimalPreviewSize(parameters);

        //set fps range.
        setFps(parameters);

        enableAutoFocus(parameters);

        mCamera.setParameters(parameters);

        setCameraConfig();
    }

    private void setCameraConfig() {
        if (mCameraConfig != null) {
            mCameraConfig.setFacing(mCameraInfo.facing);
            mCameraConfig.setPreviewSize(new Point(mPreviewSize.width, mPreviewSize.height));
            mCameraConfig.setFps(mFrameRate);
            mCameraConfig.setOrientationDegree(mOrientationDegree);
            mCameraConfig.setPreviewFormat(ImageFormat.NV21);
            mCameraConfig.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCameraConfig.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            mCameraConfig.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        }
    }

    private int mFrameRate;

    private void setFps(Camera.Parameters parameters) {
        int minFps = 0;
        int maxFps = 0;
        List<int[]> supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        for (int[] fps : supportedPreviewFpsRange) {
            if (minFps <= fps[PREVIEW_FPS_MIN_INDEX] && maxFps <= fps[PREVIEW_FPS_MAX_INDEX]) {
                minFps = fps[PREVIEW_FPS_MIN_INDEX];
                maxFps = fps[PREVIEW_FPS_MAX_INDEX];
            }
        }
        //设置相机预览帧率
        Log.d("sqm", "CameraDevice setFps------minFps:" + minFps + ",maxFps: " + maxFps);
        parameters.setPreviewFpsRange(minFps, maxFps);
        mFrameRate = maxFps / 1000;
    }

    private int setCameraDisplayOrientation() {
        Log.d("sqm", "setCameraDisplayOrientation:");
        int rotation = mController.getRotation();
        short degrees = 0;
        switch (rotation) {
            case 0:
                degrees = 0;
                break;
            case 1:
                degrees = 90;
                break;
            case 2:
                degrees = 180;
                break;
            case 3:
                degrees = 270;
        }

        int result;
        if (mCameraInfo.facing == 1) {
            result = (mCameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
            /*if (this.rotate_front != -1) {
                result = this.rotate_front;
            }*/
        } else {
            result = (mCameraInfo.orientation - degrees + 360) % 360;
            /*if (this.rotate != -1) {
                result = this.rotate;
            }*/
        }

        Log.d("sqm", "setCameraDisplayOrientation " + rotation + " : " + mCameraInfo.orientation + " : " + result);
        mCamera.setDisplayOrientation(0);
        mOrientationDegree = 0;
        return 0;
    }

    private void setOptimalPreviewSize(Camera.Parameters parameters) {
        Log.d("sqm", "setOptimalPreviewSize:");
        int viewWidth = mCameraView.getWidth();
        int viewHeight = mCameraView.getHeight();
        float ratio = 0;
        if (mController.getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            ratio = (float) viewHeight / viewWidth;
        } else {
            ratio = (float) viewWidth / viewHeight;
        }
        Log.d("sqm", "setOptimalPreviewSize:" + viewWidth + "," + viewHeight + "," + ratio);
        Camera.Size previewSize = parameters.getPreviewSize();
        Log.d("sqm", "setOptimalPreviewSize default preview size:" + previewSize.width + "," + previewSize.height);
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size left, Camera.Size right) {
                int leftSize = left.width * left.height;
                int rightSize = right.width * right.height;
                if (leftSize > rightSize) {
                    return -1;
                } else if (leftSize < rightSize) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        if (null != supportedPreviewSizes) {
            float delta = 0.03f;
            for (Camera.Size size : supportedPreviewSizes) {
                Log.d("sqm", "setOptimalPreviewSize----------size.content:" + size.width + "," + size.height);
                float temp = Math.abs((float) size.height / size.width - ratio);
                Log.d("sqm", "setOptimalPreviewSize----------size.ratio=" + ((float) size.height / size.width));
                String previewSizeStr = size.width + "x" + size.height;
                if (temp < delta && CameraConfig.VIDEO_SIZES.contains(previewSizeStr)) {
                    delta = temp;
                    previewSize = size;
                }
            }
            Log.d("sqm", "setOptimalPreviewSize---end:ratio=" + ((float) previewSize.height / previewSize.width)
                    + "size=" + previewSize.width + "," + previewSize.height);
            mPreviewSize = previewSize;
            int width = previewSize.width;
            int height = previewSize.height;
            parameters.setPreviewSize(width, height);
            mCameraView.setAspectRatio((double) width / height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("sqm", "surfaceCreated");
        mSurfaceCreated = true;
        mCameraView.setSurfaceCreated(true);
        if (mPendingOpen) {
            openCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("sqm", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d("sqm", "surfaceDestroyed");
        mSurfaceCreated = false;
        closeCamera();
    }

    protected void changeCameraViewSize(Size size) {
        if (size == null) {
            return;
        }
        Log.d("sqm", "CameraDevice changeCameraViewSize: " + size);
        mCameraViewSize = size;
        ViewGroup.LayoutParams params = mCameraView.getLayoutParams();
        params.width = size.getWidth();
        params.height = size.getHeight();
        mCameraView.requestLayout();
    }

    public Size getCameraViewSize() {
        return mCameraViewSize;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public Camera getCamera() {
        return mCamera.getCamera();
    }

    public int getFrameRate() {
        return mFrameRate;
    }

    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    public SurfaceView getCameraView() {
        return mCameraView;
    }

    public int getCameraOrientationDegree() {
        return mOrientationDegree;
    }

    protected void setPreviewCallback(PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
    }

    protected void setCallback(Callback callback) {
        mCallback = callback;
    }

    protected void setStateCallback(StateCallback stateCallback) {
        mStateCallback = stateCallback;
    }

    private void setPreviewCallbackWithBuffer() {
        if (mCamera == null) {
            return;
        }
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (mPreviewCallback == null) {
                    return;
                }
                //mCamera.addCallbackBuffer(data);
                if (mCamera != null) {
                    mCamera.addCallbackBuffer(data);
                }
                mPreviewCallback.onPreviewFrame(data, mCameraId);
            }
        });
        mCamera.addCallbackBuffer(new byte[mPreviewSize.width * mPreviewSize.height
                * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
    }

    public interface Callback {
        void onCameraOpened(CameraDevice camera, int cameraId);
    }

    public interface PreviewCallback {
        void onPreviewFrame(byte[] data, int cameraId);
    }

    public interface StateCallback {
        void onStateChange(CameraDevice cameraDevice, State oldState, State newState, int cameraId);
    }

    private Camera.PreviewCallback mOneShot = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            setState(State.PREVIEW_START);
            if (mCallback != null) {
                mCallback.onCameraOpened(CameraDevice.this, mCameraId);
            }
            if (mPreviewCallback != null) {
                setPreviewCallbackWithBuffer();
            }
        }
    };
}
