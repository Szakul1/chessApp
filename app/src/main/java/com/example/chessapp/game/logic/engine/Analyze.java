package com.example.chessapp.game.logic.engine;

import android.util.Log;
import android.widget.ProgressBar;

import com.example.chessapp.game.Move;
import com.example.chessapp.game.logic.Game;

import java.util.Arrays;
import java.util.List;

public class Analyze {

    public int[] moveScores;
    public int[] bestScores;
    public String[] bestMoves;
    private final Game game;
    private final Engine engine;
    public int currentMove = -1;
    public List<Move> moves;

    public Analyze(Game game, Engine engine) {
        this.game = game;
        this.engine = engine;
    }

    public void analyzeGame(List<Move> moves, long[] boards, boolean[] castleFlags, boolean white,
                            ProgressBar bar) {
        bar.setMax(moves.size());
        this.moves = moves;
        moveScores = new int[moves.size()];
        bestScores = new int[moves.size()];
        bestMoves = new String[moves.size()];

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            int score = engine.findBestMove(boards, castleFlags, white);
            score = white ? score : -score;
            bestMoves[i] = engine.bestMove.toString();
            bestScores[i] = score;
            castleFlags = game.updateCastling(move, boards, castleFlags);
            boards = game.makeMove(move, boards);

            white = !white;
            score = engine.scoreMove(boards, castleFlags, white);
            score = white ? score : -score;
            moveScores[i] = score;
            bar.setProgress(i);
        }
        Log.d("test", "best: " + Arrays.toString(bestScores));
        Log.d("test", "actual: " + Arrays.toString(moveScores));
    }

    public int moveForward() {
        if (currentMove == moves.size() - 1)
            return currentMove;
        currentMove++;
        game.updateBoard(moves.get(currentMove));
        return currentMove;
    }

    public int moveBack() {
        if (currentMove == -1)
            return currentMove;
        currentMove--;
        game.undoMove(moves.get(currentMove));
        return currentMove;
    }

}
