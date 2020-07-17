package com.licheedev.adplayer.loader;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.licheedev.adplayer.data.AdData;
import com.licheedev.adplayer.data.AdDataHelper;
import java.net.URI;

/**
 * 默认的视频下载器
 */
public class DefaultGlideImageLoader implements ImageLoader {

    @Override
    public void loadImage(ImageView imageView, AdData adData) {

        URI uri = adData.getURI();
        RequestBuilder<Drawable> requestManager;
        @AdData.Scheme String scheme = uri.getScheme().toLowerCase();
        switch (scheme) {
            case AdData.SCHEME_HTTP:
            case AdData.SCHEME_HTTPS:
                requestManager = Glide.with(imageView).load(AdDataHelper.toUrl(uri));
                break;
            case AdData.SCHEME_FILE:
                requestManager = Glide.with(imageView).load(AdDataHelper.toFile(uri));
                break;
            case AdData.SCHEME_DRAWABLE:
                requestManager = Glide.with(imageView).load(AdDataHelper.toDrawableId(uri));
                break;
            case AdData.SCHEME_RAW: {
                requestManager = Glide.with(imageView)
                    .load(AdDataHelper.toRawForGlide(uri, imageView.getContext()));
                break;
            }
            case AdData.SCHEME_ASSET: {
                requestManager = Glide.with(imageView).load(AdDataHelper.toAssetForGlide(uri));
                break;
            }
            default:
                throw new IllegalArgumentException("Not support loading uri:{" + uri + "}");
        }

        requestManager.apply(
            new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate())
            .into(imageView);
    }
}
