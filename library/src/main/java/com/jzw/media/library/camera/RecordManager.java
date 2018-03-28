package com.jzw.media.library.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.dev.jzw.helper.util.FileUtil;
import com.jzw.media.library.Utils;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @anthor created by jingzhanwu
 * @date 2018/2/24 0024
 * @change
 * @describe 视频录制类
 **/
public class RecordManager implements IStatus {
    private String TAG = "RecordManager";
    private String mVideoFile;
    private MediaRecorder mMediaRecorder;
    private RecordOptions mRecordOptions;
    private CameraManager mCameraManager;
    private OnVideoRecordListener mListener;
    private Timer mTimer;
    private int mTimeCount = 0;
    private boolean mIsRecording = false;

    private int mMaxDuration = 0;
    /**
     * 当前录制的状态
     */
    private int mCurrentStatus;

    public RecordManager(CameraManager cameraManager, RecordOptions recordOptions) {
        mCameraManager = cameraManager;
        mRecordOptions = recordOptions;
        mCurrentStatus = PREPARE;
        mTimeCount = 0;
    }

    public RecordManager(CameraManager cameraManager) {
        mCameraManager = cameraManager;
        mRecordOptions = new RecordOptions();
        mCurrentStatus = PREPARE;
        mTimeCount = 0;
    }

    public void setOnVideoRecordListener(OnVideoRecordListener listener) {
        mListener = listener;
    }

    public void setOnCameraChangeListener(CameraCallback listener) {
        if (mCameraManager != null) {
            mCameraManager.setOnCameraCallback(listener);
        }
    }


    /**
     * 开始录制
     */
    public void startRecord() {
        switch (mCurrentStatus) {
            case PREPARE:
                record();
                break;
            case RECORDDING:
                pauseRecord();
                break;
            case PAUSE:
                restartRecord();
                break;
            case STOP:
                resetRecord();
                record();
                break;
        }
    }

    /**
     * 录制
     */
    public void record() {
        if (!mIsRecording) {
            if (mCameraManager.getCameraViewImpl() == null) {
                mCameraManager.start();
            }
            initRecorder();
            if (mMediaRecorder == null) {
                return;
            }
            //调用start之前一定要调用Camera的unlock()方法，否则一直报 faild -19错误
            mCameraManager.getCameraViewImpl().getCamera().unlock();
            mMediaRecorder.start();
            mIsRecording = true;
            mCurrentStatus = RECORDDING;
            startTimer();
            if (mListener != null) {
                mListener.onStartRecord(mVideoFile);
            }
        }
    }

    /**
     * 暂停录制
     */
    @SuppressLint("NewApi")
    public void pauseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.pause();
            mCurrentStatus = PAUSE;
            mIsRecording = false;
            if (mCameraManager != null && mCameraManager.getCameraViewImpl() != null) {
                mCameraManager.getCameraViewImpl().getCamera().stopPreview();
            }
            stopTimer();
            if (mListener != null) {
                mListener.onPauseRecord(mVideoFile);
            }
        }
    }

    /**
     * 继续录制，暂停之后又开始录制时调用
     */
    @SuppressLint("NewApi")
    public void restartRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.resume();
            mCameraManager.getCameraViewImpl().getCamera().startPreview();
            mCurrentStatus = RECORDDING;
            mIsRecording = true;
            startTimer();
            if (mListener != null) {
                mListener.onRestartRecord(mVideoFile);
            }
        }
    }

    /**
     * 录制完成
     */
    public void finishRecord() {
        stopRecord();
        if (mListener != null) {
            mListener.onCompleteRecord(mVideoFile);
        }
    }

    /**
     * 重置录制器，设置到录制初始状态
     */
    public void resetRecord() {
        stopRecord();
        deleteFile(mVideoFile);
        if (mListener != null) {
            mListener.onResetRecord(mVideoFile);
        }
    }

    /**
     * 停止录制
     */
    @SuppressLint("NewApi")
    public void stopRecord() {
        if (mMediaRecorder != null) {
            if (mIsRecording) {
                mMediaRecorder.stop();
            }
        }
        mCurrentStatus = PREPARE;
        mIsRecording = false;
        mTimeCount = 0;
        if (mCameraManager != null && mCameraManager.getCameraViewImpl().getCamera() != null) {
            mCameraManager.getCameraViewImpl().getCamera().startPreview();
        }
        stopTimer();
    }

    /**
     * 闪光灯关闭 打开
     */
    public void clickFlash() {
        if (mCameraManager != null) {
            mCameraManager.clickFlash();
        }

    }

    /**
     * 前后摄像头切换
     */
    public void switchCamera() {
        if (mCameraManager != null) {
            mCameraManager.switchCamera();
        }
    }

    /**
     * 视频录制器初始化
     */
    private void initRecorder() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.reset();
        }
        try {
            mMediaRecorder.setCamera(mCameraManager.getCameraViewImpl().getCamera());
            // 这两项需要放在setOutputFormat之前
            mMediaRecorder.setAudioSource(mRecordOptions.getAudioSource());
            mMediaRecorder.setVideoSource(mRecordOptions.getVideoSource());
            // Set output file format，输出格式
            mMediaRecorder.setOutputFormat(mRecordOptions.getOutputFormat());
            //必须在setEncoder之前
            mMediaRecorder.setVideoFrameRate(mRecordOptions.getVideoFrameRate());  //帧数  一分钟帧，15帧就够了
            Camera.Size size = getVideoSize();
            Log.i(TAG, "videoSize>> " + size.width + " * " + size.height);
            mMediaRecorder.setVideoSize(size.width, size.height);//这个大小就够了
            // 这两项需要放在setOutputFormat之后
            mMediaRecorder.setAudioEncoder(mRecordOptions.getAudioEncoder());
            mMediaRecorder.setVideoEncoder(mRecordOptions.getVideoEncoder());

            mMediaRecorder.setVideoEncodingBitRate(mRecordOptions.getVideoEncoderBitRate());//第一个数字越大，清晰度就越高，考虑文件大小的缘故，就调整为1

            int facing = mCameraManager.getFacing();
            int orientation = mCameraManager.getCameraViewImpl().getDisplayOrientationHint();
            if (facing == 1) {
                orientation = 270;
            }
            mMediaRecorder.setOrientationHint(orientation);
            //设置记录会话的最大持续时间（毫秒）
            mMaxDuration = mRecordOptions.getMaxDuration();
            mMediaRecorder.setMaxDuration(mMaxDuration);
            //把摄像头的画面给它
            Surface surface = mCameraManager.getPreview().getSurface();
            mMediaRecorder.setPreviewDisplay(surface);
            //创建好视频文件用来保存
            //设置创建好的输入路径
            mMediaRecorder.setOutputFile(createOutputFile());
            mMediaRecorder.prepare();
        } catch (Exception e) {
            //一般没有录制权限或者录制参数出现问题都走这里
            e.printStackTrace();
            //还是没权限啊
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * 创建视频输出文件
     *
     * @return
     */
    private String createOutputFile() {
        if (!TextUtils.isEmpty(mRecordOptions.getOutputFile())) {
            mVideoFile = mRecordOptions.getOutputFile();
            return mVideoFile;
        }
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException("no such sdcard");
        }
        String videoDir = FileUtil.getVideoDir();
        mVideoFile = videoDir + File.separator + "video_" + System.currentTimeMillis() + ".mp4";
        return mVideoFile;
    }

    /**
     * 计算视频的输出大小
     *
     * @return
     */
    private Camera.Size getVideoSize() {
        //根据Camera设置的预览大小设置视频的输出大小
        Camera.Size preSize = mCameraManager.getCameraViewImpl().getCurrentPreviewSize();
        List<Camera.Size> videoSize = mCameraManager.getCameraViewImpl().getSupportedVideoSize();
        //将预览大小的一半再 * 0.8 作为计算视频输出大小的参考
        int w = (int) ((preSize.width / 2) * 0.8);
        int h = (int) ((preSize.height / 2) * 0.8);
        Camera.Size size = Utils.getCameraOptimalSize(videoSize, w, h);
        return size;
    }

    /**
     * 释放资源
     */
    private void releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            try {
                mMediaRecorder.reset();
                mMediaRecorder.release();
                if (mCameraManager != null) {
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder = null;
    }

    /**
     * 完全释放所有资源，包括录制器和摄像头
     */
    public void release() {
        releaseRecord();
        if (mCameraManager != null) {
            mCameraManager.stop();
            mCameraManager = null;
        }
        if (mRecordOptions != null) {
            mRecordOptions = null;
        }
    }

    private void startTimer() {
        stopTimer();
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mTimeCount++;
                updateTimer();
                if (mTimeCount * 1000 >= mMaxDuration) {
                    //已经录制到设置的最大市场,完成录制
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            finishRecord();
                        }
                    });
                }
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }


    /**
     * 更新计时器
     */
    private void updateTimer() {
        if (mListener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mListener.onRecordTimer(Utils.formatSeconds(mTimeCount), mTimeCount);
                }
            });
        }
    }

    /**
     * 删除录制的文件
     *
     * @param fileName
     * @return
     */
    private boolean deleteFile(String fileName) {
        return FileUtil.deleteFileFromDir(fileName, false);
    }

    public MediaRecorder getMediaRecorder() {
        return mMediaRecorder;
    }

    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    public RecordOptions getRecordOptions() {
        return mRecordOptions;
    }

    public String getOutputFile() {
        return mRecordOptions.getOutputFile();
    }

    public RecordManager setCameraManager(CameraManager cameraManager) {
        mCameraManager = cameraManager;
        return this;
    }

    public RecordManager setRecordOptions(RecordOptions recordOptions) {
        mRecordOptions = recordOptions;
        return this;
    }
}
