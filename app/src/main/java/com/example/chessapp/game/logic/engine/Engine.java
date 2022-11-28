package com.example.chessapp.game.logic.engine;

import static com.example.chessapp.game.type.BitBoards.startPiece;
import static com.example.chessapp.game.type.BitBoards.targetSquare;
import static com.example.chessapp.game.type.HashFlag.ALPHA;
import static com.example.chessapp.game.type.HashFlag.BETA;
import static com.example.chessapp.game.type.HashFlag.EXACT;

import com.example.chessapp.game.logic.MoveGenerator;
import com.example.chessapp.game.type.HashFlag;
import com.example.chessapp.game.type.Move;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Engine {
    public final static int LOWER_MATE = 48_000;
    private final static int MATE_SCORE = 49_000;
    private final static int INFINITY = 50_000;

    private static final int MAX_PLY = 32;

    public static int globalDepth = 5;
    private final Rating rating;
    public Move bestMove;

    // Principal variation
    private Move[][] pvTable; // table of pv
    private boolean followPv;
    private boolean scorePv;
    private int currentDepth;

    // transposition table
    private final Map<Long, TranspositionTable> transpositionTable;
    public final Zobrist zobrist;

    public Engine() {
        rating = new Rating();
        transpositionTable = new HashMap<>();
        zobrist = new Zobrist();
    }

    public int scorePosition(long[] boards, boolean[] castleFlags, boolean white, long hashKey) {
        bestMove = null;
        return alphaBeta(-INFINITY, INFINITY, globalDepth - 1, boards, castleFlags, white, 0, hashKey);
    }


    /**
     * Finds best move for given position and scores it
     * Assigns best move to global variable bestMove
     * @param boards bitboards
     * @param castleFlags castle flags
     * @param white player to move
     * @param hashKey hash key of position
     * @return score of position
     */
    public int findBestMove(long[] boards, boolean[] castleFlags, boolean white, long hashKey) {
        // clearing flags
        rating.killerMoves = new Move[2][MAX_PLY];
        rating.historyMoves = new int[12][64];
        pvTable = new Move[MAX_PLY][MAX_PLY];
        followPv = false;
        scorePv = false;

        nodes = 0;
        int score = 0;
        int alpha = -INFINITY;
        int beta = INFINITY;
//        long start = System.currentTimeMillis();

        // iterative deepening
        for (currentDepth = 1; currentDepth <= globalDepth; currentDepth++) {
            followPv = true;

            score = alphaBeta(alpha, beta, currentDepth, boards, castleFlags, white, 0, hashKey);
            if (score <= alpha || score >= beta) {
                alpha = -INFINITY;
                beta = INFINITY;
                continue;
            }
            // narrow window
            alpha = score - 50;
            beta = score + 50;
        }

//        long end = System.currentTimeMillis();
//        System.out.println("time: " + (end - start));
        bestMove = pvTable[0][0];
//        for (int i = 0; i < globalDepth && pvTable[0][i] != null; i++)
//            System.out.println(pvTable[0][i]);
//
//        System.out.println("nodes: " + nodes);

        return score;
    }

    private int nodes = 0;

    /**
     * Alpha beta algorithm for finding best move in position
     * and scoring it
     * @param alpha alpha param
     * @param beta beta param
     * @param depth current depth
     * @param boards bitboards
     * @param castleFlags castle flags
     * @param white player to move
     * @param ply move counter
     * @param hashKey hash of current position
     * @return score of position
     */
    private int alphaBeta(int alpha, int beta, int depth, long[] boards, boolean[] castleFlags, boolean white, int ply,
                          long hashKey) {
        int score;
        HashFlag hashFlag = ALPHA;
        Move bestMove = null;

        // check if node is already stored in transposition table
        if (transpositionTable.containsKey(hashKey)) {
            TranspositionTable entry = transpositionTable.get(hashKey);
            Integer value = Objects.requireNonNull(entry).readEntry(alpha, beta, depth, ply);
            if (value != null) {
                if (ply == 0)
                    pvTable[0][0] = entry.move;
                return value;
            } else {
                bestMove = entry.move;
            }
        }

        nodes++;
        if (depth == 0) {
            return quiescence(alpha, beta, boards, castleFlags, white, ply);
        }
        if (ply > MAX_PLY - 1)
            return rating.evaluate(boards, white);

        boolean kingSafe = MoveGenerator.kingSafe(white, boards);
        if (!kingSafe)
            depth++;

        List<Move> moves = MoveGenerator.possibleMoves(white, boards, castleFlags);

        if (followPv)
            enablePvScoring(moves, ply);

        long opponentPieces = MoveGenerator.getMyPieces(!white, boards);
        int[] scores = scoreMoves(moves, boards, opponentPieces, ply, bestMove);

        for (int i = 0; i < moves.size(); i++) {
            pickMove(i, moves, scores);
            Move move = moves.get(i);
            long[] nextBoards = MoveGenerator.makeMove(move, boards);
            boolean[] nextFlags = MoveGenerator.updateCastling(move, boards, castleFlags);
            long newHashKey = zobrist.hashMove(hashKey, move, boards, castleFlags, white);

            score = -alphaBeta(-beta, -alpha, depth - 1, nextBoards, nextFlags, !white, ply + 1, newHashKey);

            if (score > alpha) {
                hashFlag = EXACT;

                saveHistoryMoves(move, boards, opponentPieces, depth);
                alpha = score;
                bestMove = move;

                if (score >= beta) {
                    transpositionTable.put(hashKey, new TranspositionTable(depth, BETA, beta, ply, move));

                    saveKillerMoves(move, opponentPieces, ply);
                    return beta;
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

        if (bestMove != null) {
            // pv
            pvTable[ply][ply] = bestMove;
            for (int nextPly = ply + 1; nextPly < currentDepth; nextPly++)
                pvTable[ply][nextPly] = pvTable[ply + 1][nextPly];
        }

        transpositionTable.put(hashKey, new TranspositionTable(depth, hashFlag, alpha, ply, bestMove));

        return alpha;
    }

    private int quiescence(int alpha, int beta, long[] boards, boolean[] castleFlags, boolean white, int ply) {
        int score = rating.evaluate(boards, white);
        if (ply > MAX_PLY - 1)
            return score;

        if (score >= beta) {
            return beta;
        }
        if (score > alpha) {
            alpha = score;
        }

        nodes++;
        List<Move> moves = MoveGenerator.possibleMoves(white, boards, castleFlags);

        long opponentPieces = MoveGenerator.getMyPieces(!white, boards);
        int[] scores = scoreMoves(moves, boards, opponentPieces, ply, null);

        for (int i = 0; i < moves.size(); i++) {
            pickMove(i, moves, scores);
            Move move = moves.get(i);

            if (!MoveGenerator.captureMove(move, opponentPieces)) // only captures
                continue;

            long[] nextBoards = MoveGenerator.makeMove(move, boards);
            boolean[] nextFlags = MoveGenerator.updateCastling(move, boards, castleFlags);

            score = -quiescence(-beta, -alpha, nextBoards, nextFlags, !white, ply + 1);

            if (score > alpha) {
                alpha = score;

                if (score >= beta) {
                    return beta;
                }
            }
        }

        return alpha;
    }

    private void saveHistoryMoves(Move move, long[] boards, long opponentPieces, int depth) {
        if (!MoveGenerator.captureMove(move, opponentPieces)) {
            int[] cords = MoveGenerator.getPieces(move, boards);
            rating.historyMoves[cords[startPiece]][cords[targetSquare]] += depth;
        }
    }

    private void saveKillerMoves(Move move, long opponentPieces, int ply) {
        if (!MoveGenerator.captureMove(move, opponentPieces)) {
            rating.killerMoves[1][ply] = rating.killerMoves[0][ply];
            rating.killerMoves[0][ply] = move;
        }
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

    private int[] scoreMoves(List<Move> moves, long[] boards, long opponentPieces, int ply, Move bestMove) {
        int[] moveScores = new int[moves.size()];

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            moveScores[i] = scorePosition(move, boards, opponentPieces, ply, bestMove);
        }

        return moveScores;
    }

    private void pickMove(int start, List<Move> moves, int[] scores) {
        int bestScore = -INFINITY;
        int bestIndex = 0;

        for (int i = start; i < moves.size(); i++) {
            if (scores[i] > bestScore) {
                bestScore = scores[i];
                bestIndex = i;
            }
        }
        swap(moves, scores, start, bestIndex);
    }

    private int scorePosition(Move move, long[] boards, long opponentPieces, int ply, Move bestMove) {
        if (move.equals(bestMove)) {
            return 30_000;
        }
        if (this.scorePv && move.equals(pvTable[0][ply])) { // pv scoring is enabled
            scorePv = false;
            return 20_000;
        }
        return rating.scoreMove(move, boards, opponentPieces, ply);
    }

    private void swap(List<Move> moves, int[] scores, int i1, int i2) {
        Move tempMove = moves.get(i1);
        moves.set(i1, moves.get(i2));
        moves.set(i2, tempMove);

        int tempScore = scores[i1];
        scores[i1] = scores[i2];
        scores[i2] = tempScore;
    }

    public static String isMate(int score) {
        String result = null;
        if (score > LOWER_MATE)
            result = "Mate in " + (MATE_SCORE - score);
        else if (score < -LOWER_MATE)
            result = "Mate in " + (MATE_SCORE + score);
        return result;
    }

    public static int adjustMateForAnalyze(int score) {
        if (score > LOWER_MATE)
            return score + 1;
        else if (score < -LOWER_MATE)
            return score - 1;
        return score;
    }
}
