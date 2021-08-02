package com.example.chessapp.game.logic.engine;

import static com.example.chessapp.game.logic.BitBoards.*;
import static com.example.chessapp.game.logic.Game.getBoardFromChar;
import static com.example.chessapp.game.logic.Game.isDoublePush;
import static com.example.chessapp.game.logic.Game.updateCastling;
import static com.example.chessapp.game.logic.Move.*;

import android.util.Log;

import com.example.chessapp.game.logic.Move;

import java.security.SecureRandom;

public class Zobrist {
    private final SecureRandom random;
    private final long[][] pieceKeys = new long[13][64];
    private final long[] castleKeys = new long[4];
    private long blackMove;

    public Zobrist() {
        random = new SecureRandom();
        initRandomKeys();
    }

    public long hashPiece(long hashKey, String move, long[] boards, boolean[] castleFlags, boolean white) {
        Move parsedMove = parseMove(move);
        int start = parsedMove.startRow * 8 + parsedMove.startCol;
        int target = parsedMove.targetRow * 8 + parsedMove.targetCol;
        int startBoard = getBoard(boards, start);
        int targetBoard = getBoard(boards, target);
        hashKey ^= blackMove;
        hashKey ^= pieceKeys[startBoard][start];
        if (parsedMove.promotion) {
            hashKey ^= pieceKeys[getBoardFromChar(parsedMove.promotionPiece)][target];
        } else {
            hashKey ^= pieceKeys[startBoard][target];
        }
        if (targetBoard != -1) { // capture
            hashKey ^= pieceKeys[targetBoard][target];
        }

        int enPassantPos = parsedMove.startRow * 8 + parsedMove.targetCol;
        if (boards[EP] != 0) {
            int pos = Long.numberOfTrailingZeros(boards[EP]);
            hashKey ^= pieceKeys[EP][pos];
        }
        if (parsedMove.enPassant) {
            hashKey ^= pieceKeys[white ? BP : WP][enPassantPos];
        } else if (isDoublePush(move, boards[WP] | boards[BP])) { // hash double push history
            hashKey ^= pieceKeys[EP][target];
        }

        for (int i = 0; i < castleFlags.length; i++) { // remove castle flags
            if (castleFlags[i])
                hashKey ^= castleKeys[i];
        }
        castleFlags = updateCastling(move, boards, castleFlags);
        for (int i = 0; i < castleFlags.length; i++) { // add castle flags
            if (castleFlags[i])
                hashKey ^= castleKeys[i];
        }

        if (parsedMove.castle) { // hash castled rook
            int rook = white ? WR : BR;
            hashKey ^= pieceKeys[rook][parsedMove.startRow * 8 + parsedMove.rookStartCol];
            hashKey ^= pieceKeys[rook][parsedMove.startRow * 8 + parsedMove.rookTargetCol];
        }

        return hashKey;
    }

    private void initRandomKeys() {
        for (int board = 0; board < pieceKeys.length; board++) {
            for (int square = 0; square < pieceKeys[board].length; square++) {
                pieceKeys[board][square] = random.nextLong();
            }
        }

        for (int i = 0; i < castleKeys.length; i++) {
            castleKeys[i] = random.nextLong();
        }

        blackMove = random.nextLong();
    }

    public long generateHashKey(long[] boards, boolean[] castleFlags, boolean white) {
        long hashKey = 0L;

        for (int square = 0; square < 64; square++) {
            for (int board = 0; board < boards.length; board++) {
                if (((boards[board] >> square) & 1) == 1) {
                    hashKey ^= pieceKeys[board][square];
                }
            }
        }

        for (int i = 0; i < castleFlags.length; i++) {
            if (castleFlags[i]) {
                hashKey ^= castleKeys[i];
            }
        }

        if (!white) {
            hashKey ^= blackMove;
        }

        return hashKey;
    }
}
