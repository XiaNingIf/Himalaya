package com.jit.himalaya.presenters;

import android.content.Context;
import android.content.SharedPreferences;

import com.jit.himalaya.base.BaseApplication;
import com.jit.himalaya.interfaces.IPlayerCallback;
import com.jit.himalaya.interfaces.IPlayerPresenter;
import com.jit.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerPresenter implements IPlayerPresenter, IXmAdsStatusListener, IXmPlayerStatusListener {

    private static final String TAG = "PlayerPresenter";
    private final XmPlayerManager mPlayerManager;

    List<IPlayerCallback> mCallback = new ArrayList<>();
    private Track mCurrentTrack;
    private int mCurrentIndex;
    private final SharedPreferences mPlayModeSp;
    private XmPlayListControl.PlayMode mCurrentPlayMode = PLAY_MODEL_LIST;
    private boolean mIsReverse = false;

//    PLAY_MODEL_LIST
//    PLAY_MODEL_LIST_LOOP
//    PLAY_MODEL_RANDOM
//    PLAY_MODEL_SINGLE_LOOP

    public static final int PLAY_MODEL_LIST_INT = 0;
    public static final int PLAY_MODEL_LIST_LOOP_INT = 1;
    public static final int PLAY_MODEL_RANDOM_INT = 2;
    public static final int PLAY_MODEL_SINGLE_LOOP_INT = 3;

    //sp's key and name
    public static final String PLAY_MODE_SP_NAME = "PlayMod";
    public static final String PLAY_MODE_SP_KEY = "currentPlayMode";


    private PlayerPresenter(){
        mPlayerManager = XmPlayerManager.getInstance(BaseApplication.getAppContext());
        //广告物料相关的接口
        mPlayerManager.addAdsStatusListener(this);
        //注册播放器相关的状态接口
        mPlayerManager.addPlayerStatusListener(this);
        //需要记录当前的播放模式
        mPlayModeSp = BaseApplication.getAppContext().getSharedPreferences(PLAY_MODE_SP_NAME, Context.MODE_PRIVATE);
    }

    private static PlayerPresenter sPlayerPresenter;

    public static PlayerPresenter getPlayerPresenter(){
        if (sPlayerPresenter==null){
            synchronized (PlayerPresenter.class){
                if (sPlayerPresenter==null){
                    sPlayerPresenter=new PlayerPresenter();
                }
            }
        }
        return sPlayerPresenter;
    }

    private boolean isPlayListSet = false;

    public void setPlayerList(List<Track> list, int playIndex){
        if (mPlayerManager != null) {
            mPlayerManager.setPlayList(list,playIndex);
            isPlayListSet=true;
            mCurrentTrack = list.get(playIndex);
            mCurrentIndex = playIndex;
        }else{
            LogUtil.e(TAG,"mPlayerManager is null");
        }


    }

    @Override
    public void play() {
        if (isPlayListSet){
            mPlayerManager.play();
        }
    }

    @Override
    public void pause() {
        if (mPlayerManager != null){
            mPlayerManager.pause();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void playPre() {
        if (mPlayerManager != null) {
            mPlayerManager.playPre();
        }
    }

    @Override
    public void playNext() {
        if (mPlayerManager != null) {
            mPlayerManager.playNext();
        }
    }

    @Override
    public void switchPlayMode(XmPlayListControl.PlayMode mode) {
        if (mPlayerManager != null) {
            mCurrentPlayMode = mode;
            mPlayerManager.setPlayMode(mode);
            for (IPlayerCallback iPlayerCallback : mCallback) {
                iPlayerCallback.onPlayModeChange(mode);
            }
            //保存到sp中去
            SharedPreferences.Editor edit = mPlayModeSp.edit();
            edit.putInt(PLAY_MODE_SP_KEY,getIntByPlayMode(mode));
            edit.commit();
        }
    }

    private int getIntByPlayMode(XmPlayListControl.PlayMode mode){
        switch(mode) {
            case PLAY_MODEL_SINGLE_LOOP:
                return PLAY_MODEL_SINGLE_LOOP_INT;
            case PLAY_MODEL_LIST_LOOP:
                return PLAY_MODEL_LIST_LOOP_INT;
            case PLAY_MODEL_RANDOM:
                return PLAY_MODEL_RANDOM_INT;
            case PLAY_MODEL_LIST:
                return PLAY_MODEL_LIST_INT;
        }
        return PLAY_MODEL_LIST_INT;
    }

    private XmPlayListControl.PlayMode getModeByInt(int index) {
        switch(index) {
            case PLAY_MODEL_SINGLE_LOOP_INT:
                return PLAY_MODEL_SINGLE_LOOP;
            case PLAY_MODEL_LIST_LOOP_INT:
                return PLAY_MODEL_LIST_LOOP;
            case PLAY_MODEL_RANDOM_INT:
                return PLAY_MODEL_RANDOM;
            case PLAY_MODEL_LIST_INT:
                return PLAY_MODEL_LIST;
        }
        return PLAY_MODEL_LIST;
    }

    @Override
    public void getPlayList() {
        if (mPlayerManager != null) {
            List<Track> playList = mPlayerManager.getPlayList();
            for (IPlayerCallback iPlayerCallback : mCallback) {
                iPlayerCallback.onListLoaded(playList);
            }
        }
    }

    @Override
    public void playByIndex(int index) {
        //切换播放器到第index的位置进行播放
        if (mPlayerManager != null) {
            mPlayerManager.play(index);
        }
    }

    @Override
    public void seekTo(int progress) {
        //更新播放器的进度
        mPlayerManager.seekTo(progress);
    }

    @Override
    public boolean isPlaying() {
        //返回当前是否正在播放
        return mPlayerManager.isPlaying();
    }

    @Override
    public void reversePlayList() {
        //那播放列表反转
        List<Track> playList = mPlayerManager.getPlayList();
        Collections.reverse(playList);
        mIsReverse = !mIsReverse;
        mCurrentIndex = playList.size()-1-mCurrentIndex;
        mPlayerManager.setPlayList(playList,mCurrentIndex);
        //更新UI
        mCurrentTrack = (Track) mPlayerManager.getCurrSound();
        for (IPlayerCallback iPlayerCallback : mCallback) {
            iPlayerCallback.onListLoaded(playList);
            iPlayerCallback.onTrackUpdate(mCurrentTrack,mCurrentIndex);
            iPlayerCallback.updateListOrder(mIsReverse);
        }
    }

    @Override
    public void registerViewCallback(IPlayerCallback iPlayerCallback) {
        iPlayerCallback.onTrackUpdate(mCurrentTrack,mCurrentIndex);
        int modelIndex = mPlayModeSp.getInt(PLAY_MODE_SP_KEY, PLAY_MODEL_LIST_INT);
        mCurrentPlayMode = getModeByInt(modelIndex);
        iPlayerCallback.onPlayModeChange(mCurrentPlayMode);
        if (!mCallback.contains(iPlayerCallback) ) {
            mCallback.add(iPlayerCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(IPlayerCallback iPlayerCallback) {
        if (mCallback!=null){
            mCallback.remove(iPlayerCallback);
        }
    }

    //===============广告相关的回调 start========================



    @Override
    public void onStartGetAdsInfo() {
        LogUtil.e(TAG,"onStartGetAdsInfo");
    }

    @Override
    public void onGetAdsInfo(AdvertisList advertisList) {
        LogUtil.e(TAG,"onGetAdsInfo");
    }

    @Override
    public void onAdsStartBuffering() {
        LogUtil.e(TAG,"onAdsStartBuffering");
    }

    @Override
    public void onAdsStopBuffering() {
        LogUtil.e(TAG,"onAdsStopBuffering");
    }

    @Override
    public void onStartPlayAds(Advertis advertis, int i) {
        LogUtil.e(TAG,"onStartPlayAds");
    }

    @Override
    public void onCompletePlayAds() {
        LogUtil.e(TAG,"onCompletePlayAds");
    }

    @Override
    public void onError(int i, int i1) {
        LogUtil.e(TAG,"onError");
    }
    //===============广告相关的回调 end========================

    //===============播放器相关的回调 start======================

    @Override
    public void onPlayStart() {
        LogUtil.e(TAG,"onPlayStart");
        for (IPlayerCallback iPlayerCallback : mCallback) {
            iPlayerCallback.onPlayStart();
        }
    }

    @Override
    public void onPlayPause() {
        LogUtil.e(TAG,"onPlayPause");
        for (IPlayerCallback iPlayerCallback : mCallback) {
            iPlayerCallback.onPlayPause();
        }
    }

    @Override
    public void onPlayStop() {
        LogUtil.e(TAG,"onPlayStop");
        for (IPlayerCallback iPlayerCallback : mCallback) {
            iPlayerCallback.onPlayStop();
        }
    }

    @Override
    public void onSoundPlayComplete() {
        LogUtil.e(TAG,"onSoundPlayComplete");
    }

    @Override
    public void onSoundPrepared() {
        LogUtil.e(TAG,"onSoundPrepared");
        mPlayerManager.setPlayMode(mCurrentPlayMode);
        if (mPlayerManager.getPlayerStatus() == PlayerConstants.STATE_PREPARED) {
            mPlayerManager.play();
        }
    }

    @Override
    public void onSoundSwitch(PlayableModel lastModel, PlayableModel curModel) {
        LogUtil.e(TAG,"onSoundSwitch");
        //curModel代表的是当前播放的内容
        //通过getKind方法来获取他是什么类型
        //track类型
        //第一种写法:不推荐
        //        if ("track".equals(curModel.getKind())){
        //            Track currentTrack = (Track)curModel;
        //        }
        //第二种写法
        mCurrentIndex = mPlayerManager.getCurrentIndex();
        if (curModel instanceof Track) {
            mCurrentTrack = (Track)curModel;
            //更新UI
            for (IPlayerCallback iPlayerCallback : mCallback) {
                iPlayerCallback.onTrackUpdate(mCurrentTrack,mCurrentIndex);
            }
        }
    }

    @Override
    public void onBufferingStart() {
        LogUtil.e(TAG,"onBufferingStart");
    }

    @Override
    public void onBufferingStop() {
        LogUtil.e(TAG,"onBufferingStop");
    }

    @Override
    public void onBufferProgress(int i) {
        LogUtil.e(TAG,"onBufferProgress");
    }

    @Override
    public void onPlayProgress(int currPos, int duration) {
        //单位是毫秒
        for (IPlayerCallback iPlayerCallback : mCallback) {
            iPlayerCallback.onProgressChange(currPos,duration);
        }
    }

    @Override
    public boolean onError(XmPlayerException e) {
        LogUtil.e(TAG,"onError");
        return false;
    }

    //===============播放器相关的回调 end======================


}
