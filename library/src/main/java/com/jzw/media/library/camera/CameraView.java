package com.jzw.media.library.camera;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.jzw.media.library.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @company 上海道枢信息科技-->
 * @anthor created by jingzhanwu
 * @date 2018/3/1 0001
 * @change
 * @describe 摄像头具体实现类, 默认实现的是SurfaceView
 **/
public class CameraView extends CameraViewImpl {
    private static final String TAG = "CameraView";
    /*最短自动对焦时间*/
    private static final long MIN_TIME_FOR_AUTOFOCUS = 2000;
    /*闪光灯模式*/
    private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();

    /**
     * 初始化闪光灯相关参数
     */
    static {
        FLASH_MODES.put(CameraConstants.FLASH_OFF, Camera.Parameters.FLASH_MODE_OFF);
        FLASH_MODES.put(CameraConstants.FLASH_ON, Camera.Parameters.FLASH_MODE_ON);
        FLASH_MODES.put(CameraConstants.FLASH_TORCH, Camera.Parameters.FLASH_MODE_TORCH);
        FLASH_MODES.put(CameraConstants.FLASH_AUTO, Camera.Parameters.FLASH_MODE_AUTO);
        FLASH_MODES.put(CameraConstants.FLASH_RED_EYE, Camera.Parameters.FLASH_MODE_RED_EYE);
    }

    /*打开的摄像头id*/
    private int mCameraId;
    /*当前打开的摄像头对象*/
    private Camera mCamera;
    /*当前打开的摄像头配置参数*/
    private Camera.Parameters mCameraParameters;
    /*当前打开的摄像头信息*/
    private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();

    /*当前打开的是前置还是后置摄像头,前置==1，后置==0*/
    private int mFacing;
    /*当前摄像头的闪光灯是否关闭*/
    private int mFlash;
    /*是否开启了自动对焦*/
    private boolean mAutoFocus;
    /*摄像头预览画面旋转角度*/
    private int mDisplayOrientation;
    /*手机旋转角度*/
    private int mRotation = 0;

    /*支持的预览大小*/
    private final List<Camera.Size> mPreviewSizes = new ArrayList<>();
    /*支持的图片输出大小*/
    private final List<Camera.Size> mPictureSizes = new ArrayList<>();

    /**
     * 当前Camera设置的 预览大小
     */
    private Camera.Size mPreviewSize = null;
    /*是否正在预览*/
    private boolean mShowingPreview;
    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);
    private final AtomicBoolean isAutoFocusInProgress = new AtomicBoolean(false);

    private Handler mHandler = new Handler();


    /**
     * 默认构造函数
     *
     * @param callback
     * @param preview
     */
    protected CameraView(CameraCallback callback, PreviewImpl preview) {
        super(callback, preview);
        if (mPreview != null) {
            mPreview.setCallBack(new PreviewImpl.CallBack() {
                @Override
                public void onSurfaceChanged() {
                    if (mCamera != null) {
                        setUpPreview();
                        adjustCameraParameters();
                    }
                }
            });
        }
    }


    /**
     * 开启Camera的入口函数
     *
     * @return
     */
    @Override
    protected boolean start() {
        if (!chooseCamera()) {
            return false;
        }
        openCamera();
        if (mPreview.isReady()) {
            setUpPreview();
        }
        mShowingPreview = true;
        mCamera.startPreview();
        return true;
    }

    /**
     * 重置摄像头
     */
    @Override
    protected void reset() {

        stop();
        start();
    }

    @Override
    protected void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mShowingPreview = false;
        isPictureCaptureInProgress.set(false);
        isAutoFocusInProgress.set(false);
        releaseCamera();
    }

    @Override
    protected boolean isCameraOpened() {
        return mCamera != null;
    }

    /**
     * 重新设置Camera id,这个时候先停止之前的摄像头
     * 再开启现在设置的摄像头
     *
     * @param facing
     */
    @Override
    protected void setFacing(int facing) {
        if (mFacing == facing) {
            return;
        }
        mFacing = facing;
        if (isCameraOpened()) {
            start();
            start();
        }
    }

    @Override
    protected int getFacing() {
        return mFacing;
    }

    /**
     * 获取Camrea支持的预览尺寸宽高集合
     *
     * @return
     */

    @Override
    protected List<Camera.Size> getSupportedPreviewSize() {
        return mPreviewSizes;
    }

    /**
     * 获取Camera支持的拍照尺寸
     *
     * @return
     */
    @Override
    protected List<Camera.Size> getSupportedPictureSize() {
        return mPictureSizes;
    }

    /**
     * 获取Camera支持的视频大小
     *
     * @return
     */
    @Override
    protected List<Camera.Size> getSupportedVideoSize() {
        return mCameraParameters.getSupportedVideoSizes();
    }

    @Override
    protected Camera.Size getCurrentPreviewSize() {
        return mPreviewSize;
    }

    @Override
    protected void setAutoFocus(boolean autoFocus) {
        if (mAutoFocus == autoFocus) {
            return;
        }
        if (setAutoFocusInternal(autoFocus)) {
            mCamera.setParameters(mCameraParameters);
        }
    }

    @Override
    protected boolean getAutoFocus() {
        if (!isCameraOpened()) {
            return mAutoFocus;
        }
        String focusMode = mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }

    @Override
    protected void setFlash(int flash) {
        if (flash == mFlash) {
            return;
        }
        if (setFlashInternal(flash)) {
            mCamera.setParameters(mCameraParameters);
        }
    }

    @Override
    protected int getFlash() {
        return mFlash;
    }

    /**
     * 拍照实现
     */
    @Override
    protected void takePicture() {
        if (!isCameraOpened()) {
            return;
        }
        if (getAutoFocus()) {
            mCamera.cancelAutoFocus();
            //mCamera.autoFocus进行自动对焦，对焦好了之后再拍照，魅族MX6手机上对焦比较慢，导致这里可能需要等待好几秒才拍照成功
            //这里为了更好的体验，限制3秒之内一定要进行拍照，也就是说3秒钟之内对焦还没有成功的话那就直接进行拍照
            isAutoFocusInProgress.getAndSet(true);
            try {//从数据上报来看，部分相机自动对焦失败会发生crash，所以这里需要catch住，如果自动对焦失败了，那么就直接进行拍照
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (isAutoFocusInProgress.get()) {
                            isAutoFocusInProgress.set(false);
                            takePictureInternal();
                        }
                    }
                });
            } catch (Exception error) {
                if (isAutoFocusInProgress.get()) {
                    isAutoFocusInProgress.set(false);
                    takePictureInternal();
                }
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAutoFocusInProgress.get()) {
                        isAutoFocusInProgress.set(false);
                        takePictureInternal();
                    }
                }
            }, MIN_TIME_FOR_AUTOFOCUS);
        } else {
            takePictureInternal();
        }
    }

    @Override
    protected Camera getCamera() {
        return mCamera;
    }

    @Override
    protected void setRotation(int rotation) {
        if (mRotation == rotation) {
            return;
        }
        mRotation = rotation;
        if (isCameraOpened()) {
            int rotat = calcCameraRotation(rotation);
            mCameraParameters.setRotation(rotat);
            mCamera.setParameters(mCameraParameters);
        }
    }

    @Override
    protected int getRotation() {
        return mRotation;
    }

    @Override
    protected void setDisplayOrientation(int displayOrientation) {
        if (mDisplayOrientation == displayOrientation) {
            return;
        }
        mDisplayOrientation = displayOrientation;
        if (isCameraOpened()) {
            if (mShowingPreview) {
                mCamera.stopPreview();
            }
            int orientation = calcDisplayOrientation(displayOrientation);
            mCamera.setDisplayOrientation(orientation);
            if (mShowingPreview) {
                mCamera.startPreview();
            }
        }
    }

    @Override
    protected int getDisplayOrientation() {
        return mDisplayOrientation;
    }

    /**
     * 获取 视频录制需要设置的DisplayOrientationHint值
     *
     * @return
     */
    @Override
    protected int getDisplayOrientationHint() {
        return calcDisplayOrientation(getDisplayOrientation());
    }


    /**
     * 拍照逻辑
     * 上面的mCamera.autoFocus中的onAutoFocus这个回调会被调用两次，
     * 所以takePictureInternal方法中使用isPictureCaptureInProgress来控制takePicture的调用
     */
    private void takePictureInternal() {
        if (isCameraOpened() && !isPictureCaptureInProgress.getAndSet(true)) {
            mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    isPictureCaptureInProgress.set(false);
                    if (mCallback != null) {
                        mCallback.onPictureTaken(data);
                    }
                    camera.cancelAutoFocus();
                    camera.startPreview();
                }
            });
        }
    }

    /**
     * 从手机中选择一个摄像头,有的手机不知一个摄像头
     *
     * @return
     */
    private boolean chooseCamera() {
        int count = Camera.getNumberOfCameras();
        for (int i = 0; i < count; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            if (mCameraInfo.facing == mFacing) {
                mCameraId = i;
                return true;
            }
        }
        return false;
    }

    /**
     * 设置Camera预览view
     */
    private void setUpPreview() {
        try {
            if (mPreview.getOutputClass() == SurfaceHolder.class) {
                if (mShowingPreview) {
                    mCamera.stopPreview();
                }
                mCamera.setPreviewDisplay(mPreview.getSurfaceHolder());
                if (mShowingPreview) {
                    mCamera.startPreview();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开一个Camera，Camera的真正初始化
     */
    private void openCamera() {
        if (mCamera != null) {
            releaseCamera();
        }
        mCamera = Camera.open(mCameraId);
        mCameraParameters = mCamera.getParameters();
        // Supported preview sizes
        mPreviewSizes.clear();
        //获得Camera支持的预览大小集合
        for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
            mPreviewSizes.add(size);
        }
        mPictureSizes.clear();
        //获取Camera支持的picture 大小集合
        for (Camera.Size size : mCameraParameters.getSupportedPictureSizes()) {
            mPictureSizes.add(size);
        }
        //设置Camera的 配置参数
        adjustCameraParameters();
        //设置Camera的预览旋转角度
        mCamera.setDisplayOrientation(calcDisplayOrientation(mDisplayOrientation));
        Log.i(TAG, "Camera is Opened");
    }

    /**
     * 释放摄像头资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            Log.i(TAG, "Camera is Closed");
        }
    }


    /**
     * 配置Camera的Parameters
     */
    private void adjustCameraParameters() {
        //拿到制定宽高比的 支持大小集合
        if (mPreviewSizes.size() <= 0) { // Not supported
            mPreviewSizes.addAll(mCameraParameters.getSupportedPreviewSizes());
        }
        int w = mPreview.getWidth();
        int h = mPreview.getHeight();
        mPreviewSize = Utils.getCameraOptimalSize(mPreviewSizes, Math.max(w, h), Math.min(w, h));
        Log.i(TAG, "Camera预览大小" + mPreviewSize.width + " * " + mPreviewSize.height);
        //计算Camera的图片输出大小，PictureSize
        Camera.Size pictureSize = Utils.getCameraOptimalSize(mPictureSizes, Math.max(w, h), Math.min(w, h));
        Log.i(TAG, "Camera Picture大小" + pictureSize.width + " * " + pictureSize.height);

        if (mShowingPreview) {
            mCamera.stopPreview();//在重新设置CameraParameters之前需要停止预览
        }
        mCameraParameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        mCameraParameters.setPictureSize(pictureSize.width, pictureSize.height);
        //设置拍照后照片的旋转角度
        mCameraParameters.setRotation(calcCameraRotation(mRotation));
        setAutoFocusInternal(mAutoFocus);
        setFlashInternal(mFlash);
        mCamera.setParameters(mCameraParameters);
        //开启HDR
        if (mCameraParameters.getSupportedSceneModes().contains(Camera.Parameters.SCENE_MODE_HDR)) {
            mCameraParameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
        }
        //开启防抖动
        if (mCameraParameters.isVideoStabilizationSupported()) {
            mCameraParameters.setVideoStabilization(true);
        }
        Log.i(TAG, "Camera 参数设置成功");
        if (mShowingPreview) {
            mCamera.startPreview();
        }
    }


    /**
     * 根据屏幕旋转的度数来判断是否是横屏
     */
    private boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == CameraConstants.LANDSCAPE_90 || orientationDegrees == CameraConstants.LANDSCAPE_270);
    }

    /**
     * 根据手机旋转角度 计算camera Rotation,主要用于拍照时图片输出旋转角度设置
     *
     * @param screenOrientationDegrees
     * @return
     */
    private int calcCameraRotation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (mCameraInfo.orientation + screenOrientationDegrees) % 360;
        } else {
            final int landscapeFlip = isLandscape(screenOrientationDegrees) ? 180 : 0;
            return (mCameraInfo.orientation + screenOrientationDegrees + landscapeFlip) % 360;
        }
    }

    /**
     * 计算Camera的预览画面旋转角度
     *
     * @param screenOrientationDegrees
     * @return
     */
    private int calcDisplayOrientation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - (mCameraInfo.orientation + screenOrientationDegrees) % 360) % 360;
        } else {
            return (mCameraInfo.orientation - screenOrientationDegrees + 360) % 360;
        }
    }

    /**
     * 设置自动对焦
     */
    private boolean setAutoFocusInternal(boolean autoFocus) {
        mAutoFocus = autoFocus;
        if (isCameraOpened()) {
            final List<String> modes = mCameraParameters.getSupportedFocusModes();
            if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                attachFocusTapListener();
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                detachFocusTapListener();
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                detachFocusTapListener();
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            } else {
                detachFocusTapListener();
                mCameraParameters.setFocusMode(modes.get(0));//getSupportedFocusModes方法返回的列表至少有一个元素
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置闪光灯
     */
    private boolean setFlashInternal(int flash) {
        if (isCameraOpened()) {
            List<String> modes = mCameraParameters.getSupportedFlashModes();//如果不支持设置闪关灯的话，getSupportedFlashModes方法会返回null
            String mode = FLASH_MODES.get(flash);
            if (modes != null && modes.contains(mode)) {
                mCameraParameters.setFlashMode(mode);
                mFlash = flash;
                return true;
            }
            String currentMode = FLASH_MODES.get(mFlash);
            if (modes == null || !modes.contains(currentMode)) {
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlash = CameraConstants.FLASH_OFF;
                return true;
            }
            return false;
        } else {
            mFlash = flash;
            return false;
        }
    }

    @TargetApi(14)
    private void attachFocusTapListener() {
        mPreview.getView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mCamera != null) {
                        Camera.Parameters parameters = mCamera.getParameters();
                        String focusMode = parameters.getFocusMode();
                        Rect rect = calculateFocusArea(event.getX(), event.getY());
                        List<Camera.Area> meteringAreas = new ArrayList<>();
                        meteringAreas.add(new Camera.Area(rect, getFocusMeteringAreaWeight()));

                        if (parameters.getMaxNumFocusAreas() != 0 && focusMode != null &&
                                (focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO) ||
                                        focusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO) ||
                                        focusMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) ||
                                        focusMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                                ) {
                            if (!parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                return false; //cannot autoFocus
                            }
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                            parameters.setFocusAreas(meteringAreas);
                            if (parameters.getMaxNumMeteringAreas() > 0) {
                                parameters.setMeteringAreas(meteringAreas);
                            }
                            mCamera.setParameters(parameters);

                            try {
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                        resetFocus(success, camera);
                                    }
                                });
                            } catch (Exception error) {
                                //ignore this exception
                            }
                        } else if (parameters.getMaxNumMeteringAreas() > 0) {
                            if (!parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                return false; //cannot autoFocus
                            }
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                            parameters.setFocusAreas(meteringAreas);
                            parameters.setMeteringAreas(meteringAreas);
                            mCamera.setParameters(parameters);

                            try {
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                        resetFocus(success, camera);
                                    }
                                });
                            } catch (Exception error) {
                                //ignore this exception
                            }
                        } else {
                            try {
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                    }
                                });
                            } catch (Exception error) {
                                error.printStackTrace();
                            }
                        }
                    }
                }
                return true;
            }
        });
    }

    @TargetApi(14)
    private void resetFocus(final boolean success, final Camera camera) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    camera.cancelAutoFocus();
                    try {
                        Camera.Parameters params = camera.getParameters();//数据上报中红米Note3在这里可能crash
                        if (params != null && !params.getFocusMode().equalsIgnoreCase(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            //之前这里并没有考虑相机是否支持FOCUS_MODE_CONTINUOUS_PICTURE，可能是因为这个原因导致部分三星机型上调用后面的setParameters失败
                            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                                params.setFocusAreas(null);
                                params.setMeteringAreas(null);
                                camera.setParameters(params);//数据上报中三星低端机型在这里可能crash
                            }
                        }
                    } catch (Exception error) {
                        //ignore this exception
                    }
                }
            }
        }, DELAY_MILLIS_FEFORE_RESETTING_FOCUS);
    }

    /**
     * 计算对焦区域
     *
     * @param x
     * @param y
     * @return
     */
    private Rect calculateFocusArea(float x, float y) {
        int buffer = getFocusAreaSize() / 2;
        int centerX = calculateCenter(x, mPreview.getView().getWidth(), buffer);
        int centerY = calculateCenter(y, mPreview.getView().getHeight(), buffer);
        return new Rect(
                centerX - buffer,
                centerY - buffer,
                centerX + buffer,
                centerY + buffer
        );
    }

    private static int calculateCenter(float coord, int dimen, int buffer) {
        int normalized = (int) ((coord / dimen) * 2000 - 1000);
        if (Math.abs(normalized) + buffer > 1000) {
            if (normalized > 0) {
                return 1000 - buffer;
            } else {
                return -1000 + buffer;
            }
        } else {
            return normalized;
        }
    }


}
