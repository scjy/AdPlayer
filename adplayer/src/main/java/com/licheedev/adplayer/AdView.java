package com.licheedev.adplayer;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.kk.taurus.playerbase.assist.InterEvent;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.event.OnErrorEventListener;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.render.AspectRatio;
import com.kk.taurus.playerbase.widget.BaseVideoView;
import com.licheedev.adplayer.data.AdData;
import com.licheedev.adplayer.data.AdDataHelper;
import com.licheedev.adplayer.loader.ImageLoader;
import com.licheedev.myutils.LogPlus;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URI;

public class AdView extends FrameLayout {

    public static final int SHOW_VIDEO = 10;
    public static final int SHOW_IMAGE = 11;
    public static final int SHOW_LOADING = 12;
    public static final int SHOW_ERROR = 13;
    public static final int SHOW_NONE = 14;

    private AdPlayer.DataSourceTask mDataSourceTask;
    private BaseVideoView mBottomVideo;
    private BaseVideoView mTopVideo;
    private View mOverView;
    private boolean mEnableImageAnim = false;
    private boolean mEnableVideoAnim = false;

    private Handler mHandler;
    private float mFromAlpha = 0.1f;
    private long mDuration = 500;
    private Interpolator mInterpolator = new DecelerateInterpolator();
    private int mToTranslationX;
    private BaseVideoView mPlayingVideoView;

    @IntDef(value = {
        SHOW_VIDEO, SHOW_IMAGE, SHOW_LOADING, SHOW_ERROR, SHOW_NONE
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ShowType {
    }

    private ImageView mImageView;
    private View mLoadingView;
    private View mErrorView;

    private ImageLoader mImageLoader;
    private AdPlayer mAdPlayer;

    public AdView(@NonNull Context context) {
        this(context, null);
    }

    public AdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHandler = new Handler();

        int loadingLayoutId = R.layout.layout_loading_cover;
        int errorLayoutId = R.layout.layout_error_cover;
        int itemBgColor = Color.BLACK;

        //<enum name="centerCrop" value="0" />
        //<enum name="fitCenter" value="1" />
        //<enum name="center" value="2" />
        //<enum name="fitXY" value="3" />
        //<enum name="centerInside" value="4" />
        int imageScaleType = 3;

        //<enum name="aspect_ratio_16_9" value="0" />
        //<enum name="aspect_ratio_4_3" value="1" />
        //<enum name="aspect_ratio_match_parent" value="2" />
        //<enum name="aspect_ratio_fill_parent" value="3" />
        //<enum name="aspect_ratio_fit_parent" value="4" />
        //<enum name="aspect_ratio_origin" value="5" />
        //<enum name="aspect_ratio_fill_width" value="6" />
        //<enum name="aspect_ratio_fill_height" value="7" />
        int videoAspectRatio = 2;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AdView);
            loadingLayoutId =
                ta.getResourceId(R.styleable.AdView_ad_override_loading_layout, loadingLayoutId);

            errorLayoutId =
                ta.getResourceId(R.styleable.AdView_ad_override_error_layout, errorLayoutId);

            itemBgColor = ta.getColor(R.styleable.AdView_ad_background_color, itemBgColor);

            mEnableImageAnim =
                ta.getBoolean(R.styleable.AdView_ad_enable_image_anim, mEnableImageAnim);

            mEnableVideoAnim =
                ta.getBoolean(R.styleable.AdView_ad_enable_video_anim, mEnableVideoAnim);

            imageScaleType = ta.getInt(R.styleable.AdView_ad_image_scale_type, imageScaleType);
            videoAspectRatio =
                ta.getInt(R.styleable.AdView_ad_video_aspect_ratio, videoAspectRatio);

            ta.recycle();
        }

        // 视频
        mBottomVideo = new BaseVideoView(context, attrs, defStyleAttr);
        mBottomVideo.setBackgroundColor(itemBgColor);
        setVideoAspectRatio(mBottomVideo, videoAspectRatio);
        addView(mBottomVideo, newLayoutParams());

        mTopVideo = new BaseVideoView(context, attrs, defStyleAttr);
        mTopVideo.setBackgroundColor(itemBgColor);
        setVideoAspectRatio(mTopVideo, videoAspectRatio);
        addView(mTopVideo, newLayoutParams());

        mPlayingVideoView = mBottomVideo;

        // 遮挡用的
        mOverView = new View(context, attrs, defStyleAttr);
        mOverView.setBackgroundColor(itemBgColor);
        addView(mOverView, newLayoutParams());

        // 图片
        mImageView = new ImageView(context, attrs, defStyleAttr);
        mImageView.setBackgroundColor(itemBgColor);

        //<enum name="centerCrop" value="0" />
        //<enum name="fitCenter" value="1" />
        //<enum name="center" value="2" />
        //<enum name="fitXY" value="3" />
        //<enum name="centerInside" value="4" />
        switch (imageScaleType) {
            case 0:
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case 1:
                mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                break;
            case 2:
                mImageView.setScaleType(ImageView.ScaleType.CENTER);
                break;
            case 3:
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                break;
            case 4:
                mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                break;
        }

        addView(mImageView, newLayoutParams());
        // 图片加载器
        mImageLoader = AdPlayerConfig.instance().getImageLoader();

        //// 加载布局
        mLoadingView = LayoutInflater.from(context).inflate(loadingLayoutId, this, false);
        addView(mLoadingView);
        //
        //// 错误布局
        mErrorView = LayoutInflater.from(context).inflate(errorLayoutId, this, false);
        addView(mErrorView);

        // 默认显示等待菊花
        switchView(SHOW_LOADING);
    }

    /**
     * 设置视频比例
     *
     * @param videoView
     * @param videoAspectRatio
     */
    private void setVideoAspectRatio(BaseVideoView videoView, int videoAspectRatio) {

        //<enum name="aspect_ratio_16_9" value="0" />
        //<enum name="aspect_ratio_4_3" value="1" />
        //<enum name="aspect_ratio_match_parent" value="2" />
        //<enum name="aspect_ratio_fill_parent" value="3" />
        //<enum name="aspect_ratio_fit_parent" value="4" />
        //<enum name="aspect_ratio_origin" value="5" />
        //<enum name="aspect_ratio_fill_width" value="6" />
        //<enum name="aspect_ratio_fill_height" value="7" />

        switch (videoAspectRatio) {
            case 0:
                videoView.setAspectRatio(AspectRatio.AspectRatio_16_9);
                break;
            case 1:
                videoView.setAspectRatio(AspectRatio.AspectRatio_4_3);
                break;
            case 2:
                videoView.setAspectRatio(AspectRatio.AspectRatio_MATCH_PARENT);
                break;
            case 3:
                videoView.setAspectRatio(AspectRatio.AspectRatio_FILL_PARENT);
                break;
            case 4:
                videoView.setAspectRatio(AspectRatio.AspectRatio_FIT_PARENT);
                break;
            case 5:
                videoView.setAspectRatio(AspectRatio.AspectRatio_ORIGIN);
                break;
            case 6:
                videoView.setAspectRatio(AspectRatio.AspectRatio_FILL_WIDTH);
                break;
            case 7:
                videoView.setAspectRatio(AspectRatio.AspectRatio_FILL_HEIGHT);
                break;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mToTranslationX = w + 1;

        if (mTopVideo != mPlayingVideoView) {
            makeViewOutside(mTopVideo);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private FrameLayout.LayoutParams newLayoutParams() {
        final FrameLayout.LayoutParams params = generateDefaultLayoutParams();
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        return params;
    }

    void pause() {
        cleanHandlerTasks();
        mBottomVideo.pause();
        mTopVideo.pause();
    }

    void resume() {
        cleanHandlerTasks();
        mPlayingVideoView.resume();
    }

    void stop() {
        cleanHandlerTasks();
        resetPlayerListener();
        mBottomVideo.stop();
        mTopVideo.stop();
    }

    private void resetPlayerListener() {
        mBottomVideo.setOnErrorEventListener(null);
        mTopVideo.setOnPlayerEventListener(null);
    }

    void setVolume(float left, float right) {
        mBottomVideo.setVolume(left, right);
        mTopVideo.setVolume(left, right);
    }

    void destroy() {
        cleanHandlerTasks();
        if (mDataSourceTask != null) {
            // 取消任务并清空回调
            mDataSourceTask.cancel();
        }

        mBottomVideo.stopPlayback();
        mTopVideo.stopPlayback();
    }

    public void switchView(@ShowType int showType) {
        switch (showType) {
            case SHOW_IMAGE:
                mImageView.setVisibility(VISIBLE);
                mOverView.setVisibility(VISIBLE);
                mLoadingView.setVisibility(GONE);
                mErrorView.setVisibility(GONE);
                break;
            case SHOW_LOADING:
                mImageView.setVisibility(GONE);
                mOverView.setVisibility(VISIBLE);
                mLoadingView.setVisibility(VISIBLE);
                mErrorView.setVisibility(GONE);
                break;
            case SHOW_ERROR:
                mImageView.setVisibility(GONE);
                mOverView.setVisibility(VISIBLE);
                mLoadingView.setVisibility(GONE);
                mErrorView.setVisibility(VISIBLE);
                break;
            case SHOW_VIDEO:
                mOverView.setVisibility(GONE);
                mImageView.setVisibility(GONE);
                mLoadingView.setVisibility(GONE);
                mErrorView.setVisibility(GONE);
                break;
            default:
                mOverView.setVisibility(VISIBLE);
                mImageView.setVisibility(GONE);
                mLoadingView.setVisibility(GONE);
                mErrorView.setVisibility(GONE);
                break;
        }
    }

    public void showNone() {
        switchView(SHOW_NONE);
        mBottomVideo.stop();
        mTopVideo.stop();
    }

    /**
     * 播放广告
     *
     * @param data
     * @param playingItemSign
     * @param startTime
     */
    void playItem(AdData data, String playingItemSign, long startTime) {

        cleanHandlerTasks();
        resetPlayerListener();

        mPlayingVideoView.stop();

        // 第一次加载，
        if (mAdPlayer.getAndChangeFirstLoad()) {
            switchView(SHOW_NONE);
        }

        updateState(PlayEvent.EVENT_SET_DATASOURCE, data, startTime);

        LogPlus.i("Play Item=" + data.getURI());

        if (data.getType() == AdData.TYPE_VIDEO || data.getType() == AdData.TYPE_MUSIC) {
            processVideo(data, playingItemSign, startTime);
        } else { // 其他类型的，都按图片处理
            processImage(data, playingItemSign, startTime);
        }
    }

    /**
     * 绑定AdPlayer
     *
     * @param adPlayer
     */
    void bindAdPlayer(AdPlayer adPlayer) {
        mAdPlayer = adPlayer;
    }

    /**
     * 更新状态
     */
    public void updateState(@PlayEvent.Event int state, AdData adData, long startTime) {
        mAdPlayer.updateState(state, adData, startTime);
    }

    /**
     * 处理加载图片
     *
     * @param data
     * @param playingItemSign
     * @param startTime
     */
    private void processImage(AdData data, String playingItemSign, long startTime) {

        // 显示图片
        switchView(SHOW_IMAGE);
        try {
            mImageLoader.loadImage(mImageView, data);
        } catch (Exception e) {
            switchView(SHOW_ERROR);
            updateState(PlayEvent.EVENT_PLAY_ERROR, data, startTime);
            return;
        }

        if (mEnableImageAnim) {
            clearAnimation(mImageView, true);
            playAnimation(mImageView);
        }

        // 更新状态
        updateState(PlayEvent.EVENT_IMAGE_PREPARED, data, startTime);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAdPlayer.isOutData(playingItemSign)) {
                    return;
                }
                // 发送播放完成
                updateState(PlayEvent.EVENT_IMAGE_PLAY_COMPLETE, data, startTime);
            }
        }, mAdPlayer.getImageDuration());
    }

    /**
     * 清除Handler未执行的任务
     */
    private void cleanHandlerTasks() {
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 根据事件编码获取事件名称定义
     *
     * @param eventCode
     * @return
     */
    String getEventName(int eventCode) {

        StringBuilder txt = new StringBuilder();

        String fieldName =
            Util.getFieldName(eventCode, OnPlayerEventListener.class, OnErrorEventListener.class,
                InterEvent.class);
        if (fieldName == null) {
            fieldName = "UNKNOWN_EVENT_CODE";
        }

        txt.append(fieldName).append("(").append(eventCode).append(")");
        return txt.toString();
    }

    /**
     * 处理加载视频
     *
     * @param data
     * @param playingItemSign
     * @param startTime
     */
    @SuppressLint("StaticFieldLeak")
    private void processVideo(AdData data, String playingItemSign, long startTime) {

        // 先尝试停掉前一个任务
        if (mDataSourceTask != null) {
            // 取消任务并清空回调
            mDataSourceTask.cancel();
        }

        if (mEnableVideoAnim) {
            clearAnimation(mTopVideo, false);
            clearAnimation(mBottomVideo, true);
        }

        mPlayingVideoView = mBottomVideo;

        // 为了能追踪playingItemSign，每次播放都重新设置监听器
        mPlayingVideoView.setOnPlayerEventListener(new OnPlayerEventListener() {
            @Override
            public void onPlayerEvent(int eventCode, Bundle bundle) {

                if (mAdPlayer.isOutData(playingItemSign)) {
                    return;
                }

                //if (bundle != null) {
                //    LogPlus.i("onPlayerEvent",
                //        "eventCode=" + getEventName(eventCode) + ",bundle=" + bundle);
                //} else {
                //    LogPlus.i("onPlayerEvent", "eventCode=" + getEventName(eventCode));
                //}

                switch (eventCode) {
                    case OnPlayerEventListener.PLAYER_EVENT_ON_AUDIO_DECODER_START:
                        if (data.getType() != AdData.TYPE_MUSIC) {
                            break;
                        }
                    case OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_RENDER_START:
                        changeVideoPlace(mPlayingVideoView);

                        switchView(SHOW_VIDEO);
                        updateState(PlayEvent.EVENT_VIDEO_PREPARED, data, startTime);
                        break;
                    case OnPlayerEventListener.PLAYER_EVENT_ON_PLAY_COMPLETE:
                        updateState(PlayEvent.EVENT_VIDEO_PLAY_COMPLETE, data, startTime);
                        break;
                }
            }
        });

        mPlayingVideoView.setOnErrorEventListener(new OnErrorEventListener() {
            @Override
            public void onErrorEvent(int eventCode, Bundle bundle) {

                LogPlus.w("onErrorEvent", "eventCode=" + getEventName(eventCode));

                if (mAdPlayer.isOutData(playingItemSign)) {
                    return;
                }

                // 显示错误
                switchView(SHOW_ERROR);
                // 更新状态
                updateState(PlayEvent.EVENT_PLAY_ERROR, data, startTime);
            }
        });

        URI uri = data.getURI();
        @AdData.Scheme String scheme = uri.getScheme().toLowerCase();

        switch (scheme) {
            case AdData.SCHEME_HTTP:
            case AdData.SCHEME_HTTPS: {
                String url = AdDataHelper.toUrl(uri);
                mDataSourceTask =
                    mAdPlayer.url2DataSourceWrapper(url, new AdPlayer.DataSourceCallback() {
                        @Override
                        public void beforeGetDataSource() {
                            if (mAdPlayer.isOutData(playingItemSign)) {
                                return;
                            }
                            // 显示加载
                            switchView(SHOW_LOADING);
                        }

                        @Override
                        public void getDataSourceSuccess(DataSource dataSource) {
                            if (mAdPlayer.isOutData(playingItemSign)) {
                                return;
                            }

                            LogPlus.i("文件路径=" + dataSource.getData());

                            startPlay(dataSource);
                        }

                        @Override
                        public void getDataSourceFailure() {
                            if (mAdPlayer.isOutData(playingItemSign)) {
                                return;
                            }
                            LogPlus.w("文件下载失败，url=" + url);
                            // 切换错误
                            switchView(SHOW_ERROR);
                            updateState(PlayEvent.EVENT_PLAY_ERROR, data, startTime);
                        }
                    });

                mDataSourceTask.start();
                break;
            }
            case AdData.SCHEME_FILE: {
                DataSource dataSource = new DataSource(AdDataHelper.toFilePath(uri));
                startPlay(dataSource);
                break;
            }
            case AdData.SCHEME_RAW: {
                DataSource dataSource = new DataSource();
                dataSource.setRawId(AdDataHelper.toRawId(uri));
                startPlay(dataSource);
                break;
            }
            case AdData.SCHEME_ASSET: {
                DataSource dataSource = new DataSource();
                dataSource.setAssetsPath(AdDataHelper.toAsset(uri));
                startPlay(dataSource);
                break;
            }
            default:
                startPlay(new DataSource(""));
                break;
        }
    }

    private void startPlay(DataSource dataSource) {
        mPlayingVideoView.setDataSource(dataSource);
        mPlayingVideoView.start(0);
    }

    void replay() {
        mPlayingVideoView.rePlay(0);
    }

    /**
     * 切换视频控件位置
     *
     * @param playingVideoView
     */
    private void changeVideoPlace(BaseVideoView playingVideoView) {

        if (playingVideoView != mTopVideo) {
            BaseVideoView top = mTopVideo;
            mTopVideo = playingVideoView;
            mBottomVideo = top;
        }

        resetViewTranslation(playingVideoView);
        makeViewOutside(mBottomVideo);

        if (mEnableVideoAnim) {
            playAnimation(playingVideoView);
        }
    }

    private void makeViewOutside(View view) {
        view.setTranslationX(mToTranslationX);
    }

    private void resetViewTranslation(View view) {
        view.setTranslationX(0);
    }

    /**
     * 播放动画
     *
     * @param view
     */
    private void playAnimation(View view) {

        ViewPropertyAnimator animate = view.animate();
        animate.cancel();
        view.setAlpha(mFromAlpha);

        animate.alphaBy(1).setDuration(mDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setAlpha(1);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animate.setInterpolator(mInterpolator);
        animate.start();
    }

    private void clearAnimation(View view, boolean resetAlpha) {
        ViewPropertyAnimator animate = view.animate();
        animate.cancel();
        if (resetAlpha) {
            view.setAlpha(1);
        }
    }

    /**
     * 设置动画参数
     *
     * @param fromAlpha 初始透明度
     * @param duration 动画时间
     */
    public void setAnimateParams(float fromAlpha, long duration) {

        if (fromAlpha > 1) {
            throw new IllegalArgumentException(
                "fromAlpha=" + fromAlpha + ",fromAlpha should less than 1");
        }

        mFromAlpha = fromAlpha;

        if (duration <= 0) {
            throw new IllegalArgumentException("illegal duration");
        }

        mDuration = duration;
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public boolean isEnableImageAnim() {
        return mEnableImageAnim;
    }

    public void setEnableImageAnim(boolean enableImageAnim) {
        mEnableImageAnim = enableImageAnim;
    }

    public boolean isEnableVideoAnim() {
        return mEnableVideoAnim;
    }

    public void setEnableVideoAnim(boolean enableVideoAnim) {
        mEnableVideoAnim = enableVideoAnim;
    }
}
