package com.jit.himalaya;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jit.himalaya.adapters.IndicatorAdapter;
import com.jit.himalaya.adapters.MainCotentAdapter;
import com.jit.himalaya.interfaces.IPlayerCallback;
import com.jit.himalaya.presenters.PlayerPresenter;
import com.jit.himalaya.presenters.RecommendPresenter;
import com.jit.himalaya.utils.LogUtil;
import com.jit.himalaya.views.RoundRectImageView;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import java.util.List;

public class MainActivity extends FragmentActivity implements IPlayerCallback {

    private static final String TAG = "MainActivity";
    private MagicIndicator mMagicIndicator;
    private ViewPager mContentPager;
    private IndicatorAdapter mIndicatorAdapter;
    private RoundRectImageView mRoundRectImageView;
    private TextView mHeaderTitle;
    private TextView mSubTitle;
    private ImageView mPlayControl;
    private PlayerPresenter mPlayerPresenter;
    private View mPlayControlItem;
    private View mSearchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        initPresenter();
    }

    private void initPresenter() {
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallback(this);
    }

    private void initEvent() {
        mIndicatorAdapter.setOnIndicatorTapClickListener(new IndicatorAdapter.OnIndicatorTapClickListener() {
            @Override
            public void onTabClick(int index) {
                if (mContentPager != null) {
                    mContentPager.setCurrentItem(index);
                }
            }
        });

        mPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerPresenter != null) {
                    boolean hasPlayList = mPlayerPresenter.hasPlayList();
                    if (!hasPlayList) {
                        //没有设置播放列表，就播放第一个推荐专辑
                        playFirstRecommend();
                    }else{
                        if (mPlayerPresenter.isPlaying()) {
                            mPlayerPresenter.pause();
                        }else{
                            mPlayerPresenter.play();
                        }
                    }
                }
            }
        });

        mPlayControlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到播放器界面
                boolean hasPlayList = mPlayerPresenter.hasPlayList();
                if (!hasPlayList) {
                    playFirstRecommend();
                }
                startActivity(new Intent(MainActivity.this,PlayerActivity.class));
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                LogUtil.e(TAG,"mSearchBtn");
                startActivity(intent);
            }
        });
    }

    private void playFirstRecommend() {
        List<Album> currentRecommend = RecommendPresenter.getInstance().getCurrentRecommend();
        if (currentRecommend != null&&currentRecommend.size()>0) {
            Album album = currentRecommend.get(0);
            long albumId = album.getId();
            mPlayerPresenter.playByAlbumId(albumId);
        }
    }

    private void initView() {
        mMagicIndicator = this.findViewById(R.id.main_indicator);
        mMagicIndicator.setBackgroundColor(this.getResources().getColor(R.color.main_color));
        //创建indicator的适配器
        mIndicatorAdapter = new IndicatorAdapter(this);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdjustMode(true);
        commonNavigator.setAdapter(mIndicatorAdapter);


        //ViewPager
        mContentPager = this.findViewById(R.id.content_page);

        //创建内容适配器
        FragmentManager supportFragmentManger = getSupportFragmentManager();
        MainCotentAdapter mainCotentAdapter = new MainCotentAdapter(supportFragmentManger,1);
        mContentPager.setAdapter(mainCotentAdapter);

        //把ViewPager和indicator相联
        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator,mContentPager);

        //控制播放相关的
        mRoundRectImageView = this.findViewById(R.id.main_track_cover);
        mHeaderTitle = this.findViewById(R.id.main_head_title);
        mHeaderTitle.setSelected(true);
        mSubTitle = this.findViewById(R.id.main_sub_title);
        mPlayControl = this.findViewById(R.id.main_play_control);
        mPlayControlItem = this.findViewById(R.id.main_play_control_item);
        //搜索
        mSearchBtn = this.findViewById(R.id.search_btn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onPlayStart() {
        updatePlayControl(true);
    }

    private void updatePlayControl(boolean isPlaying){
        if (mPlayControl != null) {
            mPlayControl.setImageResource(isPlaying?R.drawable.selector_player_stop:R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayPause() {
        updatePlayControl(false);
    }

    @Override
    public void onPlayStop() {
        updatePlayControl(false);
    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void nextPlay(Track track) {

    }

    @Override
    public void onPrePlay(Track track) {

    }

    @Override
    public void onListLoaded(List<Track> list) {

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {

    }

    @Override
    public void onProgressChange(int currentProgress, int total) {

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track, int playIndex) {
        if (track != null) {
            String trackTitle = track.getTrackTitle();
            String nickname = track.getAnnouncer().getNickname();
            String coverUrlLarge = track.getCoverUrlLarge();
            if (mHeaderTitle != null) {
                mHeaderTitle.setText(trackTitle);
            }
            if (mSubTitle != null) {
                mSubTitle.setText(nickname);
            }
            Picasso.get().load(coverUrlLarge).into(mRoundRectImageView);
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }
}