package com.example.chessapp.game.logic.engine;

import static com.example.chessapp.game.type.BitBoards.BB;
import static com.example.chessapp.game.type.BitBoards.BK;
import static com.example.chessapp.game.type.BitBoards.BP;
import static com.example.chessapp.game.type.BitBoards.BQ;
import static com.example.chessapp.game.type.BitBoards.BR;
import static com.example.chessapp.game.type.BitBoards.WB;
import static com.example.chessapp.game.type.BitBoards.WK;
import static com.example.chessapp.game.type.BitBoards.WP;
import static com.example.chessapp.game.type.BitBoards.WQ;
import static com.example.chessapp.game.type.BitBoards.WR;
import static com.example.chessapp.game.type.BitBoards.startPiece;
import static com.example.chessapp.game.type.BitBoards.targetPiece;
import static com.example.chessapp.game.type.BitBoards.targetSquare;
import static com.example.chessapp.game.type.MoveType.EN_PASSANT;

import com.example.chessapp.game.logic.MoveGenerator;
import com.example.chessapp.game.type.Move;

public class Rating {

    private static final long[] ISOLATED_PAWN_MASKS;
    private static final long[][] WHITE_PASSED_PAWN_MASKS;
    private static final long[][] BLACK_PASSED_PAWN_MASKS;

    static {
        ISOLATED_PAWN_MASKS = new long[8];
        for (int i = 0; i < 8; i++) {
            long mask = 0L;
            if (i > 0)
                mask |= MoveGenerator.COLUMN_MASKS[i - 1];
            if (i < 7)
                mask |= MoveGenerator.COLUMN_MASKS[i + 1];
            ISOLATED_PAWN_MASKS[i] = mask;
        }

        WHITE_PASSED_PAWN_MASKS = new long[8][8];
        BLACK_PASSED_PAWN_MASKS = new long[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                long mask = ISOLATED_PAWN_MASKS[j] | MoveGenerator.COLUMN_MASKS[j];
                for (int k = i; k < 8; k++)
                    mask &= ~MoveGenerator.ROW_MASKS[k];
                WHITE_PASSED_PAWN_MASKS[i][j] = mask;

                mask = ISOLATED_PAWN_MASKS[j] | MoveGenerator.COLUMN_MASKS[j];
                for (int k = 0; k <= i; k++)
                    mask &= ~MoveGenerator.ROW_MASKS[k];
                BLACK_PASSED_PAWN_MASKS[i][j] = mask;
            }
        }
    }

    public int scoreMove(Move move, long[] boards, long opponentPieces, int ply) {
        if (move.type == EN_PASSANT)
            return MVV_LVA[WP][BP];

        if (MoveGenerator.captureMove(move, opponentPieces)) {
            int[] cords = MoveGenerator.getPieces(move, boards);
            return MVV_LVA[cords[startPiece]][cords[targetPiece]] + 10_000;
        } else {
            if (killerMoves[0][ply] != null && killerMoves[0][ply].equals(move)) {
                return 9000;
            } else if (killerMoves[1][ply] != null && killerMoves[1][ply].equals(move)) {
                return 8000;
            } else {
                int[] cords = MoveGenerator.getPieces(move, boards);
                return historyMoves[cords[startPiece]][cords[targetSquare]];
            }
        }

    }

    public int evaluate(long[] boards, boolean white) {
        int score = 0;

        // white
        for (int i = 0; i < 6; i++) {
            long board = boards[i];
            long piece = board & -board; // &(board-1)
            while (piece != 0) {
                score += PIECE_SCORES[i];
                int j = Long.numberOfTrailingZeros(piece);
                if (i != WQ) // not queen
                    score += PIECE_POSITION_SCORES[i][j];

                if (i == WP)
                    score += scorePawnStructure(j, boards[WP], boards[BP], true);
                else if (i == WR)
                    score += scoreOpenPosition(boards[WP], boards[BP], j);
                else if (i == WK) {
                    // penalty for open king
                    score -= scoreOpenPosition(boards[WP], boards[BP], j);
                    score += Long.bitCount(MoveGenerator.getKingMoves(boards[WK]) & MoveGenerator.getMyPieces(true, boards))
                            * KING_SHIELD_BONUS;
                }

                board &= ~piece;
                piece = board & -board;
            }
        }

        // black
        for (int i = 6; i < 12; i++) {
            long board = boards[i];
            long piece = board & -board; // &(board-1)
            while (piece != 0) {
                score += PIECE_SCORES[i];
                int j = Long.numberOfTrailingZeros(piece);
                if (i != BQ) // not queen
                    score -= PIECE_POSITION_SCORES[i - 6][mirror(j)];

                if (i == BP)
                    score -= scorePawnStructure(j, boards[BP], boards[WP], false);
                else if (i == BR)
                    score -= scoreOpenPosition(boards[BP], boards[WP], j);
                else if (i == BK) {
                    // penalty for open king
                    score += scoreOpenPosition(boards[BP], boards[WP], j);
                    score -= Long.bitCount(MoveGenerator.getKingMoves(boards[BK]) & MoveGenerator.getMyPieces(false, boards))
                            * KING_SHIELD_BONUS;
                }

                board &= ~piece;
                piece = board & -board;
            }
        }

        // score for attacked squares
        score += Long.bitCount(MoveGenerator.getAttackedSquares(boards, true));
        score -= Long.bitCount(MoveGenerator.getAttackedSquares(boards, false));

        // bishop pair
        if (Long.bitCount(boards[WB]) > 1)
            score += BISHOP_PAIR_BONUS;
        if (Long.bitCount(boards[BB]) > 1)
            score -= BISHOP_PAIR_BONUS;

        return white ? score : -score;
    }

    private int scoreOpenPosition(long pawns, long opponentPawns, int location) {
        int score = 0;
        int col = location % 8;
        if ((pawns & MoveGenerator.COLUMN_MASKS[col]) == 0)
            score += SEMI_OPEN_FILE_SCORE;
        if (((pawns | opponentPawns) & MoveGenerator.COLUMN_MASKS[col]) == 0)
            score += OPEN_FILE_SCORE;

        return score;
    }

    private int scorePawnStructure(int location, long pawns, long opponentPawns, boolean white) {
        int score = 0;
        int row = location / 8, col = location % 8;

        // doubled
        int doublePawns = Long.bitCount(pawns & MoveGenerator.COLUMN_MASKS[col]);
        if (doublePawns > 1)
            score += doublePawns * DOUBLE_PAWN_PENALTY;

        // isolated
        if ((pawns & ISOLATED_PAWN_MASKS[col]) == 0) {
            score += ISOLATED_PAWN_PENALTY;
        }

        // passed
        long[][] mask = white ? WHITE_PASSED_PAWN_MASKS : BLACK_PASSED_PAWN_MASKS;
        if ((opponentPawns & mask[row][col]) == 0) {
            score += PASSED_PAWN_BONUS[white ? row : 7 - row];
        }

        return score;
    }

    private int mirror(int position) {
        return 63 - position;
    }

    /*
        (Victims) Pawn Knight Bishop   Rook  Queen   King
      (Attackers)
            Pawn   105    205    305    405    505    605
          Knight   104    204    304    404    504    604
          Bishop   103    203    303    403    503    603
            Rook   102    202    302    402    502    602
           Queen   101    201    301    401    501    601
            King   100    200    300    400    500    600
    */

    // [attacker][victim]
    private static final int[][] MVV_LVA = {
            {105, 205, 305, 405, 505, 605, 105, 205, 305, 405, 505, 605},
            {104, 204, 304, 404, 504, 604, 104, 204, 304, 404, 504, 604},
            {103, 203, 303, 403, 503, 603, 103, 203, 303, 403, 503, 603},
            {102, 202, 302, 402, 502, 602, 102, 202, 302, 402, 502, 602},
            {101, 201, 301, 401, 501, 601, 101, 201, 301, 401, 501, 601},
            {100, 200, 300, 400, 500, 600, 100, 200, 300, 400, 500, 600},
            {105, 205, 305, 405, 505, 605, 105, 205, 305, 405, 505, 605},
            {104, 204, 304, 404, 504, 604, 104, 204, 304, 404, 504, 604},
            {103, 203, 303, 403, 503, 603, 103, 203, 303, 403, 503, 603},
            {102, 202, 302, 402, 502, 602, 102, 202, 302, 402, 502, 602},
            {101, 201, 301, 401, 501, 601, 101, 201, 301, 401, 501, 601},
            {100, 200, 300, 400, 500, 600, 100, 200, 300, 400, 500, 600}
    };

    // [id][ply]
    public Move[][] killerMoves;
    // [piece][square]
    public int[][] historyMoves;

    // pawn positional score
    private static final int[] PAWN_SCORE =
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
    private static final int[] KNIGHT_SCORE =
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
    private static final int[] BISHOP_SCORE =
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
    private static final int[] ROOK_SCORE =
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
    private static final int[] KING_SCORE =
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

    private static final int[] PIECE_SCORES = {
            100, 300, 350, 500, 900, 10_000,
            -100, -300, -350, -500, -900, -10_000
    };

    private static final int[][] PIECE_POSITION_SCORES = {
            PAWN_SCORE,
            KNIGHT_SCORE,
            BISHOP_SCORE,
            ROOK_SCORE,
            {},
            KING_SCORE
    };

    private final static int DOUBLE_PAWN_PENALTY = -10;
    private final static int ISOLATED_PAWN_PENALTY = -10;
    private final static int[] PASSED_PAWN_BONUS = {0, 10, 30, 50, 75, 150, 200};

    private final static int SEMI_OPEN_FILE_SCORE = 10;
    private final static int OPEN_FILE_SCORE = 15;

    private final static int KING_SHIELD_BONUS = 5;

    private final static int BISHOP_PAIR_BONUS = 20;
}
