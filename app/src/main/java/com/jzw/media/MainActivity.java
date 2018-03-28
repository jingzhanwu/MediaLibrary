package com.jzw.media;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.jzw.media.library.MediaConfig;
import com.jzw.media.library.media.JCameraView;
import com.jzw.media.library.media.JFile;
import com.jzw.media.library.ui.JMediaView;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        imageView = findViewById(R.id.iv_imageview);
        findViewById(R.id.btn_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JMediaView.with(MainActivity.this)
                        .setMode(JCameraView.BUTTON_STATE_ONLY_RECORDER)
                        .startCamera();
            }
        });

        findViewById(R.id.btn_takePicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JMediaView.with(MainActivity.this)
                        .setMode(JCameraView.BUTTON_STATE_ONLY_CAPTURE)
                        .startCamera();
            }
        });

        findViewById(R.id.btn_picture_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JMediaView.with(MainActivity.this)
                        .setMode(JCameraView.BUTTON_STATE_BOTH)
                        .enableMultiPicture(true)
                        .startCamera();
            }
        });

        findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });

        findViewById(R.id.btn_camera_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RecordActivity.class));
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            List<JFile> files = data.getParcelableArrayListExtra(MediaConfig.RESULT);

            switch (requestCode) {
                case MediaConfig.VIDEO_CODE:
                    String videoPath = files.get(0).getUrl();
                    Toast.makeText(this, "视频路径\n" + videoPath, Toast.LENGTH_LONG).show();
                    break;
                case MediaConfig.PICTURE_CODE:
                    String picPath = files.get(0).getUrl();
                    Bitmap bmp = BitmapFactory.decodeFile(picPath);
                    imageView.setImageBitmap(bmp);
                    break;
                case MediaConfig.PICTURE_VIDEO_CODE:
                    for (JFile file : files) {
                        if (file.getType() == JFile.PICTURE_TYPE) {
                            //图片

                        } else if (file.getType() == JFile.VIDEO_TYPE) {
                            //视频
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 获取权限
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                    .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager
                            .PERMISSION_GRANTED) {

            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA}, GET_PERMISSION_REQUEST);
            }
        } else {
        }
    }

    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            int size = 0;
            if (grantResults.length >= 1) {
                int writeResult = grantResults[0];
                //读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (!writeGranted) {
                    size++;
                }
                //录音权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                //相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }
                if (size == 0) {
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

}
