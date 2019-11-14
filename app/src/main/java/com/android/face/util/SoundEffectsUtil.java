package com.android.face.util;

import android.media.AudioManager;
import android.media.SoundPool;

public class SoundEffectsUtil {
    private static final String DIRECTORY = "/system/media/audio/tsl/";
    private static final String INVALID_CARD = "/system/media/audio/ui/KeypressInvalid.ogg";
    private static final String UNLOCK = DIRECTORY + "unlock.wav";
    private static final String PASSWD_ERR = DIRECTORY + "passwd_err.wav";
    private static final String FACE_FAILED = DIRECTORY + "face_failed.wav";
    public static int INVALID_CARD_ID;
    public static int UNLOCK_ID;
    public static int PASSWD_ERR_ID;
    public static int FACE_FAILED_ID;


    private static SoundPool pool;

    public static void load() {
        pool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        INVALID_CARD_ID = pool.load(INVALID_CARD, 1);
        UNLOCK_ID = pool.load(UNLOCK, 1);
        PASSWD_ERR_ID = pool.load(PASSWD_ERR, 1);
        FACE_FAILED_ID = pool.load(FACE_FAILED, 1);
    }

    public static void play(int id) {
        if (id == UNLOCK_ID) {
            pool.play(id, 0.5f, 0.5f, 0, 0, 1);
        } else if (id == INVALID_CARD_ID) {
            pool.play(id, 0.1f, 0.1f, 0, 1, 1);
        } else if(id == FACE_FAILED_ID){
            pool.play(id, 0.5f, 0.5f, 0, 0, 1);
        } else {
            pool.play(id, 0.1f, 0.1f, 0, 0, 1);
        }
    }

    public static void release() {
        pool.release();
    }
}
