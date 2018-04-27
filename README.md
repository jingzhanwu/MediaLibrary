# MediaLibrary

功能：

    1、拍照
    2、录制
    3、拍照+录制
    4、支持UI自定义
    5、音频播放控制


引入项目：
    compile 'com.jzw:medialibrary:1.1.1'
    
使用：

  一、JMediaView完成拍照录制
  
    1、仅录制：
        JMediaView.with(MainActivity.this)
                   .setMode(JCameraView.BUTTON_STATE_ONLY_RECORDER)
                   .startCamera();
    2、仅拍照：
         JMediaView.with(MainActivity.this)
                   .setMode(JCameraView.BUTTON_STATE_ONLY_CAPTURE)
                   .startCamera();
    3、拍照加录制：
        JMediaView.with(MainActivity.this)
                  .setMode(JCameraView.BUTTON_STATE_BOTH)
                  .enableMultiPicture(true)
                  .startCamera();

    4、开启多拍功能：
        JMediaView.enableMultiPicture(true)
        
        
  二、CameraManager 和RecordManager完成拍照和录制
  
    1、拍照：
       第一步：获取SrufaceView
       SurfaceView surfaceView = findViewById(R.id.surface_view);
       
       第二步：创建CameraManager对象
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
       第三步：开始拍照
        mCameraManager.takePicture();
        
       第四步：回调中处理数据
                public void onPictureTaken(byte[] data) {
                       Log.i(TAG, "Camera Taken Picture Callback");
                       if (data != null) {
                           Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                      
                           ivPicture.setImageBitmap(bmp);
                       }
                   }
       第五步：停止拍照：
         mCameraManager.stop();
         
       第六步：UI处理和闪关灯，摄像头旋转控制
           
           1、闪光灯：
             mCameraManager.clickFlash();
             对应回调：
             @Override
             public void onFlashOn(int cameraFlag) {
                    Log.i(TAG, "Camera Flash on");
              }
                    
             @Override
             public void onFlagOff(int cameraFlag) {
                    Log.i(TAG, "Camera Flash off");
             }
             
           2、摄像头旋转：
              mCameraManager.switchCamera();
              对应回调：
              @Override
              public void onCameraChange(int facing) {
                    Log.i(TAG, "Camera Change");
              } 
    2、录制：
        第一步：获取SurfaceView
        surfaceView = findViewById(R.id.surface_view);
        
        第二步：创建CameraManager对象
        CameraManager cameraManager = new CameraManager(this, surfaceView, new CameraCallback() {}
          ...
          ...
          具体代码同上
        
        第三步：创建RecordManager对象
        mRecorder = new RecordManager(cameraManager);
        
        第四步：添加录制监听
        mRecorder.setOnVideoRecordListener(new OnVideoRecordListener() {
                    @Override
                    public void onStartRecord(String path) {
                        //开始录制时回调 
                    }
        
                    @Override
                    public void onRestartRecord(String path) {
                        //从暂停状态重新开始录制时回调
                    }
        
                    @Override
                    public void onPauseRecord(String path) {
                        //录制暂停时回调
                    }
        
                    @Override
                    public void onCompleteRecord(String path) {
                        //录制完成时回调
                    }
        
                    @Override
                    public void onResetRecord(String path) {
                     //重置录制器后回调
                    }
        
                    @Override
                    public void onRecordTimer(final String time, int secounds) {
                        //处理录制时长
                    }
                });
                
        第五步：录制
         mRecorder.startRecord();
         
        第六步：停止录制、释放资源
          mRecorder.stopRecord();
          mRecorder.release();
          
        第七步：其他操作
            暂停，停止，录制完成等调用同开始一样，各个状态下的UI都在对应的回调
            方法中处理，闪光灯和摄像头的监听处理同拍照一样
            
        注：进入录制和拍照界面前一定要先申请录制和Camera、读写等权限，否则可能出现
            初始化失败。
   
  三、音频播放控制器
        
        1、播放
         PlayerManager.with().playAudio("音频地址", new PlayerManager.OnPlayerCallback() {
                     @Override
                     public void onPrepare(MediaPlayer mp) {
                         //播放准备时回调
                     }
         
                     @Override
                     public void onStart(MediaPlayer mp, String url) {
                            //开始播放时回调
                     }
         
                     @Override
                     public void onPlaying(MediaPlayer mp) {
                            //播放中回调
                     }
         
                     @Override
                     public void onStop() {
                            //停止播放时回调
                     }
         
                     @Override
                     public void onFaild(MediaPlayer mp) {
                            //播放失败 出错时回调
                     }
                 });
         2、停止：
              PlayerManager.with().releasPlayer();
              
  想体验MVP开发的点这里
  https://github.com/jingzhanwu/MvpBase
  
  Retrofit+Rxjava的一个请求库
  https://github.com/jingzhanwu/RetrofitRxjavaClient
  
  Android 开发工具库
  https://github.com/jingzhanwu/DevUtils