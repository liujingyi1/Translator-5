package com.android.face;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.android.rgk.common.util.LogUtil;

import java.util.ArrayList;

public class ProximitySensor implements SensorEventListener {
    private SensorManager mSensorManager;
    private boolean mIsNear = true;

    private ArrayList<Listener> mListeners = new ArrayList<>();

    public ProximitySensor(Context context) {
        LogUtil.d("ProximitySensor", "ProximitySensor");
        mSensorManager = context.getSystemService(SensorManager.class);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            mIsNear = event.values[0] < event.sensor.getMaximumRange();
            LogUtil.i("ProximitySensor", "onSensorChanged near: " + mIsNear);

            for (Listener listener : mListeners) {
                listener.onProximityChange(mIsNear);
            }
        }
    }

    public boolean isNear() {
        return mIsNear;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void release() {
        mSensorManager.unregisterListener(this);
        mListeners.clear();
    }

    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }

    public interface Listener {
        void onProximityChange(boolean isNear);
    }
}
