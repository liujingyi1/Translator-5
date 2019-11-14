package com.android.face;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.View;

public class BaseActivity extends Activity {
    @Override
    protected void onResume() {
        super.onResume();
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN //hide statusBar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; //hide navigationBar
        getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
}
