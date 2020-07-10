package com.jit.himalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jit.himalaya.R;
import com.jit.himalaya.base.BaseApplication;
import com.jit.himalaya.views.SobPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.InnerHolder> {

    private List<Track> mData = new ArrayList<>();
    private int playingIndex = 0;
    private SobPopWindow.PlayListItemClickListener mItemOnClickListener = null;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paly_list, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemOnClickListener != null) {
                    mItemOnClickListener.onItemClick(position);
                }
            }
        });

        //设置数据
        Track track = mData.get(position);
        TextView trackTitleTv = holder.itemView.findViewById(R.id.track_title_tv);
        trackTitleTv.setTextColor(BaseApplication.getAppContext().getResources().getColor(playingIndex==position?
                R.color.second_color:R.color.play_list_text_color));
        trackTitleTv.setText(track.getTrackTitle());
        //找到播放状态的图标
        View playingIconView = holder.itemView.findViewById(R.id.play_icon_iv);
        playingIconView.setVisibility(playingIndex==position?View.VISIBLE:View.GONE);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Track> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void setCurrentPlayCurrent(int position) {
        playingIndex=position;
        notifyDataSetChanged();
    }

    public void setOnItemCLickListener(SobPopWindow.PlayListItemClickListener listener) {
        this.mItemOnClickListener = listener;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
