package com.jzw.media.library.player;

import android.media.MediaPlayer;
import android.text.TextUtils;

import java.io.IOException;

/**
 * @company 上海道枢信息科技-->
 * @anthor created by jingzhanwu
 * @date 2018-02-22
 * @change
 * @describe 音视频 播放管理类
 **/
public class PlayerManager {
    /**
     * 录制 和播放的文件地址
     */
    private String filePath;
    private MediaPlayer mediaPlayer;
    private OnPlayerCallback mCallback;

    private static PlayerManager mInstance;

    private PlayerManager() {
    }

    public static PlayerManager get() {
        if (mInstance == null) {
            mInstance = new PlayerManager();
        }
        return mInstance;
    }

    public static PlayerManager with() {
        return get();
    }

    public void setOnPlayerCallback(OnPlayerCallback callback) {
        mCallback = callback;
    }

    public PlayerManager playAudio(String audioPath, OnPlayerCallback callback) {
        if (TextUtils.isEmpty(audioPath)) {
            return mInstance;
        }
        releasPlayer();
        mCallback = callback;
        filePath = audioPath;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.reset();
            if (mCallback != null) {
                mCallback.onPrepare(mediaPlayer);
            }
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    if (mCallback != null) {
                        mCallback.onStart(mp, filePath);
                    }
                }
            });

            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (mCallback != null) {
                        mCallback.onPlaying(mp);
                    }
                    return false;
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mCallback != null) {
                        mCallback.onStop();
                    }
                    stop();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (mCallback != null) {
                        mCallback.onFaild(mp);
                    }
                    return false;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mInstance;
    }

    /**
     * 释放播放器资源
     */
    public void releasPlayer() {
        stop();
        if (mCallback != null) {
            mCallback.onStop();
        }
        mCallback = null;
    }

    private void stop() {
        if (mediaPlayer == null) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                filePath = null;
            }
        }.start();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isPlaying() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    public interface OnPlayerCallback {
        void onPrepare(MediaPlayer mp);

        void onStart(MediaPlayer mp, String url);

        void onPlaying(MediaPlayer mp);

        void onStop();

        void onFaild(MediaPlayer mp);
    }
}
