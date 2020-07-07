package com.jit.himalaya.interfaces;

public interface IRecommendPresenter {
    /**
     * 获取推荐内容
     */
    void getRecommendList();

    /**
     * 下拉刷新
     */
    void pullToRefreshMore();

    /**
     * 下拉加载更多
     */
    void loadMore();

    /**
     * 这个方法用于注册UI的回调
     * @param callback
     */
    void registerViewCallback(IRecommendViewCallback callback);

    /**
     * 取消UI的回调
     * @param callback
     */
    void unRegisterViewCallback(IRecommendViewCallback callback);
}
