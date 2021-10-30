package com.example.chessapp.game.logic.engine;

import com.example.chessapp.game.logic.MoveGenerator;
import com.example.chessapp.game.type.Move;

import java.util.List;

public class Engine {
    public static int globalDepth = 5;
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
        int score;
        long start = System.currentTimeMillis();
        score = alphaBeta(-infinity, infinity, globalDepth, boards, castleFlags, white);
        long end = System.currentTimeMillis();
        System.out.println("time: " + (end - start));
        if (Math.abs(score) >= mateScore - globalDepth) {
            mate = (mateScore - Math.abs(score)) / 2;
        }
        System.out.println("nodes: " + nodes);
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
        long opponentPieces = MoveGenerator.getMyPieces(!white, boards);
        sortMoves(moves, boards, white, opponentPieces, ply);
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
                    if (!MoveGenerator.captureMove(move, opponentPieces)) {
                        rating.killerMoves[1][ply] = rating.killerMoves[0][ply];
                        rating.killerMoves[0][ply] = move;
                    }

                    return beta;
                }
                MoveGenerator.getPieces(move, boards);
                rating.historyMoves[MoveGenerator.startPiece][MoveGenerator.targetSquare] += depth;

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

    private void sortMoves(List<Move> moves, long[] boards, boolean white, long opponentPieces, int ply) {
        int[] moveScores = new int[moves.size()];

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            moveScores[i] = rating.scoreMove(move, boards, white, opponentPieces, ply);
        }

        quickSort(moves, moveScores, 0, moves.size() - 1);
    }

    private void quickSort(List<Move> moves, int[] scores, int start, int end) {
        if (start < end) {
            int pivot = partition(moves, scores, start, end);
            quickSort(moves, scores, start, pivot - 1);
            quickSort(moves, scores, pivot + 1, end);
        }
    }

    // sorting in descending order
    private int partition(List<Move> moves, int[] scores, int start, int end) {
        int pivot = scores[start];
        int greaterIndex = start;

        for (int i = start + 1; i <= end; i++) {
            if (scores[i] > pivot) {
                greaterIndex++;
                swap(moves, scores, greaterIndex, i);
            }
        }
        swap(moves, scores, greaterIndex, start);
        return greaterIndex;
    }

    private void swap(List<Move> moves, int[] scores, int i1, int i2) {
        Move tempMove = moves.get(i1);
        moves.set(i1, moves.get(i2));
        moves.set(i2, tempMove);

        int tempScore = scores[i1];
        scores[i1] = scores[i2];
        scores[i2] = tempScore;
    }
}
