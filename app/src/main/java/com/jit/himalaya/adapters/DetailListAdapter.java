package com.jit.himalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jit.himalaya.R;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DetailListAdapter extends RecyclerView.Adapter<DetailListAdapter.InnerHolder> {

    private static final String TAG = "DetailListAdapter";
    private List<Track> mDetailData = new ArrayList<>();

    //格式化时间
    private SimpleDateFormat mUpdateDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat mDurationFormat = new SimpleDateFormat("mm:ss");
    private ItemClickListener mItemClickListener = null;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_detail, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, final int position) {
        //找到控件
        View itemView = holder.itemView;
        TextView orderTv = itemView.findViewById(R.id.order_text);
        TextView titleTv = itemView.findViewById(R.id.detail_item_title);
        final TextView playCountTv = itemView.findViewById(R.id.detail_item_play_count);
        TextView durationTv = itemView.findViewById(R.id.detail_item_duration);
        TextView updateDateTv = itemView.findViewById(R.id.detail_item_update_time);

        Track track = mDetailData.get(position);
        orderTv.setText((position + 1) + "");
        titleTv.setText(track.getTrackTitle());
        playCountTv.setText(track.getPlayCount()+"");
        int durationMil = track.getDuration()*1000;
        String duration = mDurationFormat.format(durationMil);
        durationTv.setText(duration);
        String updateTimeText = mUpdateDateFormat.format(track.getUpdatedAt());
        updateDateTv.setText(updateTimeText);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    //参数需要有列表和位置
                    mItemClickListener.onItemClick(mDetailData,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDetailData.size();
    }

    public void setData(List<Track> tracks) {
        mDetailData.clear();
        mDetailData.addAll(tracks);
        notifyDataSetChanged();
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setItemClickListener(ItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public interface ItemClickListener{
        void onItemClick(List<Track> detailData, int position);
    }
}
