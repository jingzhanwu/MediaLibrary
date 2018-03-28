package com.jzw.media.library.ui;

import android.app.Activity;
import android.content.Intent;

import com.dev.jzw.helper.util.FileUtil;
import com.jzw.media.library.MediaConfig;
import com.jzw.media.library.media.JCameraView;

/**
 * @company 上海道枢信息科技-->
 * @anthor created by jingzhanwu
 * @date 2018/3/22 0022
 * @change
 * @describe 对外提供的入口类
 **/
public class JMediaView {

    private static JMediaView mInstance;
    private static Activity mActivity;
    /**
     * 摄像头模式，拍照 ，录制  拍照+录制
     */
    private int mMode;
    /**
     * 视频存储地址
     */
    private String mVideoPath;
    /**
     * 提示语
     */
    private String mTip;
    /**
     * 视频录制质量
     */
    private int mMediaQuality;
    /**
     * 是否开启多图片拍照模式 ，模式不开启
     */
    private boolean mMultiPic;
    /**
     * 视频录制的最大时长  毫秒
     */
    private int mMaxDuration;

    private JMediaView() {
        init();
    }

    public static JMediaView with(Activity activity) {
        mActivity = activity;
        if (mInstance == null) {
            synchronized (JMediaView.class) {
                if (mInstance == null) {
                    mInstance = new JMediaView();
                }
            }
        }
        return mInstance;
    }

    private void init() {
        mMode = JCameraView.BUTTON_STATE_BOTH;
        mVideoPath = FileUtil.getVideoDir();
        mTip = "轻触拍照，长按录制";
        mMediaQuality = JCameraView.MEDIA_QUALITY_MIDDLE;
        mMaxDuration = 1000 * 60;
    }

    public JMediaView setMode(int mode) {
        mMode = mode;
        return mInstance;
    }

    public JMediaView enableMultiPicture(boolean multiPicture) {
        mMultiPic = multiPicture;
        return mInstance;
    }

    public JMediaView setVideoPath(String dirPath) {
        mVideoPath = dirPath;
        return mInstance;
    }

    public JMediaView setTip(String tip) {
        mTip = tip;
        return mInstance;
    }

    public JMediaView setMaxDuration(int duration) {
        mMaxDuration = duration;
        return mInstance;
    }

    public JMediaView setMediaQuality(int mediaQuality) {
        mMediaQuality = mediaQuality;
        return mInstance;
    }

    public JMediaView startCamera() {
        int code = MediaConfig.PICTURE_VIDEO_CODE;
        if (mMode == JCameraView.BUTTON_STATE_ONLY_CAPTURE) {
            code = MediaConfig.PICTURE_CODE;
            mTip = "";
        } else if (mMode == JCameraView.BUTTON_STATE_ONLY_RECORDER) {
            code = MediaConfig.VIDEO_CODE;
            mTip = "";
        } else if (mMode == JCameraView.BUTTON_STATE_BOTH) {
            mTip = "轻触拍照，长按录制";
            code = MediaConfig.PICTURE_VIDEO_CODE;
        }

        Intent intent = new Intent(mActivity, JCameraActivity.class);
        intent.putExtra("mode", mMode);
        intent.putExtra("videoPath", mVideoPath);
        intent.putExtra("quality", mMediaQuality);
        intent.putExtra("tip", mTip);
        intent.putExtra("multiPic", mMultiPic);
        intent.putExtra("duration", mMaxDuration);

        mActivity.startActivityForResult(intent, code);
        return mInstance;
    }

}
