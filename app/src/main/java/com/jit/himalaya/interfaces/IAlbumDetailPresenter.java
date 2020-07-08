package com.jit.himalaya.interfaces;

public interface IAlbumDetailPresenter {
    /**
     * 下拉刷新更多的内容
     */
    void pullToRefreshMore();

    /**
     * 上接加载更多
     */
    void loadMore();

    /**
     * 获取专辑详情
     *
     * @param albumId
     * @param page
     */
    void getAlbumDetail(int albumId,int page);

    void registerViewCallback(IAlbumDetailViewCallback detailViewCallback);

    void unRegisterViewCallback(IAlbumDetailViewCallback detailViewCallback);
}
