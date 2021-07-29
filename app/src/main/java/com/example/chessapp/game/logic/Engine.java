package com.example.chessapp.game.logic;

import android.service.controls.Control;
import android.util.Log;

import com.example.chessapp.R;

import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;

import static com.example.chessapp.game.logic.BitBoards.*;

public class Engine {
    private final Game game;
    public static int globalDepth = 4;
    private final static int mateScore = 49000;
    private final static int infinity = 50000;
    private final Rating rating;
    public String bestMove;
    public int mate = -1;

    public Engine(Game game) {
        this.game = game;
        rating = new Rating();
    }

    public int findBestMove(long[] boards, boolean[] castleFlags, boolean white) {
        bestMove = "";
        mate = -1;
        nodes = 0;
        int score = alphaBeta(-infinity, infinity, globalDepth, boards, castleFlags, white);
        Log.d("test", "nodes: " + nodes);
        if (Math.abs(score) >= mateScore - globalDepth) {
            mate = (mateScore - Math.abs(score)) / 2;
        }
        return score;
    }

    int nodes = 0;

    private int alphaBeta(int alpha, int beta, int depth, long[] boards, boolean[] castleFlags, boolean white) {
        nodes++;
        if (depth == 0) {
            costam = 0;
            int score = quiescence(alpha, beta, boards, castleFlags, white);
//            Log.d("test", costam+"");
            nodes += costam;
            return score;
        }

        String moves = game.possibleMoves(white, boards, castleFlags);
        moves = sortMoves(moves, boards, white);
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

        if (depth == globalDepth && alpha != oldAlpha) {
            this.bestMove = bestMove;
        }

        return alpha;
    }

    public String sortMoves(String moves, long[] boards, boolean white) {
        int[] moveScores = new int[moves.length() / 4];

        for (int i = 0; i < moves.length(); i += 4) {
            String move = moves.substring(i, i + 4);
            moveScores[i / 4] = rating.scoreMove(move, boards, white);
        }

        StringBuilder sortedMoves = new StringBuilder();
        for (int i = 0; i < moves.length() / 4; i++) {
            int maxScore = Integer.MIN_VALUE;
            int maxIndex = 0;
            for (int j = 0; j < moves.length() / 4; j++) {
                if (maxScore < moveScores[j]) {
                    maxScore = moveScores[j];
                    maxIndex = j;
                }
            }
            moveScores[maxIndex] = Integer.MIN_VALUE;
            sortedMoves.append(moves.substring(maxIndex * 4, maxIndex * 4 + 4));
        }
        return sortedMoves.toString();
    }

    int costam = 0;

    public int quiescence(int alpha, int beta, long[] boards, boolean[] castleFlags, boolean white) {
        costam++;
        int score = rating.evaluate(boards, white);
        return score;

//        if (score > alpha) {
//            if (score >= beta) {
//                return beta;
//            }
//            alpha = score;
//        }
//
//        String moves = game.possibleMoves(white, boards, castleFlags);
//        moves = sortMoves(moves, boards, white);
//
//        // only captures
//        long opponentPieces = Game.getMyPieces(!white, boards);
//        for (int i = 0; i < moves.length(); i += 4) {
//            String move = moves.substring(i, i + 4);
//
//            // TODO
//            if (move.charAt(3) != 'E') {
//                if (!Character.isDigit(move.charAt(3)))
//                    continue;
//                int position = Game.getValFromString(move, 2) * 8 + Game.getValFromString(move, 3);
//                if (((1L << position) & opponentPieces) == 0) { // not capture
//                    continue;
//                }
//            }
//
//            long[] nextBoards = game.makeMove(move, boards);
//            if (!game.kingSafe(white, nextBoards)) {
//                continue;
//            }
//
//            score = -quiescence(-beta, -alpha, nextBoards, castleFlags, !white);
//
//            if (score > alpha) {
//                if (score >= beta) {
//                    return beta;
//                }
//                alpha = score;
//            }
//        }
//
//        return alpha;
    }
}
