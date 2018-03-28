package com.jzw.media.library.camera;

/**
 * @company 上海道枢信息科技-->
 * @anthor created by jingzhanwu
 * @date 2018/3/2 0002
 * @change
 * @describe Camera相关操作回调接口
 **/
public interface CameraCallback {
    /**
     * 拍照
     *
     * @param data
     */
    void onPictureTaken(byte[] data);

    /**
     * 摄像头打开时回调
     *
     * @param cameraFlag 摄像头方向 前置或后置
     */
    void onFlashOn(int cameraFlag);


    /**
     * 闪关灯关闭时回调
     *
     * @param cameraFlag 摄像头方向 前置或后置
     */
    void onFlagOff(int cameraFlag);

    /**
     * 摄像头发生改变时回调
     *
     * @param facing 摄像头方向，前置或后置
     */
    void onCameraChange(int facing);
}
