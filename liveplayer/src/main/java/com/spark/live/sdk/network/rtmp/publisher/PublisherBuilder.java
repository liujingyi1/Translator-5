package com.spark.live.sdk.network.rtmp.publisher;

import android.util.Log;

import com.spark.live.sdk.util.LogUtil;

import java.io.IOException;

/**
 *
 * Created by devzhaoyou on 9/18/16.
 */

public class PublisherBuilder {

    private PublisherKnight knight = null;

    public PublisherBuilder(String rtmpUrl, final IPublishBuilderCallback callback) {
        knight = new PublisherKnight(rtmpUrl, new IPublisherCallback() {
            @Override
            public void onConnect() {
                Log.d("sqm", "onConnect:");
                try {
                    knight.createStream();
                } catch (IOException e) {
                    onError(e.getMessage());
                }
            }

            @Override
            public void onCreateStream() {
                Log.d("sqm", "onCreateStream:");
                try {
                    knight.publish(IPublisher.PUBLISH_TYPE_LIVE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSendPublish() {
                Log.d("sqm", "onSendPublish:");
                if (callback != null) {
                    callback.onBuildPublisher(knight);
                }
            }

            @Override
            public void onCreateSocket() {
                Log.d("sqm", "onCreateSocket:");
                knight.init();
                knight.doRtmpHandshake();
            }

            @Override
            public void onHandshake() {
                Log.d("sqm", "onHandshake:");
                try {
                    knight.rtmpConnect();
                } catch (IOException e) {
                    onError(e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                Log.d("sqm", "onError:");
                LogUtil.e(error);
                if (callback != null) {
                    callback.onBuildError(error);
                }

            }
        });
    }

    public void build() {
        knight.createRtmpEnvoy();
    }
}
