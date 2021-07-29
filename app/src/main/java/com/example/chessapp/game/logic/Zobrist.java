package com.example.chessapp.game.logic;

import java.security.SecureRandom;

public class Zobrist {
    private final SecureRandom random;
    private long[][] pieceKeys = new long[13][64];
    private long[] castleKeys = new long[4];
    private long blackMove;

    public Zobrist() {
        random = new SecureRandom();
        initRandomKeys();
    }

//    public long hashPiece(int hashKey, ) {
//
//    }

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
                    break;
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
