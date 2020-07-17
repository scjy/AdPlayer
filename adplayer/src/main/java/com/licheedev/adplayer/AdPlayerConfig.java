package com.licheedev.adplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.danikula.videocache.HttpProxyCacheServer;
import com.kk.taurus.ijkplayer.IjkPlayer;
import com.kk.taurus.playerbase.config.PlayerConfig;
import com.kk.taurus.playerbase.config.PlayerLibrary;
import com.kk.taurus.playerbase.entity.DecoderPlan;
import com.licheedev.adplayer.cache.FileLruCache;
import com.licheedev.adplayer.loader.DefaultGlideImageLoader;
import com.licheedev.adplayer.loader.DefaultVideoDownLoader;
import com.licheedev.adplayer.loader.ImageLoader;
import com.licheedev.adplayer.loader.VideoDownLoader;
import java.io.File;
import java.io.IOException;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class AdPlayerConfig {

    private static final int APP_VERSION = 1;
    private static final String CACHE_DIR = "ad_video_cache";

    public static final int PLAN_ID_IJK = 1;

    private Context mContext;
    private FileLruCache mLruCache;

    public static final int STRATEGY_FILE_LRU_CACHE = 0;
    public static final int STRATEGY_VIDEO_CACHE = 1;

    private int mCacheStrategy = STRATEGY_FILE_LRU_CACHE;
    private CacheServerCreator mCacheServerCreator;
    private HttpProxyCacheServer mCacheServer;

    private static class InstanceHolder {
        private static final AdPlayerConfig instance = new AdPlayerConfig();
    }

    public static AdPlayerConfig instance() {
        return InstanceHolder.instance;
    }

    private ImageLoader mImageLoader = new DefaultGlideImageLoader();

    private VideoDownLoader mVideoDownLoader = new DefaultVideoDownLoader();

    private File mCacheDir;

    private long mMaxCacheSize = 1024 * 1024 * 1024; // 默认1G

    /**
     * 初始化，必须调用
     *
     * @param context
     */
    public AdPlayerConfig init(Context context) {
        mContext = context.getApplicationContext();

        PlayerConfig.addDecoderPlan(
            new DecoderPlan(PLAN_ID_IJK, IjkPlayer.class.getName(), "IjkPlayer"));
        PlayerConfig.setDefaultPlanId(PLAN_ID_IJK);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IjkMediaPlayer.loadLibrariesOnce(null);
                    IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //初始化库
        PlayerConfig.setUseDefaultNetworkEventProducer(true);
        PlayerLibrary.init(mContext);

        setCacheDir(new File(context.getFilesDir(), CACHE_DIR));
        return this;
    }

    /**
     * 获取缓存文件夹最大容量
     *
     * @return
     */
    long getMaxCacheSize() {
        return mMaxCacheSize;
    }

    /**
     * 设置缓存文件夹最大容量
     *
     * @param maxCacheSize
     */
    private void setMaxCacheSize(long maxCacheSize) {
        mMaxCacheSize = maxCacheSize;
        closeLruCache();
    }

    /**
     * 获取图片加载器
     *
     * @return
     */
    ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * 设置图片加载器，可选，默认使用Glide加载图片（需要手动添加Glide依赖）
     *
     * @param imageLoader
     * @return
     */
    public AdPlayerConfig setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
        return this;
    }

    /**
     * 获取视频下载器
     *
     * @return
     */
    VideoDownLoader getVideoDownLoader() {
        return mVideoDownLoader;
    }

    /**
     * 设置视频文件下载器，可选
     *
     * @param videoDownLoader
     * @return
     */
    public AdPlayerConfig setVideoDownLoader(VideoDownLoader videoDownLoader) {
        mVideoDownLoader = videoDownLoader;
        return this;
    }

    /**
     * 获取缓存的文件夹
     *
     * @return
     */
    public File getCacheDir() {
        return mCacheDir;
    }

    /**
     * 设置缓存文件夹，可选，默认为<app>/data/ad_video_cache/
     *
     * @param cacheDir
     * @return
     */
    public AdPlayerConfig setCacheDir(File cacheDir) {
        mCacheDir = cacheDir;
        closeLruCache();
        return this;
    }

    public AdPlayerConfig setUseFileLruCache(long maxCacheSize) {
        mCacheStrategy = STRATEGY_FILE_LRU_CACHE;
        setMaxCacheSize(maxCacheSize);
        return this;
    }

    public AdPlayerConfig setUseVideoCache(@NonNull CacheServerCreator creator) {
        mCacheStrategy = STRATEGY_VIDEO_CACHE;
        mCacheServerCreator = creator;
        return this;
    }

    int getCacheStrategy() {
        return mCacheStrategy;
    }

    /**
     * 获取LRU缓存
     *
     * @return
     */
    synchronized @Nullable
    FileLruCache getLruCache() {
        try {
            if (mLruCache == null) {
                mLruCache = FileLruCache.open(mCacheDir, APP_VERSION, mMaxCacheSize);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mLruCache;
    }

    /**
     * 关闭LRU缓存
     */
    public synchronized void closeLruCache() {
        try {
            if (mLruCache != null) {
                mLruCache.close();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            mLruCache = null;
        }
    }

    /**
     * 获取视频缓存服务器
     *
     * @return
     */
    synchronized HttpProxyCacheServer getCacheProxyServer() {
        HttpProxyCacheServer proxyCacheServer = mCacheServer;
        if (proxyCacheServer == null) {
            proxyCacheServer = mCacheServerCreator.getProxyCacheServer(mCacheDir);
            mCacheServer = proxyCacheServer;
        }
        return proxyCacheServer;
    }

    /**
     * 关闭视频缓存服务器
     */
    public synchronized void shutdownCacheSercer() {
        try {
            if (mCacheServer != null) {
                mCacheServer.shutdown();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            mCacheServer = null;
        }
    }
}
