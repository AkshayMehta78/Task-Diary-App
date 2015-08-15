package com.taskdiary.adapter;

/**
 * Created by akshaymehta on 05/08/15.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;


public class PagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> mFragments;
    private Fragment mFragment = null;
    private final String[] TAB_TITLES;

    public PagerAdapter(android.support.v4.app.FragmentManager fm,
                        ArrayList<Fragment> fragments, String[] tabTitles) {
        super(fm);
        mFragments = fragments;
        TAB_TITLES = tabTitles;
    }

    @Override

    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position];
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        mFragment = mFragments.get(position);
        return mFragment;
    }


}