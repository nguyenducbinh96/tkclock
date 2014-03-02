package com.tkclock.adapters;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TkPageFragAdapter extends FragmentPagerAdapter {
	private List<Fragment> mFragments;
	
	public TkPageFragAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public int getCount() {
    	if(mFragments == null)
    		return 0;
    	else
    		return mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }
}