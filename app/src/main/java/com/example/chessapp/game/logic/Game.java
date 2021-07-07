package com.example.chessapp.game.logic;

import android.util.Log;

public class Game {
    // r - rook, k - knight, b - bishop, q - queen, a - king p - pawn
    // capital for white lower for black
    public char[][] chessBoard = {
            {'r', 'k', 'b', 'q', 'a', 'b', 'k', 'r'},
            {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
            {'R', 'K', 'B', 'Q', 'A', 'B', 'K', 'R'}};

    private static int kingPosWhite = 60, kingPosBlack = 4;

    public boolean checkMove(String move) {
        Log.d("test", possibleMoves() + "move:" + move);
        if (possibleMoves().contains(move)) {
            makeMove(Character.getNumericValue(move.charAt(0)),
                    Character.getNumericValue(move.charAt(1)),
                    Character.getNumericValue(move.charAt(2)),
                    Character.getNumericValue(move.charAt(3)));
            return true;
        }
        return false;
    }

    private void makeMove(int row, int col, int newRow, int newCol) {
        // TODO promotion
        Log.d("test", "move");
        chessBoard[newRow][newCol] = chessBoard[row][col];
        chessBoard[row][col] = ' ';
    }

    private void undoMove(int row, int col, int newRow, int newCol) {
        // TODO promotion
        Log.d("test", "move");
        chessBoard[row][col] = chessBoard[newRow][newCol];
        chessBoard[newRow][newCol] = ' ';
    }

    public String possibleMoves() {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            checkMoveForPiece(i, list);
        }
        return list.toString(); // x1, y1, x2, y2, captured piece;
    }

    public void checkMoveForPiece(int position, StringBuilder list) {
        switch (chessBoard[position / 8][position % 8]) {
            case 'P':
                list.append(possibleP(position));
                break;
            case 'R':
                list.append(possibleR(position));
                break;
            case 'K':
                list.append(possibleK(position));
                break;
            case 'B':
                list.append(possibleB(position));
                break;
            case 'Q':
                list.append(possibleQ(position));
                break;
            case 'A':
                list.append(possibleA(position));
                break;
        }
    }

    public String possibleP(int position) {
        StringBuilder list = new StringBuilder();
        int row = position / 8, col = position % 8;
        for (int i = -1; i <= 1; i += 2) { // capture and promotion
            int checkRow = row - 1, checkCol = col + i;
            if (checkBounds(checkRow, checkCol) &&
                    Character.isLowerCase(chessBoard[row - 1][col + i]))
                if (position >= 16) { // No promotion
                    checkSafety(row, col, checkRow, checkCol, list, 'P');
                } else { // promotion
                    checkPromotion(row, col, checkRow, checkCol, list);
                }
        }
        if (' ' == chessBoard[row - 1][col]) {
            if (position >= 16) { // move one up
                checkSafety(row, col, row - 1, col, list, 'P');
            } else { // promotion and no capture
                checkPromotion(row, col, row - 1, col, list);
            }
            if (position >= 48 && ' ' == chessBoard[row - 2][col]) { // move two up
                checkSafety(row, col, row - 2, col, list, 'P');
            }
        }
        return list.toString();
    }

    public void checkPromotion(int row, int col, int checkRow, int checkCol, StringBuilder list) {
        Character[] promotion = {'Q', 'R', 'B', 'K'};
        for (Character piece : promotion) {
            char oldPiece = chessBoard[checkRow][checkCol];
            chessBoard[row][col] = ' ';
            chessBoard[checkRow][checkCol] = piece;
            if (kingSafe()) {
                list.append(col).append(checkCol).append(oldPiece)
                        .append(piece).append('U');
            }
            chessBoard[row][col] = piece;
            chessBoard[checkRow][checkCol] = oldPiece;
        }
    }

    public String possibleR(int position) {
        StringBuilder list = new StringBuilder();
        int row = position / 8, col = position % 8;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if ((i + j) % 2 != 0) {
                    checkMoves(row, col, i, j, list, 'R');
                }
            }
        }
        return list.toString();
    }

    public String possibleK(int position) {
        StringBuilder list = new StringBuilder();
        int row = position / 8, col = position % 8;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int checkRow = row + i, checkCol = col + j * 2;
                if (checkBounds(checkRow, checkCol) &&
                        (Character.isLowerCase(chessBoard[checkRow][checkCol]) ||
                                ' ' == chessBoard[checkRow][checkCol])) {
                    checkSafety(row, col, checkRow, checkCol, list, 'K');
                }
                checkRow = row + i * 2;
                checkCol = col + j;
                if (checkBounds(checkRow, checkCol) &&
                        (Character.isLowerCase(chessBoard[checkRow][checkCol]) ||
                                ' ' == chessBoard[checkRow][checkCol])) {
                    checkSafety(row, col, checkRow, checkCol, list, 'K');
                }
            }
        }
        return list.toString();
    }

    public String possibleB(int position) {
        StringBuilder list = new StringBuilder();
        int row = position / 8, col = position % 8;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                checkMoves(row, col, i, j, list, 'B');
            }
        }
        return list.toString();
    }

    public String possibleQ(int position) {
        StringBuilder list = new StringBuilder();
        int row = position / 8, col = position % 8;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 || j != 0) {
                    checkMoves(row, col, i, j, list, 'Q');
                }
            }
        }
        return list.toString();
    }

    private void checkMoves(int row, int col, int i, int j, StringBuilder list, char piece) {
        int temp = 1;
        int checkRow = row + temp * i, checkCol = col + temp * j;
        while (checkBounds(checkRow, checkCol) && ' ' == chessBoard[checkRow][checkCol]) {
            checkSafety(row, col, checkRow, checkCol, list, piece);
            temp++;
            checkRow = row + temp * i;
            checkCol = col + temp * j;
        }
        if (checkBounds(checkRow, checkCol) && Character.isLowerCase(chessBoard[checkRow][checkCol])) {
            checkSafety(row, col, checkRow, checkCol, list, piece);
        }
    }

    private void checkSafety(int row, int col, int checkRow, int checkCol, StringBuilder list,
                             char piece) {
        char oldPiece = chessBoard[checkRow][checkCol];
        chessBoard[row][col] = ' ';
        chessBoard[checkRow][checkCol] = piece;
        if (kingSafe()) {
            list.append(row).append(col).append(checkRow)
                    .append(checkCol).append(oldPiece);
        }
        chessBoard[row][col] = piece;
        chessBoard[checkRow][checkCol] = oldPiece;
    }

    public String possibleA(int position) {
        StringBuilder list = new StringBuilder();
        char oldPiece;
        int row = position / 8, col = position % 8;
        for (int i = 0; i < 9; i++) {
            int checkRow = row - 1 + i / 3, checkCol = col - 1 + i % 3;
            if (i != 4 && checkBounds(checkRow, checkCol)) {
                if (Character.isLowerCase(chessBoard[checkRow][checkCol]) ||
                        ' ' == (chessBoard[checkRow][checkCol])) {
                    oldPiece = chessBoard[checkRow][checkCol];
                    chessBoard[row][col] = ' ';
                    chessBoard[checkRow][checkCol] = 'A';
                    int kingTemp = kingPosWhite;
                    kingPosWhite = position + i / 3 * 8 + i % 3 - 9;
                    if (kingSafe()) {
                        list.append(row).append(col).append(checkRow).append(checkCol).append(oldPiece);
                    }
                    chessBoard[row][col] = 'A';
                    chessBoard[checkRow][checkCol] = oldPiece;
                    kingPosWhite = kingTemp;
                }
            }
        }
        // castling
        return list.toString();
    }

    private boolean kingSafe() {
        // bishop | queen
        int temp = 1;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int checkRow = kingPosWhite / 8 + temp * i, chekCol = kingPosWhite % 8 + temp * j;
                while (checkBounds(checkRow, chekCol) && ' ' == chessBoard[checkRow][chekCol]) {
                    temp++;
                    checkRow = kingPosWhite / 8 + temp * i;
                    chekCol = kingPosWhite % 8 + temp * j;
                }
                if (checkBounds(checkRow, chekCol) && ('b' == chessBoard[checkRow][chekCol] ||
                        'q' == chessBoard[checkRow][chekCol])) {
                    return false;
                }
                temp = 1;
            }
        }
        // rock | queen
        for (int i = -1; i <= 1; i += 2) {
            // column check
            int checkRow = kingPosWhite / 8 + temp, chekCol = kingPosWhite % 8 + temp * i;
            while (checkBounds(checkRow, chekCol) && ' ' == chessBoard[checkRow][chekCol]) {
                temp++;
                checkRow = kingPosWhite / 8 + temp;
                chekCol = kingPosWhite % 8 + temp * i;
            }
            if (checkBounds(checkRow, chekCol) && ('r' == chessBoard[checkRow][chekCol] ||
                    'q' == chessBoard[checkRow][chekCol])) {
                return false;
            }
            temp = 1;
            // row check
            checkRow = kingPosWhite / 8 + temp * i;
            chekCol = kingPosWhite % 8 + temp;
            while (checkBounds(checkRow, chekCol) && ' ' == chessBoard[checkRow][chekCol]) {
                temp++;
                checkRow = kingPosWhite / 8 + temp * i;
                chekCol = kingPosWhite % 8 + temp;
            }
            if (checkBounds(checkRow, chekCol) && ('r' == chessBoard[checkRow][chekCol] ||
                    'q' == chessBoard[checkRow][chekCol])) {
                return false;
            }
            temp = 1;
        }

        // knight
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int checkRow = kingPosWhite / 8 + i, chekCol = kingPosWhite % 8 + j * 2;
                if (checkBounds(checkRow, chekCol) && 'k' == chessBoard[checkRow][chekCol]) {
                    return false;
                }
                checkRow = kingPosWhite / 8 + i * 2;
                chekCol = kingPosWhite % 8 + j;
                if (checkBounds(checkRow, chekCol) && 'k' == chessBoard[checkRow][chekCol]) {
                    return false;
                }
            }
        }

        if (kingPosWhite >= 16) {
            // pawn
            int checkRow = kingPosWhite / 8 - 1, checkCol = kingPosWhite % 8 - 1;
            if (checkBounds(checkRow, checkCol) && 'p' == chessBoard[checkRow][checkCol])
                return false;
            checkRow = kingPosWhite / 8 + 1;
            checkCol = kingPosWhite % 8 + 1;
            if (checkBounds(checkRow, checkCol) && 'p' == chessBoard[checkRow][checkCol])
                return false;
            // king
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0)
                        continue;
                    checkRow = kingPosWhite / 8 + i;
                    checkCol = kingPosWhite % 8 + j;
                    if (checkBounds(checkRow, checkCol) && 'a' == chessBoard[checkRow][checkCol]) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean checkBounds(int row, int col) {
        return row >= 0 && col >= 0 && row < 8 && col < 8;
    }
}
