package com.licheedev.adplayer.loader;

import java.io.File;

/**
 * 视频文件下载
 */
public interface VideoDownLoader {

    /**
     * 下载视频文件
     *
     * @param url 视频的url
     * @param destFile 下载到的目标文件
     * @throws Exception
     */
    void download(String url, File destFile) throws Exception;
}
