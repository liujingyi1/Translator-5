package com.android.face.settings;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ragentek.face.R;

import java.io.IOException;

public class ParameterSettings extends Activity {
    private static final String TAG = "vol";
    private TextView vol_control, vol_data;
    private MediaPlayer mMediaPlayer = null;
    private AudioManager mAudioManager;
    int maxV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //继承AppCompatActivity时无效
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_parameter_settings);

        vol_control = (TextView) findViewById(R.id.vol_text);
        vol_data = (TextView) findViewById(R.id.vol_data);

        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        maxV = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int v = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        vol_data.setText(""+(int)(6.0 * v / maxV+0.5));
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.test);
        mMediaPlayer.setLooping(true);
        try {
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
        //mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    protected void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
                vol_data.setText("0");
                break;
            case KeyEvent.KEYCODE_1:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1*maxV/6, AudioManager.FLAG_PLAY_SOUND);
                vol_data.setText("1");
                break;
            case KeyEvent.KEYCODE_2:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 2*maxV/6, AudioManager.FLAG_PLAY_SOUND);
                vol_data.setText("2");
                break;
            case KeyEvent.KEYCODE_3:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 3*maxV/6, AudioManager.FLAG_PLAY_SOUND);
                vol_data.setText("3");
                break;
            case KeyEvent.KEYCODE_4:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 4*maxV/6, AudioManager.FLAG_PLAY_SOUND);
                vol_data.setText("4");
                break;
            case KeyEvent.KEYCODE_5:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5*maxV/6, AudioManager.FLAG_PLAY_SOUND);
                vol_data.setText("5");
                break;
            case KeyEvent.KEYCODE_6:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 6*maxV/6, AudioManager.FLAG_PLAY_SOUND);
                vol_data.setText("6");
                break;
            case KeyEvent.KEYCODE_7:
                break;
            case KeyEvent.KEYCODE_8:
                break;
            case KeyEvent.KEYCODE_9:
                break;
            case KeyEvent.KEYCODE_F2:
                onBackPressed();
                break;
            case KeyEvent.KEYCODE_F3:
                break;
            case KeyEvent.KEYCODE_BACK:
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
