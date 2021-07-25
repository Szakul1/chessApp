package com.example.chessapp.game.logic;

import android.service.controls.Control;
import android.util.Log;

import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;

import static com.example.chessapp.game.logic.BitBoards.*;

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
            return quiescence(alpha, beta, boards, castleFlags, white);
        }

        String moves = game.possibleMoves(white, boards, castleFlags);
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

    public int quiescence(int alpha, int beta, long[] boards, boolean[] castleFlags, boolean white) {
        int score = evaluate(boards, white);

        if (score > alpha) {
            if (score >= beta) {
                return beta;
            }
            alpha = score;
        }

        String moves = game.possibleMoves(white, boards, castleFlags);

        // only captures
        long opponentPieces = game.getMyPieces(!white, boards);
        for (int i = 0; i < moves.length(); i += 4) {
            String move = moves.substring(i, i + 4);

            // TODO
            if (move.charAt(3) != 'E') {
                if (!Character.isDigit(move.charAt(3)))
                    continue;
                int position = Game.getValFromString(move, 2) * 8 + Game.getValFromString(move, 3);
                if (((1L << position) & opponentPieces) == 0) { // not capture
                    continue;
                }
            }

            long[] nextBoards = game.makeMove(move, boards);
            if (!game.kingSafe(white, nextBoards)) {
                continue;
            }

            score = -quiescence(-beta, -alpha, nextBoards, castleFlags, !white);

            if (score > alpha) {
                if (score >= beta) {
                    return beta;
                }
                alpha = score;
            }
        }

        return alpha;
    }

    public int evaluate(long[] boards, boolean white) {
        int score = 0;

        for (int i = 0; i < 6; i++) {
            long board = boards[i];
            long piece = board & -board; // &(board-1)
            while (piece != 0) {
                score += pieceScores[i];
                int j = Long.numberOfTrailingZeros(piece);
                if (i != 4)
                    score += piecePositionScores[i][j];
                board &= ~piece;
                piece = board & -board; // &(WP-1)
            }
        }

        for (int i = 6; i < 12; i++) {
            long board = boards[i];
            long piece = board & -board; // &(board-1)
            while (piece != 0) {
                score += pieceScores[i];
                int j = Long.numberOfTrailingZeros(piece);
                if (i != 10)
                    score -= piecePositionScores[i - 6][mirror(j)];
                board &= ~piece;
                piece = board & -board; // &(WP-1)
            }
        }

        return white ? score : -score;
    }

    private int mirror(int position) {
        return (7 - position / 8) * 8 + position % 8;
    }

    // pawn positional score
    private final int pawn_score[] =
            {
                    90, 90, 90, 90, 90, 90, 90, 90,
                    30, 30, 30, 40, 40, 30, 30, 30,
                    20, 20, 20, 30, 30, 30, 20, 20,
                    10, 10, 10, 20, 20, 10, 10, 10,
                    5, 5, 10, 20, 20, 5, 5, 5,
                    0, 0, 0, 5, 5, 0, 0, 0,
                    0, 0, 0, -10, -10, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0
            };

    // knight positional score
    private final int knight_score[] =
            {
                    -5, 0, 0, 0, 0, 0, 0, -5,
                    -5, 0, 0, 10, 10, 0, 0, -5,
                    -5, 5, 20, 20, 20, 20, 5, -5,
                    -5, 10, 20, 30, 30, 20, 10, -5,
                    -5, 10, 20, 30, 30, 20, 10, -5,
                    -5, 5, 20, 10, 10, 20, 5, -5,
                    -5, 0, 0, 0, 0, 0, 0, -5,
                    -5, -10, 0, 0, 0, 0, -10, -5
            };

    // bishop positional score
    private final int bishop_score[] =
            {
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 10, 10, 0, 0, 0,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 10, 0, 0, 0, 0, 10, 0,
                    0, 30, 0, 0, 0, 0, 30, 0,
                    0, 0, -10, 0, 0, -10, 0, 0

            };

    // rook positional score
    private final int rook_score[] =
            {
                    50, 50, 50, 50, 50, 50, 50, 50,
                    50, 50, 50, 50, 50, 50, 50, 50,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 0, 0, 20, 20, 0, 0, 0

            };

    // king positional score
    private final int king_score[] =
            {
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 5, 5, 5, 5, 0, 0,
                    0, 5, 5, 10, 10, 5, 5, 0,
                    0, 5, 10, 20, 20, 10, 5, 0,
                    0, 5, 10, 20, 20, 10, 5, 0,
                    0, 0, 5, 10, 10, 5, 0, 0,
                    0, 5, 5, -5, -5, 0, 5, 0,
                    0, 0, 5, 0, -15, 0, 10, 0
            };

    private int[] pieceScores = {
            100, 300, 350, 500, 900, 10_000,
            -100, -300, -350, -500, -900, -10_000
    };

    private final int[][] piecePositionScores = {
            pawn_score,
            knight_score,
            bishop_score,
            rook_score,
            {},
            king_score
    };
}
