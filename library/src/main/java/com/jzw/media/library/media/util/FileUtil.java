package com.jzw.media.library.media.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * =====================================
 * 作    者: jingzhanwu
 * 版    本：1.1.4
 * 创建日期：2017/4/25
 * 描    述：
 * =====================================
 */
public class FileUtil {

    public static String saveBitmap(Bitmap b) {
        long dataTake = System.currentTimeMillis();
        String jpegName = getPicDir() + File.separator + "picture_" + dataTake + ".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return jpegName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取外部存储卡目录，并且创建一个名字为 PROJECT_DIR 的文件夹
     * /storage/emulated/0
     *
     * @return
     */
    public static String getExternalDir() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException("没有存储卡");
        }
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File file = new File(path + "MediaLibrary");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }


    /**
     * 获取外部存储卡/storage/emulated/0/ PROJECT_DIR
     * 下的一个目录，如果没有就创建一个
     *
     * @param dirName
     * @return
     */
    public static String createDir(String dirName) {
        String dir = getExternalDir();
        String subDir = dir + File.separator + dirName;
        File file = new File(subDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 获取外部存储卡上本项目下图片的存放目录
     * /storage/emulated/0/ PROJECT_DIR /picture
     *
     * @return
     */
    public static String getPicDir() {
        return createDir("picture");
    }

    /**
     * 获取外部存储卡上本项目下视频的存放目录
     * /storage/emulated/0/ PROJECT_DIR /video
     *
     * @return
     */
    public static String getVideoDir() {
        return createDir("video");
    }

    public static String getAudioDir() {
        return createDir("audio");
    }


    /**
     * 获得外部存储卡 /Android/data/包名/cache 目录
     * 这个目录下的文件 在应用卸载时一并会删除
     *
     * @param context
     * @return
     */
    public static String getProjectCacheDir(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException("没有存储卡");
        }
        return context.getExternalCacheDir().getAbsolutePath();
    }

    /**
     * 从/Android/data/包名/cache 目录 获取指定文件夹名称 的目录地址
     *
     * @param context
     * @param dirName 目录名
     * @return
     */
    public static String createDir(Context context, String dirName) {
        String cacheDir = getProjectCacheDir(context);
        String dir = cacheDir + File.separator + dirName;
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }


    /**
     * 获取音频存储路径，/Android/data/包名/cache/audio
     *
     * @param context
     * @return
     */
    public static String getAudioDir(Context context) {
        return createDir(context, "audio");
    }

    /**
     * 获取视频存储路径，/Android/data/包名/cache/video
     *
     * @param context
     * @return
     */
    public static String getVideoDir(Context context) {
        return createDir(context, "video");
    }

    /**
     * 获取图片存储路径，/Android/data/包名/cache/pic
     *
     * @param context
     * @return
     */
    public static String getPicDir(Context context) {
        return createDir(context, "picture");
    }


    /**
     * 获取缓存目录，/Android/data/包名/cache/cache
     *
     * @param context
     * @return
     */
    public static String getCacheDir(Context context) {
        return createDir(context, "cache");
    }


    public static boolean deleteFileFromDir(String dirName) {
        return deleteFileFromDir(new File(dirName), true);
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param dir 要删除的根目录
     */
    public static boolean deleteFileFromDir(File dir) {
        return deleteFileFromDir(dir, true);
    }

    /**
     * 递归删除文件，需要运行时 文件读写权限，否则文件删除失败
     *
     * @param dir
     * @param deleteSelf 是否删除根文件夹本身
     * @return
     */
    public static boolean deleteFileFromDir(File dir, boolean deleteSelf) {
        if (null == dir || !dir.exists()) {
            return true;
        }
        if (dir.isFile()) {
            return dir.delete();
        }
        if (dir.isDirectory()) {
            File[] childFile = dir.listFiles();
            if (childFile == null || childFile.length == 0) {
                return dir.delete();
            }
            for (File f : childFile) {
                deleteFileFromDir(f);
            }
            if (deleteSelf) {
                dir.delete();
            }
        }
        return true;
    }

    /**
     * 删除单个文件
     *
     * @param fileName
     * @return
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        return deleteFile(file);
    }

    /**
     * 删除一个文件
     *
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        if (null == file || !file.exists()) {
            return true;
        }
        return file.delete();
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
