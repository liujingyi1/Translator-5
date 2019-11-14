package com.android.face.dialpad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.android.face.HttpActivity;
import com.android.face.IpInputActivity;
import com.android.face.ViewManager;
import com.android.face.linphone.LinphonePreferences;
import com.android.face.linphone.utils.ILinphoneCallStateListener;
import com.android.face.linphone.utils.LinphoneUtils;
import com.android.face.mcu.McuManager;
import com.android.face.util.SoundEffectsUtil;
import com.android.rgk.common.camera.CameraConfig;
import com.android.rgk.common.camera.CameraController;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.lock.LockManager;
import com.ragentek.face.R;

import java.util.ArrayList;

public class DialpadManager extends ViewManager {
    private static final String CIT_CODE = "60350007";
    private static final String IP_EIDT_CODE = "60350008";
    private static final String HTTP_EIDT_CODE = "60350010";

    private static final String PROPERTY_FLAG = "0000";
    private static final String PROPERTY_NUMBER = "13916354536";

    private static final long INPUT_TIMEOUT = 10 * 1000;
    private static final int MSG_INPUT_TIMEOUT = 1000;

    private Activity mActivity;

    private EditText mInformation;

    private String Setting_CODE = "60350006";

    private State mState = State.NORMAL;

    private String mRoom;

    private enum State {
        NORMAL,
        RESET,
        PASSWORD,
        SECURITYCODE,
        TOCALL,
        INCALL,
        TIMEOUT
    }

    private boolean isConnected = false;

    private ArrayList<String> numbers;
    private int position = 0;

    public DialpadManager(IParentView parentView, Activity activity) {
        super(parentView);

        mActivity = activity;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INPUT_TIMEOUT:
                    mState = State.TIMEOUT;
                    timeOut(VIEW_LAYER_DIALPAD);
                    break;
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            resetTime();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if ((mState == State.RESET || mState == State.NORMAL || mState == State.TIMEOUT) && s.length() != 0) {
                mState = State.TOCALL;
            }
        }
    };

    public void resetTime() {
        if (mState != State.INCALL) {
            mHandler.removeMessages(MSG_INPUT_TIMEOUT);
            Message msg = mHandler.obtainMessage(MSG_INPUT_TIMEOUT);
            mHandler.sendMessageDelayed(msg, INPUT_TIMEOUT);
        }
    }

    public void setTimeOut() {
        mState = State.TIMEOUT;
    }

    public void resetInfo() {
        cleanInfo();

        mState = State.RESET;
    }

    public void cleanInfo() {
        if (mInformation != null) {
            mInformation.setText("");
            mInformation.setHint(R.string.please_input);
            mInformation.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    }

    @Override
    public View getView(int layer) {
        if (layer == ViewManager.VIEW_LAYER_2) {
            return null;
        }
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.sip_call, null);
        mInformation = (EditText) view.findViewById(R.id.information);
        mInformation.setFocusable(true);
        mInformation.requestFocus();
        mInformation.addTextChangedListener(mTextWatcher);
        isConnected = false;
        position = 0;
        read();
        if (numbers != null) {
            numbers.clear();
        }

        resetTime();

        return view;
    }

    /*
     * KeyEvent.KEYCODE_F2 星号键 ，按一下重置状态，按两下回到人脸识别界面；取消键
     * KeyEvent.KEYCODE_F3 井号键 ，按一下输入密码，按两下输入工程密码界面，输入不同的密码进入不同的功能界面；确认键
     * */
    public void onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F2: {
                if (mState == State.RESET) {
                    finish();
                } else if (mState == State.INCALL) {
                    LinphoneUtils.endCall();
                } else {
                    resetInfo();
                }
                break;
            }
            case KeyEvent.KEYCODE_F3: {
                switch (mState) {
                    case RESET:
                    case TIMEOUT:
                    case NORMAL: {
                        mInformation.setHint(R.string.door_password);
                        mState = State.PASSWORD;
                        mInformation.setTransformationMethod(new PasswordCharSequenceStyle());
                        break;
                    }
                    case PASSWORD: {
                        if (TextUtils.isEmpty(mInformation.getText().toString())) {
                            mInformation.setHint(R.string.engineering_password);
                            mState = State.SECURITYCODE;
                            mInformation.setTransformationMethod(new PasswordCharSequenceStyle());
                        } else {
                            checkPassword(mInformation.getText().toString());
                            mInformation.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        }
                        break;
                    }
                    case SECURITYCODE: {
                        mHandler.removeMessages(MSG_INPUT_TIMEOUT);
                        if (CIT_CODE.equals(mInformation.getText().toString())) {
                            gotoCIT();
                        } else if (Setting_CODE.equals(mInformation.getText().toString())) {
                            gotoSetting();
                        } else if (IP_EIDT_CODE.equals(mInformation.getText().toString())) {
                            gotoIpActivity();
                        } else if (HTTP_EIDT_CODE.equals(mInformation.getText().toString())) {
                            gotoHTTPActivity();
                        } else {
                            resetInfo();
                            resetTime();
                        }
                        break;
                    }
                    case TOCALL: {
                        mRoom = getRoomByNumber(mInformation.getText().toString());
                        if (!TextUtils.isEmpty(mRoom)) {
                            makeCall(mRoom, mInformation.getText().toString());
                        } else {
                            position = 0;
                            mRoom = mInformation.getText().toString();

                            if (PROPERTY_FLAG.equals(mInformation.getText().toString())) {
                                makeCall(mRoom, McuManager.getCenterSip(PROPERTY_NUMBER));
                            } else {
                                DataOperator dataOperator = DataOperator.getInstance();
                                numbers = dataOperator.getNumberByRoom(mInformation.getText().toString());
                                if (numbers == null) {
                                    resetInfo();
                                    return;
                                }

                                makeCall(mRoom, numbers.get(position));
                            }
                        }
                        break;
                    }
                }
                break;
            }
            case KeyEvent.KEYCODE_BACK: {
                finish();
            }
            default: {
                if (mState != State.INCALL) {
                    mState = State.TOCALL;
                    mInformation.setText(String.valueOf(event.getNumber()));
                    mInformation.setSelection(1);
                }
                break;
            }
        }
    }

    private void read() {
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        Setting_CODE = sharedPreferences.getString("pwd", "60350006");
        Log.d("xmw", "setting_code = " + Setting_CODE);
    }

    public class PasswordCharSequenceStyle extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;

            public PasswordCharSequence(CharSequence source) {
                mSource = source;
            }

            public char charAt(int index) {
                return '#';
            }

            public int length() {
                return mSource.length();
            }

            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end);
            }
        }
    }

    public void makeCall(String room, String number) {
        isConnected = false;
        mHandler.removeMessages(MSG_INPUT_TIMEOUT);
        mState = State.INCALL;
        mInformation.setFocusable(false);
        LinphonePreferences.instance().setPreferredVideoSize(CameraConfig.getInstance(CameraController.CAMERA_ID_RGB).getVideoSize());

        LinphoneUtils.setLinphoneCallStateListener(new ILinphoneCallStateListener() {
            @Override
            public void onIdle() {

            }

            @Override
            public void onIncomingReceived() {

            }

            @Override
            public void onOutgoingInit() {
                mInformation.setText(R.string.alerting);
            }

            @Override
            public void onOutgoingProgress() {

            }

            @Override
            public void onOutgoingRinging() {

            }

            @Override
            public void onOutgoingEarlyMedia() {

            }

            @Override
            public void onConnected() {

            }

            @Override
            public void onStreamsRunning() {
                mInformation.setText(R.string.active);
                isConnected = true;
                LinphoneUtils.enableSpeaker();
            }

            @Override
            public void onPausing() {

            }

            @Override
            public void onPaused() {

            }

            @Override
            public void onResuming() {

            }

            @Override
            public void onRefered() {

            }

            @Override
            public void onError() {
            }

            @Override
            public void onCallEnd() {
            }

            @Override
            public void onPausedByRemote() {

            }

            @Override
            public void onCallUpdatedByRemote() {

            }

            @Override
            public void onCallIncomingEarlyMedia() {

            }

            @Override
            public void onCallUpdating() {

            }

            @Override
            public void onCallReleased() {
                position++;
                if (!isConnected && numbers != null && position < numbers.size()) {
                    makeCall(mRoom, numbers.get(position));
                } else {
                    mInformation.setFocusable(true);
                    finish();
                }
            }

            @Override
            public void onCallEarlyUpdatedByRemote() {

            }

            @Override
            public void onCallEarlyUpdating() {

            }
        });
        LinphoneUtils.makeCall(room, number, "Door Access");
    }

    private void checkPassword(String pw) {
        DataOperator dataOperator = DataOperator.getInstance();
        if (dataOperator.checkPassword(pw, System.currentTimeMillis()) ||
                McuManager.getDoorOpenPassword("123456").equals(pw)) {
            String detail = getJsonString(pw, 1);
            LockManager.getInstance().unlock(LockManager.UNLOCK_TYPE_PASSWORD, detail);
        }else {
            SoundEffectsUtil.play(SoundEffectsUtil.PASSWD_ERR_ID);
        }
        resetInfo();
    }

    private String getRoomByNumber(String number) {
        DataOperator dataOperator = DataOperator.getInstance();

        return dataOperator.getRoomByNumber(number);
    }

    private String getJsonString(String password, int passwordType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("passwordType", passwordType);
        jsonObject.put("password", password);
        return jsonObject.toJSONString();
    }

    private void gotoCIT() {
        mActivity.startActivity(new Intent()
                .setClassName("com.rgk.factory", "com.rgk.factory.ItemTestActivity"));
    }

    private void gotoSetting() {
        mActivity.startActivity(new Intent()
                .setClassName("com.ragentek.face", "com.android.face.settings.MainMenu"));
    }

    private void gotoIpActivity() {
        Intent intent = new Intent(mActivity, IpInputActivity.class);
        mActivity.startActivity(intent);
    }

    private void gotoHTTPActivity() {
        Intent intent = new Intent(mActivity, HttpActivity.class);
        mActivity.startActivity(intent);
    }

    public void finish() {
        mState = State.NORMAL;
        mHandler.removeMessages(MSG_INPUT_TIMEOUT);
        hide(VIEW_LAYER_DIALPAD);
    }

    public boolean ignoreProximity() {
        return isShow() && mState != State.TIMEOUT;
    }
}
