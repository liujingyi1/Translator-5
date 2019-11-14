// ILockManager.aidl
package com.android.internal.lock;

import com.android.internal.lock.IStateCallback;

// Declare any non-default types here with import statements

interface ILockManager {
    boolean setGpioLow();
    boolean setGpioHigh();
    String getGpioValue();
    int getLockState();
    void setStateCallback(IStateCallback stateCallback);

    String getNfcValue();
    void setNfcLight(String brightness);

    int writeNvStr(int id, String str);
    String readNvStr(int id);

    void setTime(long when);

    String getSdcardPath();
    boolean installPackage(String otaPackagePath);
    void writeSerialport(in byte[] bytes);
    byte[] readSerialport();

    void setLightValue(int val);
    int getLightValue();
    String getEthernet();
    boolean setEthernet(String str);
}
