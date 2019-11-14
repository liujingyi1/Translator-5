package com.android.face.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.ragentek.face.R;

import java.util.Locale;

public class MainMenu extends Activity implements View.OnClickListener{
    private static String TAG = "MainMenu";
    private Button bt1,bt2,bt3,bt4,bt5,bt6,bt7,bt8,bt9;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //继承AppCompatActivity时无效
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_menu);

        initView();
    }

    public void initView(){
        bt1 = (Button) findViewById(R.id.bt1);
        bt2 = (Button) findViewById(R.id.bt2);
        bt3 = (Button) findViewById(R.id.bt3);
        bt4 = (Button) findViewById(R.id.bt4);
        bt5 = (Button) findViewById(R.id.bt5);
        bt6 = (Button) findViewById(R.id.bt6);
        bt7 = (Button) findViewById(R.id.bt7);
        bt8 = (Button) findViewById(R.id.bt8);
        bt9 = (Button) findViewById(R.id.bt9);
        textView = (TextView) findViewById(R.id.exit);

        bt1.setOnClickListener(this);
        bt2.setOnClickListener(this);
        bt3.setOnClickListener(this);
        bt4.setOnClickListener(this);
        bt5.setOnClickListener(this);
        bt6.setOnClickListener(this);
        bt7.setOnClickListener(this);
        bt8.setOnClickListener(this);
        bt9.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt1:
                startActivity(new Intent(MainMenu.this,ParameterSettings.class));
                Log.d(TAG,"start activity 1");
                break;
            case R.id.bt2:
                startActivity(new Intent(MainMenu.this,MenuPassword.class));
                Log.d(TAG,"start activity 2");
                break;
            case R.id.bt3:
                startActivity(new Intent(MainMenu.this,UserInformation.class));
                Log.d(TAG,"start activity 3");
                break;
            case R.id.bt4:
                startActivity(new Intent(MainMenu.this,AccessCardPermission.class));
                Log.d(TAG,"start activity 4");
                break;
            case R.id.bt5:
                startActivity(new Intent(MainMenu.this,OpenDoorSetup.class));
                Log.d(TAG,"start activity 5");
                break;
            case R.id.bt6:
                startActivity(new Intent(MainMenu.this,BuildingInformation.class));
                Log.d(TAG,"start activity 6");
                break;
            case R.id.bt7:
                startActivity(new Intent(MainMenu.this,TimeSetting.class));
                Log.d(TAG,"start activity 7");
                break;
            case R.id.bt8:
                startActivity(new Intent(MainMenu.this,RestoreFactory.class));
                Log.d(TAG,"start activity 8");
                break;
            case R.id.bt9:
                startActivity(new Intent(MainMenu.this,ReadHeadSetting.class));
                Log.d(TAG,"start activity 9");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        //gridView.invalidateViews();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_0:
                changeLanguage();
                break;
            case KeyEvent.KEYCODE_1:
                startActivity(new Intent(MainMenu.this,ParameterSettings.class));
                Log.d(TAG,"start activity 1");
                break;
            case KeyEvent.KEYCODE_2:
                startActivity(new Intent(MainMenu.this,MenuPassword.class));
                Log.d(TAG,"start activity 2");
                break;
            case KeyEvent.KEYCODE_3:
                startActivity(new Intent(MainMenu.this,UserInformation.class));
                Log.d(TAG,"start activity 3");
                break;
            case KeyEvent.KEYCODE_4:
                //startActivity(new Intent(MainMenu.this,AccessCardPermission.class));
                startActivity(new Intent().setClassName("com.ragentek.face","com.android.face.RegisterActivity"));
                Log.d(TAG,"start activity 4");
                break;
            case KeyEvent.KEYCODE_5:
                startActivity(new Intent(MainMenu.this,OpenDoorSetup.class));
                Log.d(TAG,"start activity 5");
                break;
            case KeyEvent.KEYCODE_6:
                startActivity(new Intent(MainMenu.this,ReadHeadSetting.class));
                Log.d(TAG,"start activity 6");
                break;
            case KeyEvent.KEYCODE_7:
                startActivity(new Intent(MainMenu.this,TimeSetting.class));
                Log.d(TAG,"start activity 7");
                break;
            case KeyEvent.KEYCODE_8:
                startActivity(new Intent(MainMenu.this,RestoreFactory.class));
                Log.d(TAG,"start activity 8");
                break;
            case KeyEvent.KEYCODE_9:
                //startActivity(new Intent(MainMenu.this,BuildingInformation.class));
                Log.d(TAG,"start activity 9");
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

    public void changeLanguage() {
        Resources res = getResources();
        Configuration config = res.getConfiguration();
        Log.i(TAG, "changeLanguage:"+config.locale);
        if(!config.locale.equals(Locale.SIMPLIFIED_CHINESE)) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            config.locale = Locale.ENGLISH;
        }
        DisplayMetrics dm = res.getDisplayMetrics();
        res.updateConfiguration(config, dm);
        super.recreate();
    }
}
