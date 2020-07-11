package com.jit.himalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jit.himalaya.DetailActivity;
import com.jit.himalaya.R;
import com.jit.himalaya.adapters.AlbumListAdapter;
import com.jit.himalaya.base.BaseFragment;
import com.jit.himalaya.interfaces.IRecommendViewCallback;
import com.jit.himalaya.presenters.AlbumDetailPresenter;
import com.jit.himalaya.presenters.RecommendPresenter;
import com.jit.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class RecommendFragment extends BaseFragment implements IRecommendViewCallback, UILoader.OnRetryClickListener, AlbumListAdapter.OnRecommendClickListener {
    private static final String TAG = "RecommendFragment";
    private View mRootView;
    private RecyclerView mRecommendView;
    private AlbumListAdapter mRecommendListAdapter;
    private RecommendPresenter mRecommendPresenter;
    private UILoader mUILoader;

    @Override
    protected View onSubViewLoaded(final LayoutInflater layoutInflater, ViewGroup container) {

        mUILoader = new UILoader(getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(layoutInflater,container);
            }
        };

        //获取到逻辑层的对象
        mRecommendPresenter = RecommendPresenter.getInstance();
        //先要设置通知接口的注册
        mRecommendPresenter.registerViewCallback(this);
        //获取推荐列表
        mRecommendPresenter.getRecommendList();

        if (mUILoader.getParent() instanceof ViewGroup) {
            ((ViewGroup) mUILoader.getParent()).removeView(mUILoader);
        }

        mUILoader.setOnRetryClickListener(this);

        //返回view，给界面显示
        return mUILoader;
    }

    private View createSuccessView(LayoutInflater layoutInflater, ViewGroup container) {
        //view加载完成
        mRootView = layoutInflater.inflate((R.layout.fragment_recommend), container,false);

        mRecommendView = mRootView.findViewById(R.id.recommend_list);
        TwinklingRefreshLayout twinklingRefreshLayout = mRootView.findViewById(R.id.over_scroll_view);
        twinklingRefreshLayout.setPureScrollModeOn();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecommendView.setLayoutManager(linearLayoutManager);
        mRecommendView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(),5);
                outRect.bottom = UIUtil.dip2px(view.getContext(),5);
                outRect.left = UIUtil.dip2px(view.getContext(),5);
                outRect.right = UIUtil.dip2px(view.getContext(),5);
            }
        });
        mRecommendListAdapter = new AlbumListAdapter();
        mRecommendView.setAdapter(mRecommendListAdapter);
        mRecommendListAdapter.setOnRecommendClickListener(this);
        return mRootView;
    }

    @Override
    public void onRecommendListLoaded(List<Album> result) {
        //获取推荐内容
        //当我们获取到推荐内容后，这个方法就会被调用
        mRecommendListAdapter.setData(result);
        mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
    }

    @Override
    public void onNetworkError() {
        mUILoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onEmpty() {
        mUILoader.updateStatus(UILoader.UIStatus.EMPTY);
    }

    @Override
    public void onLoading() {
        mUILoader.updateStatus(UILoader.UIStatus.LOADING);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消接口的注册
        if (mRecommendPresenter != null) {
            mRecommendPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onRetryClick() {
        //表示网络不佳的时候，用户点击了重试
        if (mRecommendPresenter != null) {
            mRecommendPresenter.getRecommendList();
        }
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        //item被点击了,跳转到详情界面
        Intent intent = new Intent(getContext(), DetailActivity.class);
        startActivity(intent);
    }
}
