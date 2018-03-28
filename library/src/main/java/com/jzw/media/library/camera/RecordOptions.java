package com.jzw.media.library.camera;

import android.media.MediaRecorder;

import com.jzw.media.library.MediaConfig;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @anthor created by jingzhanwu
 * @date 2018/2/23 0023
 * @change
 * @describe 视频录制相关配置参数
 **/
public class RecordOptions {
    /**
     * 音频源
     */
    private int mAudioSource;
    /**
     * 视频源 默认是摄像头
     */
    private int mVideoSource;
    /**
     * 输出文件的编码格式 MP4，mp3，avi等等
     */
    private int mOutputFormat;
    /**
     * 采集的帧数，值越大视频越清晰，文件越大
     */
    private int mVideoFrameRate;
    /**
     * 视频输出宽度
     */
    private int mVideoWidth;
    /**
     * 视频输出高度
     */
    private int mVideoHeight;
    /**
     * 音频录制格式
     */
    private int mAudioEncoder;
    /**
     * 视频录制格式
     */
    private int mVideoEncoder;
    /**
     * 帧频率 1*1024 * 1024 前面的值越大越清晰
     */
    private int mVideoEncoderBitRate;

    /**
     * 视频输出方向，默认设置为90，保持竖屏录制
     */
    private int mOrientationHint;
    /**
     * 最大录制时长
     */
    private int mMaxDuration;

    /**
     * 输出文件名，全路劲
     */
    private String mOutputFile;

    public RecordOptions() {
        mAudioSource = MediaRecorder.AudioSource.CAMCORDER;
        mVideoSource = MediaRecorder.VideoSource.CAMERA;
        mOutputFormat = MediaRecorder.OutputFormat.MPEG_4;
        mVideoFrameRate = 30;
        mVideoWidth = MediaConfig.SIZE_1;
        mVideoHeight = MediaConfig.SIZE_2;
        mAudioEncoder = MediaRecorder.AudioEncoder.AAC;
        mVideoEncoder = MediaRecorder.VideoEncoder.H264;
        mVideoEncoderBitRate = 1 * 1024 * 1024;
        mOrientationHint = 90;
        mMaxDuration = 1000 * 60;
    }

    public RecordOptions setOutputFile(String filepath) {
        mOutputFile = filepath;
        return this;
    }


    public String getOutputFile() {
        return mOutputFile;
    }

    public RecordOptions setOutputFile(File file) throws FileNotFoundException {
        if (file == null) {
            throw new FileNotFoundException("file not found!");
        }
        mOutputFile = file.getPath();
        return this;
    }

    public RecordOptions setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        return this;
    }

    public RecordOptions setMaxDuration(int duration) {
        mMaxDuration = duration;
        return this;
    }

    public int getMaxDuration() {
        return mMaxDuration;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public RecordOptions setAudioSource(int audioSource) {
        this.mAudioSource = audioSource;
        return this;
    }

    public int getAudioSource() {
        return mAudioSource;
    }

    public RecordOptions setVideoSource(int videoSource) {
        this.mVideoSource = videoSource;
        return this;
    }

    public int getVideoSource() {
        return mVideoSource;
    }

    public RecordOptions setOutputFormat(int outputFormat) {
        this.mOutputFormat = outputFormat;
        return this;
    }

    public int getOutputFormat() {
        return mOutputFormat;
    }

    public RecordOptions setVideoFrameRate(int videoFrameRate) {
        this.mVideoFrameRate = videoFrameRate;
        return this;
    }

    public int getVideoFrameRate() {
        return mVideoFrameRate;
    }

    public RecordOptions setAudioEncoder(int audioEncoder) {
        this.mAudioEncoder = audioEncoder;
        return this;
    }

    public int getAudioEncoder() {
        return mAudioEncoder;
    }

    public RecordOptions setVideoEncoder(int videoEncoder) {
        this.mVideoEncoder = videoEncoder;
        return this;
    }

    public int getVideoEncoder() {
        return mVideoEncoder;
    }

    public RecordOptions setVideoEncoderBitRate(int videoEncoderBitRate) {
        this.mVideoEncoderBitRate = videoEncoderBitRate;
        return this;
    }

    public int getVideoEncoderBitRate() {
        return mVideoEncoderBitRate;
    }
}
