package com.example.chessapp.game.type;

import static com.example.chessapp.game.type.MoveType.CASTLE;
import static com.example.chessapp.game.type.MoveType.PROMOTION;

import androidx.annotation.NonNull;

public class Move {
    public int startRow;
    public int startCol;
    public int endRow;
    public int endCol;
    public MoveType type;
    public char capturePiece = ' ';

    public int rookStartCol;
    public int rookEndCol;
    public char promotionPiece;

    // NORMAL
    public Move(int startRow, int startCol, int endRow, int endCol, MoveType type) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.type = type;
    }

    // PROMOTION
    public Move(int startRow, int startCol, int endRow, int endCol, MoveType type, char promotionPiece) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.type = type;
        this.promotionPiece = promotionPiece;
    }

    // CASTLE
    public Move(int startRow, int startCol, int endRow, int endCol, MoveType type, int rookStartCol, int rookEndCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.type = type;
        this.rookStartCol = rookStartCol;
        this.rookEndCol = rookEndCol;
    }

    @NonNull
    @Override
    public String toString() {
        String moveString = "";
        if (type == CASTLE) {
            moveString = rookEndCol == 0 ? "0-0-0" : "0-0";
        } else {
            moveString += (char) ('a' + startCol);
            moveString += 8 - startRow;
            moveString += capturePiece == ' ' ? " ➝ " : " × ";
            moveString += (char) ('a' + endCol);
            moveString += 8 - endRow;
        }
        if (type == PROMOTION) {
            moveString += promotionPiece;
        }
        return moveString;
    }
}
