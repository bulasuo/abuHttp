package com.abu.healthandroidpad.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import com.abu.healthandroidpad.util.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by abu on 2016/5/16 20:36.
 * 运存保存资源的key就是 url_str;硬盘则是url_str的MD5值
 * CacheManager.getDiskLruCache(activity.getApplicationContext(), "bitmap").flush();//不应该常用,应该在activity.onPause()里用
 */
public class CacheManager {

    private static DiskLruCache mDiskLruCache;
    private static LruCache<String, Bitmap> mLruCache;





    public static LruCache<String, Bitmap> getLruCache(){
        if(mLruCache == null){
            // 设置图片缓存大小为程序最大可用内存的1/8
            int cacheSize = (int) Runtime.getRuntime().maxMemory() / 8;
            mLruCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // 重写此方法来衡量每张图片的大小，默认返回图片数量。
                    return bitmap.getRowBytes();
                }

                @Override
                protected void entryRemoved(boolean evicted, String key,
                                            Bitmap oldValue, Bitmap newValue) {
                    Log.v("tag", "hard cache is full , push to soft cache");

                }
            };
        }
        return mLruCache;
    }

    /**
     *@author   abu   2016/5/16   21:44
     * 图片保存到LruCache
     */
    public static void addBitmapToLruCache(String key, Bitmap bitmap) {
        if (getBitmapFromLruCache(key) == null) {
            getLruCache().put(key, bitmap);
        }
    }

    /**
     *@author   abu   2016/5/16   21:45
     * 从LruCache里取图片
     */
    public static Bitmap getBitmapFromLruCache(String key) {
        return getLruCache().get(key);
    }

    /**
     *@author   abu   2016/5/16   21:49
     * 从LruCache里移除图片
     */
    public static void removeBitmapFromLruCache(String key) {
        if (key != null) {
            if (getLruCache() != null) {
                Bitmap bm = getLruCache().remove(key);
                if (bm != null)
                    bm.recycle();
            }
        }
    }

    /**
     *@author   abu   2016/5/16   20:48
     * params: 上下文, 缓存子路径
     * 获取DiskLruCache
     */
    public static DiskLruCache getDiskLruCache(Context context, String uniqueName) {
        if(mDiskLruCache == null){
            try {
                File cacheDir = getDiskCacheDir(context, uniqueName);
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
                mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 600 * 1024 * 1024);//600M缓存
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return mDiskLruCache;

    }

    /**
     *@author   abu   2016/5/16   20:37
     * 获取缓存路径
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     *@author   abu   2016/5/16   20:39
     * 获取app版本号 int
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     *@author   abu   2016/5/16   20:53
     * 计算string的MD5值
     */
    public static String str2md5(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }


}
