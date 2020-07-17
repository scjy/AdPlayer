package com.licheedev.adplayer;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.View;
import com.kk.taurus.playerbase.entity.DataSource;
import com.licheedev.adplayer.cache.FileLruCache;
import com.licheedev.adplayer.data.AdData;
import com.licheedev.adplayer.loader.VideoDownLoader;
import com.licheedev.myutils.LogPlus;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdPlayer<T extends AdData> {

    private final Handler mHandler;
    private final AdView mAdView;

    private List<T> mData = new ArrayList<>();
    private int mCurrent = -1;
    private int mSize = 0;

    /**
     * （设置列表后）第一次加载
     */
    private AtomicBoolean mFirstLoad = new AtomicBoolean(true);

    /**
     * 已经开始播放任务
     */
    private boolean mStarted;
    /**
     * 已经暂停任务
     */
    private boolean mPaused;

    /**
     * 播放列表的ID，用来区分新旧播放列表
     */
    private int mPlayListId = 0;
    private int mSignFactor = 0;

    private String mPlayingItemSign;
    private OnClickAdListener<T> mOnClickAdListener;
    private T mCurrentData;
    private OnPlayListener<T> mOnPlayListener;

    /**
     * (图片)播放时间
     */
    private long mImageDuration = 5000L;
    /**
     * 强制图片/视频/音频播放相同的时间
     */
    private boolean mSameDurationWithImage = false;
    /**
     * 当强制图片/视频/音频播放相同的时间时，是否允许最后一次播放完整
     */
    private boolean mAllowFinalComplete = true;
    /**
     * 播放错误后，播放下一首的延时
     */
    private long mErrorDelay = mImageDuration;

    public AdPlayer(AdView adView) {
        mHandler = new Handler();
        mAdView = adView;
        mAdView.bindAdPlayer(this);
    }

    /**
     * 设置音量
     *
     * @param left
     * @param right
     */
    public void setVolume(float left, float right) {
        mAdView.setVolume(left, right);
    }

    /**
     * 播放时间
     *
     * @return
     */
    public long getImageDuration() {
        return mImageDuration;
    }

    /**
     * 设置（图片播放时间）播放时间
     *
     * @param duration (图片)播放时间
     */
    public void setImageDuration(long duration) {
        mImageDuration = duration;
    }

    /**
     * 设置视频播放规则
     *
     * @param sameDurationWithImage 强制图片/视频/音频播放相同的时间。
     * 对于音视频，如果文件时长&lt;duration，则重播，如果文件时长 &gt;=duration，则播放duration后直接播放下一首
     * @param allowFinalComplete 只有sameDurationWithImage为true时才有用。表示是否允许最后一次播放完整
     */
    public void setVideoDuration(boolean sameDurationWithImage, boolean allowFinalComplete) {
        mSameDurationWithImage = sameDurationWithImage;
        mAllowFinalComplete = allowFinalComplete;
    }

    /**
     * 播放错误后，播放下一首的延时
     *
     * @return
     */
    public long getErrorDelay() {
        return mErrorDelay;
    }

    /**
     * 设置播放错误后，播放下一首的延时
     *
     * @param errorDelay
     */
    public void setErrorDelay(long errorDelay) {
        mErrorDelay = errorDelay;
    }

    /**
     * 设置播放数据
     *
     * @param data
     */
    public void setNewData(List<? extends T> data) {
        // 停止播放
        removeCallbacks();
        mStarted = false;
        mPlayListId++;
        clearPlayingItemSign();
        mAdView.stop();
        mFirstLoad.set(true);

        if (data == null || data.size() < 1) {
            mData.clear();
            mSize = 0;
            mCurrent = -1;
            mCurrentData = null;
            mAdView.showNone();
        } else {
            mData.clear();
            mData.addAll(data);
            mSize = mData.size();
            mCurrent = 0;
            start();
        }

        if (mOnPlayListener != null) {
            mOnPlayListener.onSetNewData(mData);
        }
    }

    public List<T> getData() {
        return mData;
    }

    /**
     * 生成当前条目签名播放条目签名,用于异步回调中来区分是否还在当前条目
     *
     * @return
     */
    private String generatePlayingItemSign(AdData adData) {
        return mPlayListId
            + "_"
            + SystemClock.uptimeMillis()
            + "_"
            + (++mSignFactor)
            + "_"
            + adData;
    }

    /**
     * 清空播放签名
     */
    private void clearPlayingItemSign() {
        mPlayingItemSign = generatePlayingItemSign(null);
    }

    /**
     * 判断某广告是否已经过时
     *
     * @param playingItemSign 要判断的广告的播放签名
     * @return true，已过时（已经开始播放其他项目了）；false，没过时（正在播放传入签名的项目）
     */
    boolean isOutData(String playingItemSign) {
        return !playingItemSign.equals(mPlayingItemSign);
    }

    boolean getAndChangeFirstLoad() {
        return mFirstLoad.getAndSet(false);
    }

    boolean isFirstLoad() {
        return mFirstLoad.get();
    }

    /**
     * 更新播放状态
     *
     * @param state
     * @param adData
     * @param startTime
     */
    void updateState(@PlayEvent.Event int state, AdData adData, long startTime) {
        T t = (T) adData;
        switch (state) {
            case PlayEvent.EVENT_SET_DATASOURCE:
                if (mOnPlayListener != null) {
                    mOnPlayListener.onPreparingAdData(t, mCurrent);
                }
                break;
            case PlayEvent.EVENT_IMAGE_PREPARED:
                if (mOnPlayListener != null) {
                    mOnPlayListener.onStartPlay(t, mCurrent);
                }
                break;
            case PlayEvent.EVENT_IMAGE_PLAY_COMPLETE:
                playNextItem(0);
                break;
            case PlayEvent.EVENT_VIDEO_PREPARED:
                if (mOnPlayListener != null) {
                    mOnPlayListener.onStartPlay(t, mCurrent);
                }
                // 如果指定了视频时间，并且不允许最后一次播放完毕
                if (mSameDurationWithImage && !mAllowFinalComplete) {
                    // 每次播放（重播）都会进来一次，所以可能会叠多个延时任务，
                    // 不过因为第一个任务正式运行时（播放下一首），会清掉所有任务，所以不用管这个
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 直接播放下一首
                            playNextItem(0);
                        }
                    }, mImageDuration);
                }
                break;
            case PlayEvent.EVENT_VIDEO_PLAY_COMPLETE:
                if (mOnPlayListener != null) {
                    mOnPlayListener.onPlayCompleted(t, mCurrent);
                }
                if (mSameDurationWithImage) {
                    long remainTime = mImageDuration - (SystemClock.uptimeMillis() - startTime);
                    // 如果剩余时间够长，才重播，否则播下一首
                    if (remainTime >= 100) {
                        LogPlus.e("重播=" + adData.getURI());
                        mAdView.replay();
                    } else {
                        playNextItem(0);
                    }
                } else {
                    playNextItem(0);
                }
                break;
            case PlayEvent.EVENT_PLAY_ERROR:
                if (mOnPlayListener != null) {
                    mOnPlayListener.onPlayFailed(t, mCurrent);
                }
                playNextItem(mErrorDelay);
                break;
        }
    }

    private void playCurrentItem(boolean force) {
        // 已经开启且没有暂停，才播放
        // 生成播放签名
        mPlayingItemSign = generatePlayingItemSign(mCurrentData);
        if (force || (mStarted && !mPaused)) {
            mCurrentData = mData.get(mCurrent);
            mAdView.playItem(mCurrentData, mPlayingItemSign, SystemClock.uptimeMillis());
        }
    }

    private void playNextItem(long delay) {
        removeCallbacks();
        if (delay > 0) {
            mHandler.postDelayed(mPlayNextTask, delay);
        } else {
            mPlayNextTask.run();
        }
    }

    /**
     * 强制手动播放下一曲
     */
    public void playNextManually() {
        removeCallbacks();
        int next = mCurrent + 1;
        if (next >= mSize) {
            next = 0;
        }
        mCurrent = next;
        mStarted = true;
        mPaused = false;
        playCurrentItem(true);
    }

    private Runnable mPlayNextTask = new Runnable() {
        @Override
        public void run() {
            int next = mCurrent + 1;
            if (next >= mSize) {
                next = 0;
            }
            mCurrent = next;
            playCurrentItem(false);
        }
    };

    /**
     * 恢复播放
     */
    public void resume() {
        removeCallbacks();
        if (mStarted && mPaused) {
            mPaused = false;
            playCurrentItem(false);
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        removeCallbacks();
        // 清空播放签名
        clearPlayingItemSign();
        if (mStarted) {
            mPaused = true;
            mAdView.pause();
        }
    }

    public void start() {
        if (mData == null || mData.size() < 1 || mCurrent < 0) {
            return;
        }
        mStarted = true;
        playCurrentItem(false);
    }

    public void stop() {
        removeCallbacks();
        mAdView.stop();
        mStarted = false;
        mPaused = false;
    }

    public void release() {
        removeCallbacks();
        mAdView.destroy();
    }

    /**
     * 移除所有Handler任务
     */
    private void removeCallbacks() {
        mHandler.removeCallbacksAndMessages(null);
    }

    //
    //

    public interface OnClickAdListener<T extends AdData> {

        /**
         * 点击广告
         *
         * @param adData
         * @param position
         */
        void onClickAd(T adData, int position);
    }

    /**
     * 设置点击广告监听
     *
     * @param listener
     */
    public void setOnClickAdListener(OnClickAdListener<T> listener) {
        mOnClickAdListener = listener;
        mAdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickAdListener != null
                    && mData != null
                    && mCurrent > -1
                    && mCurrentData != null) {
                    mOnClickAdListener.onClickAd(mCurrentData, mCurrent);
                }
            }
        });
    }

    public interface OnPlayListener<T extends AdData> {

        /**
         * 当设置新的广告数据
         *
         * @param adDataList
         */
        void onSetNewData(@NonNull List<T> adDataList);

        /**
         * 当开始准备（设置）播放数据
         *
         * @param adData
         * @param position
         */
        void onPreparingAdData(T adData, int position);

        /**
         * 当开始播放
         *
         * @param adData
         * @param position
         */
        void onStartPlay(T adData, int position);

        /**
         * 当播放完毕
         *
         * @param adData
         * @param position
         */
        void onPlayCompleted(T adData, int position);

        /**
         * 当播放失败
         *
         * @param adData
         * @param position
         */
        void onPlayFailed(T adData, int position);
    }

    public void setOnOnPlayListener(OnPlayListener<T> listener) {
        mOnPlayListener = listener;
    }

    //
    //
    //
    // http url的任务
    DataSourceTask url2DataSourceWrapper(String url, DataSourceCallback callback) {

        if (AdPlayerConfig.instance().getCacheStrategy()
            == AdPlayerConfig.STRATEGY_FILE_LRU_CACHE) {
            return new LruDownTask(callback, url);
        } else {
            return new VideoCacheTask(callback, url);
        }
    }

    interface DataSourceTask {

        void start();

        void cancel();
    }

    interface DataSourceCallback {

        void beforeGetDataSource();

        void getDataSourceSuccess(DataSource dataSource);

        void getDataSourceFailure();
    }

    // 边下边播缓存
    private static class VideoCacheTask implements DataSourceTask {

        private DataSourceCallback mCallback;
        private final String mUrl;

        public VideoCacheTask(DataSourceCallback callback, String url) {
            mCallback = callback;
            mUrl = url;
        }

        @Override
        public void start() {

            String proxyUrl = AdPlayerConfig.instance().getCacheProxyServer().getProxyUrl(mUrl);
            DataSource dataSource = new DataSource(proxyUrl);
            if (mCallback != null) {
                mCallback.getDataSourceSuccess(dataSource);
            }
        }

        @Override
        public void cancel() {
            mCallback = null;
        }
    }

    // 下载文件任务
    private static class LruDownTask extends AsyncTask<String, Void, File>
        implements DataSourceTask {

        private DataSourceCallback mCallback;
        private final String mUrl;

        public LruDownTask(DataSourceCallback callback, String url) {
            mCallback = callback;
            mUrl = url;
        }

        private FileLruCache getCache() {
            return AdPlayerConfig.instance().getLruCache();
        }

        @Override
        protected File doInBackground(String... strings) {

            FileLruCache cache = getCache();

            // 如果还是没有缓存，就直接返回空
            if (cache == null) {
                return null;
            }

            String url = strings[0];
            String mds = Util.md5(url);
            FileLruCache.Editor edit = null;

            try {
                edit = cache.edit(mds);
                VideoDownLoader videoDownLoader = AdPlayerConfig.instance().getVideoDownLoader();
                videoDownLoader.download(url, edit.newOutputFile());
                edit.commit();
            } catch (Exception e) {
                e.printStackTrace();
                if (edit != null) {
                    try {
                        edit.abort();
                    } catch (IOException ex) {
                        //ex.printStackTrace();
                    }
                }
            }

            try {
                cache.flush();
                // 立即尝试获取
                return cache.getFile(mds);
            } catch (IOException e) {
                //e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(File file) {
            if (mCallback != null) {
                if (file == null) {
                    mCallback.getDataSourceFailure();
                } else {
                    mCallback.getDataSourceSuccess(new DataSource(file.getAbsolutePath()));
                }
            }
        }

        @Override
        public void start() {

            String md5 = Util.md5(mUrl);
            FileLruCache cache = getCache();
            if (cache != null) {
                File file = cache.getFile(md5);
                if (file != null && mCallback != null) {
                    mCallback.getDataSourceSuccess(new DataSource(file.getAbsolutePath()));
                    cancel();
                    return;
                }
            }

            if (mCallback != null) {
                mCallback.beforeGetDataSource();
            }

            execute(mUrl);
        }

        @Override
        public void cancel() {
            mCallback = null;
            cancel(false);
        }
    }
}
