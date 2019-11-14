package com.android.face.register;

import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.face.IFaceTrackUI;
import com.android.face.ViewManager;
import com.android.rgk.common.camera.CameraController;

import com.ragentek.face.R;

public class RegisterManager extends ViewManager implements View.OnClickListener {
    private Activity mActivity;
    private CameraController mCameraController;
    private IFaceTrackUI mIRegisterUI;

    private ViewGroup mRootView;
    private View mActionContainer;
    private Button mFromPicBtn;
    private Button mFromVideoBtn;
    private Button mFromCameraBtn;

    private LayoutInflater mInflater;

    public RegisterManager(IParentView parentView, Activity activity,
                           CameraController cameraController) {
        super(parentView);
        mActivity = activity;
        mCameraController = cameraController;
        mInflater = LayoutInflater.from(mActivity);
    }

    @Override
    public View getView(int layer) {
        if (layer == ViewManager.VIEW_LAYER_DIALPAD) {
            return null;
        }
        Log.d("sqmsqm", "Register getView");
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.register, null);
        initViews(view);

        return view;
    }

    private void initViews(View view) {
        mRootView = (ViewGroup) view.findViewById(R.id.root);
        mActionContainer = view.findViewById(R.id.action_container);
        mFromPicBtn = (Button) view.findViewById(R.id.from_pic);
        mFromPicBtn.setOnClickListener(this);
        mFromVideoBtn = (Button) view.findViewById(R.id.from_video);
        mFromVideoBtn.setOnClickListener(this);
        mFromCameraBtn = (Button) view.findViewById(R.id.from_camera);
        mFromCameraBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.from_pic: {
                mActionContainer.setVisibility(View.GONE);
                mIRegisterUI = new RegisterUIFromPic(mActivity);
                mIRegisterUI.createView(mInflater, mRootView);
                mIRegisterUI.onResume();
                break;
            }

            case R.id.from_video: {
                /*mActionContainer.setVisibility(View.GONE);
                mIRegisterUI = new RegisterUIFromVideo(mActivity, mCameraController);
                mIRegisterUI.createView(mInflater, mRootView);
                mIRegisterUI.onResume();*/
                break;
            }

            case R.id.from_camera: {
                mActionContainer.setVisibility(View.GONE);
                mIRegisterUI = new RegisterUIFromCamera(mActivity, mCameraController);
                mIRegisterUI.createView(mInflater, mRootView);
                mIRegisterUI.onResume();
                break;
            }

            default:
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("xxx","key = "+keyCode);
        switch (keyCode){
            case KeyEvent.KEYCODE_F3:
                if (mIRegisterUI == null) {
                    mActionContainer.setVisibility(View.GONE);
                    mIRegisterUI = new RegisterUIFromCamera(mActivity, mCameraController);
                    mIRegisterUI.createView(mInflater, mRootView);
                    mIRegisterUI.onResume();
                } else {
                    ((RegisterUIFromCamera)mIRegisterUI).register();
                }

                return true;
            case KeyEvent.KEYCODE_F2:
                mActivity.finish();
                return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onBackPressed() {
        if (mIRegisterUI != null && mIRegisterUI.onBackPressed()) {
            return true;
        } else if (mActionContainer != null && mActionContainer.getVisibility() == View.GONE) {
            mActionContainer.setVisibility(View.VISIBLE);
            mIRegisterUI.onPause();
            mIRegisterUI.destroyView(mRootView);
            mIRegisterUI = null;
            return true;
        }
        return false;
    }
}
