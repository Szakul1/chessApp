package com.example.chessapp.game.logic.engine;

import static com.example.chessapp.game.type.BitBoards.EP;
import static com.example.chessapp.game.type.MoveType.PROMOTION;

import com.example.chessapp.game.logic.MoveGenerator;
import com.example.chessapp.game.type.Move;

import java.util.List;

public class Engine {
    private final static int MATE_SCORE = 49000;
    private final static int INFINITY = 50000;
    private static final int FULL_DEPTH_MOVES = 4;
    private static final int REDUCTION_LIMIT = 3;

    public static int globalDepth = 6;
    private final Rating rating;
    public Move bestMove;
    public int mate = -1;

    // Principal variation
    private Move[][] pvTable = new Move[globalDepth][globalDepth]; // table of pv
    private int currentDepth;
    private boolean followPv;
    private boolean scorePv;

    public Engine() {
        rating = new Rating();
    }

    public int scoreMove(long[] boards, boolean[] castleFlags, boolean white) {
        bestMove = null;
        return alphaBeta(-INFINITY, INFINITY, globalDepth - 1, boards, castleFlags, white);
    }

    public int findBestMove(long[] boards, boolean[] castleFlags, boolean white) {
        // clearing flags
        rating.killerMoves = new Move[2][globalDepth];
        rating.historyMoves = new int[12][64];
        pvTable = new Move[globalDepth][globalDepth];
        followPv = false;
        scorePv = false;

        mate = -1;
        nodes = 0;
        int score = 0;
        int alpha = -INFINITY;
        int beta = INFINITY;
        long start = System.currentTimeMillis();

        // iterative deepening
        for (currentDepth = 1; currentDepth <= globalDepth; currentDepth++) {
            followPv = true;

            score = alphaBeta(alpha, beta, currentDepth, boards, castleFlags, white);
            if (score <= alpha || score >= beta) {
                alpha = -INFINITY;
                beta = INFINITY;
                continue;
            }
            // narrow window
            alpha = score - 50;
            beta = score + 50;
        }

        long end = System.currentTimeMillis();
//        System.out.println("time: " + (end - start));
        bestMove = pvTable[0][0];
//        for (int i = 0; i < globalDepth; i++)
//            System.out.println(pvTable[0][i]);
        if (Math.abs(score) >= MATE_SCORE - globalDepth) {
            mate = (MATE_SCORE - Math.abs(score)) / 2;
        }
        System.out.println("nodes: " + nodes);
        return score;
    }

    int nodes = 0;

    private int alphaBeta(int alpha, int beta, int depth, long[] boards, boolean[] castleFlags,
                          boolean white) {
        int score;
        int ply = currentDepth - depth;

        nodes++;
        if (depth == 0) {
            return rating.evaluate(boards, white);
        }
        boolean kingSafe = MoveGenerator.kingSafe(white, boards);

        // null move
        if (depth >= 3 && kingSafe && ply != 0) {
            long enPassant = boards[EP]; // preserve en passant
            boards[EP] = 0L; // reset
            // reduce depth
            score = -alphaBeta(-beta, -beta + 1, depth - 1 - 2, boards, castleFlags, white); // give one more move
            boards[EP] = enPassant; // restore

            if (score >= beta)
                return beta;
        }

        List<Move> moves = MoveGenerator.possibleMoves(white, boards, castleFlags);

        if (followPv)
            enablePvScoring(moves, ply);

        long opponentPieces = MoveGenerator.getMyPieces(!white, boards);
        sortMoves(moves, boards, opponentPieces, ply);

        int searchedMoves = 0;
        for (Move move : moves) {
            long[] nextBoards = MoveGenerator.makeMove(move, boards);
            boolean[] nextFlags = MoveGenerator.updateCastling(move, boards, castleFlags);

            if (searchedMoves == 0) {
                score = -alphaBeta(-beta, -alpha, depth - 1, nextBoards, nextFlags, !white);
            } else { // late move reduction
                if (searchedMoves >= FULL_DEPTH_MOVES && depth >= REDUCTION_LIMIT && kingSafe &&
                        !MoveGenerator.captureMove(move, opponentPieces) && move.type != PROMOTION) // conditions for lmr
                    // reduce depth
                    score = -alphaBeta(-alpha - 1, -alpha, depth - 2, nextBoards, nextFlags, !white);
                else // make score > alpha true to make full depth search
                    score = alpha + 1;

                if (score > alpha) {
                    score = -alphaBeta(-alpha - 1, -alpha, depth - 1, nextBoards, nextFlags, !white);
                    if (score > alpha && score < beta)
                        score = -alphaBeta(-beta, -alpha, depth - 1, nextBoards, nextFlags, !white);
                }
            }


            searchedMoves++;

            if (score > alpha) {
                if (score >= beta) {
                    if (!MoveGenerator.captureMove(move, opponentPieces)) {
                        rating.killerMoves[1][ply] = rating.killerMoves[0][ply];
                        rating.killerMoves[0][ply] = move;
                    }

                    return beta;
                }
                if (!MoveGenerator.captureMove(move, opponentPieces)) {
                    MoveGenerator.getPieces(move, boards);
                    rating.historyMoves[MoveGenerator.startPiece][MoveGenerator.targetSquare] += depth;
                }

                alpha = score;

                // pv
                pvTable[ply][ply] = move;
                for (int nextPly = ply + 1; nextPly < currentDepth; nextPly++) {
                    pvTable[ply][nextPly] = pvTable[ply + 1][nextPly];
                }
            }
        }

        if (moves.isEmpty()) {
            if (!kingSafe) {
                return -MATE_SCORE + ply;
            } else {
                return 0;
            }
        }

        return alpha;
    }

    private void enablePvScoring(List<Move> moves, int ply) {
        followPv = false;
        for (Move move : moves) {
            if (move.equals(pvTable[0][ply])) {
                scorePv = true;
                followPv = true;
                break;
            }
        }
    }

    private void sortMoves(List<Move> moves, long[] boards, long opponentPieces, int ply) {
        int[] moveScores = new int[moves.size()];

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            moveScores[i] = scoreMove(move, boards, opponentPieces, ply);
        }

        quickSort(moves, moveScores, 0, moves.size() - 1);
    }

    private int scoreMove(Move move, long[] boards, long opponentPieces, int ply) {
        if (this.scorePv && move.equals(pvTable[0][ply])) { // pv scoring is enabled
            scorePv = false;
            return 20_000;
        }
        return rating.scoreMove(move, boards, opponentPieces, ply);
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
