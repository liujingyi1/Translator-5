package com.android.face.linphone.utils;

public interface ILinphoneCallStateListener {
    void onIdle();

    void onIncomingReceived();

    void onOutgoingInit();

    void onOutgoingProgress();

    void onOutgoingRinging();

    void onOutgoingEarlyMedia();

    void onConnected();

    void onStreamsRunning();

    void onPausing();

    void onPaused();

    void onResuming();

    void onRefered();

    void onError();

    void onCallEnd();

    void onPausedByRemote();

    void onCallUpdatedByRemote();

    void onCallIncomingEarlyMedia();

    void onCallUpdating();

    void onCallReleased();

    void onCallEarlyUpdatedByRemote();

    void onCallEarlyUpdating();
}
