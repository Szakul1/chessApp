package com.example.chessapp.game.logic.engine;

import static com.example.chessapp.game.type.BitBoards.BP;
import static com.example.chessapp.game.type.BitBoards.WP;
import static com.example.chessapp.game.type.MoveType.EN_PASSANT;

import com.example.chessapp.game.logic.MoveGenerator;
import com.example.chessapp.game.type.Move;

public class Rating {

    public int scoreMove(Move move, long[] boards, boolean white, long opponentPieces, int ply) {
        if (move.type == EN_PASSANT)
            return mvv_lva[WP][BP];

        if (MoveGenerator.captureMove(move, opponentPieces)) {
            MoveGenerator.getPieces(move, boards);
            return mvv_lva[MoveGenerator.startPiece][MoveGenerator.targetPiece] + 10000;
        }
        else {
            if (killerMoves[0][ply] != null && killerMoves[0][ply].equals(move)) {
                return 9000;
            } else if (killerMoves[1][ply] != null && killerMoves[1][ply].equals(move)) {
                return 8000;
            } else {
                MoveGenerator.getPieces(move, boards);
                return historyMoves[MoveGenerator.startPiece][MoveGenerator.targetSquare];
            }
        }

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
    private final int[][] mvv_lva = {
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
    Move[][] killerMoves = new Move[2][64];
    // [piece][square]
    int[][] historyMoves = new int[12][64];

    // pawn positional score
    private final int[] pawn_score =
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
    private final int[] knight_score =
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
    private final int[] bishop_score =
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
    private final int[] rook_score =
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
    private final int[] king_score =
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

    private final int[] pieceScores = {
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
