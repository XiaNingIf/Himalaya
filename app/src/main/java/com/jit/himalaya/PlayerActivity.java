package com.jit.himalaya;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.jit.himalaya.adapters.PlayerTrackPagerAdapter;
import com.jit.himalaya.base.BaseActivity;
import com.jit.himalaya.interfaces.IPlayerCallback;
import com.jit.himalaya.presenters.PlayerPresenter;
import com.jit.himalaya.utils.LogUtil;
import com.jit.himalaya.views.SobPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerActivity extends BaseActivity implements IPlayerCallback, ViewPager.OnPageChangeListener {

    private static final String TAG = "PlayerActivity";
    private ImageView mControlBtn;
    private PlayerPresenter mPlayerPresenter;
    private SimpleDateFormat mMinFormat = new SimpleDateFormat("mm:ss");
    private SimpleDateFormat mHourFormat = new SimpleDateFormat("hh:mm:ss");
    private TextView mTotalDuration;
    private TextView mCurrentPosition;
    private SeekBar mDurationBar;
    private int mCurrentProgress = 0;
    private boolean mIsUserTouchProgressBar = false;
    private ImageView mPlayNextBtn;
    private ImageView mPlayPreBtn;
    private TextView mTrackTitleTv;
    private String mTrackTitleText;
    private ViewPager mTrackPageView;
    private PlayerTrackPagerAdapter mPlayerPagerAdapter;
    private boolean mIsUserSlidePager = false;
    private ImageView mPlayModeSwitchBtn;
    private XmPlayListControl.PlayMode mCurrentMode = PLAY_MODEL_LIST;
    private static Map<XmPlayListControl.PlayMode, XmPlayListControl.PlayMode> sPlayModeRule = new HashMap<>();

    //处理播放模式
    //1.默认的是：PLAY_MODEL_LIST
    //2.列表循环：PLAY_MODEL_LIST_LOOP
    //3.随机播放：PLAY_MODEL_RANDOM
    //4.单曲循环：PLAY_MODEL_SINGLE_LOOP
    static {
        sPlayModeRule.put(PLAY_MODEL_LIST,PLAY_MODEL_LIST_LOOP);
        sPlayModeRule.put(PLAY_MODEL_LIST_LOOP,PLAY_MODEL_RANDOM);
        sPlayModeRule.put(PLAY_MODEL_RANDOM,PLAY_MODEL_SINGLE_LOOP);
        sPlayModeRule.put(PLAY_MODEL_SINGLE_LOOP,PLAY_MODEL_LIST);
    }

    private ImageView mPlayListBtn;
    private SobPopWindow mSobPopWindow;
    private ValueAnimator mEnterBgAnimation;
    private ValueAnimator mOutBgAnimator;
    private final int BG_ANIMATION_DURATION = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.e(TAG,"PlayerActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();
        //测试一下播放
        LogUtil.e(TAG,"onCreate--->create--->mPlayerPresenter+1");
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        LogUtil.e(TAG,"onCreate--->create--->mPlayerPresenter+1");
        mPlayerPresenter.registerViewCallback(this);
        //在界面初始化以后才去获取数据
        mPlayerPresenter.getPlayList();
        initEvent();
        initBgAnimation();
    }

    private void initBgAnimation() {
        mEnterBgAnimation = ValueAnimator.ofFloat(1.0f,0.7f);
        mEnterBgAnimation.setDuration(BG_ANIMATION_DURATION);
        mEnterBgAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                //处理一下背景，有点透明度
                updateBgAlpha(value);
            }
        });

        //退出的
        mOutBgAnimator = ValueAnimator.ofFloat(0.7f,1.0f);
        mOutBgAnimator.setDuration(BG_ANIMATION_DURATION);
        mOutBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                //处理一下背景，有点透明度
                updateBgAlpha(value);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallback(this);
            mPlayerPresenter=null;
        }
    }

    /**
     * 给控件设置相关的事件
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        mControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果现在的状态是正在播放的，那么就暂停
                if (mPlayerPresenter.isPlaying()) {
                    mPlayerPresenter.pause();
                }else{
                    //如果现在的状态是非播放的，那么我们就让播放器播放节目
                    mPlayerPresenter.play();
                }
            }
        });

        mDurationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                if (isFromUser) {
                    mCurrentProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                    mIsUserTouchProgressBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIsUserTouchProgressBar = false;
                //手离开拖动进度条的时候更新进度条
                mPlayerPresenter.seekTo(mCurrentProgress);

            }
        });

        mPlayPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playPre();
                }
            }
        });

        mPlayNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playNext();
                }
            }
        });

        mTrackPageView.addOnPageChangeListener(this);

        mTrackPageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    mIsUserSlidePager = true;
                }
                return false;
            }
        });

        mPlayModeSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //处理播放模式
                //1.默认的是：PLAY_MODEL_LIST
                //2.列表循环：PLAY_MODEL_LIST_LOOP
                //3.随机播放：PLAY_MODEL_RANDOM
                //4.单曲循环：PLAY_MODEL_SINGLE_LOOP
                switchPlayMode();
            }
        });

        mPlayListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSobPopWindow.showAtLocation(v, Gravity.BOTTOM,0,0);
                //修改背景的透明有一个渐变的过程
                mEnterBgAnimation.start();
            }
        });
        mSobPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //pop窗体消失以后，恢复透明度
                mOutBgAnimator.start();
            }
        });

        mSobPopWindow.setPlayListItemClickListener(new SobPopWindow.PlayListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //播放列表里的item被点击了
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playByIndex(position);
                }
            }
        });

        mSobPopWindow.setPlayListActionClickListener(new SobPopWindow.PlayListActionListener() {
            @Override
            public void onPlayModeClick() {
                switchPlayMode();
            }

            @Override
            public void onOrderClick() {
                //点击切换顺序和逆序
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.reversePlayList();
                }
                mSobPopWindow.updateOrderIcon(!textOrder);
                textOrder = !textOrder;
            }
        });
    }

    private boolean textOrder = false;

    private void switchPlayMode() {
        //根据当前的mode获取mode
        XmPlayListControl.PlayMode playMode = sPlayModeRule.get(mCurrentMode);
        //修改播放模式
        if (mPlayerPresenter != null) {
            mPlayerPresenter.switchPlayMode(playMode);
            mCurrentMode = playMode;
        }
    }

    public void updateBgAlpha(float alpha){
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        attributes.alpha = alpha;
        window.setAttributes(attributes);
    }

    /**
     * 根据当前的状态，更新播放模式图标
     */
    private void updatePlayModeBtnImg() {
        int resId = R.drawable.selector_play_mode_list_order;
        switch (mCurrentMode){
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_play_mode_list_order;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_play_mode_list_order_looper;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_play_mode_single_loop;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_play_mode_random;
                break;
        }
        mPlayModeSwitchBtn.setImageResource(resId);
    }

    /**
     * 找到各个控件
     */
    private void initView() {
        mControlBtn = this.findViewById(R.id.play_or_pause_btn);
        mTotalDuration = this.findViewById(R.id.track_duration);
        mCurrentPosition = this.findViewById(R.id.current_position);
        mDurationBar = this.findViewById(R.id.track_seek_bar);
        mPlayNextBtn = this.findViewById(R.id.play_next);
        mPlayPreBtn = this.findViewById(R.id.play_pre);
        mTrackTitleTv = this.findViewById(R.id.track_title_tv);
        if (!TextUtils.isEmpty(mTrackTitleText)) {
            mTrackTitleTv.setText(mTrackTitleText);
        }
        mTrackPageView = this.findViewById(R.id.track_pager_view);
        mPlayerPagerAdapter = new PlayerTrackPagerAdapter();
        mTrackPageView.setAdapter(mPlayerPagerAdapter);
        //切换播放模式的按钮
        mPlayModeSwitchBtn = this.findViewById(R.id.player_mode_switch_btn);
        //播放列表
        mPlayListBtn = this.findViewById(R.id.player_list);
        mSobPopWindow = new SobPopWindow();
    }


    @Override
    public void onPlayStart() {
        //开始播放，修改UI暂停的按钮
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_stop);
        }
    }

    @Override
    public void onPlayPause() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayStop() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }
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
        //把数据设置到适配器里
        if (mPlayerPagerAdapter != null) {
            mPlayerPagerAdapter.setData(list);
        }
        //数据回来以后，也要给节目列表一份
        if (mSobPopWindow != null) {
            mSobPopWindow.setListData(list);
        }

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {
        //更新播放模式，并且更新UI
        mCurrentMode=playMode;
        mSobPopWindow.updatePlayMode(mCurrentMode);
        updatePlayModeBtnImg();
    }

    @Override
    public void onProgressChange(int currentProgress, int total) {
        mDurationBar.setMax(total);
        //更新进度条
        String totalDuration;
        String currentDuration;
        if (total>1000*60*60) {
            totalDuration = mHourFormat.format(total);
            currentDuration = mHourFormat.format(currentProgress);
        }else{
            totalDuration = mMinFormat.format(total);
            currentDuration = mMinFormat.format(currentProgress);
        }
        if (mTotalDuration != null) {
            mTotalDuration.setText(totalDuration);
        }
        //更新当前时间
        if (mCurrentPosition != null) {
            mCurrentPosition.setText(currentDuration);
        }
        //更新进度
        //更新当前进度
        if (!mIsUserTouchProgressBar){
            mDurationBar.setProgress(currentProgress);
        }

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track,int position) {
        this.mTrackTitleText = track.getTrackTitle();
        if (mTrackTitleTv != null) {
            mTrackTitleTv.setText(mTrackTitleText);
        }
        //当节目改变的时候我们就获取到播放器中的位置
        //当节目改变以后，要修改页面的图片
        if (mTrackPageView != null) {
            mTrackPageView.setCurrentItem(position,true);
        }
        if (mSobPopWindow != null) {
            mSobPopWindow.setCurrentPlayPosition(position);
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {
        mSobPopWindow.updateOrderIcon(isReverse);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //当页面选中的时候，就去切换播放的内容
        if (mPlayerPresenter != null && mIsUserSlidePager) {
            mPlayerPresenter.playByIndex(position);
        }
        mIsUserSlidePager = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}