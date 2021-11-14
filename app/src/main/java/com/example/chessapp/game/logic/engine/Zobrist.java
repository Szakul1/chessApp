package com.example.chessapp.game.logic.engine;

import static com.example.chessapp.game.type.BitBoards.BB;
import static com.example.chessapp.game.type.BitBoards.BN;
import static com.example.chessapp.game.type.BitBoards.BP;
import static com.example.chessapp.game.type.BitBoards.BR;
import static com.example.chessapp.game.type.BitBoards.EP;
import static com.example.chessapp.game.type.BitBoards.WB;
import static com.example.chessapp.game.type.BitBoards.WN;
import static com.example.chessapp.game.type.BitBoards.WP;
import static com.example.chessapp.game.type.BitBoards.WR;
import static com.example.chessapp.game.type.BitBoards.startPiece;
import static com.example.chessapp.game.type.BitBoards.targetPiece;
import static com.example.chessapp.game.type.MoveType.CASTLE;
import static com.example.chessapp.game.type.MoveType.EN_PASSANT;
import static com.example.chessapp.game.type.MoveType.PROMOTION;

import com.example.chessapp.game.logic.MoveGenerator;
import com.example.chessapp.game.type.Move;

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

    public long hashMove(long hashKey, Move move, long[] boards, boolean[] castleFlags, boolean white) {
        int start = move.startRow * 8 + move.startCol;
        int target = move.endRow * 8 + move.endCol;
        int[] cords = MoveGenerator.getPieces(move, boards);
        hashKey ^= blackMove; // hash turn
        hashKey ^= pieceKeys[cords[startPiece]][start]; // remove from start
        if (move.type == PROMOTION) {
            hashKey ^= pieceKeys[getPromotion(move.promotionPiece)][target]; // add promotion piece to end
        } else {
            hashKey ^= pieceKeys[cords[startPiece]][target]; // add piece to end
        }
        if (cords[targetPiece] != -1) { // capture
            hashKey ^= pieceKeys[cords[targetPiece]][target]; // remove capture from target
        }

        hashKey = removeEnPassant(hashKey, boards[EP]);
        int enPassantPos = move.startRow * 8 + move.endCol;
        if (move.type == EN_PASSANT) { // remove en passant capture pawn
            hashKey ^= pieceKeys[white ? BP : WP][enPassantPos];
        } else if (MoveGenerator.isDoublePush(move, boards[WP] | boards[BP])) { // hash double push history
            hashKey ^= pieceKeys[EP][target];
        }

        for (int i = 0; i < castleFlags.length; i++) { // remove castle flags
            if (castleFlags[i])
                hashKey ^= castleKeys[i];
        }
        castleFlags = MoveGenerator.updateCastling(move, boards, castleFlags);
        for (int i = 0; i < castleFlags.length; i++) { // add castle flags
            if (castleFlags[i])
                hashKey ^= castleKeys[i];
        }

        if (move.type == CASTLE) { // hash castled rook
            int rook = white ? WR : BR;
            hashKey ^= pieceKeys[rook][move.startRow * 8 + move.rookStartCol];
            hashKey ^= pieceKeys[rook][move.startRow * 8 + move.rookEndCol];
        }

        return hashKey;
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

    public long removeEnPassant(long hashKey, long enPassant) {
        if (enPassant != 0) { // remove en passant memory
            int pos = Long.numberOfTrailingZeros(enPassant);
            hashKey ^= pieceKeys[EP][pos];
        }
        return hashKey;
    }

    public long hashSide(long hashKey) {
        return hashKey ^ blackMove;
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

    private int getPromotion(char promotionPiece) {
        switch (promotionPiece) {
            case 'Q':
                return WP;
            case 'R':
                return WR;
            case 'B':
                return WB;
            case 'N':
                return WN;
            case 'q':
                return BP;
            case 'r':
                return BR;
            case 'b':
                return BB;
            case 'n':
                return BN;
        }
        return -1;
    }
}
