package com.example.chessapp.game.logic;

import static com.example.chessapp.game.logic.Game.getValFromString;
import static com.example.chessapp.game.logic.BitBoards.*;

public class Move {

    public int startRow, startCol, targetRow, targetCol;
    public int rookStartCol, rookTargetCol;
    public boolean castle, promotion, enPassant;
    public char promotionPiece;

    public Move() {
        castle = false;
        enPassant = false;
        promotion = false;
        promotionPiece = ' ';
    }

    public static Move parseMove(String move) {
        Move parsedMove = new Move();
        if (Character.isDigit(move.charAt(3))) {// 'regular' move
            parsedMove.startRow = getValFromString(move, 0);
            parsedMove.startCol = getValFromString(move, 1);
            parsedMove.targetRow = getValFromString(move, 2);
            parsedMove.targetCol = getValFromString(move, 3);
        } else if (move.charAt(3) == 'C') {
            parsedMove.castle = true;
            parsedMove.startCol = 4;
            if (move.charAt(1) == 'W') {
                parsedMove.startRow = 7;
                parsedMove.targetRow = 7;
            } else {
                parsedMove.startRow = 0;
                parsedMove.targetRow = 0;
            }
            if (move.charAt(2) == 'Q') {
                parsedMove.targetCol = 2;
                parsedMove.rookStartCol = 0;
                parsedMove.rookTargetCol = 3;
            } else {
                parsedMove.targetCol = 6;
                parsedMove.rookStartCol = 7;
                parsedMove.rookTargetCol = 5;
            }
        } else if (move.charAt(3) == 'P') {
            boolean white = Character.isUpperCase(move.charAt(2));
            parsedMove.promotion = true;
            parsedMove.promotionPiece = move.charAt(2);
            parsedMove.startRow = white ? 1 : 6;
            parsedMove.startCol = getValFromString(move, 0);
            parsedMove.targetRow = white ? 0 : 7;
            parsedMove.targetCol = getValFromString(move, 1);
        } else {
            boolean white = move.charAt(2) == 'W';
            parsedMove.enPassant = true;
            parsedMove.startRow = white ? 3 : 4;
            parsedMove.startCol = getValFromString(move, 0);
            parsedMove.targetRow = white ? 2 : 5;
            parsedMove.targetCol = getValFromString(move, 1);
        }
        return parsedMove;
    }

    public static int getBoard(long boards[], int pos) {
        for (int i = 0; i < boards.length - 1; i++) {
            if ((boards[i] & (1L << pos)) != 0) {
                return i;
            }
        }
        // error
        return -1;
    }
}
