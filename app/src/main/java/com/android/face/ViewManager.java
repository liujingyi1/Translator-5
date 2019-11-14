package com.android.face;

import android.view.View;

public abstract class ViewManager {
    public static final int VIEW_LAYER_DIALPAD = 0;
    public static final int VIEW_LAYER_2 = 1;

    protected View[] mViews = new View[2];

    private IParentView mIParentView;

    public ViewManager(IParentView parentView) {
        if (parentView == null) {
            throw new IllegalArgumentException("The parentView can't be null.");
        }
        mIParentView = parentView;
    }

    public void show(int layer) {
        if (isShow(layer)) {
            return;
        }
        mViews[layer] = getView(layer);
        if (mViews[layer] != null) {
            mIParentView.addView(mViews[layer], layer);
        }
    }

    public void show() {
        for (int i = VIEW_LAYER_DIALPAD; i < mViews.length; i++) {
            if (isShow(i)) {
                continue;
            }
            mViews[i] = getView(i);
            if (mViews[i] != null) {
                mIParentView.addView(mViews[i], i);
            }
        }
    }

    public void hide(int layer) {
        if (mViews[layer] != null) {
            mIParentView.removeView(mViews[layer], layer);
            mViews[layer] = null;
        }
    }

    public void remove(int layer) {
        if (mViews[layer] != null) {
            mViews[layer] = null;
        }
    }

    public void hide() {
        for (int i = VIEW_LAYER_DIALPAD; i < mViews.length; i++) {
            if (mViews[i] != null) {
                mIParentView.removeView(mViews[i], i);
                mViews[i] = null;
            }
        }
    }

    public void timeOut(int layer) {
        mIParentView.timeOut(mViews[layer], layer);
    }

    public boolean isShow() {
        for (int i = VIEW_LAYER_DIALPAD; i < mViews.length; i++) {
            if (mViews[i] != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isShow(int layer) {
        return mViews[layer] != null;
    }

    public abstract View getView(int layer);

    public interface IParentView {
        void addView(View view);

        void addView(View view, int layer);

        void removeView(View view);

        void removeView(View view, int layer);

        void timeOut(View view, int layer);
    }

}
