package com.example.chessapp.game.logic.game;

import static com.example.chessapp.game.type.BitBoards.BB;
import static com.example.chessapp.game.type.BitBoards.BK;
import static com.example.chessapp.game.type.BitBoards.BN;
import static com.example.chessapp.game.type.BitBoards.BP;
import static com.example.chessapp.game.type.BitBoards.BQ;
import static com.example.chessapp.game.type.BitBoards.BR;
import static com.example.chessapp.game.type.BitBoards.WB;
import static com.example.chessapp.game.type.BitBoards.WK;
import static com.example.chessapp.game.type.BitBoards.WN;
import static com.example.chessapp.game.type.BitBoards.WP;
import static com.example.chessapp.game.type.BitBoards.WQ;
import static com.example.chessapp.game.type.BitBoards.WR;
import static org.junit.Assert.assertEquals;

import com.example.chessapp.game.logic.MoveGenerator;
import com.example.chessapp.game.type.Move;

import org.junit.Test;

import java.util.List;

/**
 * Perft testing with results based on: https://www.chessprogramming.org/Perft_Results
 */
public class PerftTest {

    private int perfCounter;
    private static final char[][] advancedBoard = {
            {'r', ' ', ' ', ' ', 'k', ' ', ' ', 'r'},
            {'p', ' ', 'p', 'p', 'q', 'p', 'b', ' '},
            {'b', 'n', ' ', ' ', 'p', 'n', 'p', ' '},
            {' ', ' ', ' ', 'P', 'N', ' ', ' ', ' '},
            {' ', 'p', ' ', ' ', 'P', ' ', ' ', ' '},
            {' ', ' ', 'N', ' ', ' ', 'Q', ' ', 'p'},
            {'P', 'P', 'P', 'B', 'B', 'P', 'P', 'P'},
            {'R', ' ', ' ', ' ', 'K', ' ', ' ', 'R'}};
    private static final char[][] startingBoard = {
            {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
            {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
            {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}};

    @Test
    public void startingPositionDepth2() {
        // given
        perfCounter = 0;
        long[] boards = arrayToBitboards(startingBoard);
        boolean[] castleFlags = {true, true, true, true};

        // when
        perft(2, boards, castleFlags, true);

        // then
        assertEquals(400, perfCounter);
    }

    @Test
    public void startingPositionDepth5() {
        // given
        perfCounter = 0;
        long[] boards = arrayToBitboards(startingBoard);
        boolean[] castleFlags = {true, true, true, true};

        // when
        perft(5, boards, castleFlags, true);

        // then
        assertEquals(4_865_609, perfCounter);
    }

    @Test
    public void advancedPositionDepth2() {
        // given
        perfCounter = 0;
        long[] boards = arrayToBitboards(advancedBoard);
        boolean[] castleFlags = {true, true, true, true};

        // when
        perft(2, boards, castleFlags, true);

        // then
        assertEquals(2039, perfCounter);
    }

    @Test
    public void advancedPositionDepth4() {
        // given
        perfCounter = 0;
        long[] boards = arrayToBitboards(advancedBoard);
        boolean[] castleFlags = {true, true, true, true};

        // when
        perft(4, boards, castleFlags, true);

        // then
        assertEquals(4_085_603, perfCounter);
    }

    private void perft(int depth, long[] boards, boolean[] castleFlags, boolean white) {
        if (depth > 0) {
            List<Move> moves = MoveGenerator.possibleMoves(white, boards, castleFlags);
            for (Move move : moves) {
                long[] nextBoards = MoveGenerator.makeMove(move, boards);
                boolean[] nextFlags = MoveGenerator.updateCastling(move, boards, castleFlags);

                if (depth == 1) {
                    perfCounter++;
                }
                perft(depth - 1, nextBoards, nextFlags, !white);
            }
        }
    }

    private long[] arrayToBitboards(char[][] chessboard) {
        long[] boards = new long[]{0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,};
        long binary = 1L;
        for (int i = 0; i < 64; i++) {
            int board = getBoardFromChar(chessboard[i / 8][i % 8]);
            if (board != -1) {
                boards[board] += binary;
            }
            binary = binary << 1;
        }
        return boards;
    }

    private static int getBoardFromChar(char piece) {
        switch (piece) {
            case 'P':
                return WP;
            case 'N':
                return WN;
            case 'B':
                return WB;
            case 'R':
                return WR;
            case 'Q':
                return WQ;
            case 'K':
                return WK;
            case 'p':
                return BP;
            case 'n':
                return BN;
            case 'b':
                return BB;
            case 'r':
                return BR;
            case 'q':
                return BQ;
            case 'k':
                return BK;
        }
        // empty
        return -1;
    }
}
