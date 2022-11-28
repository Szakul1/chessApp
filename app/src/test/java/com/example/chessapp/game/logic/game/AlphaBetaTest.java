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
import static com.example.chessapp.game.type.MoveType.NORMAL;
import static org.junit.Assert.assertEquals;

import com.example.chessapp.game.logic.engine.Engine;
import com.example.chessapp.game.type.Move;

import org.junit.Test;

public class AlphaBetaTest {

    private static final char[][] startingBoard = {
            {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
            {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
            {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}};
    private static final char[][] advancedBoard = {
            {'r', ' ', ' ', ' ', 'k', ' ', ' ', 'r'},
            {'p', ' ', 'p', 'p', 'q', 'p', 'b', ' '},
            {'b', 'n', ' ', ' ', 'p', 'n', 'p', ' '},
            {' ', ' ', ' ', 'P', 'N', ' ', ' ', ' '},
            {' ', 'p', ' ', ' ', 'P', ' ', ' ', ' '},
            {' ', ' ', 'N', ' ', ' ', 'Q', ' ', 'p'},
            {'P', 'P', 'P', 'B', 'B', 'P', 'P', 'P'},
            {'R', ' ', ' ', ' ', 'K', ' ', ' ', 'R'}};
    private static final char[][] mateIn2 = {
            {' ', ' ', ' ', ' ', 'k', ' ', ' ', ' '},
            {'Q', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', 'K', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}};
    private static final boolean[] castleFlags = {true, true, true, true};

    @Test
    public void testScoreAndMove() {
        // given
        Engine.globalDepth = 4;
        Engine engine = new Engine();
        long[] boards = arrayToBitboards(advancedBoard);
        long hashKey = engine.zobrist.hashPosition(boards, castleFlags, true);

        // when
        engine.findBestMove(boards, castleFlags, true, hashKey);

        // then
//        assertEquals(0, score);
        assertEquals(new Move(6, 4, 2, 0, NORMAL), engine.bestMove);
    }

    @Test
    public void mateIn2Test() {
        // given
        Engine.globalDepth = 8;
        Engine engine = new Engine();
        long[] boards = arrayToBitboards(mateIn2);
        long hashKey = engine.zobrist.hashPosition(boards, castleFlags, true);

        // when
        engine.findBestMove(boards, castleFlags, true, hashKey);

        // then
//        assertEquals(0, score);
        assertEquals(new Move(3, 4, 2, 4, NORMAL), engine.bestMove);
    }

    @Test
    public void testNodesAndTimeStartingPosition() {
        // given

        Engine.globalDepth = 8;
        Engine engine = new Engine();
        // no optimizations depth 5 - 239036 nodes, time: 193
        // sorting nodes: 147838 time: 250ms
        // quick sort nodes: 147173 time: 112ms
        // killer moves: nodes: 43404 time: 58ms
        // history moves: nodes: 30780 time: 57ms
        // pv sort: nodes: 27485 time: 42ms
        // aspiration window lmr: nodes: 6816 time: 31ms
        // null move: nodes: 5445 time: 23ms
        // narrow window: nodes: 4465 time: 21ms
        // beta cutoff last: nodes: 4963 time: 21ms
        // tt: nodes: 5700

        // depth 8 - nodes: 4742488 aspiration window
        // tt: nodes: 1843318
        // null move: 618931
        long[] boards = arrayToBitboards(startingBoard);
        long hashKey = engine.zobrist.hashPosition(boards, castleFlags, true);

        // when
        long time = 0;
        int iterations = 1;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            engine.findBestMove(boards, castleFlags, true, hashKey);
            long end = System.nanoTime();
            time += (end - start);
        }
        System.out.println("time: " + time / iterations / 1_000_000 + "ms");

        // then

    }

    @Test
    public void testNodesAndTimeAdvancedPosition() {
        // given
        Engine.globalDepth = 8;
        Engine engine = new Engine();
        // no optimizations depth 5 - nodes: 356944 time: 594ms
        // sorting moves nodes: 80501 time: 91ms
        // quick sort nodes: 82831 time: 88ms
        // killer moves: nodes: 82739 time: 103ms
        // history moves: nodes: 82680 time: 89ms
        // aspiration window lmr: nodes: 31220 time: 158ms
        // null move: nodes: 25907 time: 188ms
        // narrow window: nodes: 23691 time: 106ms
        // tt: nodes: 13983

        // depth 8 nodes: 6953851
        // tt: nodes: 2567063
        // null move: nodes: 913713
        long[] boards = arrayToBitboards(advancedBoard);
        long hashKey = engine.zobrist.hashPosition(boards, castleFlags, true);

        // when
        long time = 0;
        int iterations = 1;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            engine.findBestMove(boards, castleFlags, true, hashKey);
            long end = System.nanoTime();
            time += (end - start);
        }
        System.out.println("time: " + time / iterations / 1_000_000 + "ms");

        // then

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
