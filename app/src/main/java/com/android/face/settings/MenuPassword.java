package com.android.face.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.ragentek.face.R;

public class MenuPassword extends Activity {
    private static String TAG = "MenuPassword";
    private TextView textView;
    private TextView textView2;
    private EditText editText;
    private String passWord;
    //SharedPreferences sp = getSharedPreferences("settings", Context.MODE_PRIVATE);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //继承AppCompatActivity时无效
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu_password);
        initView();
        read();
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
                passWord = s.toString();
                Log.d(TAG,"passWord = "+passWord+", count = "+count);
                if (passWord.length() > 8) {
                    Toast.makeText(getApplicationContext(),"密码超出8位了！", Toast.LENGTH_SHORT).show();
                    passWord = passWord.substring(0, 8);
                    Log.d(TAG,"passWord = "+passWord);
                    editText.setText(passWord);
                    editText.setSelection(passWord.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public int write(String s){
        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String org = sharedPreferences.getString("pwd","60350006");
        Log.d(TAG,"write s = "+s);
        if (s!= null && !s.equals(org) && s.length()==8){
            editor.putString("pwd",s);
            editor.commit();
            Toast.makeText(this,"密码更新成功！", Toast.LENGTH_SHORT).show();
            onBackPressed();
            return 1;
        } else {
            Toast.makeText(this,"请重新输入8位新密码！", Toast.LENGTH_SHORT).show();
            editText.setText("");
        }
        return 0;
    }

    public String read(){
        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String passWord = sharedPreferences.getString("pwd","20180506");
        Log.d(TAG,"read passWord = "+passWord);
        return passWord;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
            case KeyEvent.KEYCODE_F2:
                onBackPressed();
                break;
            case KeyEvent.KEYCODE_F3:
                int result = write(passWord);
                Log.d(TAG,"result = "+result);
                break;
            case KeyEvent.KEYCODE_BACK:
                int result1 = write(passWord);
                Log.d(TAG,"result1 = "+result1);
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
