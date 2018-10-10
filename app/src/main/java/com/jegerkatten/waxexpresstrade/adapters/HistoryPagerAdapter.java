package com.jegerkatten.waxexpresstrade.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jegerkatten.waxexpresstrade.fragments.HistoryFragment;
import com.jegerkatten.waxexpresstrade.fragments.MainFragment;

public class HistoryPagerAdapter extends FragmentPagerAdapter {

    private String title[] = {"Incoming", "Sent"};

    public HistoryPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return HistoryFragment.getInstance(position);
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
