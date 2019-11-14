package com.android.face;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

public class LightSensor implements SensorEventListener {

    private static final float DEFAULT_LIGHT_VALUE = 1280;
    private static final float DARK_VALUE = 500;

    private SensorManager mSensorManager;

    private float mLight = DEFAULT_LIGHT_VALUE;

    private ArrayList<Listener> mListeners = new ArrayList<>();

    public LightSensor(Context context) {
        mSensorManager = context.getSystemService(SensorManager.class);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            mLight = event.values[0];

            for (Listener listener : mListeners) {
                listener.onLightChange(mLight);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void release() {
        mSensorManager.unregisterListener(this);
        mListeners.clear();
    }

    public boolean isDark() {
        return mLight < DARK_VALUE;
    }

    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }

    public interface Listener {
        void onLightChange(float value);
    }
}
