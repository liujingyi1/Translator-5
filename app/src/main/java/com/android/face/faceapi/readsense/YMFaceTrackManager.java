package com.android.face.faceapi.readsense;

import android.content.Context;
import android.widget.Toast;

import com.android.rgk.common.Constant;

import readsense.api.core.RSDeepFace;
import readsense.api.core.RSDetect;
import readsense.api.core.RSFaceAttr;
import readsense.api.core.RSFaceQuality;
import readsense.api.core.RSFaceRecognition;
import readsense.api.core.RSLicense;
import readsense.api.core.RSLivenessDetect;
import readsense.api.core.RSTrack;
import readsense.api.info.RSConstant;

public class YMFaceTrackManager {
    public static final String APPID = "61b17c72c8fbed79d2781491934f95f6";
    public static final String APP_SECRET = "df15ac362ed452cf117b3f08aa67742bd6bbf489";
    private static boolean mInitialized;

    private static RSLicense mRSLicense;
    private static RSTrack mRSTrack;
    private static RSFaceQuality mRSFaceQuality;
    private static RSFaceAttr mRSFaceAttr;
    private static RSLivenessDetect mRSLivenessDetect;
    private static RSDeepFace mRSDeepFace;
    private static RSDetect mRSDetect;
    private static RSFaceRecognition mRSFaceRecognition;

    public static boolean init(Context context) {
        if (mInitialized) {
            return true;
        }
        mRSLicense = new RSLicense(context, APPID, APP_SECRET);
        mRSLicense.init();
        long result = mRSLicense.handle;
        if (result == 0) {
            mInitialized = true;
            showShortToast(context, "初始化检测器成功");
        } else {
            mInitialized = false;
            release();
            showShortToast(context, "初始化检测器失败: " + result);
        }
        return mInitialized;
    }

    public static RSLicense getRSLicense() {
        return mRSLicense;
    }

    public static RSTrack getRSTrack() {
        if (mRSLicense == null) {
            throw new RuntimeException("The init method is not called.");
        }
        if (mRSTrack == null) {
            mRSTrack = new RSTrack(mRSLicense);
            mRSTrack.setDistanceType(RSConstant.DISTANCE_TYPE_NEAR);
            mRSTrack.init();
        }
        return mRSTrack;
    }

    public static RSFaceQuality getRSFaceQuality() {
        if (mRSLicense == null) {
            throw new RuntimeException("The init method is not called.");
        }
        if (mRSFaceQuality == null) {
            mRSFaceQuality = new RSFaceQuality(mRSLicense);
            mRSFaceQuality.init();
        }
        return mRSFaceQuality;
    }

    public static RSFaceAttr getRSFaceAttr() {
        if (mRSLicense == null) {
            throw new RuntimeException("The init method is not called.");
        }
        if (mRSFaceAttr == null) {
            mRSFaceAttr = new RSFaceAttr(mRSLicense);
            mRSFaceAttr.init();
        }
        return mRSFaceAttr;
    }

    public static RSLivenessDetect getRSLivenessDetect() {
        if (mRSLicense == null) {
            throw new RuntimeException("The init method is not called.");
        }
        if (mRSLivenessDetect == null) {
            mRSLivenessDetect = new RSLivenessDetect(mRSLicense);
            mRSLivenessDetect.initInfrared();
        }
        return mRSLivenessDetect;
    }

    public static RSDeepFace getRSDeepFace() {
        if (mRSLicense == null) {
            throw new RuntimeException("The init method is not called.");
        }
        if (mRSDeepFace == null) {
            mRSDeepFace = new RSDeepFace(mRSLicense);
            mRSDeepFace.init();
        }
        return mRSDeepFace;
    }

    public static RSDetect getRSDetect() {
        if (mRSLicense == null) {
            throw new RuntimeException("The init method is not called.");
        }
        if (mRSDetect == null) {
            mRSDetect = new RSDetect(mRSLicense);
            mRSDetect.init();
        }
        return mRSDetect;
    }

    public static RSFaceRecognition getRSFaceRecognition() {
        if (mRSLicense == null) {
            throw new RuntimeException("The init method is not called.");
        }
        if (mRSFaceRecognition == null) {
            mRSFaceRecognition = new RSFaceRecognition(mRSLicense, Constant.FeatureDatabasePath);
            mRSFaceRecognition.init();
        }
        return mRSFaceRecognition;
    }

    public static void release() {
        if (mRSLicense != null) {
            mRSLicense.unInit();
            mRSLicense = null;
        }
        if (mRSTrack != null) {
            mRSTrack.unInit();
            mRSTrack = null;
        }
        if (mRSFaceQuality != null) {
            mRSFaceQuality.unInit();
            mRSFaceQuality = null;
        }
        if (mRSFaceAttr != null) {
            mRSFaceAttr.unInit();
            mRSFaceAttr = null;
        }
        if (mRSLivenessDetect != null) {
            mRSLivenessDetect.unInitInfrared();
            mRSLivenessDetect = null;
        }
        if (mRSDeepFace != null) {
            mRSDeepFace.unInit();
            mRSDeepFace = null;
        }
        if (mRSDetect != null) {
            mRSDetect.unInit();
            mRSDetect = null;
        }
        if (mRSFaceRecognition != null) {
            mRSFaceRecognition.unInit();
            mRSFaceRecognition = null;
        }
        mInitialized = false;
    }

    public static boolean isInitialized() {
        return mInitialized;
    }

    private static void showShortToast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }
}
