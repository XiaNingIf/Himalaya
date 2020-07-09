package com.jit.himalaya.base;

import com.jit.himalaya.interfaces.IRecommendViewCallback;

public interface IBasePresenter<T> {
    /**
     * 这个方法用于注册UI的回调
     */
    void registerViewCallback(T t);

    /**
     * 取消UI的回调
     */
    void unRegisterViewCallback(T t);
}
