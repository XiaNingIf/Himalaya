package com.jit.himalaya.api;

import com.jit.himalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.HashMap;
import java.util.Map;

public class XimalayaApi {

    private XimalayaApi(){}

    public static XimalayaApi sXimalayaApi;

    public static XimalayaApi getXimalayaApi(){
        if (sXimalayaApi == null) {
            synchronized (XimalayaApi.class){
                if (sXimalayaApi == null) {
                    sXimalayaApi = new XimalayaApi();
                }
            }
        }
        return sXimalayaApi;
    }

    /**
     * 获取推荐内容
     *
     * @param callBack
     */
    public void getRecommendList(IDataCallBack<GussLikeAlbumList> callBack){
        //封装参数
        Map<String, String> map = new HashMap<>();
        //这个参数表示一页数据返回多少条
        map.put(DTransferConstants.LIKE_COUNT, Constants.COUNT_RECOMMEND +"");
        CommonRequest.getGuessLikeAlbum(map,callBack);
    }

    /**
     * 根据专辑的id获取专辑内容
     *
     * @param callBack 获取专辑详情的回调接口
     * @param albumId
     * @param pageIndex
     */
    public void getAlbumDetail(IDataCallBack<TrackList> callBack,long albumId,int pageIndex){
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.ALBUM_ID,albumId+"");
        map.put(DTransferConstants.SORT, "asc");
        map.put(DTransferConstants.PAGE, pageIndex+"");
        map.put(DTransferConstants.PAGE_SIZE, Constants.COUNT_DEFAULT+"");
        CommonRequest.getTracks(map,callBack);
    }

    /**
     * 根据关键词字进行搜索
     *
     * @param keyword
     */
    public void searchByKeyword(String keyword, int page, IDataCallBack<SearchAlbumList> callback) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        map.put(DTransferConstants.PAGE, page + "");
        map.put(DTransferConstants.PAGE_SIZE, Constants.COUNT_DEFAULT + "");
        CommonRequest.getSearchedAlbums(map, callback);
    }

    /**
     * 获取推荐的热词
     *
     * @param callback
     */
    public void getHotWords(IDataCallBack<HotWordList> callback) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.TOP, String.valueOf(Constants.COUNT_HOT_WORD));
        CommonRequest.getHotWords(map, callback);
    }

    /**
     * 根据关键字获取联想词
     *
     * @param keyword  关键字
     * @param callback 回调
     */
    public void getSuggestWord(String keyword, IDataCallBack<SuggestWords> callback) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        CommonRequest.getSuggestWord(map, callback);
    }
}
