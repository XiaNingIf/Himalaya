package com.jit.himalaya.interfaces;

import com.jit.himalaya.base.IBasePresenter;

public interface IRecommendPresenter extends IBasePresenter<IRecommendViewCallback> {
    /**
     * 获取推荐内容
     */
    void getRecommendList();

    /**
     * 下拉刷新更多的内容
     */
    void pullToRefreshMore();

    /**
     * 上接加载更多
     */
    void loadMore();
}
