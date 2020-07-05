package com.jit.himalaya.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.jit.himalaya.MainActivity;
import com.jit.himalaya.R;
import com.jit.himalaya.utils.LogUtil;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

public class IndicatorAdapter extends CommonNavigatorAdapter {

    private static final String TAG = "IndicatorAdapter";
    private final String[] mTitle;

    public IndicatorAdapter(Context context) {
        mTitle = context.getResources().getStringArray(R.array.indicator_title);
    }

    @Override
    public int getCount() {
        if(mTitle!=null){
            LogUtil.e(TAG,"调试Adapter----->"+mTitle.length);
            return mTitle.length;
        }
        return 0;
    }

    @Override
    public IPagerTitleView getTitleView(Context context, final int index) {
        ColorTransitionPagerTitleView colorTransitionPagerTitleView = new ColorTransitionPagerTitleView(context);
        colorTransitionPagerTitleView.setNormalColor((Color.parseColor("#aaffffff")));
        colorTransitionPagerTitleView.setSelectedColor(Color.parseColor("#ffffff"));
        colorTransitionPagerTitleView.setTextSize(18);
        colorTransitionPagerTitleView.setText(mTitle[index]);
        colorTransitionPagerTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:
            }
        });
        return colorTransitionPagerTitleView;
        //        SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
        //        simplePagerTitleView.setNormalColor(Color.GRAY);
        //        simplePagerTitleView.setSelectedColor(Color.WHITE);
        //        simplePagerTitleView.setText(mTitle[index]);
        //        simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        ////                mViewPager.setCurrentItem(index);
        //            }
        //        });
        //        return simplePagerTitleView;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
        LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
        linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
        linePagerIndicator.setColors(Color.parseColor("#ffffff"));
        return linePagerIndicator;
    }
}
