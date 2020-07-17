package com.licheedev.adplayer;

import com.danikula.videocache.HttpProxyCacheServer;
import java.io.File;

public interface CacheServerCreator {

    /**
     * 创建缓存代理服务器
     *
     * @param defaultCacheDir 默认的缓存文件夹，可以不用
     * @return
     */
    HttpProxyCacheServer getProxyCacheServer(File defaultCacheDir);
}
