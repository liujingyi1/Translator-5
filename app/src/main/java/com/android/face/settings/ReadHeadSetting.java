package com.android.face.settings;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.face.mcu.McuManager;
import com.ragentek.face.R;

public class ReadHeadSetting extends Activity implements View.OnClickListener {
    private static String TAG = "Bt_Pw";
    private TextView bt_pw;
    private EditText bt_et;
    private Button reset_bt, confirm_bt;
    private String bt = "abcdef";
    private static int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //继承AppCompatActivity时无效
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_read_head_setting);

        bt_pw = (TextView) findViewById(R.id.textView2);
        bt_et = (EditText) findViewById(R.id.editText);
        reset_bt = (Button) findViewById(R.id.reset_bt);
        confirm_bt = (Button) findViewById(R.id.confirm_bt);
        confirm_bt.setEnabled(false);

        bt_pw.setText(R.string.bt_pw);
        String bt_password = McuManager.getBluetoothPassword("666666");
        Log.d(TAG,"bt_password = "+bt_password);
        bt_et.setText(bt_password);
        bt_et.setFocusable(false);
        bt_et.clearFocus();

        bt_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG,"charSequence = "+charSequence);
                bt = charSequence.toString();
                if (bt.length() > 6) {
                    Toast.makeText(getApplicationContext(),"蓝牙密码超出6位了！", Toast.LENGTH_SHORT).show();
                    bt = bt.substring(0, 6);
                    Log.d(TAG,"bt = "+bt);
                    bt_et.setText(bt);
                    bt_et.setSelection(bt.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.d(TAG,"id = "+id);
        switch (id) {
            case R.id.reset_bt:
                bt_pw.setText(R.string.tv92);
                bt_et.setText("");
                bt_et.setFocusable(true);
                bt_et.requestFocus();

                reset_bt.setClickable(false);
                confirm_bt.setClickable(true);
                break;
            case R.id.confirm_bt:
                bt_et.setFocusable(false);
                bt_et.clearFocus();
                McuManager.setBluetoothPassword(bt);
                confirm_bt.setClickable(false);
                reset_bt.setClickable(true);
                break;
            default:
                break;

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
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
                Log.d(TAG,"i = "+i);
                if (i%2 == 0){
                    bt_pw.setText(R.string.tv92);
                    bt_et.setText("");
                    bt_et.setFocusable(true);
                    bt_et.requestFocus();

                    reset_bt.setEnabled(false);
                    confirm_bt.setEnabled(true);
                    i++;
                } else {
                    bt_et.setFocusable(false);
                    bt_et.clearFocus();
                    Log.d(TAG,"bt = "+bt);
                    McuManager.setBluetoothPassword(bt);
                    Toast.makeText(getApplicationContext(),"蓝牙密码更新成功！",Toast.LENGTH_SHORT).show();
                    confirm_bt.setEnabled(false);
                    reset_bt.setEnabled(true);
                    i++;
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
