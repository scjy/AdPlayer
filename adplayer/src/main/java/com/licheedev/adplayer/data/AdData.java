package com.licheedev.adplayer.data;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URI;

public interface AdData {

    /**
     * 未知类型
     */
    int TYPE_UNKNOWN = 0;

    /**
     * 图片
     */
    int TYPE_IMAGE = 1;

    /**
     * 视频
     */
    int TYPE_VIDEO = 2;
    /**
     * 音乐
     */
    int TYPE_MUSIC = 3;

    /**
     * 数据类型
     */
    @IntDef({ TYPE_UNKNOWN, TYPE_IMAGE, TYPE_VIDEO, TYPE_MUSIC })
    @Retention(RetentionPolicy.SOURCE)
    @interface DataType {
    }

    String SCHEME_HTTP = "http";
    String SCHEME_HTTPS = "https";
    String SCHEME_FILE = "file";
    String SCHEME_DRAWABLE = "drawable";
    String SCHEME_RAW = "raw";
    String SCHEME_ASSET = "asset";

    /**
     * 支持的协议
     */
    @StringDef(value = {
        SCHEME_HTTP, SCHEME_HTTPS, SCHEME_DRAWABLE, SCHEME_RAW, SCHEME_ASSET
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Scheme {
    }

    /**
     * 文件源，支持下列几种资源，可以使用{@link AdDataHelper} 中有各种fromXxx()进行转换
     * 1. http链接，URI.create("https://dss0.bdstatic.com/5aV1bjqh_Q23odCf/static/superman/img/logo_top_86d58ae1.png");
     * 2. 本地文件路径，URI.create("file:///storage/emulated/0/textjpg.jpg")，注意有3个“/”;
     * 3. res资源（R.drawable.xxx那种），只支持图片，URI.create("drawable://id的int值")，注意是int值，不是R.drawable.xxx这种ID字符串；
     * 4. raw资源（R.raw.xxx那种），URI.create("raw://raw_id的int值"),注意是int值，不是R.raw.xxx这种ID字符串；
     * 5. asset资源，URI.create("asset://aaa/bbb.img")
     *
     * @return
     */
    URI getURI();

    /**
     * 文件类型
     *
     * @return
     */
    @DataType
    int getType();
}
