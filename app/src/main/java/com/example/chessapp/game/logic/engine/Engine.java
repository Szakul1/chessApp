package com.example.chessapp.game.logic.engine;

import android.util.Log;

import com.example.chessapp.game.logic.Game;

import java.util.HashMap;

import static com.example.chessapp.game.logic.BitBoards.*;

public class Engine {
    private final Game game;
    private final Zobrist zobrist;
    public static int globalDepth = 4;
    private final static int mateScore = 49000;
    private final static int infinity = 50000;
    private final Rating rating;
    public String bestMove;
    public int mate = -1;

    public HashMap<Long, TranspositionTable> table = new HashMap();

    public Engine(Game game, Zobrist zobrist) {
        this.game = game;
        this.zobrist = zobrist;
        rating = new Rating();
    }

    public int scoreMove(long[] boards, boolean[] castleFlags, boolean white, long hashKey) {
        return alphaBeta(-infinity, infinity, globalDepth - 1, boards, castleFlags, white, hashKey);
    }

    public int findBestMove(long[] boards, boolean[] castleFlags, boolean white, long hashKey) {
        bestMove = "";
        mate = -1;
        nodes = 0;
        test = 0;
        depth0 = 0;
        int score=0;
        // TODO iterative deepening
        for (int i = globalDepth; i <= globalDepth; i++) {
            nodes = 0;
            score = alphaBeta(-infinity, infinity, i, boards, castleFlags, white, hashKey);
            Log.d("test", "nodes: " + nodes);
        }
        if (Math.abs(score) >= mateScore - globalDepth) {
            mate = (mateScore - Math.abs(score)) / 2;
        }

        Log.d("test", "size: " + table.size());
//        Log.d("test", "depth0: " + depth0);
//        Log.d("test", "test: " + test);
        return score;
    }

    int nodes = 0;
    int depth0 = 0;
    int test = 0;

    private int alphaBeta(int alpha, int beta, int depth, long[] boards, boolean[] castleFlags,
                          boolean white, long hashKey) {
        int hashFlag = hash_alpha;
        int score;
        if (table.containsKey(hashKey)) {
            TranspositionTable tt = table.get(hashKey);
            Integer val = tt.readEntry(alpha, beta, depth);
            if (val != null) {
                if (depth == globalDepth) {
                    this.bestMove = tt.best;
                }
                return val;
            } else {
                test++;
            }
        }

        nodes++;
        if (depth == 0) {
            depth0++;
            costam = 0;
            score = quiescence(alpha, beta, boards, castleFlags, white);
//            Log.d("test", costam+"");
            nodes += costam;
            return score;
        }

        int ply = globalDepth - depth;
        String moves = game.possibleMoves(white, boards, castleFlags);
        moves = sortMoves(moves, boards, white, ply);
        int legalMoves = 0;
        String bestMove = "";
        int oldAlpha = alpha;

        for (int i = 0; i < moves.length(); i += 4) {
            String move = moves.substring(i, i + 4);
            long newHashKey = zobrist.hashPiece(hashKey, move, boards, castleFlags, white);
            long[] nextBoards = game.makeMove(move, boards);

            legalMoves++;
            boolean[] nextFlags = game.updateCastling(move, boards, castleFlags);
            score = -alphaBeta(-beta, -alpha, depth - 1, nextBoards, nextFlags, !white, newHashKey);

            if (score > alpha) {
                if (score >= beta) {
                    TranspositionTable tt = new TranspositionTable(depth, hash_beta, move, score);
                    table.put(hashKey, tt);

                    // TODO
                    // rating.killerMoves[1][ply] = rating.killerMoves[0][ply];
//                    rating.killerMoves[0][ply] = move;

                    return beta;
                }
                hashFlag = hash_exact;
                alpha = score;
                bestMove = move;

            }
        }

        if (legalMoves == 0) {
            if (!game.kingSafe(white, boards)) {
                return -mateScore + ply;
            } else {
                return 0;
            }
        }

        if (depth == globalDepth && alpha != oldAlpha) {
            this.bestMove = bestMove;
        }

        TranspositionTable tt = new TranspositionTable(depth, hashFlag, bestMove, alpha);
        table.put(hashKey, tt);
        return alpha;
    }

    public String sortMoves(String moves, long[] boards, boolean white, int ply) {
        int[] moveScores = new int[moves.length() / 4];

        for (int i = 0; i < moves.length(); i += 4) {
            String move = moves.substring(i, i + 4);
            moveScores[i / 4] = rating.scoreMove(move, boards, white, ply);
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
//        costam++;
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
