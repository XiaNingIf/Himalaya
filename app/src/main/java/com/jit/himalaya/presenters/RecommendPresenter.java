package com.jit.himalaya.presenters;

import com.jit.himalaya.interfaces.IRecommendPresenter;
import com.jit.himalaya.interfaces.IRecommendViewCallback;
import com.jit.himalaya.utils.Constants;
import com.jit.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendPresenter implements IRecommendPresenter {

    private static final String TAG = "RecommendPresenter";

    private List<IRecommendViewCallback> mCallbacks = new ArrayList<>();

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

    @Override
    public void getRecommendList() {
        getRecommendData();
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
        //封装参数
        Map<String, String> map = new HashMap<>();
        //这个参数表示一页数据返回多少条
        map.put(DTransferConstants.LIKE_COUNT, Constants.COUNT_RECOMMEND +"");
        CommonRequest.getGuessLikeAlbum(map, new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                //数据获取成功
                if(gussLikeAlbumList!=null){
                    List<Album> albumList = gussLikeAlbumList.getAlbumList();
                    if(albumList!=null){
                        LogUtil.e(TAG,"size---->"+albumList.size());
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
            }
        }

    }

}
