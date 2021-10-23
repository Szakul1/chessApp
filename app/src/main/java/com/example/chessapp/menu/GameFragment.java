package com.example.chessapp.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chessapp.R;
import com.example.chessapp.game.gui.Board;

public class GameFragment extends Fragment {

    private final boolean twoPlayers;
    private final boolean color;

    public GameFragment(boolean twoPlayers, boolean color) {
        this.twoPlayers = twoPlayers;
        this.color = color;
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
        Board board = new Board(getActivity(), twoPlayers, color);
        board.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.addView(board, 1);
    }
}