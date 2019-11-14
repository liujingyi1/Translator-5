// IStateCallback.aidl
package com.android.internal.lock;

// Declare any non-default types here with import statements

oneway interface IStateCallback {
    void onStateChange(int oldState, int newState);
}
