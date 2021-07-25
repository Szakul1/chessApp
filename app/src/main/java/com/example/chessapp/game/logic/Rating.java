package com.example.chessapp.game.logic;

import static com.example.chessapp.game.logic.BitBoards.*;

public class Rating {

    public int scoreMove(String move, long[] boards, boolean white) {
        long opponentPieces = Game.getMyPieces(!white, boards);
        if (move.charAt(3) == 'E')
            return mvv_lva[WP][BP];
        int startPieces = 0;
        int targetPiece = 0;
        if (captureMove(move, opponentPieces)) {
            for (int i = 0; i < boards.length - 1; i++) {
                int start = Game.getValFromString(move, 0) * 8 + Game.getValFromString(move, 1);
                int target = Game.getValFromString(move, 2) * 8 + Game.getValFromString(move, 3);
                if ((boards[i] & (1L << start)) != 0) {
                    startPieces = i;
                } else if ((boards[i] & (1L << target)) != 0) {
                    targetPiece = i;
                }
            }
            return mvv_lva[startPieces][targetPiece];
        }
        // TODO quiet move

        return 0;
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

    public boolean captureMove(String move, long opponentPieces) {
        // TODO castle
        if (move.charAt(3) != 'E') {
            if (!Character.isDigit(move.charAt(3)))
                return false;
            int position = Game.getValFromString(move, 2) * 8 + Game.getValFromString(move, 3);
            // not capture
            return ((1L << position) & opponentPieces) != 0;
        }
        return true;
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
