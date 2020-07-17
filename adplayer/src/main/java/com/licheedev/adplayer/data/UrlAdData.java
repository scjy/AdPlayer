package com.licheedev.adplayer.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class UrlAdData implements AdData {

    private final URI mURI;
    private final int mType;

    public UrlAdData(String url) {

        mURI = url == null ? URI.create("http://null") : AdDataHelper.fromUrl(url);

        if (isImage(url)) {
            mType = TYPE_IMAGE;
        } else if (isVideo(url)) {
            mType = TYPE_VIDEO;
        } else if (isMusic(url)) {
            mType = TYPE_MUSIC;
        } else {
            mType = TYPE_UNKNOWN;
        }
    }

    @Override
    public URI getURI() {
        return mURI;
    }

    @Override
    public int getType() {
        return mType;
    }

    public static boolean isImage(String url) {
        return StringUtils.containsIgnoreCase(url, ".jpg") //
            || StringUtils.containsIgnoreCase(url, ".jpeg") //
            || StringUtils.containsIgnoreCase(url, ".png") //
            || StringUtils.containsIgnoreCase(url, ".bmp") //
            || StringUtils.containsIgnoreCase(url, ".gif");
    }

    public static boolean isVideo(String url) {
        return StringUtils.containsIgnoreCase(url, ".mp4") //
            || StringUtils.containsIgnoreCase(url, ".avi") //
            || StringUtils.containsIgnoreCase(url, ".wmv") //
            ;
    }

    public static boolean isMusic(String url) {
        return StringUtils.containsIgnoreCase(url, ".mp3") //
            || StringUtils.containsIgnoreCase(url, ".aac") //
            || StringUtils.containsIgnoreCase(url, ".wma") //
            || StringUtils.containsIgnoreCase(url, ".wav") //
            || StringUtils.containsIgnoreCase(url, ".flac") //
            || StringUtils.containsIgnoreCase(url, ".ogg") //
            ;
    }

    public static List<UrlAdData> convert(List<String> urls) {
        if (urls == null || urls.size() < 1) {
            return new ArrayList<>();
        }

        ArrayList<UrlAdData> data = new ArrayList<>();
        for (String url : urls) {
            data.add(new UrlAdData(url));
        }

        return data;
    }

    public static List<UrlAdData> convert(String[] urls) {
        if (urls == null || urls.length < 1) {
            return new ArrayList<>();
        }

        ArrayList<UrlAdData> data = new ArrayList<>();
        for (String url : urls) {
            data.add(new UrlAdData(url));
        }

        return data;
    }
}
