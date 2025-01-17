package com.jit.himalaya;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jit.himalaya.adapters.AlbumListAdapter;
import com.jit.himalaya.adapters.SearchRecommendAdapter;
import com.jit.himalaya.base.BaseActivity;
import com.jit.himalaya.interfaces.ISearchCallback;
import com.jit.himalaya.presenters.AlbumDetailPresenter;
import com.jit.himalaya.presenters.SearchPresenter;
import com.jit.himalaya.utils.LogUtil;
import com.jit.himalaya.views.FlowTextLayout;
import com.jit.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends BaseActivity implements ISearchCallback, AlbumListAdapter.OnRecommendClickListener {

    private static final String TAG = "SearchActivity";
    private View mBackBtn;
    private EditText mInputBox;
    private View mSearchBtn;
    private FrameLayout mResultContainer;
    private SearchPresenter mSearchPresenter;
    private FlowTextLayout mFlowTextLayout;
    private UILoader mUILoader;
    private RecyclerView mResultListView;
    private AlbumListAdapter mAlbumListAdapter;
    private InputMethodManager mImm;
    private View mDelBtn;
    public static final int DEFAULT_SHOW_IMM = 500;
    private RecyclerView mSearchRecommendList;
    private SearchRecommendAdapter mRecommendAdapter;
    private TwinklingRefreshLayout mRefreshLayout;
    private boolean mNeedSuggestionWords = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        LogUtil.e(TAG,"onCreate1");
        initView();
        LogUtil.e(TAG,"onCreate2");
        initEvent();
        LogUtil.e(TAG,"onCreate3");
        initPresenter();
        LogUtil.e(TAG,"onCreate4");
    }

    private void initPresenter() {
        mImm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        mSearchPresenter = SearchPresenter.getSearchPresenter();
        mSearchPresenter.registerViewCallback(this);
        //去拿热词
        mSearchPresenter.getHotWord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearchPresenter != null) {
            mSearchPresenter.unRegisterViewCallback(this);
            mSearchPresenter = null;
        }
    }

    private void initEvent() {
        mAlbumListAdapter.setAlbumItemClickListener(this);

        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                //加载更多的内容
                if (mSearchPresenter != null) {
                    mSearchPresenter.loadMore();
                }
            }
        });

        if (mRecommendAdapter != null) {
            mRecommendAdapter.setItemClickListener(new SearchRecommendAdapter.ItemClickListener() {
                @Override
                public void onItemClick(String keyword) {
                    //不需要联想
                    mNeedSuggestionWords = false;
                    //推荐热词的点击
                    switch2Search(keyword);
                }
            });
        }

        mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputBox.setText("");
            }
        });
        mFlowTextLayout.setClickListener(new FlowTextLayout.ItemClickListener() {
            @Override
            public void onItemClick(String text) {
                //不需要联想
                mNeedSuggestionWords = false;
                switch2Search(text);
            }
        });
        mUILoader.setOnRetryClickListener(new UILoader.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                if (mSearchPresenter != null) {
                    mSearchPresenter.reSearch();
                    mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                }
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = mInputBox.getText().toString().trim();
                if (TextUtils.isEmpty(keyword)) {
                    Toast.makeText(SearchActivity.this,"搜索关键字不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mSearchPresenter != null) {
                    mSearchPresenter.doSearch(keyword);
                    mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                }
            }
        });

        mInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mSearchPresenter.getHotWord();
                    mDelBtn.setVisibility(View.GONE);
                }else {
                    mDelBtn.setVisibility(View.VISIBLE);
                    if (mNeedSuggestionWords) {
                        //触发联想查询
                        getSuggestWord(s.toString());
                    }else{
                        mNeedSuggestionWords = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        mFlowTextLayout.setClickListener(new FlowTextLayout.ItemClickListener() {
//            @Override
//            public void onItemClick(String text) {
//                Toast.makeText(SearchActivity.this,text,Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void switch2Search(String text) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this,"搜索关键字不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        //第一步，把热词输入到输入框里
        mInputBox.setText(text);
        mInputBox.setSelection(text.length());
        //第二步，发起搜索
        if (mSearchPresenter != null) {
            mSearchPresenter.doSearch(text);
        }
        //改变状态
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.LOADING);
        }
    }

    private void getSuggestWord(String keyword) {
        if (mSearchPresenter != null) {
            mSearchPresenter.getRecommendWord(keyword);
        }
    }

    private void initView() {
        mBackBtn = this.findViewById(R.id.search_back);
        mInputBox = this.findViewById(R.id.search_input);
        mDelBtn = this.findViewById(R.id.search_input_delete);
        mDelBtn.setVisibility(View.GONE);
        mInputBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                mInputBox.requestFocus();
                mImm.showSoftInput(mInputBox,InputMethodManager.SHOW_IMPLICIT);
            }
        },DEFAULT_SHOW_IMM);
        mSearchBtn = this.findViewById(R.id.search_button);
        mResultContainer = this.findViewById(R.id.search_container);
        // mFlowTextLayout = this.findViewById(R.id.flow_text_layout);

        if (mUILoader == null) {
            mUILoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }

                @Override
                protected View getEmptyView() {
                    //创建一个新的
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView tipsView = emptyView.findViewById(R.id.empty_view_tips_tv);
                    tipsView.setText(R.string.search_no_content_tips_text);
                    return emptyView;
                }
            };
            if (mUILoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUILoader.getParent()).removeView(mUILoader);
            }
            mResultContainer.addView(mUILoader);
        }
    }

    private View createSuccessView() {
        View resultView = LayoutInflater.from(this).inflate(R.layout.search_result_layout, null);
        //刷新控件
        mRefreshLayout = resultView.findViewById(R.id.search_result_refresh_layout);
        mRefreshLayout.setEnableRefresh(false);
        //显示热词
        mFlowTextLayout = resultView.findViewById(R.id.recommend_hot_word_view);

        mResultListView = resultView.findViewById(R.id.result_list_view);
        //设置布局管理器
        LinearLayoutManager resultLayoutManager = new LinearLayoutManager(this);
        mResultListView.setLayoutManager(resultLayoutManager);
        mAlbumListAdapter = new AlbumListAdapter();
        mResultListView.setAdapter(mAlbumListAdapter);
        mResultListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
        mSearchRecommendList = resultView.findViewById(R.id.search_recommend_list);
        LinearLayoutManager recommendLayoutManager = new LinearLayoutManager(this);
        mSearchRecommendList.setLayoutManager(recommendLayoutManager);
        mSearchRecommendList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 2);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 2);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);
            }
        });
        mRecommendAdapter = new SearchRecommendAdapter();
        mSearchRecommendList.setAdapter(mRecommendAdapter);
        return resultView;
    }

    @Override
    public void onSearchResultLoaded(List<Album> result) {
        handleSearchResult(result);
        mImm.hideSoftInputFromWindow(mInputBox.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void handleSearchResult(List<Album> result) {
        hideSuccessView();
        mRefreshLayout.setVisibility(View.VISIBLE);

        if (result != null) {
            if (result.size()==0) {
                //数据为空
                if (mUILoader !=null){
                    mUILoader.updateStatus(UILoader.UIStatus.EMPTY);
                }
            }else{
                //数据不为空
                mAlbumListAdapter.setData(result);
                mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
        }
    }

    @Override
    public void onHotWordLoaded(List<HotWord> hotWordList) {
        hideSuccessView();
        mFlowTextLayout.setVisibility(View.VISIBLE);
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        List<String> hotWords = new ArrayList<>();
        hotWords.clear();
        for (HotWord hotWord : hotWordList) {
            String searchWord = hotWord.getSearchword();
            hotWords.add(searchWord);
        }
        Collections.sort(hotWords);
        //更新UI
        mFlowTextLayout.setTextContents(hotWords);
    }

    @Override
    public void onLoadMoreResult(List<Album> result, boolean isOkay) {
        //处理加载更多的结果
        if (mRefreshLayout != null) {
            mRefreshLayout.onFinishLoadMore();
        }
        if (isOkay) {
            handleSearchResult(result);
        }else{
            Toast.makeText(SearchActivity.this,"没有更多内容",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRecommendWordLoaded(List<QueryResult> keyWordList) {
        if (mRecommendAdapter != null) {
            mRecommendAdapter.setData(keyWordList);
        }
        //控制UI的显示
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        //控制显示和隐藏
        hideSuccessView();
        mSearchRecommendList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
        }
    }

    private void hideSuccessView(){
        mSearchRecommendList.setVisibility(View.GONE);
        mFlowTextLayout.setVisibility(View.GONE);
        mRefreshLayout.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        //item被点击了,跳转到详情界面
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }
}