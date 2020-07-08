package com.jit.himalaya.interfaces;

public interface IRecommendPresenter {
    /**
     * 获取推荐内容
     */
    void getRecommendList();


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

    /**
     * 下拉刷新更多的内容
     */
    void pullToRefreshMore();

    /**
     * 上接加载更多
     */
    void loadMore();
}
