package com.jzw.media.library.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.view.Display;
import android.view.SurfaceView;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @company 上海道枢信息科技-->
 * @anthor created by jingzhanwu
 * @date 2018/3/1 0001
 * @change
 * @describe Camera真正操作管理类
 **/
public class CameraManager {
    private static final String TAG = "CameraManager";
    public static final int FACING_BACK = CameraConstants.FACING_BACK;
    public static final int FACING_FRONT = CameraConstants.FACING_FRONT;

    @IntDef({FACING_BACK, FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Facing {
    }

    public static final int FLASH_OFF = CameraConstants.FLASH_OFF;
    public static final int FLASH_ON = CameraConstants.FLASH_ON;
    public static final int FLASH_TORCH = CameraConstants.FLASH_TORCH;//手电筒状态
    public static final int FLASH_AUTO = CameraConstants.FLASH_AUTO;
    public static final int FLASH_RED_EYE = CameraConstants.FLASH_RED_EYE;

    @IntDef({FLASH_OFF, FLASH_ON, FLASH_TORCH, FLASH_AUTO, FLASH_RED_EYE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Flash {
    }

    private Context mContext;
    private PreviewImpl mPreviewImpl;
    private CameraViewImpl mCameraViewImpl;
    private SurfaceView mSurfaceView;
    private DisplayOrientationDetector mDisplayOrientationDetector;
    private CameraCallback mCameraCallback;


    @SuppressLint("WrongConstant")
    public CameraManager(Context context, SurfaceView sufaceView, CameraCallback callback) {
        mContext = context;
        mSurfaceView = sufaceView;
        mCameraCallback = callback;
        mPreviewImpl = new SurfaceViewPreview(mSurfaceView);
        mCameraViewImpl = new CameraView(mCameraCallback, mPreviewImpl);

        //默认设置为后置摄像头
        setFacing(FACING_BACK);
        //默认开启自动对焦
        setAutoFocus(true);
        //默认关闭闪光灯
        setFlash(CameraConstants.FLASH_OFF);
        //启监听手机旋转角度变化监听器
        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {
            @Override
            public void onDisplayOrientationChanged(int displayOrientation) {
                mCameraViewImpl.setDisplayOrientation(displayOrientation);
            }

            @Override
            public void onRotationChanged(int rotation) {
                mCameraViewImpl.setRotation(rotation);
            }
        };
        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        mDisplayOrientationDetector.enable(display);
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * 设置摄像头操作监听器
     *
     * @param listener
     */

    public void setOnCameraCallback(CameraCallback listener) {
        mCameraCallback = listener;
    }

    public CameraViewImpl getCameraViewImpl() {
        return mCameraViewImpl;
    }

    public PreviewImpl getPreview() {
        return mPreviewImpl;
    }

    public boolean start() {
        boolean isSuccess = mCameraViewImpl.start();
        if (!isSuccess) {
            if (mPreviewImpl == null || mPreviewImpl.getView() == null) {
                mPreviewImpl = new SurfaceViewPreview(mSurfaceView);
            }
            mCameraViewImpl = new CameraView(mCameraCallback, mPreviewImpl);
            isSuccess = mCameraViewImpl.start();
        }
        return isSuccess;
    }

    public void stop() {
        mCameraViewImpl.stop();
    }

    public void reset() {
        mCameraViewImpl.reset();
    }

    /**
     * 判断摄像头是否打开了
     */
    public boolean isCameraOpened() {
        return mCameraViewImpl.isCameraOpened();
    }

    /**
     * 设置使用前置还是后置摄像头
     */
    public void setFacing(@Facing int facing) {
        mCameraViewImpl.setFacing(facing);
        if (mCameraCallback != null) {
            mCameraCallback.onCameraChange(facing);
        }
    }

    @Facing
    public int getFacing() {
        return mCameraViewImpl.getFacing();
    }

    /**
     * 设置自动对焦模式
     */
    public void setAutoFocus(boolean autoFocus) {
        mCameraViewImpl.setAutoFocus(autoFocus);
    }

    public boolean getAutoFocus() {
        return mCameraViewImpl.getAutoFocus();
    }

    public void clickFlash() {
        if (getFlash() == FLASH_OFF) {
            setFlash(FLASH_ON);
        } else {
            setFlash(FLASH_OFF);
        }
    }

    /**
     * 设置闪光灯模式
     */
    public void setFlash(@Flash int flash) {
        mCameraViewImpl.setFlash(flash);
        if (mCameraCallback != null) {
            if (flash == FLASH_OFF) {
                mCameraCallback.onFlagOff(flash);
            } else if (flash == FLASH_ON) {
                mCameraCallback.onFlashOn(flash);
            }
        }
    }

    @Flash
    public int getFlash() {
        return mCameraViewImpl.getFlash();
    }

    /**
     * 拍照
     */
    public void takePicture() {
        mCameraViewImpl.takePicture();
    }

    /**
     * 摄像头前后置切换
     */
    public void switchCamera() {
        if (mCameraViewImpl != null) {
            int facing = getFacing();
            mCameraViewImpl.setFacing(facing == FACING_FRONT ? FACING_BACK : FACING_FRONT);
            if (mCameraCallback != null) {
                mCameraCallback.onCameraChange(facing == FACING_FRONT ? FACING_BACK : FACING_FRONT);
            }
        }
    }
}
