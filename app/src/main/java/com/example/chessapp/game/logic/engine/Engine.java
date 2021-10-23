package com.example.chessapp.game.logic.engine;

import com.example.chessapp.game.logic.MoveGenerator;
import com.example.chessapp.game.type.Move;

import java.util.ArrayList;
import java.util.List;

public class Engine {
    public static int globalDepth = 4;
    private final static int mateScore = 49000;
    private final static int infinity = 50000;
    private final Rating rating;
    public Move bestMove;
    public int mate = -1;

    public Engine() {
        rating = new Rating();
    }

    public int scoreMove(long[] boards, boolean[] castleFlags, boolean white) {
        bestMove = null;
        return alphaBeta(-infinity, infinity, globalDepth - 1, boards, castleFlags, white);
    }

    public int findBestMove(long[] boards, boolean[] castleFlags, boolean white) {
        bestMove = null;
        mate = -1;
        nodes = 0;
        test = 0;
        depth0 = 0;
        int score = 0;
        score = alphaBeta(-infinity, infinity, globalDepth, boards, castleFlags, white);
        if (Math.abs(score) >= mateScore - globalDepth) {
            mate = (mateScore - Math.abs(score)) / 2;
        }

        return score;
    }

    int nodes = 0;
    int depth0 = 0;
    int test = 0;

    private int alphaBeta(int alpha, int beta, int depth, long[] boards, boolean[] castleFlags,
                          boolean white) {
        int score;

        nodes++;
        if (depth == 0) {
            depth0++;
            score = rating.evaluate(boards, white);
            return score;
        }

        int ply = globalDepth - depth;
        List<Move> moves = MoveGenerator.possibleMoves(white, boards, castleFlags);
        moves = sortMoves(moves, boards, white, ply);
        int legalMoves = 0;
        Move bestMove = null;
        int oldAlpha = alpha;

        for (Move move : moves) {
            long[] nextBoards = MoveGenerator.makeMove(move, boards);

            legalMoves++;
            boolean[] nextFlags = MoveGenerator.updateCastling(move, boards, castleFlags);
            score = -alphaBeta(-beta, -alpha, depth - 1, nextBoards, nextFlags, !white);

            if (score > alpha) {
                if (score >= beta) {

                    // TODO
                    // rating.killerMoves[1][ply] = rating.killerMoves[0][ply];
//                    rating.killerMoves[0][ply] = move;

                    return beta;
                }
                alpha = score;
                bestMove = move;
            }
        }

        if (legalMoves == 0) {
            if (!MoveGenerator.kingSafe(white, boards)) {
                return -mateScore + ply;
            } else {
                return 0;
            }
        }

        if (depth == globalDepth && alpha != oldAlpha) {
            this.bestMove = bestMove;
        }

        return alpha;
    }

    public List<Move> sortMoves(List<Move> moves, long[] boards, boolean white, int ply) {
        int[] moveScores = new int[moves.size()];

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            moveScores[i] = rating.scoreMove(move, boards, white, ply);
        }

        List<Move> sortedMoves = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            int maxScore = Integer.MIN_VALUE;
            int maxIndex = 0;
            for (int j = 0; j < moves.size(); j++) {
                if (maxScore < moveScores[j]) {
                    maxScore = moveScores[j];
                    maxIndex = j;
                }
            }
            moveScores[maxIndex] = Integer.MIN_VALUE;
            sortedMoves.add(moves.get(i));
        }
        return sortedMoves;
    }

}
