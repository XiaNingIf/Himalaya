package com.jit.himalaya.presenters;

import com.jit.himalaya.data.XimalayaApi;
import com.jit.himalaya.interfaces.ISearchCallback;
import com.jit.himalaya.interfaces.ISearchPresenter;
import com.jit.himalaya.utils.Constants;
import com.jit.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter implements ISearchPresenter {

    private List<Album> mSearchResult = new ArrayList<>();

    private static final String TAG = "SearchPresenter";
    private String mCurrentKeyword = null;
    private final XimalayaApi mXimalayaApi;
    private static final int DEFAULT_PAGE = 1;
    private int mCurrentPage = DEFAULT_PAGE;


    private SearchPresenter(){
        mXimalayaApi = XimalayaApi.getXimalayaApi();
    }

    public static SearchPresenter sSearchPresenter = null;

    public static SearchPresenter getSearchPresenter(){
        if (sSearchPresenter == null) {
            synchronized (SearchPresenter.class){
                if (sSearchPresenter == null) {
                    sSearchPresenter = new SearchPresenter();
                }
            }
        }
        return sSearchPresenter;
    }

    private List<ISearchCallback> mCallback = new ArrayList<>();

    @Override
    public void doSearch(String keyword) {
        mCurrentPage = DEFAULT_PAGE;
        mSearchResult.clear();
        //用于得新搜索
        //当网络不好的时候 ,用户会点击重新搜索
        this.mCurrentKeyword = keyword;
        Search(keyword);
    }

    private void Search(String keyword) {
        mXimalayaApi.searchByKeyword(keyword, mCurrentPage, new IDataCallBack<SearchAlbumList>() {
            @Override
            public void onSuccess(SearchAlbumList searchAlbumList) {
                List<Album> albums = searchAlbumList.getAlbums();
                mSearchResult.addAll(albums);
                if (albums != null) {
                    if (mIsLoadMore) {
                        for (ISearchCallback iSearchCallback : mCallback) {
                            iSearchCallback.onLoadMoreResult(mSearchResult,albums.size()!=0);
                        }
                    }else{
                        for (ISearchCallback iSearchCallback : mCallback) {
                            iSearchCallback.onSearchResultLoaded(mSearchResult);
                        }
                    }
                }else{
                    LogUtil.e(TAG,"album is null");
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.e(TAG,"errorCode=====>"+errorCode);
                LogUtil.e(TAG,"errorMsg=====>"+errorMsg);
                for (ISearchCallback iSearchCallback : mCallback) {
                    if (mIsLoadMore) {
                        iSearchCallback.onLoadMoreResult(mSearchResult,false);
                        mCurrentPage--;
                        mIsLoadMore = false;
                    }else {
                        iSearchCallback.onError(errorCode,errorMsg);
                    }
                }
            }
        });
    }

    @Override
    public void reSearch() {
        Search(mCurrentKeyword);
    }

    private boolean mIsLoadMore = false;

    @Override
    public void loadMore() {
        //判断有没有必要加载更多
        if (mSearchResult.size()< Constants.COUNT_DEFAULT) {
            for (ISearchCallback iSearchCallback : mCallback) {
                iSearchCallback.onLoadMoreResult(mSearchResult,false);
            }
        }else{
            mIsLoadMore = true;
            mCurrentPage++;
            Search(mCurrentKeyword);
        }
        
    }

    @Override
    public void getHotWord() {
        //todo:做一个缓存
        mXimalayaApi.getHotWords(new IDataCallBack<HotWordList>() {
            @Override
            public void onSuccess(HotWordList hotWordList) {
                if (hotWordList != null) {
                    List<HotWord> hotWords = hotWordList.getHotWordList();
                    LogUtil.d(TAG, "hotWords size -- > " + hotWords.size());
                    for (ISearchCallback iSearchCallback : mCallback) {
                        iSearchCallback.onHotWordLoaded(hotWords);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.e(TAG,"errorCode=====>"+errorCode);
                LogUtil.e(TAG,"errorMsg=====>"+errorMsg);
            }
        });
    }

    @Override
    public void getRecommendWord(String keyword) {
        mXimalayaApi.getSuggestWord(keyword, new IDataCallBack<SuggestWords>() {
            @Override
            public void onSuccess(SuggestWords suggestWords) {
                if (suggestWords != null) {
                    List<QueryResult> keyWordList = suggestWords.getKeyWordList();
                    LogUtil.e(TAG,"KeyWordList.size====>"+keyWordList.size());
                    for (ISearchCallback iSearchCallback : mCallback) {
                        iSearchCallback.onRecommendWordLoaded(keyWordList);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.e(TAG,"errorCode=====>"+errorCode);
                LogUtil.e(TAG,"errorMsg=====>"+errorMsg);
                for (ISearchCallback iSearchCallback : mCallback) {
                    iSearchCallback.onError(errorCode,errorMsg);
                }
            }
        });
    }

    @Override
    public void registerViewCallback(ISearchCallback iSearchCallback) {
        if (!mCallback.contains(iSearchCallback)) {
            mCallback.add(iSearchCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(ISearchCallback iSearchCallback) {
        mCallback.remove(iSearchCallback);
    }
}
