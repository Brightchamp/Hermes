package com.example.hermesbetav2.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.hermesbetav2.AllCommunities;
import com.example.hermesbetav2.MyCommunities;

public class TabAdapter extends FragmentPagerAdapter {
    public TabAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0:
                MyCommunities myCommunities = new MyCommunities();
                return myCommunities;

            case 1:
                AllCommunities allCommunities = new AllCommunities();
                return allCommunities;

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {

            case 0:
                return "My Communities";

            case 1:
                return "All Communities";

            default:
                return null;
        }
    }
}
