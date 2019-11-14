package com.android.face;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.android.face.alarm.AlarmController;
import com.android.face.dialpad.DialpadManager;
import com.android.face.faceapi.readsense.YMFaceTrackManager;
import com.android.face.live.LiveManager;
import com.android.face.mcu.McuManager;
import com.android.face.net.ClientCallback;
import com.android.face.record.AVmediaMuxer;
import com.android.face.register.RegisterManager;
import com.android.face.util.SoundEffectsUtil;
import com.android.rgk.common.camera.CameraController;
import com.android.rgk.common.camera.CameraDevice;
import com.android.rgk.common.camera.ui.PreviewSurfaceView;
import com.android.rgk.common.db.DataExecutor;
import com.android.rgk.common.lock.LockManager;
import com.android.rgk.common.net.MqttManager;
import com.android.rgk.common.net.MqttTopics;
import com.android.rgk.common.util.LogUtil;
import com.android.rgk.common.util.NetUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ragentek.face.R;

import java.util.ArrayList;

import static com.android.face.ViewManager.VIEW_LAYER_DIALPAD;

public class CameraActivity extends BaseActivity implements CameraController.CameraContext,
        CameraController.CameraStateCallback, ViewManager.IParentView, ProximitySensor.Listener,
        LightSensor.Listener {
    private static final String TAG = "CameraActivity";
    private CameraController mCameraController;

    private Button btnStart;
    private ViewGroup mDialpadLayer;
    private ViewGroup mOverlayLayer;

    private boolean isStarted;
    private AVmediaMuxer mediaMuxer;

    private RegisterManager mRegisterManager;
    private FaceManager mFaceManager;
    private LiveManager mLiveManager;

    private DialpadManager mDialpadManager;

    private MqttManager mMqttManager;
    // private NfcManager mNfcManager;

    private ProximitySensor mProximitySensor;
    private LightSensor mLightSensor;

    private IsNear mIsNear = IsNear.INIT;

    private enum IsNear {
        INIT,
        TRUE,
        FALSE
    }

    private AlarmController mAlarmController;

    private static final ArrayList<String> TOPICS = new ArrayList<>();

    static {
        TOPICS.add(MqttTopics.DISPATCH_FILTER_ITEM);
        TOPICS.add(MqttTopics.DISPATCH_FILTER_LIST);
        TOPICS.add(MqttTopics.SIP_ITEM);
        TOPICS.add(MqttTopics.SIP_ITEM_LIST);
        TOPICS.add(MqttTopics.SIP_CONFIG_INFO);
        TOPICS.add(MqttTopics.DISPATCH_OTP);
        TOPICS.add(MqttTopics.UPGRADE_REQUEST);
        TOPICS.add(MqttTopics.DEVICE_CONTROL_REQUEST);
        TOPICS.add(MqttTopics.FACE_ADD);
        TOPICS.add(MqttTopics.FACE_DELETE);
        TOPICS.add(MqttTopics.TIME_CALIBRATE);
        //add by v8.4
        TOPICS.add(MqttTopics.DISPATCH_SIP_ITEM);
        TOPICS.add(MqttTopics.DISPATCH_AD_URL);
        TOPICS.add(MqttTopics.DISPATCH_DOWNLOAD_URL);
        TOPICS.add(MqttTopics.DISPATCH_GROUP_CODES);
        TOPICS.add(MqttTopics.DISPATCH_READING_HEAD_PROGRAM_URL);
    }

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
                            mFaceManager.onYmFaceTrackInit();
                        }
                    }
                    break;
            }
        }
    };

    private static final int MSG_NO_PEOPLE = 1000;
    private static final int DELAY_TIME = 10 * 1000;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NO_PEOPLE: {
                    mIsNear = IsNear.FALSE;
                    onProximityNegative();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);
        initViews();

        boolean success = YMFaceTrackManager.init(getApplicationContext());
        if (!success) {
            hasNetReceiver = true;
            mFilter = new IntentFilter();
            mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            getApplicationContext().registerReceiver(mNetBroadcastReceiver, mFilter);
        }

        CameraController.init(this);
        mCameraController = CameraController.getInstance();
        mRegisterManager = new RegisterManager(this, this, mCameraController);
        mFaceManager = new FaceManager(this, this, mCameraController);
        mFaceManager.onCreate();

        mDialpadManager = new DialpadManager(this, this);

        mLiveManager = new LiveManager(this, this, mCameraController);

        mMqttManager = MqttManager.getInstance();
        mMqttManager.subscribeTopics(TOPICS, new ClientCallback());

        //Nfc
        // mNfcManager = new NfcManager();
        // mNfcManager.startReadNfc();

        mProximitySensor = new ProximitySensor(CameraActivity.this);
        mLightSensor = new LightSensor(CameraActivity.this);

        mAlarmController = new AlarmController(getApplicationContext());
        LockManager.getInstance().setStateCallback(new LockStateCallback(mAlarmController));
        McuManager.getInstance().onCreate();
        SoundEffectsUtil.load();
    }

    private void initViews() {
        mDialpadLayer = (ViewGroup) findViewById(R.id.dialpad_layer);
        mOverlayLayer = (ViewGroup) findViewById(R.id.overlay_layer);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setText("开始");
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //codecToggle();
                mFaceManager.onStop();
                if (YMFaceTrackManager.isInitialized()) {
                    mRegisterManager.show(ViewManager.VIEW_LAYER_2);
                }
                LockManager.getInstance().writeNvStr(200, "0");
            }
        });
        //btnStart.setEnabled(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mDialpadManager != null && keyCode != KeyEvent.KEYCODE_F4 && keyCode != KeyEvent.KEYCODE_BACK) {
            mDialpadManager.show(VIEW_LAYER_DIALPAD);
            mDialpadManager.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_F4 && mAlarmController.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mProximitySensor.addListener(this);
        mLightSensor.addListener(this);
        mCameraController.openCamera(null);
        if (mDialpadManager.isShow()) {
            mDialpadManager.resetInfo();
            mDialpadManager.resetTime();
        } else {
            mFaceManager.onStart();
        }
        mLiveManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFaceManager.onResume();
        mLiveManager.onResume();

        rebootLog();
    }

    private void rebootLog() {
        LockManager lockManager = LockManager.getInstance();
        String str = lockManager.readNvStr(200);
        int count = 0;
        if (str == null || str.length() == 0) {
            count = 0;
            lockManager.writeNvStr(200, String.valueOf(0));
        } else {
            count = Integer.parseInt(lockManager.readNvStr(200));
            lockManager.writeNvStr(200, String.valueOf(++count));
        }
        btnStart.setText("start: " + count);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFaceManager.onPause();
        mLiveManager.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProximitySensor.removeListener(this);
        mLightSensor.removeListener(this);
        mFaceManager.onStop();
        mLiveManager.onStop();
        mCameraController.closeCamera();
        LockManager.getInstance().setLightValue(0);
        if (mIdleView != null) {
            removeView(mIdleView);
            mIdleView = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (mRegisterManager.onBackPressed()) {
            //Do nothing
        } else if (mRegisterManager.isShow()) {
            mRegisterManager.hide();
            mFaceManager.onStart();
        } else if (mDialpadManager.isShow()) {
            mDialpadManager.hide();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hasNetReceiver) {
            getApplicationContext().unregisterReceiver(mNetBroadcastReceiver);
            mNetBroadcastReceiver = null;
        }
        LockManager.getInstance().setStateCallback(null);
        McuManager.getInstance().onDestroy();
        SoundEffectsUtil.release();
        // mNfcManager.stopReadNfc();
        if (isStarted) {
            isStarted = false;
            mediaMuxer.stop();
            mediaMuxer = null;
        }
        mFaceManager.onDestroy();
        mLiveManager.onDestroy();
        CameraController.destroy();
        if (YMFaceTrackManager.isInitialized()) {
            YMFaceTrackManager.release();
        }
        MqttManager.destroy();
        mProximitySensor.release();
        mLightSensor.release();
        DataExecutor.release();
    }

    private void codecToggle() {
        if (isStarted) {
            isStarted = false;
        } else {
            isStarted = true;
        }
        btnStart.setText(isStarted ? "停止" : "开始");
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
            mHandler.removeMessages(MSG_NO_PEOPLE);
            if (mIdleView != null) {
                removeView(mIdleView);
                mIdleView = null;
            }
            mDialpadLayer.addView(view);
            mFaceManager.onStop();
            mCameraController.hideCameraView(CameraController.CAMERA_ID_RGB);

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
            mDialpadLayer.removeView(view);
            mCameraController.showCameraView(CameraController.CAMERA_ID_RGB);
            if (mProximitySensor.isNear()) {
                mHandler.removeMessages(MSG_NO_PEOPLE);
                if (mCameraController.getCameraDevice(CameraController.CAMERA_ID_INFRARED) == null) {
                    mCameraController.openCamera(CameraController.CAMERA_ID_INFRARED, null);
                }
            } else {
                mHandler.sendEmptyMessageDelayed(MSG_NO_PEOPLE, DELAY_TIME);
            }
            mFaceManager.onStart();
        } else if (layer == ViewManager.VIEW_LAYER_2) {
            removeView(view);
        }
    }

    @Override
    public void timeOut(final View view, int layer) {
        if (layer == VIEW_LAYER_DIALPAD) {
            if (mProximitySensor.isNear()) {
                mDialpadManager.finish();
            } else {
                mHandler.sendEmptyMessage(MSG_NO_PEOPLE);
            }
        }
    }

    @Override
    public void onStateChange(CameraDevice camera, CameraDevice.State oldState,
                              CameraDevice.State newState, int cameraId) {
    }

    @Override
    public void onProximityChange(boolean isNear) {
        if (isNear) {
            mHandler.removeMessages(MSG_NO_PEOPLE);
            if (mIsNear != IsNear.TRUE) {
                mIsNear = IsNear.TRUE;
                onProximityPositive();
            }
        } else {
            mHandler.sendEmptyMessageDelayed(MSG_NO_PEOPLE, DELAY_TIME);
        }
    }

    @Override
    public void onLightChange(float value) {
        if (mLightSensor.isDark() && mProximitySensor.isNear()) {
            LockManager.getInstance().setLightValue(1);
        } else if (!mProximitySensor.isNear()) {
            LockManager.getInstance().setLightValue(0);
        }
    }

    private ImageView mIdleView;

    private void onProximityPositive() {
        LogUtil.i(TAG, "onProximityPositive and open light");
        if (mLightSensor.isDark()) {
            LockManager.getInstance().setLightValue(1);
        }
        if (!mDialpadManager.ignoreProximity()) {
            LogUtil.i(TAG, "onProximityPositive open infrared camera");
            mDialpadManager.finish();
            mFaceManager.onStart();
        }

        if (mIdleView != null) {
            removeView(mIdleView);
            mIdleView = null;
        }
    }

    private void onProximityNegative() {
        LogUtil.i(TAG, "onProximityNegative and close light");
        LockManager.getInstance().setLightValue(0);
        LogUtil.i(TAG, "onProximityNegative close infrared camera");
        mFaceManager.onStop();

        if (!mDialpadManager.ignoreProximity()) {
            if (mIdleView == null) {
                mIdleView = new ImageView(getApplicationContext());
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                mIdleView.setLayoutParams(params);
                mIdleView.setScaleType(ImageView.ScaleType.FIT_XY);
                addView(mIdleView);
            }
            String img = "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3278856638,1671144307&fm=15&gp=0.jpg";
            Glide.with(getApplicationContext()).load(img).listener(new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    if (mIdleView != null) {
                        removeView(mIdleView);
                        mIdleView = null;
                    }
                    if (!mDialpadManager.isShow()) {
                        mDialpadManager.show(VIEW_LAYER_DIALPAD);
                        mDialpadManager.setTimeOut();
                    }
                    mDialpadManager.cleanInfo();
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    LogUtil.i(TAG, "onProximityNegative and onResourceReady close infrared camera");
                    mDialpadManager.finish();
                    return false;
                }
            }).into(mIdleView);
        }
    }
}
