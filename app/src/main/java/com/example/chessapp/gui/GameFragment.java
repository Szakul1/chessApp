package com.example.chessapp.gui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chessapp.R;
import com.example.chessapp.game.Board;
import com.example.chessapp.game.logic.Game;

import java.util.Objects;

public class GameFragment extends Fragment {

    private final boolean twoPlayers;

    public GameFragment(boolean twoPlayers) {
        this.twoPlayers = twoPlayers;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout linearLayout = requireView().findViewById(R.id.board);
        Board board = new Board(getActivity(), twoPlayers);
        board.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.addView(board);
    }
}