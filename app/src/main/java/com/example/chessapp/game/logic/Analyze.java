package com.example.chessapp.game.logic;

import android.widget.ProgressBar;

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

    public void analyzeGame(String moves, long[] boards, boolean[] castleFlags, boolean white, ProgressBar bar) {
        engine.globalDepth = 6;
        bar.setMax(moves.length());
        this.moves = moves;
        moveScores = new int[moves.length() / 5];
        bestScores = new int[moves.length() / 5];
        bestMoves = new String[moves.length() / 5];

        for (int i = 0; i < moves.length(); i += 5) {
            int score = engine.findBestMove(boards, castleFlags, white);
            bestMoves[i / 5] = engine.bestMove;
            score = white ? score : -score;
            if (i != 0) {
                moveScores[i / 5 - 1] = -score;
            }
            bestScores[i / 5] = score;
            String move = moves.substring(i, i + 4);
            game.makeMove(move, boards);
            game.updateCastling(move, boards, castleFlags);
            white = !white;
            bar.setProgress(i);
        }
        moveScores[moveScores.length - 1] = engine.findBestMove(boards, castleFlags, white);
    }

    public void moveForward() {
        int index = currentMove * 5;
        if (index + 5 >= moves.length() - 1)
            return;
        game.updateBoard(moves.substring(index, index + 4));
        currentMove++;
    }

    public void moveBack() {
        if (currentMove == 0)
            return;
        int index = currentMove * 5;
        game.undoMove(moves.substring(index, index + 4), moves.charAt(index + 4));
        currentMove--;
    }
}
