package com.spark.live.sdk.engine;

import com.spark.live.sdk.media.device.OnAVDataCallback;
import com.spark.live.sdk.media.device.camera.ICameraEvent;

public interface ICameraManager {
    void setCameraEventCallback(ICameraEvent callback);
    void setAVDataCallback(OnAVDataCallback callback);
}
