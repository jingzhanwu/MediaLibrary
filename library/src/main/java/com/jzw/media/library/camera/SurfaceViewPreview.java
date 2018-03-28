
package com.jzw.media.library.camera;

import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * @company 上海道枢信息科技-->
 * @anthor created by jingzhanwu
 * @date 2018-03-01
 * @change
 * @describe SurfaceView实现类
 **/
public class SurfaceViewPreview extends PreviewImpl {

    private final static String TAG = "SurfaceViewPreview";
    private final SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    public SurfaceViewPreview(final SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(TAG, "SurfaceView create successfull");
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
                Log.i(TAG, "SurfaceView size has changed " + width + " * " + height);
                mSurfaceHolder = h;
                setSize(width, height);
                if (!ViewCompat.isInLayout(mSurfaceView)) {
                    dispatchSurfaceChanged();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder h) {
                setSize(0, 0);
            }
        });
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    public Surface getSurface() {
        return getSurfaceHolder().getSurface();
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    @Override
    public View getView() {
        return mSurfaceView;
    }

    @Override
    public Class getOutputClass() {
        return SurfaceHolder.class;
    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {
    }

    @Override
    public boolean isReady() {
        return getWidth() != 0 && getHeight() != 0;
    }

}
