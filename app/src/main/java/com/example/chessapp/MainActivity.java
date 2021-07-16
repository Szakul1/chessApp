package com.example.chessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.example.chessapp.gui.PromotionChoice;
import com.example.chessapp.gui.ScreenSlidePagerAdapter;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private ViewPager2 viewPager2;
    private ScreenSlidePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.activity_main);
        viewPager2 = findViewById(R.id.pager);
        viewPager2.setUserInputEnabled(false);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);
    }

    public void gameWithBot(View view) {
        pagerAdapter.setTwoPlayers(false);
        viewPager2.setAdapter(pagerAdapter);
        viewPager2.setCurrentItem(1);
    }

    public void gameWithPlayer(View view) {
        pagerAdapter.setTwoPlayers(true);
        viewPager2.setAdapter(pagerAdapter);
        viewPager2.setCurrentItem(1);
    }

    public ViewPager2 getViewPager2() {
        return viewPager2;
    }

    @Override
    public void onBackPressed() {
        if (viewPager2.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1);
        }
    }

}