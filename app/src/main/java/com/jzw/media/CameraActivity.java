package com.jzw.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.dev.jzw.helper.util.ToastUtil;
import com.jzw.media.library.camera.CameraCallback;
import com.jzw.media.library.camera.CameraManager;


/**
 * @company 上海道枢信息科技-->
 * @anthor created by jingzhanwu
 * @date 2018/3/28 0028
 * @change
 * @describe 自定义Camera  扩展  自定义UI
 **/
public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "CameraActivity--";
    private ImageView ivCancel;
    private ImageView ivCamera;
    private ImageView ivOk;
    private ImageView ivPicture;
    private SurfaceView surfaceView;

    private CameraManager mCameraManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_camera);

        ivPicture = findViewById(R.id.iv_image);
        ivPicture.setOnClickListener(this);

        ivCamera = findViewById(R.id.iv_camera);
        ivCamera.setOnClickListener(this);
        ivCancel = findViewById(R.id.iv_back);
        ivCancel.setOnClickListener(this);
        ivOk = findViewById(R.id.iv_ok);
        ivOk.setOnClickListener(this);

        surfaceView = findViewById(R.id.surface_view);
        initCamera();
    }

    public void initCamera() {
        //在此之前记得要申请权限
        //初始化Camera，
        mCameraManager = new CameraManager(this, surfaceView, new CameraCallback() {
            @Override
            public void onPictureTaken(byte[] data) {
                Log.i(TAG, "Camera Taken Picture Callback");
                if (data != null) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    ivPicture.setVisibility(View.VISIBLE);
                    ivPicture.setImageBitmap(bmp);
                }
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
        mCameraManager.start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_camera:  //拍照
                mCameraManager.takePicture();
                break;
            case R.id.iv_ok:  //
                mCameraManager.stop();
                mCameraManager = null;
                finish();
                break;
            case R.id.iv_back:
                mCameraManager.stop();
                mCameraManager = null;
                finish();
                break;

            case R.id.iv_image:  //预览图片

                ToastUtil.showToast(CameraActivity.this, "预览图片");
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraManager != null) {
            mCameraManager.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraManager != null) {
            mCameraManager.stop();
        }
    }
}
