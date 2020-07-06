package com.jit.himalaya.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jit.himalaya.R;
import com.jit.himalaya.adapters.RecommendListAdapter;
import com.jit.himalaya.utils.Constants;
import com.jit.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendFragment extends  BaseFragment {
    private static final String TAG = "RecommendFragment";
    private View mRootView;
    private RecyclerView mRecommendView;
    private RecommendListAdapter mRecommendListAdapter;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        //view加载完成
        mRootView = layoutInflater.inflate((R.layout.fragment_recommend), container,false);

        mRecommendView = mRootView.findViewById(R.id.recommend_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecommendView.setLayoutManager(linearLayoutManager);
        mRecommendListAdapter = new RecommendListAdapter();
        mRecommendView.setAdapter(mRecommendListAdapter);
        //去拿数据
        getRecommendData();

        //返回view，给界面显示
        return mRootView;
    }

    /**
     * 获取推荐内容，其实就是猜你喜欢
     * 这个接口：3.10.6 获取猜你喜欢专辑
     */
    private void getRecommendData() {
        //封装参数
        Map<String, String> map = new HashMap<>();
        //这个参数表示一页数据返回多少条
        map.put(DTransferConstants.LIKE_COUNT, Constants.Recommend_Count+"");
        CommonRequest.getGuessLikeAlbum(map, new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                //数据获取成功
                if(gussLikeAlbumList!=null){
                    List<Album> albumList = gussLikeAlbumList.getAlbumList();
                    if(albumList!=null){
                        LogUtil.e(TAG,"size---->"+albumList.size());
                        upRecommendUI(albumList);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                //数据获取失败
                LogUtil.d(TAG,"error code ---->" + i);
                LogUtil.d(TAG,"error Message ---> "+s);
            }
        });
    }

    private void upRecommendUI(List<Album> albumList) {
        mRecommendListAdapter.setData(albumList);
    }
}
