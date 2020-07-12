package com.jit.himalaya.presenters;

import com.jit.himalaya.data.XimalayaApi;
import com.jit.himalaya.interfaces.IAlbumDetailPresenter;
import com.jit.himalaya.interfaces.IAlbumDetailViewCallback;
import com.jit.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetailPresenter implements IAlbumDetailPresenter {

    private static final String TAG = "AlbumDetailPresenter";
    private List<IAlbumDetailViewCallback> mCallbacks = new ArrayList<>();
    private List<Track> mTracks = new ArrayList<>();

    private Album mTargetAlbum = null;
    //当前专辑Id
    private int mCurrentAlbumId = -1;
    //当前页码
    private int mCurrentPageIndex = 0;

    private AlbumDetailPresenter(){
    }

    private static AlbumDetailPresenter sInstance = null;

    public static AlbumDetailPresenter getInstance(){
        if (sInstance==null){
            synchronized (AlbumDetailPresenter.class){
                if (sInstance==null){
                    sInstance = new AlbumDetailPresenter();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void pullToRefreshMore() {

    }

    @Override
    public void loadMore() {
        //去加载更多内容
        mCurrentPageIndex++;
        doLoaded(true);
    }

    private void doLoaded(final boolean isLoaderMore){
        XimalayaApi ximalayaApi = XimalayaApi.getXimalayaApi();
        ximalayaApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                if (trackList!=null){
                    List<Track> tracks = trackList.getTracks();
                    LogUtil.e(TAG,"trackSize--->"+tracks.size());
                    if (isLoaderMore){
                        mTracks.addAll(tracks);
                    }else{
                        mTracks.addAll(0,tracks);
                    }
                    handlerAlbumDetailResult(mTracks);
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                if (isLoaderMore) {
                    mCurrentPageIndex--;
                }
                LogUtil.e(TAG,"errorCode--->"+errorCode);
                LogUtil.e(TAG,"errorMessage--->"+errorMessage);
                handlerError(errorCode,errorMessage);
            }
        },mCurrentAlbumId,mCurrentPageIndex);
    }

    @Override
    public void getAlbumDetail(int albumId, int page) {
        mTracks.clear();
        this.mCurrentAlbumId = albumId;
        this.mCurrentPageIndex = page;
        //根据页码和专辑id获取内容
        doLoaded(false);
    }

    private void handlerError(int errorCode, String errorMessage) {
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onNetworkError(errorCode,errorMessage);
        }
    }

    private void handlerAlbumDetailResult(List<Track> tracks) {
        for(IAlbumDetailViewCallback mCallback: mCallbacks){
            mCallback.onDetailListLoaded(tracks);
        }
    }

    @Override
    public void registerViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        if (!mCallbacks.contains(detailViewCallback)) {
            mCallbacks.add(detailViewCallback);
            if(mTargetAlbum!=null){
                detailViewCallback.onAlbumLoaded(mTargetAlbum);
            }
        }
    }

    @Override
    public void unRegisterViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        mCallbacks.remove(detailViewCallback);
    }

    public void setTargetAlbum(Album targetAlbum){
        this.mTargetAlbum = targetAlbum;
    }
}
