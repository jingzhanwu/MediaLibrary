package com.jzw.media.library.camera;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * @company 上海道枢信息科技-->
 * @anthor created by jingzhanwu
 * @date 2018/3/1 0001
 * @change
 * @describe 不同方式实现PreView的抽象基类
 **/
public abstract class PreviewImpl {
    private int mWidth;
    private int mHeight;
    private CallBack mCallback;

    public SurfaceHolder getSurfaceHolder() {
        return null;
    }

    public SurfaceView getSurfaceView() {
        return null;
    }

    public void setBufferSize(int width, int height) {

    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setCallBack(CallBack callBack) {
        mCallback = callBack;
    }

    protected void dispatchSurfaceChanged() {
        mCallback.onSurfaceChanged();
    }

    public abstract Surface getSurface();

    public abstract View getView();

    /**
     * SurfaceHolder 的class
     *
     * @return
     */
    public abstract Class getOutputClass();

    public abstract void setDisplayOrientation(int displayOrientation);

    public abstract boolean isReady();

    /**
     * SurfaceView 改变回调接口
     */
    public interface CallBack {
        void onSurfaceChanged();
    }
}
