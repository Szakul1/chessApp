package com.example.chessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chessapp.game.logic.Game;
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
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.choose_color);
        dialog.setTitle("Choose color");
        dialog.findViewById(R.id.white).setOnClickListener(button -> {
            startGame(true);
            dialog.dismiss();
        });
        dialog.findViewById(R.id.black).setOnClickListener(button -> {
            startGame(false);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void startGame(boolean color) {
        pagerAdapter.setTwoPlayers(false);
        pagerAdapter.setColor(color);
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

    public void showProgress(View view) {
        ProgressBar progressBar = findViewById(R.id.positionBar);
        TextView textView = findViewById(R.id.positionValue);
        if (progressBar.getVisibility() == View.VISIBLE) {
            ((Button) view).setText("Show\nposition");
            progressBar.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
        } else {
            ((Button) view).setText("Hide\nposition");
            progressBar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
        }
    }
}