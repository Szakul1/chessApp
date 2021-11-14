package com.example.chessapp.game.logic.engine;

import static com.example.chessapp.game.type.HashFlag.ALPHA;
import static com.example.chessapp.game.type.HashFlag.BETA;
import static com.example.chessapp.game.type.HashFlag.EXACT;

import com.example.chessapp.game.type.HashFlag;
import com.example.chessapp.game.type.Move;

public class TranspositionTable {
    private final static int LOWER_MATE = 48_000;

    public int depth;
    public HashFlag flag;
    public int score;
    public Move move;

    public TranspositionTable(int depth, HashFlag flag, int score, int ply, Move move) {
        if (score < -LOWER_MATE)
            score -= ply;
        else if (score > LOWER_MATE)
            score += ply;

        this.depth = depth;
        this.flag = flag;
        this.score = score;
        this.move = move;
    }

    public Integer readEntry(int alpha, int beta, int depth, int ply) {
        if (this.depth >= depth) {
            int score = this.score;
            if (score < -LOWER_MATE)
                score += ply;
            else if (score > LOWER_MATE)
                score -= ply;

            if (flag == EXACT) {
                return score;
            }
            if (flag == ALPHA && score <= alpha) {
                return alpha;
            }
            if (flag == BETA && score >= beta) {
                return beta;
            }
        }
        return null;
    }
}
