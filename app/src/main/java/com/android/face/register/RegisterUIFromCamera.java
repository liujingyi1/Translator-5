package com.android.face.register;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.face.BaseFaceTracker;
import com.android.face.faceapi.readsense.YMFaceTrackManager;
import com.android.rgk.common.Constant;
import com.android.rgk.common.camera.CameraController;
import com.android.rgk.common.camera.CameraDevice;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.db.UserDataUtil;
import com.android.rgk.common.db.bean.User;
import com.android.rgk.common.util.BitmapUtil;

import java.io.File;
import java.util.List;

import com.android.rgk.common.util.LogUtil;
import com.ragentek.face.R;

import readsense.api.core.RSDeepFace;
import readsense.api.core.RSFaceQuality;
import readsense.api.core.RSFaceRecognition;
import readsense.api.core.RSTrack;
import readsense.api.enity.YMFace;
import readsense.api.enity.YMPerson;
import readsense.api.info.RSImageRotation;

public class RegisterUIFromCamera extends BaseFaceTracker {
    private View mView;
    private SurfaceView mDrawView;
    private Button mRegisterBtn;
    private TextView mTextTips;

    private boolean isAdd;
    private boolean isCorrect;
    private int personId;
    private String age;
    private String gender;
    private String score;
    private Bitmap head = null;

    private RSTrack rsTrack;
    private RSFaceQuality rsFaceQuality;
    private RSDeepFace rsDeepFace;
    private RSFaceRecognition rsFaceRecognition;

    public RegisterUIFromCamera(Activity activity, CameraController cameraController) {
        super(activity, cameraController);
        rsTrack = YMFaceTrackManager.getRSTrack();
        rsFaceQuality = YMFaceTrackManager.getRSFaceQuality();
        rsDeepFace = YMFaceTrackManager.getRSDeepFace();
        rsFaceRecognition = YMFaceTrackManager.getRSFaceRecognition();
    }

    @Override
    public View createView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View v = inflater.inflate(R.layout.register_from_camera, null);
        container.addView(v);
        mRegisterBtn = (Button) v.findViewById(R.id.register);
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAdd = true;
            }
        });
        mRegisterBtn.setEnabled(false);
        mDrawView = (SurfaceView) v.findViewById(R.id.draw_view);
        mTextTips = (TextView) v.findViewById(R.id.tv_tips);
        mView = v;
        return v;
    }

    @Override
    public void destroyView(ViewGroup container) {
        if (mView != null) {
            container.removeView(mView);
            mView = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView drawView, float scaleBit, int cameraId, String fps) {

    }

    @Override
    protected List<YMFace> analyse(byte[] data, int iw, int ih) {
        if (stop) {
            return null;
        }
        final List<YMFace> ymFaces = rsTrack.runTrack(data, iw, ih, Constant.SDKOrientation);
        final byte[] bytes = new byte[data.length];
        System.arraycopy(data, 0, bytes, 0, data.length);
        final int width = iw;
        final int height = ih;
        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (null != ymFaces && ymFaces.size() > 0) {
                    checkAndRegistFace(ymFaces, bytes, width, height);
                } else {
                    mRegisterBtn.setEnabled(false);
                    mRegisterBtn.setBackgroundResource(R.mipmap.add_face_unable);
                }
            }
        });
        return ymFaces;
    }

    /**
     * 校验并且注册人脸
     *
     * @param ymFaces
     * @param bytes
     * @param iw
     * @param ih
     */
    private void checkAndRegistFace(List<YMFace> ymFaces, final byte[] bytes, int iw, int ih) {
        final float[] rect = ymFaces.get(0).getRect();
        isCorrect = checkHeadpose(ymFaces) && checkFaceQuality(bytes, ymFaces.get(0), iw, ih);
        if (isCorrect) {
            mRegisterBtn.setEnabled(true);
            mRegisterBtn.setBackgroundResource(R.mipmap.add_face_able);
            if (isAdd) {//点击add face按钮后isAdd=true
                isCorrect = false;

                final float[] feature = rsDeepFace.getDeepFaceFeature(bytes, iw, ih, RSImageRotation.RS_IMG_CLOCKWISE_ROTATE_0, rect);
                YMPerson ymPerson = rsFaceRecognition.faceIdentification(feature);
                if (null != ymPerson && ymPerson.getConfidence() >= 75) {
                    personId = ymPerson.getPerson_id();
                    User user = UserDataUtil.getUserById(personId + "", mActivity);
                    String name = personId + "";
                    if (user != null) name = user.getName();

                    rsFaceRecognition.personDelete(personId);
                    DataOperator dataOperator = DataOperator.getInstance();
                    String imgPath = mActivity.getCacheDir()
                            + "/" + personId + ".jpg";
                    File imgFile = new File(imgPath);
                    if (imgFile.exists()) {
                        imgFile.delete();
                    }
                    dataOperator.deleteUserByPersonId(personId);
                    addFace(bytes, feature, rect);
                    doEnd();
                } else {
                    addFace(bytes, feature, rect);
                    doEnd();
                }
            }
        } else {
            mTextTips.setText("请正脸面对");
            mRegisterBtn.setEnabled(false);
            mRegisterBtn.setBackgroundResource(R.mipmap.add_face_unable);
        }
        isAdd = false;
    }

    /**
     * 校验正脸数据
     *
     * @param faces
     * @return
     */
    private boolean checkHeadpose(List<YMFace> faces) {
        YMFace face = faces.get(0);
        float facialOri[] = face.getHeadpose();

        float x = facialOri[0];
        float y = facialOri[1];
        float z = facialOri[2];

        if (Math.abs(x) <= 30 && Math.abs(y) <= 30 && Math.abs(z) <= 30) {
            return true;
        }
        return false;
    }

    private boolean checkFaceQuality(byte[] data, YMFace ymFace, int iw, int ih) {
        int faceQuality = rsFaceQuality.getFaceQuality(data, iw, ih, Constant.SDKOrientation, ymFace.getLandmarks());
        if (faceQuality < 6) {
            LogUtil.e("sqm", "人脸质量不佳");
            return false;
        }
        return true;
    }

    /**
     * 注册人脸
     *
     * @param bytes
     * @param feature
     * @param rect
     */
    private void addFace(byte[] bytes, float[] feature, float[] rect) {
        personId = rsFaceRecognition.personCreate(feature);//添加人脸
        gender = " ";
        score = " ";
        age = " ";
        saveImageFromCamera(personId, 0, bytes);
        if (personId > 0) {
            Bitmap image = BitmapUtil.getBitmapFromYuvByte(bytes, iw, ih);
            //TODO 此处在保存人脸小图
            if (mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                head = Bitmap.createBitmap(image, iw - (int) rect[1] - (int) rect[3], (int) rect[0], (int) rect[3], (int) rect[2], matrix, true);
            } else if (mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                head = Bitmap.createBitmap(image, (int) rect[0], (int) rect[1], (int) rect[2], (int) rect[3], null, true);
            }
        } else {
            showShortToast(mActivity, "添加人脸失败！请重新添加");
        }
    }

    private void doEnd() {
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
                            doEnd();
                            return;
                        }
                        User user = new User("" + personId, name, age, gender);
                        user.setScore(score);
                        DataOperator dataOperator = DataOperator.getInstance();
                        dataOperator.insert(user);
                        BitmapUtil.saveBitmap(head, Constant.ImagePath + personId + ".jpg");

                        Toast.makeText(mActivity, "成功添加用户：" + name, Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F3:
                        String name = et.getText().toString();
                        if (!TextUtils.isEmpty(name.trim())) {
                        } else {
                            Toast.makeText(mActivity, "名字不能为空", Toast.LENGTH_SHORT).show();
                            doEnd();
                            return true;
                        }
                        User user = new User("" + personId, name, age, gender);
                        user.setScore(score);
                        DataOperator dataOperator = DataOperator.getInstance();
                        dataOperator.insert(user);
                        BitmapUtil.saveBitmap(head, Constant.ImagePath + personId + ".jpg");

                        Toast.makeText(mActivity, "成功添加用户：" + name, Toast.LENGTH_SHORT).show();
//                        try {
//                            Thread.sleep(3000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        mActivity.finish();
                        return true;
                    case KeyEvent.KEYCODE_F2:
                        mActivity.finish();
                        return true;
                }
                return false;
            }
        }).create().show();
    }

    @Override
    protected SurfaceView getDrawView() {
        return mDrawView;
    }

    @Override
    protected void afterAnalyse(byte[] data, int iw, int ih, List<YMFace> ymFaces) {

    }

    @Override
    public void onPreviewFrame(byte[] data) {
        super.onPreviewFrame(data);
    }

    @Override
    public void onStateChange(CameraDevice camera, CameraDevice.State oldState,
                              CameraDevice.State newState, int cameraId) {
        super.onStateChange(camera, oldState, newState, cameraId);
    }

    /**
     * 保存图片
     *
     * @param personId
     * @param i
     * @param bytes
     */
    private void saveImageFromCamera(int personId, int i, byte[] bytes) {
    }

    public void register() {
        isAdd = true;
    }
}
