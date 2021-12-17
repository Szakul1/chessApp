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

import com.example.chessapp.game.logic.MoveGenerator;
import com.example.chessapp.game.logic.engine.Zobrist;
import com.example.chessapp.game.type.Move;

import org.junit.Test;

public class ZobristTest {

    private static final char[][] startingBoard = {
            {'r', ' ', ' ', ' ', 'k', ' ', ' ', 'r'},
            {'p', ' ', 'p', 'p', 'q', 'p', 'b', ' '},
            {'b', 'n', ' ', ' ', 'p', 'n', 'p', ' '},
            {' ', ' ', ' ', 'P', 'N', ' ', ' ', ' '},
            {' ', 'p', ' ', ' ', 'P', ' ', ' ', ' '},
            {' ', ' ', 'N', ' ', ' ', 'Q', ' ', 'p'},
            {'P', 'P', 'P', 'B', 'B', 'P', 'P', 'P'},
            {'R', ' ', ' ', ' ', 'K', ' ', ' ', 'R'}};

    private static final boolean[] castleFlags = {true, true, true, true};

    @Test
    public void hashMoveTest() {
        // given
        Zobrist zobrist = new Zobrist();

        long[] boards = arrayToBitboards(startingBoard);
        long hashKey = zobrist.hashPosition(boards, castleFlags, true);

        Move move1 = new Move(7, 0 , 7, 1, NORMAL);
        Move move2 = new Move(1, 2, 3, 2, NORMAL);

        // when
        hashKey = zobrist.hashMove(hashKey, move1, boards, castleFlags, true);

        boolean[] castle = MoveGenerator.updateCastling(move1, boards, castleFlags);
        boards = MoveGenerator.makeMove(move1, boards);

        hashKey = zobrist.hashMove(hashKey, move2, boards, castle, false);

        castle = MoveGenerator.updateCastling(move2, boards, castle);
        boards = MoveGenerator.makeMove(move2, boards);

        long endHashKey = zobrist.hashPosition(boards, castle, true);

        // then
        assertEquals(endHashKey, hashKey);
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
