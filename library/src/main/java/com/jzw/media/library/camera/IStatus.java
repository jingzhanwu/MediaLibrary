package com.jzw.media.library.camera;

/**
 * 定义录制过程 状态
 * Created by 景占午 on 2017/9/20 0020.
 */

public interface IStatus {
    int PREPARE = 1;
    int RECORDDING = 2;
    int PAUSE = 3;
    int STOP = 4;
    int FINISH = 5;
}
