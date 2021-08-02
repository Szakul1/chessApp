package com.example.chessapp.game.logic.engine;

import android.util.Log;
import android.widget.ProgressBar;

import com.example.chessapp.game.logic.Game;

import java.util.Arrays;

public class Analyze {

    public int[] moveScores;
    public int[] bestScores;
    public String[] bestMoves;
    private final Game game;
    private final Engine engine;
    public int currentMove = 0;
    private String moves;

    public Analyze(Game game, Engine engine) {
        this.game = game;
        this.engine = engine;
    }

    public void analyzeGame(String moves, long[] boards, boolean[] castleFlags, boolean white,
                            ProgressBar bar, Zobrist zobrist) {
        bar.setMax(moves.length());
        this.moves = moves;
        moveScores = new int[moves.length() / 5];
        bestScores = new int[moves.length() / 5];
        bestMoves = new String[moves.length() / 5];

        long hashKey = zobrist.generateHashKey(boards, castleFlags, white);

        for (int i = 0; i < moves.length(); i += 5) {
            int score = engine.findBestMove(boards, castleFlags, white, hashKey);
            score = white ? score : -score;
            bestMoves[i / 5] = engine.bestMove;
            bestScores[i / 5] = score;
            String move = moves.substring(i, i + 4);
            hashKey = zobrist.hashPiece(hashKey, move, boards, castleFlags, white);
            castleFlags = game.updateCastling(move, boards, castleFlags);
            boards = game.makeMove(move, boards);
            white = !white;
            score = engine.scoreMove(boards, castleFlags, white, hashKey);
            score = white ? score : -score;
            moveScores[i / 5] = score;
            bar.setProgress(i);
        }
        Log.d("test", "best: " + Arrays.toString(bestScores));
        Log.d("test", "actual: " + Arrays.toString(moveScores));
    }

    public String getMove(int index) {
        return moves.substring(index, index + 4);
    }

    public int moveForward() {
        int index = currentMove * 5;
        if (index + 5 > moves.length())
            return -1;
        game.updateBoard(getMove(index));
        currentMove++;
        return currentMove - 1;
    }

    public int moveBack() {
        if (currentMove == 0)
            return -1;
        currentMove--;
        int index = currentMove * 5;
        game.undoMove(getMove(index), moves.charAt(index + 4));
        if (currentMove == 0) {
            return -2;
        }
        return currentMove - 1;
    }
}
