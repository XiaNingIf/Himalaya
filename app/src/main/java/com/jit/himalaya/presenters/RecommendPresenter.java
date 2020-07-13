package com.jit.himalaya.presenters;

import com.jit.himalaya.data.XimalayaApi;
import com.jit.himalaya.interfaces.IRecommendPresenter;
import com.jit.himalaya.interfaces.IRecommendViewCallback;
import com.jit.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.List;

public class RecommendPresenter implements IRecommendPresenter {

    private static final String TAG = "RecommendPresenter";

    private List<IRecommendViewCallback> mCallbacks = new ArrayList<>();
    private List<Album> mCurrentRecommend = null;
    private List<Album> mRecommendList;

    private RecommendPresenter(){}

    private static RecommendPresenter sInsatance = null;

    public static RecommendPresenter getInstance(){
        if (sInsatance == null){
            synchronized (RecommendPresenter.class){
                if (sInsatance == null){
                    sInsatance = new RecommendPresenter();
                }
            }
        }
        return sInsatance;
    }

    /**
     * 获取当前推荐专辑列表
     *
     * @return 使用之前要判空
     */
    public List<Album> getCurrentRecommend(){
        return mCurrentRecommend;
    }

    @Override
    public void getRecommendList() {
        //如果内容不空的话，那么直接使用当前的内容
        if(mRecommendList != null && mRecommendList.size() > 0) {
            LogUtil.d(TAG,"getRecommendList -- > from list.");
            handlerRecommendResult(mRecommendList);
            return;
        }getRecommendData();
    }


    @Override
    public void registerViewCallback(IRecommendViewCallback callback) {
        if(!mCallbacks.contains(callback)){
            mCallbacks.add(callback);
        }
    }

    @Override
    public void unRegisterViewCallback(IRecommendViewCallback callback) {
        if (mCallbacks!=null){
            mCallbacks.remove(callback);
        }
    }

    @Override
    public void pullToRefreshMore() {

    }

    @Override
    public void loadMore() {

    }

    /**
     * 获取推荐内容，其实就是猜你喜欢
     * 这个接口：3.10.6 获取猜你喜欢专辑
     */
    private void getRecommendData() {
        updateLoading();
        XimalayaApi ximalayaApi = XimalayaApi.getXimalayaApi();
        ximalayaApi.getRecommendList(new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                //数据获取成功
                if(gussLikeAlbumList!=null){
                    List<Album> albumList = gussLikeAlbumList.getAlbumList();
                    if(albumList!=null){
                        LogUtil.e(TAG,"size---->"+albumList.size());
                        mRecommendList = gussLikeAlbumList.getAlbumList();
//                        upRecommendUI(albumList);
                        handlerRecommendResult(albumList);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                //数据获取失败
                LogUtil.d(TAG,"error code ---->" + i);
                LogUtil.d(TAG,"error Message ---> "+s);
                handleError();
            }
        });
    }

    private  void updateLoading(){
        for (IRecommendViewCallback callback : mCallbacks){
            callback.onLoading();
        }
    }

    private void handleError() {
        if(mCallbacks != null){
            for(IRecommendViewCallback callback:mCallbacks){
                callback.onNetworkError();
            }
        }
    }

    private void handlerRecommendResult(List<Album> albumList) {
        if (albumList != null) {
            if(albumList.size()==0){
                for (IRecommendViewCallback callback:mCallbacks){
                    callback.onEmpty();
                }
            }else{
                //通知UI
                if(mCallbacks != null){
                    for(IRecommendViewCallback callback:mCallbacks){
                        callback.onRecommendListLoaded(albumList);
                    }
                }
                this.mCurrentRecommend = albumList;
            }
        }

    }

}
