package com.jit.himalaya.views;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jit.himalaya.R;
import com.jit.himalaya.adapters.PlayListAdapter;
import com.jit.himalaya.base.BaseApplication;
import com.jit.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public class SobPopWindow extends PopupWindow {

    private static final String TAG = "SobPopWindow";
    private final View mPopView;
    private View mCloseBtn;
    private RecyclerView mTracksList;
    private PlayListAdapter mPlayListAdapter;
    private ImageView mPlayModeIv;
    private TextView mPlayModeTv;
    private View mPlayModeContainer;
    private PlayListActionListener mPlayModeClickListener = null;
    private View mOrderBtnContainer;
    private TextView mOrderText;
    private ImageView mOrderIcon;

    public SobPopWindow(){
        //设置宽高
        super(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //这里要注意，设置setOutsideTouchable之前，要先设置setBackgroundDrawable
        //否则点击外部无法关闭pop（但我的测试机可以，应该是安卓版本和API版本的问题）
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        //载进View
        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.pop_play_list,null);
        //设置内容
        setContentView(mPopView);
        //设置窗口进入和退出的动画
        setAnimationStyle(R.style.pop_animation);
        initView();
        LogUtil.e(TAG,"SobPopWindow--->create1");
        initEvent();
    }

    private void initEvent() {
        LogUtil.e(TAG,"SobPopWindow--->create2");
        //点击关闭以后消失
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SobPopWindow.this.dismiss();
            }
        });
        LogUtil.e(TAG,"SobPopWindow--->create3");

        mPlayModeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayModeClickListener != null) {
                    mPlayModeClickListener.onPlayModeClick();
                }
            }
        });
        LogUtil.e(TAG,"SobPopWindow--->create4");

        mOrderBtnContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayModeClickListener.onOrderClick();
            }
        });
    }

    private void initView() {
        mCloseBtn = mPopView.findViewById(R.id.play_list_close_btn);
        mTracksList = mPopView.findViewById(R.id.play_list_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(BaseApplication.getAppContext());
        mTracksList.setLayoutManager(layoutManager);
        mPlayListAdapter = new PlayListAdapter();
        LogUtil.e(TAG,"SobPopWindow--->create5");
        mTracksList.setAdapter(mPlayListAdapter);
        LogUtil.e(TAG,"SobPopWindow--->create6");
        //播放器相关
        mPlayModeTv = mPopView.findViewById(R.id.play_list_play_mode_tv);
        LogUtil.e(TAG,"SobPopWindow--->create7");
        mPlayModeIv = mPopView.findViewById(R.id.play_list_play_mode_iv);
        LogUtil.e(TAG,"SobPopWindow--->create8");
        mPlayModeContainer = mPopView.findViewById(R.id.play_list_play_mode_container);
        LogUtil.e(TAG,"SobPopWindow--->create9");
        mOrderBtnContainer = mPopView.findViewById(R.id.play_list_order_container);
        mOrderIcon = mPopView.findViewById(R.id.play_list_order_iv);
        mOrderText = mPopView.findViewById(R.id.play_list_order_tv);
    }

    public void setListData(List<Track> data){
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setData(data);
        }
    }

    public void setCurrentPlayPosition(int position){
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setCurrentPlayCurrent(position);
            mTracksList.scrollToPosition(position);
        }
    }

    public void setPlayListItemClickListener(PlayListItemClickListener listener){
        mPlayListAdapter.setOnItemCLickListener(listener);
    }

    /**
     * 更新播放列表的更新模式
     *
     * @param currentMode
     */
    public void updatePlayMode(XmPlayListControl.PlayMode currentMode) {
        updatePlayModeBtnImg(currentMode);
    }

    /**
     * 更新切换顺序和逆序的按钮和文字
     *
     * @param isOrder
     */
    public void updateOrderIcon(boolean isOrder){
        mOrderIcon.setImageResource(isOrder?R.drawable.selector_play_mode_list_order:R.drawable.selector_play_mode_list_revers);
        mOrderText.setText(BaseApplication.getAppContext().getResources().getString(isOrder?R.string.order_text:R.string.revers_text));
    }

    /**
     * 根据当前的状态，更新播放模式图标
     */
    private void updatePlayModeBtnImg(XmPlayListControl.PlayMode playMode) {
        int resId = R.drawable.selector_play_mode_list_order;
        int textId = R.string.play_mode_order_text;
        switch (playMode){
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_play_mode_list_order;
                textId = R.string.play_mode_order_text;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_play_mode_list_order_looper;
                textId = R.string.play_mode_list_play_text;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_play_mode_single_loop;
                textId = R.string.play_mode_single_play_text;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_play_mode_random;
                textId = R.string.play_mode_random_text;
                break;
        }
        mPlayModeIv.setImageResource(resId);
        mPlayModeTv.setText(textId);
    }

    public interface PlayListItemClickListener{
        void onItemClick(int position);
    }

    public void setPlayListActionClickListener(PlayListActionListener playModeListener){
        mPlayModeClickListener = playModeListener;
    }

    public interface PlayListActionListener {
        void onPlayModeClick();

        void onOrderClick();
    }
}
