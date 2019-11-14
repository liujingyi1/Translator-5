package com.android.face;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.android.face.faceapi.readsense.YMFaceTrackManager;
import com.android.face.register.RegisterManager;
import com.android.rgk.common.camera.CameraController;
import com.android.rgk.common.camera.CameraDevice;
import com.android.rgk.common.camera.ui.PreviewSurfaceView;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.util.LogUtil;
import com.android.rgk.common.util.NetUtil;
import com.ragentek.face.R;

import static com.android.face.ViewManager.VIEW_LAYER_DIALPAD;

public class RegisterActivity extends BaseActivity implements CameraController.CameraContext,
        CameraController.CameraStateCallback, ViewManager.IParentView {
    private static final String TAG = "RegisterActivity";
    private ViewGroup mOverlayLayer;

    private CameraController mCameraController;
    private boolean mCameraControllerCreated;

    private RegisterManager mRegisterManager;

    private IntentFilter mFilter;
    private boolean hasNetReceiver = false;
    private BroadcastReceiver mNetBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.i("CameraActivity", "mNetBroadcastReceiver.onReceive :" + action);
            switch (action) {
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    boolean connected = NetUtil.hasConnect(context);
                    if (connected) {
                        boolean success = YMFaceTrackManager.init(context);
                        if (success) {
                            context.unregisterReceiver(mNetBroadcastReceiver);
                            mNetBroadcastReceiver = null;
                            hasNetReceiver = false;
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);
        initViews();

        if (DataOperator.getInstance() == null) {
            DataOperator.init(getApplicationContext());
        }

        boolean success = YMFaceTrackManager.init(getApplicationContext());
        if (!success) {
            hasNetReceiver = true;
            mFilter = new IntentFilter();
            mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            getApplicationContext().registerReceiver(mNetBroadcastReceiver, mFilter);
        }

        if (CameraController.getInstance() == null) {
            CameraController.init(this);
            mCameraControllerCreated = true;
        }
        mCameraController = CameraController.getInstance();
        mRegisterManager = new RegisterManager(this, this, mCameraController);
    }

    private void initViews() {
        mOverlayLayer = (ViewGroup) findViewById(R.id.overlay_layer);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mRegisterManager.onKeyDown(keyCode,event)){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(mRegisterManager.onKeyUp(keyCode,event)){
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCameraController.openCameraExt(CameraController.CAMERA_ID_RGB,
                getCameraView(CameraController.CAMERA_ID_RGB), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraController.closeCameraExt();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hasNetReceiver) {
            getApplicationContext().unregisterReceiver(mNetBroadcastReceiver);
            mNetBroadcastReceiver = null;
        }
        if (mCameraControllerCreated) {
            CameraController.destroy();
        }
    }

    @Override
    public PreviewSurfaceView getCameraView(int cameraId) {
        if (cameraId == CameraController.CAMERA_ID_RGB) {
            return (PreviewSurfaceView) findViewById(R.id.rgb_camera_surface);
        } else if (cameraId == CameraController.CAMERA_ID_INFRARED) {
            return (PreviewSurfaceView) findViewById(R.id.infrared_camera_surface);
        }
        return null;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    public void addView(View view) {
        mOverlayLayer.addView(view);
    }

    @Override
    public void addView(View view, int layer) {
        if (layer == VIEW_LAYER_DIALPAD) {
        } else if (layer == ViewManager.VIEW_LAYER_2) {
            addView(view);
        }
    }

    public void removeView(View view) {
        mOverlayLayer.removeView(view);
    }

    @Override
    public void removeView(View view, int layer) {
        if (layer == VIEW_LAYER_DIALPAD) {
        } else if (layer == ViewManager.VIEW_LAYER_2) {
            removeView(view);
        }
    }

    @Override
    public void timeOut(final View view, int layer) {

    }

    @Override
    public void onStateChange(CameraDevice camera, CameraDevice.State oldState,
                              CameraDevice.State newState, int cameraId) {
        if (cameraId == CameraController.CAMERA_ID_RGB && newState == CameraDevice.State.PREVIEW_START) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (YMFaceTrackManager.isInitialized()) {
                        mRegisterManager.show(ViewManager.VIEW_LAYER_2);
                    }
                }
            });
        }
    }
}
