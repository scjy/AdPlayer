package com.licheedev.myapplication;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import com.danikula.videocache.HttpProxyCacheServer;
import com.licheedev.adplayer.AdPlayerConfig;
import com.licheedev.adplayer.CacheServerCreator;
import com.licheedev.adplayer.loader.DefaultGlideImageLoader;
import com.licheedev.adplayer.loader.DefaultVideoDownLoader;
import java.io.File;

public class App extends Application {

    static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        AdPlayerConfig.instance().init(this)
            // 设置图片加载器（可选），默认使用Glide加载图片
            .setImageLoader(new DefaultGlideImageLoader())
            // 设置视频缓存文件夹路径
            .setCacheDir(getAdCacheDir(this))
            // 设置视频文件下载器，仅使用setUseFileLruCache()有效
            .setVideoDownLoader(new DefaultVideoDownLoader())
            // 下面两种缓存策略互斥，只有最后设置的才生效
            // 缓存策略1，先下载文件到Lru文件缓存里面，再播放
            .setUseFileLruCache(1024 * 1024 * 1024L)
            // 缓存策略2，使用AndroidVideoCache边下边播
            .setUseVideoCache(new CacheServerCreator() {
                @Override
                public HttpProxyCacheServer getProxyCacheServer(File defaultCacheDir) {
                    HttpProxyCacheServer.Builder builder =
                        new HttpProxyCacheServer.Builder(App.this)
                            // 这里也可以自定义缓存路径
                            .cacheDirectory(defaultCacheDir)
                            // 缓存策略
                            .maxCacheSize(1024 * 1024 * 1024L); // 最大缓存文件总大小
                    // .maxCacheFilesCount(10); // 或者用这个，最大缓存文件个数
                    return builder.build();
                }
            });
    }

    private  File getAdCacheDir(Context context) {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // 已挂载
            File pic =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            File dir = new File(pic, "ad_cache");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        } else {
            File cacheDir = context.getCacheDir();
            File tmpPic = new File(cacheDir, "ad_cache");
            return tmpPic;
        }
    }

}
