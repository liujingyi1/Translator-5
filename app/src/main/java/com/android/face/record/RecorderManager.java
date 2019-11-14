package com.android.face.record;

import android.os.Environment;

import com.android.rgk.common.camera.CameraController;
import com.android.rgk.common.util.LogUtil;

import java.io.File;

public class RecorderManager {
    private CameraController mCameraController;

    private boolean isStarted;
    private AVmediaMuxer mediaMuxer;

    public RecorderManager(CameraController cameraController) {
        mCameraController = cameraController;
    }

    public void onCreate() {
    }

    public void onStart() {
    }

    public void onResume() {
        startRecord();
    }

    public void onPause() {
        stopRecord();
    }

    public void onStop() {
    }

    public void onDestroy() {
        if (isStarted) {
            isStarted = false;
            mediaMuxer.stop();
            mediaMuxer = null;
        }
    }

    public void startRecord() {
        if (isStarted) {
            return;
        }

        isStarted = true;
        if (mediaMuxer == null) {
            mediaMuxer = AVmediaMuxer.newInstance(mCameraController);
        }
        String filePath = generateFilePath();
        LogUtil.d("sqm", "===zhongjihao===outfile===创建混合器,保存至:" + filePath);
        mediaMuxer.setOutFile(filePath, AVmediaMuxer.MUXER_OUTPUT_MPEG_4);
        mediaMuxer.prepare();
        mediaMuxer.start();
    }

    public void stopRecord() {
        if (isStarted) {
            isStarted = false;
            mediaMuxer.stop();
        }
    }

    private String generateFilePath() {
        String filePath = Environment.getExternalStorageDirectory()
                + "/zhongjihao";
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        filePath = filePath + "/out.mp4";
        return filePath;
    }

}
