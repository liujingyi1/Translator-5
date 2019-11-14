package com.android.face.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.face.mcu.McuManager;
import com.ragentek.face.R;

public class RestoreFactory extends Activity {
    public static String TAG = "RestoreFactory";
    private static final String verificationCode = "1234";
    private TextView textView, textView2;
    private EditText editText;
    private String code = "FFFF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //继承AppCompatActivity时无效
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_restore_factory);

        initView();
    }

    private void initView(){
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        editText = (EditText) findViewById(R.id.editText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG,"s = "+s);
                code = s.toString();
                if (code.length() > 4) {
                    Toast.makeText(getApplicationContext(),"验证码超出4位了！", Toast.LENGTH_SHORT).show();
                    code = code.substring(0, 4);
                    Log.d(TAG,"code = "+code);
                    editText.setText(code);
                    editText.setSelection(code.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_0:
                break;
            case KeyEvent.KEYCODE_1:
                break;
            case KeyEvent.KEYCODE_2:
                break;
            case KeyEvent.KEYCODE_3:
                break;
            case KeyEvent.KEYCODE_4:
                break;
            case KeyEvent.KEYCODE_5:
                break;
            case KeyEvent.KEYCODE_6:
                break;
            case KeyEvent.KEYCODE_7:
                break;
            case KeyEvent.KEYCODE_8:
                break;
            case KeyEvent.KEYCODE_9:
                break;
            case KeyEvent.KEYCODE_F2:
                onBackPressed();
                break;
            case KeyEvent.KEYCODE_F3:
                checkCode();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private int checkCode(){
        Log.d(TAG,"code = "+code);
        if(!"".equals(code) && verificationCode.equals(code)){
            Toast.makeText(this,"验证码正确，即将恢复出厂设置！", Toast.LENGTH_SHORT).show();
            //sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
            McuManager.getInstance().doMasterClear();
            return 1;
        } else {
            Toast.makeText(this,"验证码不正确，请重新输入！", Toast.LENGTH_SHORT).show();
        }
        return 0;
    }
}
