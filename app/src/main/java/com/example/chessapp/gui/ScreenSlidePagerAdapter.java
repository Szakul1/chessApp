package com.example.chessapp.gui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {

    public static final int NUM_PAGES = 2;

    public ScreenSlidePagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0)
            return new StartingPage();
        else
            return new GameFragment();
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
