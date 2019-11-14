package com.android.face;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.rgk.common.net.MqttManager;
import com.android.rgk.common.util.LogUtil;
import com.ragentek.face.R;

public class IpInputActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "IpInputActivity";
    private EditText hostEditText;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private Button submitBtn;

    private static final long INPUT_TIMEOUT = 10 * 1000;
    private static final int MSG_INPUT_TIMEOUT = 1000;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INPUT_TIMEOUT:
                    finish();
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

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ip_input);

        initViews();

        resetTime();
    }

    private void initViews() {
        hostEditText = (EditText) findViewById(R.id.host);
        //hostEditText.addTextChangedListener(mTextWatcher);
        userNameEditText = (EditText) findViewById(R.id.name);
        //userNameEditText.addTextChangedListener(mTextWatcher);
        passwordEditText = (EditText) findViewById(R.id.password);
        //passwordEditText.addTextChangedListener(mTextWatcher);
        submitBtn = (Button) findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_btn:
                resetTime();
                String host = hostEditText.getText().toString();
                String name = userNameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (TextUtils.isEmpty(host)) {
                    Toast.makeText(this, "IP地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "秘密不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                MqttManager.getInstance().config(host, name, password);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        resetTime();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F3) {
            finish();
        }
        return true;
    }

    private void resetTime() {
        /*mHandler.removeMessages(MSG_INPUT_TIMEOUT);
        mHandler.sendEmptyMessageDelayed(MSG_INPUT_TIMEOUT, INPUT_TIMEOUT);*/
    }
}
