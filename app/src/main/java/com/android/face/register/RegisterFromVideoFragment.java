package com.android.face.register;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.rgk.common.Constant;
import com.android.rgk.common.camera.CameraDevice;
import com.android.rgk.common.camera.ui.PreviewSurfaceView;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.db.UserDataUtil;
import com.android.rgk.common.db.bean.User;
import com.android.rgk.common.util.BitmapUtil;

import java.io.File;
import java.util.List;

import com.ragentek.face.R;

import readsense.api.enity.YMFace;

public class RegisterFromVideoFragment extends BaseFragment {
    private TextView mTipTextView;
    private Button mStartBtn;
    private PreviewSurfaceView mCameraView;
    private SurfaceView mDrawView;
    private View mViewLine;

    private boolean startRegister;
    private boolean stop = false;
    private boolean isSave = false;
    private Bitmap head;

    private String gender;
    private String age;

    long save_time = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.register_from_video, container, false);
        mCameraView = (PreviewSurfaceView) v.findViewById(R.id.camera_view);
        mDrawView = (SurfaceView) v.findViewById(R.id.draw_view);
        mTipTextView = (TextView) v.findViewById(R.id.tv_tips);
        mViewLine = v.findViewById(R.id.view_line);
        mStartBtn = (Button) v.findViewById(R.id.video_register_start);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegisterStart();
            }
        });
        return v;
    }

    private void onRegisterStart() {
        mTipTextView.setVisibility(View.GONE);
        mTipTextView.setVisibility(View.GONE);
        mStartBtn.setVisibility(View.GONE);
        startRegister = true;
        isSave = false;

        ValueAnimator animator = ValueAnimator.ofInt(screenW, 0);
        animator.setTarget(mViewLine);
        animator.setDuration(3000).start();
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mViewLine.getLayoutParams().width = value;
                mViewLine.requestLayout();

                if (value == 0) {
                    stop = true;
                }
            }
        });
    }

    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView drawView, float scaleBit, int cameraId, String fps) {

    }

    @Override
    protected List<YMFace> analyse(byte[] data, int iw, int ih) {
        /*if (stop) {
            return null;
        }
        if (startRegister) {
            if (save_time == 0 && !isSave) {
                save_time = System.currentTimeMillis();
            }
            faceTrack.registerFromVideo(data, iw, ih);
            if (System.currentTimeMillis() - save_time >= 1 && !isSave) {
                List<YMFace> faces = faceTrack.faceDetect(data, iw, ih);
                if (faces != null && faces.size() > 0 && !isSave) {
                    YMFace face = faces.get(0);
                    int gender_score = faceTrack.getGender(0);
                    int gender_confidence = faceTrack.getGenderConfidence(0);
                    age = faceTrack.getAge(0) + "";
                    gender = " ";
                    if (gender_confidence >= 90) {
                        gender = gender_score == 0 ? "F" : "M";
                        age = faceTrack.getAge(0) + "";
                        isSave = true;
                        float[] rect = face.getRect();
                        Bitmap image = BitmapUtil.getBitmapFromYuvByte(data, iw, ih);
                        //TODO 此处在保存人脸小图
                        Matrix matrix = new Matrix();
                        if (mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            matrix.postRotate(270);
                            head = Bitmap.createBitmap(image, iw - (int) rect[1] - (int) rect[3], (int) rect[0], (int) rect[3], (int) rect[2], matrix, true);
                        } else if (mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                            head = Bitmap.createBitmap(image, (int) rect[0], (int) rect[1], (int) rect[2], (int) rect[3], null, true);
                        }
                    }
                }
            }

            if (stop) {//personId
                stop = false;
                startRegister = false;
                long before = System.currentTimeMillis();
                final int personId = faceTrack.registerFromVideoEnd();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (personId > 0) {
                            //判断自己的数据库中是否存在此人，，存在则不重复添加
                            checkSimlar(personId);
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                            builder.setCancelable(false);
                            builder.setMessage("当前录入失败，是否重新录入？")
                                    .setNegativeButton("是   ",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    onRegisterStart();
                                                }
                                            })
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            mActivity.onBackPressed();
                                        }
                                    });
                            builder.create().show();
                        }
                    }
                });
            }
        }*/
        return null;
    }

    /**
     * 判断是否已经注册过
     *
     * @param personId
     */
    private void checkSimlar(final int personId) {
        User user = UserDataUtil.getUserById(personId + "", mActivity);
        if (user == null) {
            doEnd(personId);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle("提示").setCancelable(false);
            builder.setMessage(String.format("已识别您为 %1$s ，是否更新您的人脸库？", user.getName()))
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mActivity.onBackPressed();
                        }
                    })
                    .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //faceTrack.deletePerson(personId);
                            DataOperator dataOperator = DataOperator.getInstance();
                            String imgPath = mActivity.getCacheDir()
                                    + "/" + personId + ".jpg";
                            File imgFile = new File(imgPath);
                            if (imgFile.exists()) {
                                imgFile.delete();
                            }
                            dataOperator.deleteUserByPersonId(personId);
                            mActivity.onBackPressed();
                        }
                    });
            builder.create().show();
        }
    }

    private void doEnd(final int personId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setCancelable(false);
        final EditText et = new EditText(mActivity);
        et.setGravity(Gravity.CENTER);
        et.setHint("输入昵称不能为空");
        et.setHintTextColor(0xffc6c6c6);
        builder.setTitle("提示")
                .setMessage(String.format("人脸录入成功，Face ID =  %1$s 请输入昵称", personId))
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String name = et.getText().toString();
                        if (!TextUtils.isEmpty(name.trim())) {
                        } else {
                            doEnd(personId);
                            return;
                        }
                        User user = new User("" + personId, name, age, gender);
                        DataOperator dataOperator = DataOperator.getInstance();
                        dataOperator.insert(user);
                        BitmapUtil.saveBitmap(head, Constant.ImagePath + personId + ".jpg");

                        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setCancelable(false);
                        builder.setMessage("当前录入成功是否继续录入？")
                                .setNegativeButton("是",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                onRegisterStart();
                                            }
                                        })
                                .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mActivity.onBackPressed();
                                    }
                                });
                        builder.create().show();
                    }
                });
        builder.create().show();
    }

    @Override
    protected SurfaceView getDrawView() {
        return mDrawView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public PreviewSurfaceView getCameraView(int cameraId) {
        return mCameraView;
    }

    @Override
    public void onPreviewFrame(byte[] data) {
        super.onPreviewFrame(data);
    }

    @Override
    public void onStateChange(CameraDevice cameraDevice, CameraDevice.State oldState,
                              CameraDevice.State newState, int cameraId) {
        super.onStateChange(cameraDevice, oldState, newState, cameraId);
    }
}
