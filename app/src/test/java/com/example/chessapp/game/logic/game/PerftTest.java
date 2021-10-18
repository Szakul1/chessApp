package com.example.chessapp.game.logic.game;

import static org.junit.Assert.assertEquals;

import com.example.chessapp.game.Move;
import com.example.chessapp.game.logic.Game;

import org.junit.Test;

import java.util.List;

/**
 * Perft testing with results based on: https://www.chessprogramming.org/Perft_Results
 */
public class PerftTest {

    private int perfCounter;
    private Game game;
    private static final char[][] advancedBoard = {
            {'r', ' ', ' ', ' ', 'k', ' ', ' ', 'r'},
            {'p', ' ', 'p', 'p', 'q', 'p', 'b', ' '},
            {'b', 'n', ' ', ' ', 'p', 'n', 'p', ' '},
            {' ', ' ', ' ', 'P', 'N', ' ', ' ', ' '},
            {' ', 'p', ' ', ' ', 'P', ' ', ' ', ' '},
            {' ', ' ', 'N', ' ', ' ', 'Q', ' ', 'p'},
            {'P', 'P', 'P', 'B', 'B', 'P', 'P', 'P'},
            {'R', ' ', ' ', ' ', 'K', ' ', ' ', 'R'}};

    @Test
    public void startingPositionDepth2() {
        // given
        game = new Game(null, true);
        perfCounter = 0;

        // when
        perft(2, game.getBoards(), game.getCastleFlags(), true);

        // then
        assertEquals(400, perfCounter);
    }

    @Test
    public void startingPositionDepth5() {
        // given
        game = new Game(null, true);
        perfCounter = 0;

        // when
        perft(5, game.getBoards(), game.getCastleFlags(), true);

        // then
        assertEquals(4_865_609, perfCounter);
    }

    @Test
    public void advancedPositionDepth2() {
        // given
        game = new Game(null, true);
        perfCounter = 0;

        long[] boards = arrayToBitboards();
        game.setBoards(boards);

        // when
        perft(2, game.getBoards(), game.getCastleFlags(), true);

        // then
        assertEquals(2039, perfCounter);
    }

    @Test
    public void advancedPositionDepth4() {
        // given
        game = new Game(null, true);
        perfCounter = 0;

        long[] boards = arrayToBitboards();
        game.setBoards(boards);

        // when
        perft(4, game.getBoards(), game.getCastleFlags(), true);

        // then
        assertEquals(4_085_603, perfCounter);
    }

    private void perft(int depth, long[] boards, boolean[] castleFlags, boolean white) {
        if (depth > 0) {
            List<Move> moves = game.possibleMoves(white, boards, castleFlags);
            for (Move move : moves) {
                long[] nextBoards = game.makeMove(move, boards);
                boolean[] nextFlags = game.updateCastling(move, boards, castleFlags);

                if (depth == 1) {
                    perfCounter++;
                }
                perft(depth - 1, nextBoards, nextFlags, !white);
            }
        }
    }

    private long[] arrayToBitboards() {
        long[] boards = new long[]{0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,};
        long binary = 1L;
        for (int i = 0; i < 64; i++) {
            int board = Game.getBoardFromChar(advancedBoard[i / 8][i % 8]);
            if (board != -1) {
                boards[board] += binary;
            }
            binary = binary << 1;
        }
        return boards;
    }
}
