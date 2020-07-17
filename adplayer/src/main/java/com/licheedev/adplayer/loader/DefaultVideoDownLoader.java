package com.licheedev.adplayer.loader;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 默认的视频下载器
 */
public class DefaultVideoDownLoader implements VideoDownLoader {
    
    @Override
    public void download(String url, File destFile) throws Exception {
        UrlConnectionUtil.download(url, new FileOutputStream(destFile));
    }
}
