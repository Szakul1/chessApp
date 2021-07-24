package com.example.chessapp.game.logic;

import java.util.Arrays;

public class Engine {
    private final Game game;
    public final static int globalDepth = 4;
    private final static int mateScore = 49000;
    private final static int infinity = 50000;
    public String bestMove;

    public Engine(Game game) {
        this.game = game;
    }

    public int findBestMove(long[] boards, boolean[] castleFlags, boolean white) {
        bestMove = "";
        return alphaBeta(-infinity, infinity, globalDepth, boards, castleFlags, white);
    }

    private int alphaBeta(int alpha, int beta, int depth, long[] boards, boolean[] castleFlags, boolean white) {
        if (depth == 0) {
            return evaluate();
        }

        String moves = game.possibleMoves(white);
        int legalMoves = 0;
        int score;
        String bestMove = "";
        int oldAlpha = alpha;

        for (int i = 0; i < moves.length(); i += 4) {
            String move = moves.substring(i, i + 4);
            long[] nextBoards = game.makeMove(move, boards);
            if (!game.kingSafe(white, nextBoards)) {
                continue;
            }

            legalMoves++;
            boolean[] nextFlags = Arrays.copyOf(castleFlags, castleFlags.length);
            game.updateCastling(move, nextBoards, nextFlags);
            score = -alphaBeta(-beta, -alpha, depth - 1, nextBoards, nextFlags, !white);

            if (score > alpha) {
                if (score >= beta) {
                    return beta;
                }
                alpha = score;
                bestMove = move;
            }
        }

        if (legalMoves == 0) {
            if (!game.kingSafe(white, boards)) {
                return -mateScore + globalDepth - depth;
            } else {
                return 0;
            }
        }

        if (alpha != oldAlpha) {
            this.bestMove = bestMove;
        }

        return alpha;
    }

    private int evaluate() {
        return 0;
    }

}
