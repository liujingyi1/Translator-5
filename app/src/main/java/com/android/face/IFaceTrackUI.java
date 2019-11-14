package com.android.face;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface IFaceTrackUI {
    View createView(LayoutInflater inflater, @Nullable ViewGroup container);

    void destroyView(ViewGroup container);

    void onResume();

    void onPause();

    boolean onBackPressed();
}
