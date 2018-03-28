package com.jzw.media.library.camera;

import android.hardware.Camera;
import android.view.View;

import java.util.List;

/**
 * @company 上海道枢信息科技-->
 * @anthor created by Administrator
 * @date 2018/3/1 0001
 * @change
 * @describe describe
 **/
public abstract class CameraViewImpl {
    protected static final int FOCUS_AREA_SIZE_DEFAULT = 300;
    protected static final int FOCUS_METERING_AREA_WEIGHT_DEFAULT = 1000;
    protected static final int DELAY_MILLIS_FEFORE_RESETTING_FOCUS = 3000;

    protected final CameraCallback mCallback;
    protected final PreviewImpl mPreview;

    protected CameraViewImpl(CameraCallback callback, PreviewImpl preview) {
        mCallback = callback;
        mPreview = preview;
    }

    protected View getView() {
        return mPreview.getView();
    }

    protected int getFocusAreaSize() {
        return FOCUS_AREA_SIZE_DEFAULT;
    }

    protected int getFocusMeteringAreaWeight() {
        return FOCUS_METERING_AREA_WEIGHT_DEFAULT;
    }

    protected void detachFocusTapListener() {
        if (mPreview != null && mPreview.getView() != null) {
            mPreview.getView().setOnTouchListener(null);
        }
    }

    protected abstract boolean start();

    protected abstract void stop();

    protected abstract void reset();
    protected abstract boolean isCameraOpened();

    protected abstract void setFacing(int facing);

    protected abstract int getFacing();


    protected abstract List<Camera.Size> getSupportedPreviewSize();

    protected abstract List<Camera.Size> getSupportedPictureSize();

    protected abstract List<Camera.Size> getSupportedVideoSize();

    protected abstract Camera.Size getCurrentPreviewSize();

    protected abstract void setAutoFocus(boolean autoFocus);

    protected abstract boolean getAutoFocus();

    protected abstract void setFlash(int flash);

    protected abstract int getFlash();

    protected abstract void takePicture();

    protected abstract Camera getCamera();

    protected abstract void setRotation(int rotation);

    protected abstract int getRotation();

    protected abstract void setDisplayOrientation(int displayOrientation);

    protected abstract int getDisplayOrientation();

    protected abstract int getDisplayOrientationHint();
}
