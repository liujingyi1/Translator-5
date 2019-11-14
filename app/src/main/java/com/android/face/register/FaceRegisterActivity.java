package com.android.face.register;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.face.BaseActivity;

import com.ragentek.face.R;

public class FaceRegisterActivity extends BaseActivity implements View.OnClickListener {
    private FragmentManager mFragmentManager;
    private Fragment mCurrentFragment;

    private Button mFromPicBtn;
    private Button mFromVideoBtn;
    private Button mFromCameraBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFragmentManager = getFragmentManager();
        if (savedInstanceState != null) {
            mCurrentFragment = mFragmentManager.findFragmentById(R.id.fragment_container);
        }
        initViews();
    }

    private void initViews() {
        mFromPicBtn = (Button) findViewById(R.id.from_pic);
        mFromPicBtn.setOnClickListener(this);
        mFromVideoBtn = (Button) findViewById(R.id.from_video);
        mFromVideoBtn.setOnClickListener(this);
        mFromCameraBtn = (Button) findViewById(R.id.from_camera);
        mFromCameraBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.from_pic: {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                mCurrentFragment = new RegisterFromPicFragment();
                transaction.replace(R.id.fragment_container, mCurrentFragment);
                transaction.commit();
                break;
            }

            case R.id.from_video: {
                /*FragmentTransaction transaction = mFragmentManager.beginTransaction();
                mCurrentFragment = new RegisterFromVideoFragment();
                transaction.replace(R.id.fragment_container, mCurrentFragment);
                transaction.commit();*/
                break;
            }

            case R.id.from_camera: {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                mCurrentFragment = new RegisterFromCameraFragment();
                transaction.replace(R.id.fragment_container, mCurrentFragment);
                transaction.commit();
                break;
            }

            default:
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("sqm", "onBackPressed:");
        if (mCurrentFragment != null) {
            Log.d("sqm", "onBackPressed:------1");
            mFragmentManager.beginTransaction().remove(mCurrentFragment).commit();
            mCurrentFragment = null;
        } else {
            Log.d("sqm", "onBackPressed:------2");
            super.onBackPressed();
        }
    }
}
