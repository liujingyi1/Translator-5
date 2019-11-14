package com.android.face.ota;

import android.content.Context;
import android.content.Intent;

import com.android.face.ota.utils.Constants;
import com.android.face.ota.utils.UpdateInfo;
import com.android.rgk.common.util.LogUtil;

public class OtaManager {
    private Context mContext;
    private static OtaManager mInstance;
    private OtaManager(Context context) {
        mContext = context;
    }
    public static void init(Context context) {
        LogUtil.i("OtaManager", "init()");
        if (mInstance == null) {
            LogUtil.i("DataOperator", "init() create DataOperator");
            mInstance = new OtaManager(context);
        }
    }
    public static OtaManager getInstance() {
        return mInstance;
    }
    public void startUpdateActivity(UpdateInfo updateInfo){
        Intent intent = new Intent(mContext, UpdateActivity.class);
        intent.putExtra(Constants.OTA__UPDATE_IFNO,updateInfo);
        mContext.startActivity(intent);
    }
    public static void destroy() {
        LogUtil.i("OtaManager", "destroy()");
        if (mInstance != null) {
            mInstance = null;
            LogUtil.i("OtaManager", "destroy(): mInstance = null");
        }
    }
}
