package com.jzw.media.library.camera;

/**
 * @anthor created by jingzhanwu
 * @date 2018/2/24 0024
 * @change
 * @describe 视频录制回调接口
 **/
public interface OnVideoRecordListener {
    void onStartRecord(String path);

    void onRestartRecord(String path);

    void onPauseRecord(String path);

    void onCompleteRecord(String path);

    void onResetRecord(String path);

    void onRecordTimer(String time, int secounds);
}
