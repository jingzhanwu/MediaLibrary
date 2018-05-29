package com.jzw.media.library.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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

    private static Activity mActivity;
    private static Fragment mFragment;
    /**
     * 请求码
     */
    private int mRequestCode;
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
        return get(activity, null);
    }

    public static JMediaView with(Fragment fragment) {
        return get(fragment.getActivity(), fragment);
    }

    private static JMediaView get(Activity activity, Fragment fragment) {
        mActivity = activity;
        mFragment = fragment;
        return new JMediaView();
    }

    private void init() {
        mMode = JCameraView.BUTTON_STATE_BOTH;
        mVideoPath = FileUtil.getVideoDir();
        mTip = "轻触拍照，长按录制";
        mMediaQuality = JCameraView.MEDIA_QUALITY_MIDDLE;
        mMaxDuration = 1000 * 60;

        mRequestCode = -1;
    }

    public JMediaView setMode(int mode) {
        mMode = mode;
        return this;
    }

    public JMediaView enableMultiPicture(boolean multiPicture) {
        mMultiPic = multiPicture;
        return this;
    }

    public JMediaView setVideoPath(String dirPath) {
        mVideoPath = dirPath;
        return this;
    }

    public JMediaView setTip(String tip) {
        mTip = tip;
        return this;
    }

    public JMediaView setMaxDuration(int duration) {
        mMaxDuration = duration;
        return this;
    }

    public JMediaView setMediaQuality(int mediaQuality) {
        mMediaQuality = mediaQuality;
        return this;
    }

    public void startCamera(int requestCode) {
        mRequestCode = requestCode;
        startCamera();
    }

    public void startCamera() {
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

        if (mRequestCode >= 0) {
            code = mRequestCode;
        }

        if (mActivity == null) {
            return;
        }

        Intent intent = new Intent(mActivity, JCameraActivity.class);
        intent.putExtra("mode", mMode);
        intent.putExtra("videoPath", mVideoPath);
        intent.putExtra("quality", mMediaQuality);
        intent.putExtra("tip", mTip);
        intent.putExtra("multiPic", mMultiPic);
        intent.putExtra("duration", mMaxDuration);


        if (mFragment != null) {
            mFragment.startActivityForResult(intent, code);
        } else {
            mActivity.startActivityForResult(intent, code);
        }
    }
}
