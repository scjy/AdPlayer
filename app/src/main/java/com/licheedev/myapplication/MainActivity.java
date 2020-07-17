package com.licheedev.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.licheedev.adplayer.AdPlayer;
import com.licheedev.adplayer.AdView;
import com.licheedev.adplayer.data.AdData;
import com.licheedev.adplayer.data.UrlAdData;
import com.licheedev.myutils.LogPlus;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.adView)
    AdView mAdView;
    @BindView(R.id.btn_reset)
    Button mBtnReset;
    @BindView(R.id.btn_start)
    Button mBtnStart;
    @BindView(R.id.btn_resume)
    Button mBtnResume;
    @BindView(R.id.btn_pause)
    Button mBtnPause;
    @BindView(R.id.btn_stop)
    Button mBtnStop;
    @BindView(R.id.btn_next)
    Button mBtnNext;
    @BindView(R.id.tv_items)
    TextView mTvItems;
    @BindView(R.id.btn_jump_activity)
    Button mBtnJumpActivity;

    private String[] mUrls;

    private AdPlayer<AdData> mAdPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAdPlayer = new AdPlayer<>(mAdView);
        configAdPlayer();

        //mUrls = new String[] {
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190525/d9ec36d48962cef4f07363c5de838920.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190525/b615b496832755301fb82d0042cb6fff.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190328/ea7a173059bf15698d18ad2000ea646e.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190525/9b74e55ac9e2f7a18d77bb4b92f1ab4a.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/6a55dc85757ec2c0da23b98d006e0af2.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/48d703da5aef894206ba9790522c3453.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/c6a1488274266e7c74b444f158b99239.jpg"
        //};

        mUrls = new String[] {
            "http://hnshg.a.xiaozhuschool.com/statics/images/2019-06-17/15607536991960.mp4",
            "http://q22ma51h2.bkt.clouddn.com/01.%20%E5%B9%B3%E5%87%A1%E4%B9%8B%E8%B7%AF%20-%20%E6%9C%B4%E6%A0%91.mp3",
            "https://mov.bn.netease.com/open-movie/nos/mp4/2017/05/31/SCKR8V6E9_hd.mp4",
            //"http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190328/ea7a173059bf15698d18ad2000ea646e.jpg",
            "http://mengyi.sxitdlc.com/public/uploads/imgs/20190404/a1b3590fc03a5a745494df1958a65925.mp4",
            //"http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190525/9b74e55ac9e2f7a18d77bb4b92f1ab4a.jpg",
            //"http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/6a55dc85757ec2c0da23b98d006e0af2.jpg",
            "http://mengyi.sxitdlc.com/public/uploads/video/20190417/402dd1e51d384aed8bf647e0c5f8262e.mp4",
            "http://mengyi.sxitdlc.com/public/uploads/video/20190417/c473f9fcef16561bd1011fb1a2921210.mp4",
            //"http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/48d703da5aef894206ba9790522c3453.jpg",
            "http://mengyi.sxitdlc.com/public/uploads/video/20190429/47259e7c21e16a5ff3f48d9a22f48b77.mp4"
        };

        //mUrls = new String[] {
        //    "http://hnshg.a.xiaozhuschool.com/statics/images/2019-06-17/15607536991960.mp4",
        //    "https://mov.bn.netease.com/open-movie/nos/mp4/2017/05/31/SCKR8V6E9_hd.mp4",
        //    "http://mengyi.sxitdlc.com/public/uploads/imgs/20190404/a1b3590fc03a5a745494df1958a65925.mp4",
        //    "http://mengyi.sxitdlc.com/public/uploads/video/20190417/402dd1e51d384aed8bf647e0c5f8262e.mp4",
        //    "http://mengyi.sxitdlc.com/public/uploads/video/20190417/c473f9fcef16561bd1011fb1a2921210.mp4",
        //    "http://mengyi.sxitdlc.com/public/uploads/video/20190429/47259e7c21e16a5ff3f48d9a22f48b77.mp4",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190328/ea7a173059bf15698d18ad2000ea646e.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190525/9b74e55ac9e2f7a18d77bb4b92f1ab4a.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/6a55dc85757ec2c0da23b98d006e0af2.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/48d703da5aef894206ba9790522c3453.jpg"
        //};

        setPlayItemText(mUrls);

        // 设置播放数据源，并开始播放
        mAdPlayer.setNewData(UrlAdData.convert(mUrls));
    }

    private void configAdPlayer() {

        ///////////////////// 配置AdView

        // 设置过渡动画，可选
        mAdView.setAnimateParams(0.1f, 500L);
        // 设置动画插入器，可选
        mAdView.setInterpolator(new DecelerateInterpolator());
        //// 配置启用图片过渡动画，默认false
        mAdView.setEnableImageAnim(false);
        //// 配置启用视频过渡动画，默认false
        mAdView.setEnableVideoAnim(false);

        // 配置AdPlayer

        // 设置图片播放时长,默认5秒
        mAdPlayer.setImageDuration(20000L);
        // 配置视频播放动作[是否跟图片一样长(默认false)，是否要播完最后一次(上面一个参数为true才有效，默认true)]
        mAdPlayer.setVideoDuration(true, true);
        // 设置播放出错时，显示出错界面的时间，默认5秒
        mAdPlayer.setErrorDelay(2000L);
        // 设置视频静音，默认1，1(静音0,0)
        mAdPlayer.setVolume(1, 1);

        // 设置广告点击事件监听，可选
        mAdPlayer.setOnClickAdListener(new AdPlayer.OnClickAdListener<AdData>() {
            @Override
            public void onClickAd(AdData adData, int position) {
                Toast.makeText(MainActivity.this, adData.getURI().toString(), Toast.LENGTH_SHORT)
                    .show();
            }
        });
        // 设置广告播放事件回调，可选
        mAdPlayer.setOnOnPlayListener(new AdPlayer.OnPlayListener<AdData>() {

            @Override
            public void onSetNewData(@NonNull List<AdData> adDataList) {
                Toast.makeText(MainActivity.this, "新的广告数量=" + adDataList.size(), Toast.LENGTH_SHORT)
                    .show();
            }

            /**
             * 设置播放数据
             * @param adData
             * @param position
             */
            @Override
            public void onPreparingAdData(AdData adData, int position) {
                LogPlus.i("onPreparingAdData", adData.getURI().toString());
            }

            /**
             * 准备好，正式开始播放（重复播放也会进这里）
             * @param adData
             * @param position
             */
            @Override
            public void onStartPlay(AdData adData, int position) {
                LogPlus.i("onStartPlay", adData.getURI().toString());
            }

            /**
             * 一个广告播放完毕
             * @param adData
             * @param position
             */
            @Override
            public void onPlayCompleted(AdData adData, int position) {
                LogPlus.i("onPlayCompleted", adData.getURI().toString());
            }

            /**
             * 播放出错
             * @param adData
             * @param position
             */
            @Override
            public void onPlayFailed(AdData adData, int position) {
                LogPlus.i("onPlayFailed", adData.getURI().toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdPlayer.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        mAdPlayer.release();
        super.onDestroy();
    }

    @OnClick({
        R.id.btn_reset, R.id.btn_next, R.id.btn_start, R.id.btn_resume, R.id.btn_pause,
        R.id.btn_stop, R.id.btn_jump_activity
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reset:
                String[] strings =
                    ArrayUtils.subarray(mUrls, 0, (int) (Math.random() * (mUrls.length - 1)) + 1);
                setPlayItemText(strings);
                mAdPlayer.setNewData(UrlAdData.convert(strings));
                break;
            case R.id.btn_next:
                mAdPlayer.playNextManually();
                break;
            case R.id.btn_start:
                mAdPlayer.start();
                break;
            case R.id.btn_resume:
                mAdPlayer.resume();
                break;
            case R.id.btn_pause:
                mAdPlayer.pause();
                break;
            case R.id.btn_stop:
                mAdPlayer.stop();
                break;
            case R.id.btn_jump_activity:
                startActivity(new Intent(MainActivity.this, AnotherActivity.class));
                break;
        }
    }

    private void setPlayItemText(String[] strings) {
        String join = StringUtils.join(strings, "\n\n");
        mTvItems.setText(join);
    }
}
