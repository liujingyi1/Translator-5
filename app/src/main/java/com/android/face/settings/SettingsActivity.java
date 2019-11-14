package com.android.face.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ragentek.face.R;

public class SettingsActivity extends Activity {
    private static String TAG = "SettingsActivity";

    private TextView textView;
    private EditText editText;
    private String passWord = "88888888";
    private String DefaultCode  = null;
    //SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
    //private String DefaultCode  = sharedPreferences.getString("pwd","20180506");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //继承AppCompatActivity时无效
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        initView();
        DefaultCode = read();
    }

    public void initView(){
        textView = (TextView) findViewById(R.id.text_view);
        editText = (EditText) findViewById(R.id.edit_text);

        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        //editText.setInputType( InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
        //editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        //editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

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

    public String read(){
        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String passWord = sharedPreferences.getString("pwd","20180506");
        Log.d(TAG,"read passWord = "+passWord);
        return passWord;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG,"keycode = "+keyCode+", event = "+event);
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
                int result = checkPassword();
                Log.d(TAG,"result = "+result);
                break;
            case KeyEvent.KEYCODE_BACK:
                int result1 = checkPassword();
                Log.d(TAG,"result1 = "+result1);
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private int checkPassword(){
        DefaultCode = read();
        Log.d(TAG,"DefaultCode = "+DefaultCode+", passWord = "+passWord);
        if (passWord!=null && passWord.equals(DefaultCode)){
            Toast.makeText(getApplicationContext(),"密码正确！", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this,MainMenu.class));
            return 1;
        } else {
            Toast.makeText(getApplicationContext(),"密码不正确，请重新输入！", Toast.LENGTH_SHORT).show();
            editText.setText("");
        }
        return 0;
    }
}
