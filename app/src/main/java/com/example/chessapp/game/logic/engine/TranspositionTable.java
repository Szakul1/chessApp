package com.example.chessapp.game.logic.engine;

import static com.example.chessapp.game.logic.BitBoards.*;
import static com.example.chessapp.game.logic.engine.Engine.globalDepth;

public class TranspositionTable {
    public int depth;
    public int flags;
    public int score;
    public String best;
    private final static int lowerMate = 48000;

    public TranspositionTable(int depth, int flags, String best, int score) {
        if (score < -lowerMate)
            score -= globalDepth - depth;
        else if (score > lowerMate)
            score += +globalDepth - depth;

        this.depth = depth;
        this.flags = flags;
        this.best = best;
        this.score = score;
    }

    public Integer readEntry(int alpha, int beta, int depth) {
        if (this.depth >= depth) {
            int score = this.score;
            if (score < -lowerMate)
                score += globalDepth - depth;
            else if (score > lowerMate)
                score -= globalDepth - depth;

            if (flags == hash_exact) {
                return score;
            }
            if (flags == hash_alpha && score <= alpha) {
                return alpha;
            }
            if (flags == hash_beta && score >= beta) {
                return beta;
            }
        }
        return null;
    }
}
