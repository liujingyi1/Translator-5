package com.android.face.live;

import android.app.Activity;
import android.view.View;

import com.android.face.ViewManager;
import com.android.rgk.common.camera.CameraController;
import com.android.rgk.common.util.LogUtil;
import com.spark.live.sdk.engine.ISimpleLiveEngine;
import com.spark.live.sdk.engine.ISimpleLiveEngineEventCallback;
import com.spark.live.sdk.engine.SimpleLivePulisherEngine;

public class LiveManager extends ViewManager {
    private static final String TAG = "LiveManager";
    private Activity mActivity;
    private CameraController mCameraController;
    private ISimpleLiveEngine mISimpleLiveEngine;

    private static final String RTMP_URL = "rtmp://220.248.34.75:1935/live/camera_2";

    private ISimpleLiveEngineEventCallback engineEventCallback = new ISimpleLiveEngineEventCallback() {
        @Override
        public void onGotECliveEngineEvent(int state, String note) {
            LogUtil.d(TAG, "onGotECliveEngineEvent: " + state + ",rtmpUrl:" + note);
        }
    };

    public LiveManager(IParentView iParentView, Activity activity, CameraController cameraController) {
        super(iParentView);
        mActivity = activity;
        mCameraController = cameraController;
        mISimpleLiveEngine = new SimpleLivePulisherEngine(mActivity, mCameraController, RTMP_URL);
        mISimpleLiveEngine.setStateCallback(engineEventCallback);
    }

    @Override
    public View getView(int layer) {
        return null;
    }

    public void onStart() {
    }

    public void onResume() {
        mISimpleLiveEngine.resume();
    }

    public void onPause() {
        mISimpleLiveEngine.pause();
    }

    public void onStop() {
    }

    public void onDestroy() {
        mISimpleLiveEngine.destroy();
        mISimpleLiveEngine = null;
    }
}
