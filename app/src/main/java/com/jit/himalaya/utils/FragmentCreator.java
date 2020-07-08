package com.jit.himalaya.utils;

import com.jit.himalaya.base.BaseFragment;
import com.jit.himalaya.fragments.HistoryFragment;
import com.jit.himalaya.fragments.RecommendFragment;
import com.jit.himalaya.fragments.SubscriptionFragment;

import java.util.HashMap;
import java.util.Map;

public class FragmentCreator {
    public final static  int INDEX_RECOMMEND = 0;
    public final static  int INDEX_SUBSCRIPTION = 1;
    public final static  int INDEX_HISTORY = 2;

    public final static  int PAGE_COUNT = 3;

    private  static Map<Integer, BaseFragment> sCacha = new HashMap<>();

    public static  BaseFragment getFragment(int index){
        BaseFragment baseFragment = sCacha.get(index);
        if(baseFragment != null){
            return baseFragment;
        }
        switch (index){
            case INDEX_RECOMMEND:
                baseFragment = new RecommendFragment();
                        break;
            case INDEX_SUBSCRIPTION:
                baseFragment = new SubscriptionFragment();
            case INDEX_HISTORY:
                baseFragment = new HistoryFragment();
        }

        sCacha.put(index,baseFragment);
        return baseFragment;

    }
}
