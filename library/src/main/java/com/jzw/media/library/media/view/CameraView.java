package com.jzw.media.library.media.view;

import android.graphics.Bitmap;

/**
 * =====================================
 * 作    者: jingzhanwu
 * 版    本：1.1.4
 * 创建日期：2018/1/8
 * 描    述：
 * =====================================
 */
public interface CameraView {
    void resetState(int type);

    void confirmState(int type);

    void showPicture(Bitmap bitmap, boolean isVertical);

    void playVideo(Bitmap firstFrame, String url);

    void stopVideo();

    void setTip(String tip);

    void startPreviewCallback();

    boolean handlerFoucs(float x, float y);
}
