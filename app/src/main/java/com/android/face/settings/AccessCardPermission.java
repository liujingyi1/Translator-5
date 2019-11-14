package com.android.face.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.ragentek.face.R;

public class AccessCardPermission extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //继承AppCompatActivity时无效
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_access_card_permission);
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
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
