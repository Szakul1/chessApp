package com.example.chessapp.game.logic;

import android.util.Log;

import androidx.appcompat.widget.WithHint;

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
    private final Engine engine;

    public Game(boolean playerWhite) {
        engine = new Engine(this);
        if (!playerWhite) {
            makeMove(engine.alphaBeta(Engine.globalDepth, Integer.MAX_VALUE, Integer.MIN_VALUE,
                    "", -1));
            flipBoard();
        }
    }

    public void flipBoard() {
        char temp;
        for (int i = 0; i < 32; i++) {
            int row = i / 8, col = i % 8;
            if (Character.isUpperCase(chessBoard[row][col])) {
                temp = Character.toLowerCase(chessBoard[row][col]);
            } else {
                temp = Character.toUpperCase(chessBoard[row][col]);
            }
            if (Character.isUpperCase(chessBoard[7 - row][7 - col])) {
                chessBoard[row][col] = Character.toLowerCase(chessBoard[7 - row][7 - col]);
            } else {
                chessBoard[row][col] = Character.toUpperCase(chessBoard[7 - row][7 - col]);
            }
            chessBoard[7 - row][7 - col] = temp;
        }
        int kingTemp = kingPosWhite;
        kingPosWhite = 63 - kingPosBlack;
        kingPosBlack = 63 - kingTemp;
    }

    public boolean checkMove(String move, boolean white) {
        Log.d("test", possibleMoves(white) + "move: " + move);
        return possibleMoves(white).contains(move);
    }

    public String makeMoveAndFlip(String move) {
        makeMove(move);

//        flipBoard();
//        String returnString = engine.alphaBeta(Engine.globalDepth, Integer.MAX_VALUE,
//                Integer.MIN_VALUE, "", -1);
//        makeMove(returnString.substring(0, 5));
//        flipBoard();
//        return returnString.substring(5);
        return "";
    }

    public void makeMove(String move) {
        int row = Character.getNumericValue(move.charAt(0));
        int col = Character.getNumericValue(move.charAt(1));
        if (move.charAt(4) == 'U') { // white pawn promotion
            chessBoard[1][row] = ' ';
            chessBoard[0][col] = move.charAt(3);
        } else if (move.charAt(4) == 'u') { // black pawn promotion
            chessBoard[6][row] = ' ';
            chessBoard[7][col] = move.charAt(3);
        } else {
            int newRow = Character.getNumericValue(move.charAt(2));
            int newCol = Character.getNumericValue(move.charAt(3));
            chessBoard[newRow][newCol] = chessBoard[row][col];
            chessBoard[row][col] = ' ';
            updateKing(newRow, newCol);
        }
    }

    public void undoMove(String move) {
        int row = Character.getNumericValue(move.charAt(0));
        int col = Character.getNumericValue(move.charAt(1));
        int newCol = Character.getNumericValue(move.charAt(3));
        if (move.charAt(4) == 'U') { // white pawn promotion
            chessBoard[1][row] = 'P';
            chessBoard[0][col] = move.charAt(2);
        } else if (move.charAt(4) == 'u') { // black pawn promotion
            chessBoard[1][row] = 'p';
            chessBoard[0][col] = move.charAt(2);
        } else {
            int newRow = Character.getNumericValue(move.charAt(2));
            chessBoard[row][col] = chessBoard[newRow][newCol];
            chessBoard[newRow][newCol] = move.charAt(4);
            updateKing(row, col);
        }

    }

    private void updateKing(int row, int col) {
        if ('A' == chessBoard[row][col]) {
            kingPosWhite = 8 * row + col;
        } else if ('a' == chessBoard[row][col]) {
            kingPosBlack = 8 * row + col;
        }
    }

    public String possibleMoves(boolean white) {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            checkMoveForPiece(i, list, white);
        }
        return list.toString(); // x1, y1, x2, y2, captured piece;
    }

    public void checkMoveForPiece(int position, StringBuilder list, boolean white) {
        char piece = chessBoard[position / 8][position % 8];
        if (white ? Character.isLowerCase(piece) : Character.isUpperCase(piece)) {
            return;
        }
        switch (Character.toUpperCase(piece)) {
            case 'P':
                list.append(possibleP(position, white));
                break;
            case 'R':
                list.append(possibleR(position, white));
                break;
            case 'K':
                list.append(possibleK(position, white));
                break;
            case 'B':
                list.append(possibleB(position, white));
                break;
            case 'Q':
                list.append(possibleQ(position, white));
                break;
            case 'A':
                list.append(possibleA(position, white));
                break;
        }
    }

    private String possibleP(int position, boolean white) {
        StringBuilder list = new StringBuilder();
        int row = position / 8, col = position % 8;
        int checkRow = white ? row - 1 : row + 1;
        for (int i = -1; i <= 1; i += 2) { // capture and promotion
            int checkCol = col + i;
            if (checkBounds(checkRow, checkCol) && canTake(checkRow, checkCol, white))
                if (canBePromoted(position, white)) { // No promotion
                    checkSafety(row, col, checkRow, checkCol, list, 'P', white);
                } else { // promotion
                    checkPromotion(row, col, checkRow, checkCol, list, white);
                }
        }
        if (checkBounds(checkRow, col) && ' ' == chessBoard[checkRow][col]) {
            if (canBePromoted(position, white)) { // move one up
                checkSafety(row, col, checkRow, col, list, 'P', white);
            } else { // promotion and no capture
                checkPromotion(row, col, checkRow, col, list, white);
            }
            checkRow = white ? row - 2 : row + 2;
            if (white ? position >= 48 : position <= 16 && ' ' == chessBoard[checkRow][col]) { // move two up
                checkSafety(row, col, checkRow, col, list, 'P', white);
            }
        }
        return list.toString();
    }

    private boolean canBePromoted(int position, boolean white) {
        return white && position >= 16 || !white && position <= 48;
    }

    private void checkPromotion(int row, int col, int checkRow, int checkCol, StringBuilder list, boolean white) {
        Character[] promotion = {'Q', 'R', 'B', 'K'};
        for (Character piece : promotion) {
            if (!white) {
                piece = Character.toLowerCase(piece);
            }
            char oldPiece = chessBoard[checkRow][checkCol];
            chessBoard[row][col] = ' ';
            chessBoard[checkRow][checkCol] = piece;
            if (kingSafe(white)) {
                list.append(col).append(checkCol).append(oldPiece)
                        .append(piece).append(white ? 'U' : 'u');
            }
            chessBoard[row][col] = white ? 'P' : 'p';
            chessBoard[checkRow][checkCol] = oldPiece;
        }
    }

    private String possibleR(int position, boolean white) {
        StringBuilder list = new StringBuilder();
        int row = position / 8, col = position % 8;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if ((i + j) % 2 != 0) {
                    checkMoves(row, col, i, j, list, 'R', white);
                }
            }
        }
        return list.toString();
    }

    private String possibleK(int position, boolean white) {
        StringBuilder list = new StringBuilder();
        int row = position / 8, col = position % 8;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int checkRow = row + i, checkCol = col + j * 2;
                if (checkBounds(checkRow, checkCol) && (canTake(checkRow, checkCol, white) ||
                        ' ' == chessBoard[checkRow][checkCol])) {
                    checkSafety(row, col, checkRow, checkCol, list, 'K', white);
                }
                checkRow = row + i * 2;
                checkCol = col + j;
                if (checkBounds(checkRow, checkCol) && (canTake(checkRow, checkCol, white) ||
                        ' ' == chessBoard[checkRow][checkCol])) {
                    checkSafety(row, col, checkRow, checkCol, list, 'K', white);
                }
            }
        }
        return list.toString();
    }

    private String possibleB(int position, boolean white) {
        StringBuilder list = new StringBuilder();
        int row = position / 8, col = position % 8;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                checkMoves(row, col, i, j, list, 'B', white);
            }
        }
        return list.toString();
    }

    private String possibleQ(int position, boolean white) {
        StringBuilder list = new StringBuilder();
        int row = position / 8, col = position % 8;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 || j != 0) {
                    checkMoves(row, col, i, j, list, 'Q', white);
                }
            }
        }
        return list.toString();
    }

    private void checkMoves(int row, int col, int i, int j, StringBuilder list, char piece,
                            boolean white) {
        int temp = 1;
        int checkRow = row + temp * i, checkCol = col + temp * j;
        while (checkBounds(checkRow, checkCol) && ' ' == chessBoard[checkRow][checkCol]) {
            checkSafety(row, col, checkRow, checkCol, list, piece, white);
            temp++;
            checkRow = row + temp * i;
            checkCol = col + temp * j;
        }
        if (checkBounds(checkRow, checkCol) && canTake(checkRow, checkCol, white)) {
            checkSafety(row, col, checkRow, checkCol, list, piece, white);
        }
    }

    private void checkSafety(int row, int col, int checkRow, int checkCol, StringBuilder list,
                             char piece, boolean white) {
        piece = white ? piece : Character.toLowerCase(piece);
        char oldPiece = chessBoard[checkRow][checkCol];
        chessBoard[row][col] = ' ';
        chessBoard[checkRow][checkCol] = piece;
        if (kingSafe(white)) {
            list.append(row).append(col).append(checkRow)
                    .append(checkCol).append(oldPiece);
        }
        chessBoard[row][col] = piece;
        chessBoard[checkRow][checkCol] = oldPiece;
    }

    private String possibleA(int position, boolean white) {
        char piece = white ? 'A' : 'a';
        StringBuilder list = new StringBuilder();
        char oldPiece;
        int row = position / 8, col = position % 8;
        for (int i = 0; i < 9; i++) {
            int checkRow = row - 1 + i / 3, checkCol = col - 1 + i % 3;
            if (i != 4 && checkBounds(checkRow, checkCol)) {
                if (canTake(checkRow, checkCol, white) || ' ' == (chessBoard[checkRow][checkCol])) {
                    oldPiece = chessBoard[checkRow][checkCol];
                    chessBoard[row][col] = ' ';
                    chessBoard[checkRow][checkCol] = piece;
                    int kingTemp = white ? kingPosWhite : kingPosBlack;
                    int pos = position + i / 3 * 8 + i % 3 - 9;
                    if (white)
                        kingPosWhite = pos;
                    else
                        kingPosBlack = pos;
                    if (kingSafe(white)) {
                        list.append(row).append(col).append(checkRow).append(checkCol).append(oldPiece);
                    }
                    chessBoard[row][col] = piece;
                    chessBoard[checkRow][checkCol] = oldPiece;
                    if (white)
                        kingPosWhite = kingTemp;
                    else
                        kingPosBlack = kingTemp;
                }
            }
        }
        // castling
        return list.toString();
    }

    private boolean kingSafe(boolean white) {
        int kingPos = white ? kingPosWhite : kingPosBlack;
        // bishop | queen
        int temp = 1;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int checkRow = kingPos / 8 + temp * i, checkCol = kingPos % 8 + temp * j;
                while (checkBounds(checkRow, checkCol) && ' ' == chessBoard[checkRow][checkCol]) {
                    temp++;
                    checkRow = kingPos / 8 + temp * i;
                    checkCol = kingPos % 8 + temp * j;
                }
                if (checkBounds(checkRow, checkCol) && (kingInDanger(checkRow, checkCol, white, 'b') ||
                        kingInDanger(checkRow, checkCol, white, 'q'))) {
                    return false;
                }
                temp = 1;
            }
        }
        // rock | queen
        for (int i = -1; i <= 1; i += 2) {
            // column check
            int checkRow = kingPos / 8, checkCol = kingPos % 8 + temp * i;
            while (checkBounds(checkRow, checkCol) && ' ' == chessBoard[checkRow][checkCol]) {
                temp++;
                checkRow = kingPos / 8;
                checkCol = kingPos % 8 + temp * i;
            }
            if (checkBounds(checkRow, checkCol) && (kingInDanger(checkRow, checkCol, white, 'r') ||
                    kingInDanger(checkRow, checkCol, white, 'q'))) {
                return false;
            }
            temp = 1;
            // row check
            checkRow = kingPos / 8 + temp * i;
            checkCol = kingPos % 8;
            while (checkBounds(checkRow, checkCol) && ' ' == chessBoard[checkRow][checkCol]) {
                temp++;
                checkRow = kingPos / 8 + temp * i;
                checkCol = kingPos % 8;
            }
            if (checkBounds(checkRow, checkCol) && (kingInDanger(checkRow, checkCol, white, 'r') ||
                    kingInDanger(checkRow, checkCol, white, 'q'))) {
                return false;
            }
            temp = 1;
        }

        // knight
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int checkRow = kingPos / 8 + i, checkCol = kingPos % 8 + j * 2;
                if (checkBounds(checkRow, checkCol) && kingInDanger(checkRow, checkCol, white, 'k')) {
                    return false;
                }
                checkRow = kingPos / 8 + i * 2;
                checkCol = kingPos % 8 + j;
                if (checkBounds(checkRow, checkCol) && kingInDanger(checkRow, checkCol, white, 'k')) {
                    return false;
                }
            }
        }

        if (kingPos >= 16) {
            // pawn
            int checkRow = kingPos / 8 - 1, checkCol = kingPos % 8 - 1;
            if (checkBounds(checkRow, checkCol) && kingInDanger(checkRow, checkCol, white, 'p'))
                return false;
            checkRow = kingPos / 8 + 1;
            checkCol = kingPos % 8 + 1;
            if (checkBounds(checkRow, checkCol) && kingInDanger(checkRow, checkCol, white, 'p'))
                return false;
        }
        // king
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0)
                    continue;
                int checkRow = kingPos / 8 + i, checkCol = kingPos % 8 + j;
                if (checkBounds(checkRow, checkCol) && kingInDanger(checkRow, checkCol, white, 'a')) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean kingInDanger(int row, int col, boolean white, char piece) {
        return (white ? piece : Character.toUpperCase(piece)) == chessBoard[row][col];
    }

    public boolean canTake(int row, int col, boolean white) {
        return white ? Character.isLowerCase(chessBoard[row][col]) :
                Character.isUpperCase(chessBoard[row][col]);
    }

    private boolean checkBounds(int row, int col) {
        return row >= 0 && col >= 0 && row < 8 && col < 8;
    }
}
