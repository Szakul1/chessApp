package com.example.chessapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;

import com.example.chessapp.gui.DepthPageTransformer;
import com.example.chessapp.gui.GameFragment;
import com.example.chessapp.gui.ScreenSlidePagerAdapter;
import com.example.chessapp.gui.StartingPage;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.activity_main);
        viewPager2 = findViewById(R.id.pager);
        viewPager2.setUserInputEnabled(false);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);
        viewPager2.setPageTransformer(new DepthPageTransformer());
    }

    public void startGame(View view) {
        viewPager2.setCurrentItem(1);
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