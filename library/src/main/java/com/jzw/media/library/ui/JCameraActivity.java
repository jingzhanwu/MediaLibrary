package com.jzw.media.library.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import com.dev.jzw.helper.picture.PictureView;
import com.jzw.media.library.MediaConfig;
import com.jzw.media.library.R;
import com.jzw.media.library.media.JCameraView;
import com.jzw.media.library.media.JFile;
import com.jzw.media.library.media.listener.ClickListener;
import com.jzw.media.library.media.listener.ErrorListener;
import com.jzw.media.library.media.listener.JCameraListener;
import com.jzw.media.library.media.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JCameraActivity extends AppCompatActivity {
    private JCameraView jCameraView;

    private List<JFile> mFiles;

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_jcamera);
        jCameraView = findViewById(R.id.jcameraview);
        mFiles = new ArrayList<>();
        initCamera();
    }

    private void initCamera() {
        Intent intent = getIntent();
        String videoPath = intent.getStringExtra("videoPath");
        String tip = intent.getStringExtra("tip");
        mode = intent.getIntExtra("mode", JCameraView.BUTTON_STATE_BOTH);
        int quality = intent.getIntExtra("quality", JCameraView.MEDIA_QUALITY_MIDDLE);
        int maxDuration = intent.getIntExtra("duration", 1000 * 60);
        boolean multiPic = intent.getBooleanExtra("multiPic", false);


        //设置视频保存路径
        jCameraView.setMaxDuration(maxDuration);
        jCameraView.setSaveVideoPath(videoPath);
        jCameraView.setFeatures(mode);
        jCameraView.setEnableMultiPicture(multiPic);
        jCameraView.setTip(tip);
        jCameraView.setMediaQuality(quality);
        //多拍时设置
        jCameraView.setFinishShow(multiPic);
        if (multiPic) {
            jCameraView.setLeftImage(getResources().getDrawable(R.drawable.ic_finish));
        }
        jCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
                Log.i("JZW", "camera error");
                Intent intent = new Intent();
                setResult(MediaConfig.ERROR, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(JCameraActivity.this, "给点录音权限可以?", Toast.LENGTH_SHORT).show();
            }
        });
        //JCameraView监听
        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                if (bitmap != null) {
                    String path = FileUtil.saveBitmap(bitmap);
                    JFile file = new JFile();
                    file.setType(JFile.PICTURE_TYPE);
                    file.setWidth(bitmap.getWidth());
                    file.setHeight(bitmap.getHeight());
                    file.setUrl(path);
                    file.setBitmapPath(path);

                    mFiles.add(file);

                    jCameraView.setRightImage(bitmap);

                    if (!jCameraView.isEnableMultiPicture()) {
                        Intent intent = new Intent();
                        intent.putParcelableArrayListExtra(MediaConfig.RESULT, (ArrayList<JFile>) mFiles);
                        setResult(RESULT_OK, intent);
                        JCameraActivity.this.finish();
                    } else {
                        jCameraView.cameraHasOpened();
                    }
                }
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                if (!TextUtils.isEmpty(url)) {
                    JFile file = new JFile();
                    file.setType(JFile.VIDEO_TYPE);
                    file.setUrl(url);
                    if (firstFrame != null) {
                        String path = FileUtil.saveBitmap(firstFrame);
                        file.setBitmapPath(path);
                        file.setWidth(firstFrame.getWidth());
                        file.setHeight(firstFrame.getHeight());
                    }

                    mFiles.add(file);
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra(MediaConfig.RESULT, (ArrayList<JFile>) mFiles);
                    setResult(RESULT_OK, intent);
                    JCameraActivity.this.finish();
                }
            }
        });

        jCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                if (mode != JCameraView.BUTTON_STATE_ONLY_RECORDER
                        && jCameraView.isEnableMultiPicture()
                        && mFiles.size() > 0) {
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra(MediaConfig.RESULT, (ArrayList<JFile>) mFiles);
                    setResult(RESULT_OK, intent);
                    JCameraActivity.this.finish();
                } else {
                    deleteFiles();
                    JCameraActivity.this.finish();
                }
            }
        });
        jCameraView.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {
                //浏览图片
                browserPicture();
            }
        });

        //当开启多拍 是左上角的关闭按钮，
        jCameraView.setFinishClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFiles();
                JCameraActivity.this.finish();
            }
        });
    }

    private void deleteFiles() {
        if (mFiles.size() > 0) {
            for (JFile file : mFiles) {
                FileUtil.deleteFile(file.getUrl());
            }
            mFiles.clear();
        }
    }

    private void browserPicture() {
        if (mFiles.size() > 0) {
            final List<File> files = new ArrayList<>();
            for (JFile file : mFiles) {
                if (file.getType() == JFile.PICTURE_TYPE) {
                    files.add(new File(file.getUrl()));
                }
            }
            if (files.size() > 0) {
                PictureView.with(JCameraActivity.this)
                        .setFiles(files, 0)
                        .setOnDeleteItemListener(new PictureView.OnDeleteItemListener() {
                            @Override
                            public void onDelete(int position) {
                                String path = files.get(position).getPath();
                                files.remove(position);
                                //查找需要删除的图片索引
                                List<Integer> pos = new ArrayList<>();
                                for (int i = 0; i < mFiles.size(); i++) {
                                    if (mFiles.get(i).getUrl().equals(path)) {
                                        pos.add(i);
                                    }
                                }
                                //删除对应的图片
                                if (pos.size() > 0) {
                                    for (Integer i : pos) {
                                        FileUtil.deleteFile(mFiles.get(i.intValue()).getUrl());
                                        mFiles.remove(i.intValue());
                                    }
                                }

                            }
                        })
                        .create();
            } else {
                Toast.makeText(JCameraActivity.this, "暂无图片", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(JCameraActivity.this, "暂无图片", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (jCameraView != null) {
            jCameraView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (jCameraView != null) {
            jCameraView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (jCameraView != null) {
            jCameraView = null;
        }
    }
}
