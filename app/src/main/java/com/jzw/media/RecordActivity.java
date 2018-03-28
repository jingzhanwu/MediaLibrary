package com.jzw.media;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jzw.media.library.camera.CameraCallback;
import com.jzw.media.library.camera.CameraManager;
import com.jzw.media.library.camera.OnVideoRecordListener;
import com.jzw.media.library.camera.RecordManager;
import com.jzw.media.library.player.PlayerManager;


/**
 * @company 上海道枢信息科技-->
 * @anthor created by jingzhanwu
 * @date 2018/3/28 0028
 * @change
 * @describe 自定义录制 扩展  自定义UI
 **/
public class RecordActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "RecordActivity--";
    private ImageView ivReset;
    private ImageView ivCamera;
    private ImageView ivOk;
    private ImageView ivBack;
    private TextView tvTime;
    private SurfaceView surfaceView;

    private RecordManager mRecorder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_record);

        tvTime = findViewById(R.id.tv_timer);
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);

        ivCamera = findViewById(R.id.iv_camera);
        ivCamera.setOnClickListener(this);
        ivReset = findViewById(R.id.iv_reset);
        ivReset.setOnClickListener(this);
        ivOk = findViewById(R.id.iv_ok);
        ivOk.setOnClickListener(this);

        surfaceView = findViewById(R.id.surface_view);
        initCamera();
    }

    public void initCamera() {
        //在此之前记得要申请权限
        //初始化Camera，
        CameraManager cameraManager = new CameraManager(this, surfaceView, new CameraCallback() {

            @Override
            public void onPictureTaken(byte[] data) {
                Log.i(TAG, "Camera Taken Picture Callback");
            }

            @Override
            public void onFlashOn(int cameraFlag) {
                Log.i(TAG, "Camera Flash on");
            }

            @Override
            public void onFlagOff(int cameraFlag) {
                Log.i(TAG, "Camera Flash off");
            }

            @Override
            public void onCameraChange(int facing) {
                Log.i(TAG, "Camera Change");
            }
        });
        //开始预览
        cameraManager.start();
        mRecorder = new RecordManager(cameraManager);
        mRecorder.setOnVideoRecordListener(new OnVideoRecordListener() {
            @Override
            public void onStartRecord(String path) {
                ivCamera.setVisibility(View.VISIBLE);
                ivReset.setVisibility(View.GONE);
                ivOk.setVisibility(View.GONE);
            }

            @Override
            public void onRestartRecord(String path) {
                ivCamera.setVisibility(View.VISIBLE);
                ivReset.setVisibility(View.GONE);
                ivOk.setVisibility(View.GONE);
            }

            @Override
            public void onPauseRecord(String path) {
                ivCamera.setVisibility(View.VISIBLE);
                ivReset.setVisibility(View.VISIBLE);
                ivOk.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCompleteRecord(String path) {
                ivCamera.setVisibility(View.GONE);
                ivReset.setVisibility(View.VISIBLE);
                ivOk.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResetRecord(String path) {
                ivCamera.setVisibility(View.VISIBLE);
                ivReset.setVisibility(View.GONE);
                ivOk.setVisibility(View.GONE);
                tvTime.setText("");
            }

            @Override
            public void onRecordTimer(final String time, int secounds) {
                tvTime.setText(time);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_camera:  //录制
                mRecorder.startRecord();
                break;
            case R.id.iv_ok:  //
                stop();
                finish();
                break;
            case R.id.iv_reset:  // 重置
                mRecorder.resetRecord();
                break;
            case R.id.iv_back:
                stop();
                finish();
                break;
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stopRecord();
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    protected void onDestroy() {
        stop();
        super.onDestroy();
    }
}
