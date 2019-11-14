package com.spark.live.sdk.engine;

import android.content.Context;
import android.view.SurfaceHolder;

/**
 * 推流引擎应该有完整的生命周期
 * Created by devzhaoyou on 7/22/16.
 */
public interface ISimpleLiveEngine extends SurfaceHolder.Callback {

    /**
     * 初始化推流引擎
     * @param context 全局上下文环境
     *
     */
    //void Init(Context context);

    /**
     * 启动推流引擎
     */
    void start(String rtmpUrl);

    /**
     * 恢复推流引擎
     */
    void resume();

    /**
     * 暂停推流
     */
    void pause();

    /**
     * 切换摄像头
     */
    void switchCamera();

    /**
     * 停止推流销毁所有对象
     */
    void destroy();

    void setStateCallback(ISimpleLiveEngineEventCallback callback);

    abstract class Stub implements ISimpleLiveEngine {
        /*@Override
        public void Init(Context context) {

        }*/

        @Override
        public void start(String rtmpUrl) {

        }

        @Override
        public void resume() {

        }

        @Override
        public void pause() {

        }

        @Override
        public void switchCamera() {

        }

        @Override
        public void destroy() {

        }

        @Override
        public void setStateCallback(ISimpleLiveEngineEventCallback callback) {

        }
    }
}
