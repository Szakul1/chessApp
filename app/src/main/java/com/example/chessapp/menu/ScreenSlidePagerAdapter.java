package com.example.chessapp.menu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {

    public static final int NUM_PAGES = 2;
    private boolean twoPlayers = false;
    private boolean color;

    public ScreenSlidePagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0)
            return new StartingPage();
        else
            return new GameFragment(twoPlayers, color);
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public void setTwoPlayers(boolean twoPlayers) {
        this.twoPlayers = twoPlayers;
    }

    public void setColor(boolean color) {
        this.color = color;
    }
}
