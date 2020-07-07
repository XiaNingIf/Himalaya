package com.jit.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface IRecommendViewCallback {
    /**
     * 获取推荐内容的结果
     *
     * @param result
     */
    void onRecommendListLoaded(List<Album> result);

    /**
     * 加载更多
     *
     * @param result
     */
    void onLoaderMore(List<Album> result);

    /**
     * 下接加载更多的结果
     *
     * @param result
     */
    void onRefreshMore(List<Album> result);
}
