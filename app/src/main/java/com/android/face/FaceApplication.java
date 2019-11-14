package com.android.face;

import android.app.Application;

import com.android.face.http.HttpServerManager;
import com.android.rgk.common.lock.LockManager;
import java.io.File;

public class FaceApplication extends Application {
    private static FaceApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        LockManager.init(this);
        HttpServerManager.init(this);
    }

    public static FaceApplication getInstance() {
        return mInstance;
    }

}
