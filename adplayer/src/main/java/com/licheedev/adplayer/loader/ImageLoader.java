package com.licheedev.adplayer.loader;

import android.widget.ImageView;
import com.licheedev.adplayer.data.AdData;

/**
 * 图片加载器
 */
public interface ImageLoader {

    /**
     * 加载图片
     *
     * @param imageView
     * @param adData
     */
    void loadImage(ImageView imageView, AdData adData) throws Exception;
}


