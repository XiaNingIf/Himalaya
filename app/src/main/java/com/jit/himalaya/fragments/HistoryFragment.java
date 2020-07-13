package com.jit.himalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jit.himalaya.PlayerActivity;
import com.jit.himalaya.R;
import com.jit.himalaya.adapters.TrackListAdapter;
import com.jit.himalaya.base.BaseApplication;
import com.jit.himalaya.base.BaseFragment;
import com.jit.himalaya.interfaces.IHistoryCallback;
import com.jit.himalaya.interfaces.IHistoryPresenter;
import com.jit.himalaya.presenters.HistoryPresenter;
import com.jit.himalaya.presenters.PlayerPresenter;
import com.jit.himalaya.views.ConfirmCheckBoxDialog;
import com.jit.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class HistoryFragment extends BaseFragment implements IHistoryCallback, TrackListAdapter.ItemClickListener, TrackListAdapter.ItemLongClickListener, ConfirmCheckBoxDialog.OnDialogActionClickListener  {

    private UILoader mUiLoader;
    private TrackListAdapter mTrackListAdapter;
    private HistoryPresenter mHistoryPresenter;
    private Track mCurrentClickHistoryItem = null;
    private RecyclerView mHistoryList;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater,ViewGroup container) {
        FrameLayout rootView = (FrameLayout) layoutInflater.inflate(R.layout.fragment_history,container,false);
        if(mUiLoader == null) {
            mUiLoader = new UILoader(container.getContext()) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }

                @Override
                protected View getEmptyView() {
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView tipsView = emptyView.findViewById(R.id.empty_view_tips_tv);
                    tipsView.setText(R.string.no_history_tips_string);
                    return emptyView;
                }
            };
        }else{
            if(mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }
        }
        rootView.addView(mUiLoader);
        return rootView;
    }

    private View createSuccessView() {
        View successView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.item_history,null);
        TwinklingRefreshLayout refreshLayout = successView.findViewById(R.id.over_scroll_view);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadmore(false);
        //recyclerView.
        mHistoryList = successView.findViewById(R.id.history_list);
        mHistoryList.setLayoutManager(new LinearLayoutManager(successView.getContext()));
        //设置item的上下间距
        mHistoryList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(),5);
                outRect.bottom = UIUtil.dip2px(view.getContext(),5);
                outRect.left = UIUtil.dip2px(view.getContext(),5);
                outRect.right = UIUtil.dip2px(view.getContext(),5);
            }
        });
        //设置适配器
        mTrackListAdapter = new TrackListAdapter();
        mTrackListAdapter.setItemClickListener(this);
        mTrackListAdapter.setItemLongClickListener(this);
        mHistoryList.setAdapter(mTrackListAdapter);
        //HistoryPresenter
        mHistoryPresenter = HistoryPresenter.getHistoryPresenter();
        mHistoryPresenter.registerViewCallback(this);
        mHistoryPresenter.listHistories();
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        return successView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mHistoryPresenter != null) {
            mHistoryPresenter.unRegisterViewCallback(this);
        }
        mTrackListAdapter.setItemClickListener(null);
    }

    @Override
    public void onHistoriesLoaded(List<Track> tracks) {
        if(tracks == null || tracks.size() == 0) {
            if (mUiLoader!=null)
            mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
        } else {
            if (mUiLoader!=null) {
                mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
        }
        //更新UI
        if (mTrackListAdapter != null) {
            mTrackListAdapter.setData(tracks);
        }
    }

    @Override
    public void onItemClick(List<Track> detailData,int position) {
        //设置播放器的数据
        PlayerPresenter playerPresenter = PlayerPresenter.getPlayerPresenter();
        playerPresenter.setPlayList(detailData,position);
        //跳转到播放器界面
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Track track) {
        this.mCurrentClickHistoryItem = track;
        //去删除历史
        //Toast.makeText(getActivity(),"历史记录长按..." + track.getTrackTitle(),Toast.LENGTH_SHORT).show();
        ConfirmCheckBoxDialog dialog = new ConfirmCheckBoxDialog(getActivity());
        dialog.setOnDialogActionClickListener(this);
        dialog.show();
    }

    @Override
    public void onCancelClick() {
        //不用做，
    }

    @Override
    public void onConfirmClick(boolean isCheck) {
        //去删除历史
        if(mHistoryPresenter != null && mCurrentClickHistoryItem != null) {
            if(!isCheck) {
                mHistoryPresenter.delHistory(mCurrentClickHistoryItem);
            } else {
                mHistoryPresenter.cleanHistories();
            }
        }
    }
}
