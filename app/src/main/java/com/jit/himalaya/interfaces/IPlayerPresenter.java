package com.jit.himalaya.interfaces;

import com.jit.himalaya.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

public interface IPlayerPresenter extends IBasePresenter<IPlayerCallback> {
    /**
     * 播放
     */
    void play();


    /**
     * 暂停
     */
    void pause();

    /**
     * 停止
     */
    void stop();

    /**
     * 上一首
     */
    void playPre();

    /**
     * 下一首
     */
    void playNext();

    void switchPlayMode(XmPlayListControl.PlayMode mode);

    /**
     * 获取播放列表
     */
    void getPlayList();

    /**
     * 根据位置播放
     *
     * @param index 节目在列表中的位置
     */
     void playByIndex(int index);

    /**
     * 切换播放进度
     *
     * @param progress
     */
     void seekTo(int progress);

    /**
     * 判断播放器是否播放
     *
     * @return
     */
    boolean isPlaying();

    /**
     * 反转播放器列表内容
     */
    void reversePlayList();

    /**
     * 播放专辑的第一个节目
     *
     * @param id
     */
    void playByAlbumId(long id);
}
