package com.licheedev.adplayer;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface PlayEvent {

    /**
     * 设置数据源
     */
    int EVENT_SET_DATASOURCE = 9527;
    /**
     * 图片准备好
     */
    int EVENT_IMAGE_PREPARED = 9528;

    /**
     * 视频准备开始播放
     */
    int EVENT_VIDEO_PREPARED = 9529;

    /**
     * 播放完毕
     */
    int EVENT_IMAGE_PLAY_COMPLETE = 9530;

    /**
     * 播放完毕
     */
    int EVENT_VIDEO_PLAY_COMPLETE = 9531;
    /**
     * 播放错误
     */
    int EVENT_PLAY_ERROR = 9532;

    @IntDef(value = {
        EVENT_SET_DATASOURCE, EVENT_VIDEO_PREPARED, EVENT_IMAGE_PLAY_COMPLETE,
        EVENT_VIDEO_PLAY_COMPLETE, EVENT_PLAY_ERROR, EVENT_IMAGE_PREPARED
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Event {
    }
}
