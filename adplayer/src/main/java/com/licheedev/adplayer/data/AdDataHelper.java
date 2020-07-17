package com.licheedev.adplayer.data;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;
import java.io.File;
import java.net.URI;

public class AdDataHelper {

    /**
     * http链接转URI
     *
     * @param url
     * @return
     */
    public static URI fromUrl(String url) {
        return URI.create(url);
    }

    /**
     * 文件（路径）转URI
     *
     * @param file
     * @return
     */
    public static URI formFile(File file) {
        return URI.create("file://" + file.getAbsolutePath());
    }

    /**
     * 文件（路径）转URI
     *
     * @param filePath
     * @return
     */
    public static URI formFile(String filePath) {
        return formFile(new File(filePath));
    }

    /**
     * 图片资源id转URI
     *
     * @param drawableId
     * @return
     */
    public static URI forDrawable(@DrawableRes int drawableId) {
        return URI.create("drawable://" + drawableId);
    }

    /**
     * raw资源id转URI
     *
     * @param rawId
     * @return
     */
    public static URI fromRaw(@RawRes int rawId) {
        return URI.create("raw://" + rawId);
    }

    /**
     * assets文件夹资源路径转URI，类似这种"aaa/bbb.img"
     *
     * @param assetPath
     * @return
     */
    public static URI fromAsset(String assetPath) {
        return URI.create("asset//" + assetPath);
    }

    /**
     * URI转http链接
     *
     * @param uri
     * @return
     */
    public static String toUrl(URI uri) {
        return uri.toString();
    }

    /**
     * URI转文件路径
     *
     * @param uri
     * @return
     */
    public static File toFile(URI uri) {
        return new File(uri.getPath());
    }

    /**
     * URI转文件路径
     * @param uri
     * @return
     */
    public static String toFilePath(URI uri) {
        return uri.getPath();
    }
    

    /**
     * URI转图片资源id
     *
     * @param uri
     * @return
     */
    public static int toDrawableId(URI uri) {
        return Integer.parseInt(uri.getPath());
    }

    /**
     * URI转raw资源id
     *
     * @param uri
     * @return
     */
    public static int toRawId(URI uri) {
        return Integer.parseInt(uri.getPath());
    }

    /**
     * URI转给Glide用的raw资源路径
     *
     * @param uri
     * @param context
     * @return
     */
    public static String toRawForGlide(URI uri, Context context) {
        return "android.resource://" + context.getPackageName() + "/raw/" + uri.getPath();
    }

    /**
     * URI转asset文件路径
     *
     * @param uri
     * @return
     */
    public static String toAsset(URI uri) {
        return uri.getPath();
    }

    /**
     * URI转给Glide用的asset文件路径
     *
     * @param uri
     * @return
     */
    public static String toAssetForGlide(URI uri) {
        return "file:///android_asset/" + uri.getPath();
    }
}
