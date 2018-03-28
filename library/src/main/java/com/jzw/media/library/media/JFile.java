package com.jzw.media.library.media;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * @company 上海道枢信息科技-->
 * @anthor created by jingzhanwu
 * @date 2018/3/21 0021
 * @change
 * @describe 文件实体  图片或者视频
 **/
public class JFile implements Parcelable {
    public static int PICTURE_TYPE = 1;
    public static int VIDEO_TYPE = 2;
    private int type;
    private String url;
    private String bitmapPath;
    private int width;
    private int height;

    public JFile() {
    }

    protected JFile(Parcel in) {
        type = in.readInt();
        url = in.readString();
        bitmapPath = in.readString();
        width = in.readInt();
        height = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(url);
        dest.writeString(bitmapPath);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<JFile> CREATOR = new Creator<JFile>() {
        @Override
        public JFile createFromParcel(Parcel in) {
            return new JFile(in);
        }

        @Override
        public JFile[] newArray(int size) {
            return new JFile[size];
        }
    };

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getBitmapPath() {
        return bitmapPath;
    }

    public void setBitmapPath(String bitmapPath) {
        this.bitmapPath = bitmapPath;
    }
}
