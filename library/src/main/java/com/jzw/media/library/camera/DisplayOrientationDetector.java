/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jzw.media.library.camera;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;


/**
 * 监听手机的旋转，确定旋转角度
 */
public abstract class DisplayOrientationDetector {

    private final OrientationEventListener mOrientationEventListener;

    /**
     * Mapping from Surface.Rotation_n to degrees.
     */
    private static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();

    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270);
    }

    private Display mDisplay;
    private int mLastKnownDisplayOrientation = 0;
    private int mLastRotation = 0;

    protected DisplayOrientationDetector(Context context) {
        mOrientationEventListener = new OrientationEventListener(context) {
            /** This is either Surface.Rotation_0, _90, _180, _270, or -1 (invalid). */
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN ||
                        mDisplay == null) {
                    return;
                }
                int currentRotation = 0;
                //只检测是否有四个角度的改变
                if (orientation > 330 && orientation <= 30) { //0度 ,正向竖屏，摄像头朝上
                    currentRotation = 0;
                } else if (orientation > 70 && orientation < 110) { //90度，正向横屏，摄像头朝右
                    currentRotation = 270;
                } else if (orientation > 170 && orientation < 190) { //180度,反向竖屏，摄像头朝下
                    currentRotation = 180;
                } else if (orientation > 250 && orientation < 310) { //270度，反向横屏，摄像头朝左
                    currentRotation = 90;
                }
                if (mLastRotation != currentRotation) {
                    dispatchOnRotationChanged(currentRotation);
                }
                int rotation = mDisplay.getRotation();
                if (mLastKnownDisplayOrientation != rotation) {
                    dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(rotation));
                }
            }
        };
    }

    /**
     * 开启监听器
     *
     * @param display
     */
    protected void enable(Display display) {
        mDisplay = display;
        mOrientationEventListener.enable();
        dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(display.getRotation()));
        dispatchOnRotationChanged(mLastRotation);
    }

    /**
     * 关闭监听器监听
     */
    protected void disable() {
        mOrientationEventListener.disable();
        mDisplay = null;
    }

    protected int getLastKnownDisplayOrientation() {
        return mLastKnownDisplayOrientation;
    }

    /**
     * 分发监听到的角度变化值,分发到上层实现的子类中
     *
     * @param displayOrientation
     */
    protected void dispatchOnDisplayOrientationChanged(int displayOrientation) {
        mLastKnownDisplayOrientation = displayOrientation;
        onDisplayOrientationChanged(displayOrientation);
    }


    protected int getLastRatation() {
        return mLastRotation;
    }

    protected void dispatchOnRotationChanged(int rotation) {
        mLastRotation = rotation;
        onRotationChanged(rotation);
    }

    /**
     * Called when display orientation is changed.
     *
     * @param displayOrientation One of 0, 90, 180, and 270.
     */
    protected abstract void onDisplayOrientationChanged(int displayOrientation);

    protected abstract void onRotationChanged(int rotation);
}
