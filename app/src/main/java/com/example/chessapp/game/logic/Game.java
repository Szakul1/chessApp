package com.example.chessapp.game.logic;

import android.util.Log;

import static com.example.chessapp.game.logic.BitBoards.*;

import androidx.appcompat.widget.WithHint;

import com.example.chessapp.game.Board;

import java.lang.reflect.Method;

public class Game {
    private final Board board;
    private final Engine engine;
    // r - rook, k - knight, b - bishop, q - queen, a - king p - pawn
    // capital for white lower for black
//    public char[][] chessBoard = {
//            {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
//            {'p', 'p', 'p', ' ', 'p', 'p', 'p', 'p'},
//            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//            {' ', ' ', ' ', 'p', ' ', ' ', ' ', ' '},
//            {' ', ' ', ' ', ' ', 'P', ' ', ' ', ' '},
//            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//            {'P', 'P', 'P', 'P', ' ', 'P', 'P', 'P'},
//            {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}};
    public char[][] chessBoard = {
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', 'k', ' ', ' ', ' ', ' '},
            {' ', 'q', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', 'K', ' ', ' '}};
    private long[] boards = {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,};
    private boolean[] castleFlags = {true, true, true, true};
    long columnAB = 217020518514230019L;
    long columnGH = -4557430888798830400L;
    long KING_SPAN = 460039L;
    long KNIGHT_SPAN = 43234889994L;
    long notMyPieces;
    long myPieces;
    long occupied;
    long empty;
    long[] CASTLE_ROOKS = {63, 56, 7, 0};
    long[] rowMasks8 = /* from rank1 to rank8 */
            {0xFFL, 0xFF00L, 0xFF0000L, 0xFF000000L, 0xFF00000000L, 0xFF0000000000L, 0xFF000000000000L,
                    0xFF00000000000000L};
    long[] columnMasks8 = /* from fileA to FileH */
            {0x101010101010101L, 0x202020202020202L, 0x404040404040404L, 0x808080808080808L, 0x1010101010101010L,
                    0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L};
    long[] DiagonalMasks8 = /* from top left to bottom right */
            {0x1L, 0x102L, 0x10204L, 0x1020408L, 0x102040810L, 0x10204081020L, 0x1020408102040L, 0x102040810204080L,
                    0x204081020408000L, 0x408102040800000L, 0x810204080000000L, 0x1020408000000000L,
                    0x2040800000000000L, 0x4080000000000000L, 0x8000000000000000L};
    long[] AntiDiagonalMasks8 = /* from top right to bottom left */
            {0x80L, 0x8040L, 0x804020L, 0x80402010L, 0x8040201008L, 0x804020100804L, 0x80402010080402L,
                    0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L, 0x1008040201000000L,
                    0x804020100000000L, 0x402010000000000L, 0x201000000000000L, 0x100000000000000L};

    private void arrayToBitboards() {
        long binary = 1L;
        for (int i = 0; i < 64; i++) {
            switch (chessBoard[i / 8][i % 8]) {
                case 'P':
                    boards[WP] += binary;
                    break;
                case 'N':
                    boards[WN] += binary;
                    break;
                case 'B':
                    boards[WB] += binary;
                    break;
                case 'R':
                    boards[WR] += binary;
                    break;
                case 'Q':
                    boards[WQ] += binary;
                    break;
                case 'K':
                    boards[WK] += binary;
                    break;
                case 'p':
                    boards[BP] += binary;
                    break;
                case 'n':
                    boards[BN] += binary;
                    break;
                case 'b':
                    boards[BB] += binary;
                    break;
                case 'r':
                    boards[BR] += binary;
                    break;
                case 'q':
                    boards[BQ] += binary;
                    break;
                case 'k':
                    boards[BK] += binary;
                    break;
            }
            binary = binary << 1;
        }
    }

    public Game(Board board, boolean white) {
        this.board = board;
        arrayToBitboards();
        engine = new Engine(this);
//        if (!white) {
//            makeMove(engine.alphaBeta(Engine.globalDepth, Integer.MAX_VALUE, Integer.MIN_VALUE,
//                    "", -1, false));
//        }
    }

    public String possibleMoves(boolean white) {
        return white ? possibleMovesW(boards, castleFlags[CWK], castleFlags[CWQ]) :
                possibleMovesB(boards, castleFlags[CBK], castleFlags[CBQ]);
    }

    public String possibleMoves(boolean white, long[] boards, boolean[] castleFlags) {
        return white ? possibleMovesW(boards, castleFlags[CWK], castleFlags[CWQ]) :
                possibleMovesB(boards, castleFlags[CBK], castleFlags[CBQ]);
    }

    public void makeRealMove(String move) {
        updateCastling(move, boards, castleFlags);
        boards = makeMove(move, boards);
        int row = getValFromString(move, 0), col = getValFromString(move, 1);
        if (Character.isDigit(move.charAt(3))) {// 'regular' move
            if ("0402".equals(move) || "7472".equals(move)) {
                chessBoard[getValFromString(move, 0)][3] = chessBoard[getValFromString(move, 0)][0];
                chessBoard[getValFromString(move, 0)][0] = ' ';
            }
            else if ("0406".equals(move) || "7476".equals(move)) {
                chessBoard[getValFromString(move, 0)][5] = chessBoard[getValFromString(move, 0)][7];
                chessBoard[getValFromString(move, 0)][7] = ' ';
            }
            int rowNew = getValFromString(move, 2), colNew = getValFromString(move, 3);
            chessBoard[rowNew][colNew] = chessBoard[row][col];
            chessBoard[row][col] = ' ';
        } else {
            boolean white = Character.isUpperCase(move.charAt(2));
            if (move.charAt(3) == 'P') {
                chessBoard[white ? 0 : 7][col] = move.charAt(2);
                chessBoard[white ? 1 : 6][row] = ' ';
            } else {
                chessBoard[white ? 2 : 5][col] = chessBoard[white ? 3 : 4][row];
                chessBoard[white ? 3 : 4][row] = ' ';
                chessBoard[white ? 3 : 4][col] = ' ';
            }
        }
    }

    public long[] makeMove(String move, long[] pieces) {
        long tempWR = makeMoveForBoard(pieces[WR], move, 'R');
        long tempBR = makeMoveForBoard(pieces[BR], move, 'r');
        return new long[] {
                makeMoveForBoard(pieces[WP], move, 'P'),
                makeMoveForBoard(pieces[WN], move, 'N'),
                makeMoveForBoard(pieces[WB], move, 'B'),
                makeMoveCastle(tempWR, pieces[WK] | pieces[BK], move, 'R'),
                makeMoveForBoard(pieces[WQ], move, 'Q'),
                makeMoveForBoard(pieces[WK], move, 'K'),
                makeMoveForBoard(pieces[BP], move, 'p'),
                makeMoveForBoard(pieces[BN], move, 'n'),
                makeMoveForBoard(pieces[BB], move, 'b'),
                makeMoveCastle(tempBR, pieces[WK] | pieces[BK], move, 'r'),
                makeMoveForBoard(pieces[BQ], move, 'q'),
                makeMoveForBoard(pieces[BK], move, 'k'),
                makeMoveEP(pieces[WP] | pieces[BP], move),
        };
    }

    public boolean kingSafe(boolean white, long[] pieces) {
        return white ? (pieces[WK] & unsafeForWhite(pieces)) == 0 :
                (pieces[BK] & unsafeForBlack(pieces)) == 0;
    }

    public boolean kingSafe(boolean white) {
        return kingSafe(white, boards);
    }

    public boolean isMyPiece(int row, int col, boolean white) {
        int position = row * 8 + col;
        return (getMyPieces(white, boards) & (1L << position)) != 0;
    }

    public int response(boolean white) {
//        Log.d("test", "value: " + engine.evaluate(boards)+"");

        board.repaint();
        int score = -engine.findBestMove(boards, castleFlags, white);
//        Log.d("test", score+"");
        String move = engine.bestMove;
        if (!move.isEmpty()) {
            makeRealMove(move);
//            Log.d("test", "value: " + engine.evaluate(boards)+"");
        }
        finishGame(move.length(), white);
        return score;
    }

    public boolean checkMoveAndMake(int row, int col, int newRow, int newCol, boolean white) {
        String possibleMoves = possibleMoves(white);
        String move = "";
        if (Character.toLowerCase(chessBoard[row][col]) == 'p') {
            if (white ? newRow == 0 : newRow == 7) { // promotion
                move = "" + col + newCol + (white ? 'Q' : 'q') + 'P';
                if (movesContains(move, possibleMoves)) {
                    long[] temp = makeMove(move, boards);
                    if (kingSafe(white, temp)) {
                        board.showDialog(move);
                        return true;
                    }
                }
            } else if (col != newCol && chessBoard[newRow][newCol] == ' ') {
                move = "" + col + newCol + (white ? 'W' : 'B') + 'E';
            } else {
                move = "" + row + col + newRow + newCol;
            }
        } else {
            move = "" + row + col + newRow + newCol;
        }
        if (movesContains(move, possibleMoves)) {
            long[] temp = makeMove(move, boards);
            if (kingSafe(white, temp)) {
                makeRealMove(move);
                return true;
            }
        }
        return false;
    }

    public String getMovesForPiece(int row, int col, boolean white) {
        String moves = possibleMoves(white);
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < moves.length(); i += 4) {
            String move = moves.substring(i, i + 4);
            int moveRow, moveCol, newRow, newCol;
            if (Character.isDigit(move.charAt(3))) {// 'regular' move
                moveRow = getValFromString(move, 0);
                moveCol = getValFromString(move, 1);
                newRow = getValFromString(move, 2);
                newCol = getValFromString(move, 3);
            } else {
                moveCol = getValFromString(move, 0);
                newCol = getValFromString(move, 1);
                if (move.charAt(3) == 'P') {
                    moveRow = white ? 1 : 6;
                    newRow = white ? 0 : 7;
                } else {
                    moveRow = white ? 3 : 4;
                    newRow = white ? 2 : 5;
                }
            }
            if (moveRow == row && moveCol == col) {
                long[] pieces = makeMove(move, boards);
                if (kingSafe(white, pieces)) {
                    list.append(newRow).append(newCol);
                }
            }
        }
        return list.toString();
    }

    public void gameFinished(boolean white) {
        String moves = possibleMoves(white);
        int moveCounter = 0;
        for (int i = 0; i < moves.length(); i += 4) {
            String move = moves.substring(i, i + 4);
            long[] pieces = makeMove(move, boards);
            if (kingSafe(white, pieces)) {
                moveCounter++;
            }
        }
        finishGame(moveCounter, white);
    }

    private void finishGame(int moveCounter, boolean white) {
        if (moveCounter == 0) {
            if (kingSafe(white)) {
                board.finishedGame = "Draw Stalemate";
            } else {
                board.finishedGame = (white ? "Black" : "White") + " won";
            }
            board.showEndDialog();
        } else {
            long occupied = getOccupied(boards);
            if ((boards[WK] | boards[BK]) == occupied) {
                board.finishedGame = "Draw";
                board.showEndDialog();
            }
        }
    }

    private boolean movesContains(String move, String moves) {
        for (int i = 0; i < moves.length(); i += 4) {
            if (move.equals(moves.substring(i, i + 4))) {
                return true;
            }
        }
        return false;
    }

    public void updateCastling(String move, long[] pieces, boolean[] flags) {
        if (Character.isDigit(move.charAt(3))) {// 'regular' move
            int start = getValFromString(move, 0) * 8 + getValFromString(move, 1);
            if (((1L << start) & pieces[WK]) != 0) {
                flags[CWK] = false;
                flags[CWQ] = false;
            } else if (((1L << start) & pieces[BK]) != 0) {
                flags[CBK] = false;
                flags[CBQ] = false;
            } else if (((1L << start) & pieces[WR] & (1L << 63)) != 0) {
                flags[CWK] = false;
            } else if (((1L << start) & pieces[WR] & (1L << 56)) != 0) {
                flags[CWQ] = false;
            } else if (((1L << start) & pieces[BR] & (1L << 7)) != 0) {
                flags[CBK] = false;
            } else if (((1L << start) & pieces[BR] & 1L) != 0) {
                flags[CBQ] = false;
            }
        }
    }

    long straightMoves(int position) {
        int row = position / 8, col = position % 8;
        long binaryPiece = 1L << position;
        long movesHorizontal = (occupied - 2 * binaryPiece) ^
                Long.reverse(Long.reverse(occupied) - 2 * Long.reverse(binaryPiece));
        long movesVertical = ((occupied & columnMasks8[col]) - 2 * binaryPiece) ^
                Long.reverse(Long.reverse(occupied & columnMasks8[col]) -
                        (2 * Long.reverse(binaryPiece)));
        return (movesHorizontal & rowMasks8[row]) | (movesVertical & columnMasks8[col]);
    }

    long diagonalMoves(int position) {
        int checkPos = position / 8 + position % 8;
        long binaryPiece = 1L << position;
        long movesDiagonal = ((occupied & DiagonalMasks8[checkPos]) - (2 * binaryPiece)) ^
                Long.reverse(Long.reverse(occupied & DiagonalMasks8[checkPos]) -
                        (2 * Long.reverse(binaryPiece)));
        int checkPos2 = position / 8 + 7 - position % 8;
        long movesAntiDiagonal = ((occupied & AntiDiagonalMasks8[checkPos2]) - (2 * binaryPiece)) ^
                Long.reverse(Long.reverse(occupied & AntiDiagonalMasks8[checkPos2]) -
                        (2 * Long.reverse(binaryPiece)));
        return (movesDiagonal & DiagonalMasks8[checkPos]) |
                (movesAntiDiagonal & AntiDiagonalMasks8[checkPos2]);
    }

    public static int getValFromString(String s, int index) {
        return Character.getNumericValue(s.charAt(index));
    }

    public long makeMoveForBoard(long board, String move, char promotionType) {
        if (Character.isDigit(move.charAt(3))) { // 'regular' move
            int start = (getValFromString(move, 0) * 8) + (getValFromString(move, 1));
            int end = (getValFromString(move, 2) * 8) + (getValFromString(move, 3));
            if (((board >> start) & 1) == 1) {
                board &= ~(1L << start);
                board |= (1L << end);
            } else {
                board &= ~(1L << end);
            }
        } else if (move.charAt(3) == 'P') {// pawn promotion
            int start, end;
            if (Character.isUpperCase(move.charAt(2))) {
                start = Long.numberOfTrailingZeros(columnMasks8[getValFromString(move, 0)] & rowMasks8[1]);
                end = Long.numberOfTrailingZeros(columnMasks8[getValFromString(move, 1)] & rowMasks8[0]);
            } else {
                start = Long.numberOfTrailingZeros(columnMasks8[getValFromString(move, 0)] & rowMasks8[6]);
                end = Long.numberOfTrailingZeros(columnMasks8[getValFromString(move, 1)] & rowMasks8[7]);
            }
            if (promotionType == move.charAt(2)) {
                board |= (1L << end);
            } else {
                board &= ~(1L << start);
                board &= ~(1L << end);
            }
        } else if (move.charAt(3) == 'E') {// en passant
            int start, end;
            if (move.charAt(2) == 'W') {
                start = Long.numberOfTrailingZeros(columnMasks8[getValFromString(move, 0)] & rowMasks8[3]);
                end = Long.numberOfTrailingZeros(columnMasks8[getValFromString(move, 1)] & rowMasks8[2]);
                board &= ~(columnMasks8[getValFromString(move, 1)] & rowMasks8[3]);
            } else {
                start = Long.numberOfTrailingZeros(columnMasks8[getValFromString(move, 0)] & rowMasks8[4]);
                end = Long.numberOfTrailingZeros(columnMasks8[getValFromString(move, 1)] & rowMasks8[5]);
                board &= ~(columnMasks8[getValFromString(move, 1)] & rowMasks8[4]);
            }
            if (((board >> start) & 1) == 1) {
                board &= ~(1L << start);
                board |= (1L << end);
            }
        } else {
            System.out.print("ERROR: Invalid move type");
        }
        return board;
    }

    public long makeMoveCastle(long rookBoard, long kingBoard, String move, char type) {
        int start = (getValFromString(move, 0) * 8) + (getValFromString(move, 1));
        //'regular' move
        if ((((kingBoard >> start) & 1) == 1) &&
                (("0402".equals(move)) || ("0406".equals(move)) || ("7472".equals(move)) || ("7476".equals(move)))) {
            if (type == 'R') {
                switch (move) {
                    case "7472":
                        rookBoard &= ~(1L << CASTLE_ROOKS[1]);
                        rookBoard |= (1L << (CASTLE_ROOKS[1] + 3));
                        break;
                    case "7476":
                        rookBoard &= ~(1L << CASTLE_ROOKS[0]);
                        rookBoard |= (1L << (CASTLE_ROOKS[0] - 2));
                        break;
                }
            } else {
                switch (move) {
                    case "0402":
                        rookBoard &= ~(1L << CASTLE_ROOKS[3]);
                        rookBoard |= (1L << (CASTLE_ROOKS[3] + 3));
                        break;
                    case "0406":
                        rookBoard &= ~(1L << CASTLE_ROOKS[2]);
                        rookBoard |= (1L << (CASTLE_ROOKS[2] - 2));
                        break;
                }
            }
        }
        return rookBoard;
    }

    public long makeMoveEP(long board, String move) {
        if (Character.isDigit(move.charAt(3))) {
            int start = (getValFromString(move, 0) * 8) + (getValFromString(move, 1));
            if ((Math.abs(move.charAt(0) - move.charAt(2)) == 2) && (((board >> start) & 1) == 1)) {// pawn double push
                return columnMasks8[getValFromString(move, 1)];
            }
        }
        return 0;
    }

    public String possibleMovesW(long[] pieces, boolean CWK, boolean CWQ) {
        myPieces = getMyPieces(true, pieces);
        notMyPieces = ~(myPieces);
        occupied = getOccupied(pieces);
        empty = ~occupied;
        return possibleWP(pieces[WP], pieces[BP], pieces[EP]) + possibleN(pieces[WN]) + possibleB(pieces[WB])
                + possibleR(pieces[WR]) + possibleQ(pieces[WQ]) + possibleK(pieces[WK])
                + possibleCW(CWK, CWQ);
    }

    public String possibleMovesB(long[] pieces, boolean CBK, boolean CBQ) {
        myPieces = getMyPieces(false, pieces);
        notMyPieces = ~(myPieces);
        occupied = getOccupied(pieces);
        empty = ~occupied;
        return possibleBP(pieces[BP], pieces[WP], pieces[EP]) + possibleN(pieces[BN]) + possibleB(pieces[BB])
                + possibleR(pieces[BR]) + possibleQ(pieces[BQ]) + possibleK(pieces[BK])
                + possibleCB(CBK, CBQ);
    }

    public long getMyPieces(boolean white, long[] pieces) {
        return white ? pieces[WP] | pieces[WN] | pieces[WB] | pieces[WR] | pieces[WQ] | pieces[WK] :
                pieces[BP] | pieces[BN] | pieces[BB] | pieces[BR] | pieces[BQ] | pieces[BK];
    }

    public String possibleWP(long WP, long BP, long EP) {
        StringBuilder list = new StringBuilder();

        long pawnMoves = (WP >> 7) & notMyPieces & occupied & ~rowMasks8[0] & ~columnMasks8[0]; // capture right
        addPawnMoves(list, pawnMoves, 1, -1);

        pawnMoves = (WP >> 9) & notMyPieces & occupied & ~rowMasks8[0] & ~columnMasks8[7]; // capture left
        addPawnMoves(list, pawnMoves, 1, 1);

        pawnMoves = (WP >> 8) & empty & ~rowMasks8[0]; // move 1 up
        addPawnMoves(list, pawnMoves, 1, 0);

        pawnMoves = (WP >> 16) & empty & (empty >> 8) & rowMasks8[4]; // move 2 up
        addPawnMoves(list, pawnMoves, 2, 0);

        // promotion: y1, y2, promotion, 'P'

        pawnMoves = (WP >> 7) & notMyPieces & occupied & rowMasks8[0] & ~columnMasks8[0]; // capture right
        addPromotion(list, pawnMoves, -1, true);

        pawnMoves = (WP >> 9) & notMyPieces & occupied & rowMasks8[0] & ~columnMasks8[7]; // capture left
        addPromotion(list, pawnMoves, 1, true);

        pawnMoves = (WP >> 8) & notMyPieces & empty & rowMasks8[0]; // move 1 up
        addPromotion(list, pawnMoves, 0, true);

        // el passant: y1, y2, Space, 'E'

        // right
        pawnMoves = (WP << 1) & BP & rowMasks8[3] & ~columnMasks8[0] & EP;
        if (pawnMoves != 0) {
            int i = Long.numberOfTrailingZeros(pawnMoves);
            list.append(i % 8 - 1).append(i % 8).append("WE");
        }
        // left
        pawnMoves = (WP >> 1) & BP & rowMasks8[3] & ~columnMasks8[7] & EP;
        if (pawnMoves != 0) {
            int i = Long.numberOfTrailingZeros(pawnMoves);
            list.append(i % 8 + 1).append(i % 8).append("WE");
        }

        return list.toString();
    }

    public String possibleBP(long BP, long WP, long EP) {
        StringBuilder list = new StringBuilder();

        long pawnMoves = (BP << 7) & notMyPieces & occupied & ~rowMasks8[7] & ~columnMasks8[7]; // capture right
        addPawnMoves(list, pawnMoves, -1, 1);

        pawnMoves = (BP << 9) & notMyPieces & occupied & ~rowMasks8[7] & ~columnMasks8[0]; // capture left
        addPawnMoves(list, pawnMoves, -1, -1);

        pawnMoves = (BP << 8) & empty & ~rowMasks8[7]; // move 1 up
        addPawnMoves(list, pawnMoves, -1, 0);

        pawnMoves = (BP << 16) & empty & (empty << 8) & rowMasks8[3]; // move 2 up
        addPawnMoves(list, pawnMoves, -2, 0);

        // promotion: y1, y2, promotion, 'P'

        pawnMoves = (BP << 7) & notMyPieces & occupied & rowMasks8[7] & ~columnMasks8[0]; // capture right
        addPromotion(list, pawnMoves, 1, false);

        pawnMoves = (BP << 9) & notMyPieces & occupied & rowMasks8[7] & ~columnMasks8[7]; // capture left
        addPromotion(list, pawnMoves, -1, false);

        pawnMoves = (BP << 8) & notMyPieces & empty & rowMasks8[7]; // move 1 up
        addPromotion(list, pawnMoves, 0, false);

        // el passant: y1, y2, Space, 'E'

        // right
        pawnMoves = (BP >> 1) & WP & rowMasks8[4] & ~columnMasks8[7] & EP;
        if (pawnMoves != 0) {
            int i = Long.numberOfTrailingZeros(pawnMoves);
            list.append(i % 8 + 1).append(i % 8).append("BE");
        }
        // left
        pawnMoves = (BP << 1) & WP & rowMasks8[4] & ~columnMasks8[0] & EP;
        if (pawnMoves != 0) {
            int i = Long.numberOfTrailingZeros(pawnMoves);
            list.append(i % 8 - 1).append(i % 8).append("BE");
        }

        return list.toString();
    }

    private void addPromotion(StringBuilder list, long pawnMoves, int addCol, boolean white) {
        char[] promotion = white ? new char[]{'Q', 'R', 'B', 'N'} : new char[]{'q', 'r', 'b', 'n'};
        long firstPawn = pawnMoves & -pawnMoves; //&(pawnMoves-1)
        while (firstPawn != 0) {
            int i = Long.numberOfTrailingZeros(firstPawn);
            int col = i % 8;
            for (char piece : promotion) {
                list.append(col + addCol).append(col).append(piece).append('P');
            }
            pawnMoves &= ~firstPawn;
            firstPawn = pawnMoves & -pawnMoves; //&(pawnMoves-1)
        }
    }

    private void addPawnMoves(StringBuilder list, long pawnMoves, int addRow, int addCol) {
        long pawn = pawnMoves & -pawnMoves; //&(pawnMoves-1)
        while (pawn != 0) {
            int i = Long.numberOfTrailingZeros(pawn);
            int row = i / 8, col = i % 8;
            list.append(row + addRow).append(col + addCol).append(row).append(col);
            pawnMoves &= ~pawn;
            pawn = pawnMoves & -pawnMoves; //&(pawnMoves-1)
        }
    }

    private String possibleB(long BBoard) {
        return getMoves(BBoard, this::diagonalMoves);
    }

    public String possibleR(long RBoard) {
        return getMoves(RBoard, this::straightMoves);
    }

    public String possibleQ(long QBoard) {
        return getMoves(QBoard, this::getQueenMoves);
    }

    private long getQueenMoves(int position) {
        return straightMoves(position) | diagonalMoves(position);
    }

    private interface GetMoves {
        long getMoves(int location);

    }

    public String possibleK(long ABoard) {
        StringBuilder list = new StringBuilder();
        int location = Long.numberOfTrailingZeros(ABoard);
        long moves = location > 9 ?
                KING_SPAN << (location - 9) :
                KING_SPAN >> (9 - location);
        checkArrayOut(list, location, moves);
        return list.toString();
    }

    public String possibleN(long KBoard) {
        StringBuilder list = new StringBuilder();
        long knight = KBoard & -KBoard;
        while (knight != 0) {
            int location = Long.numberOfTrailingZeros(knight);
            long moves = location > 18 ?
                    KNIGHT_SPAN << (location - 18) :
                    KNIGHT_SPAN >> (18 - location);
            checkArrayOut(list, location, moves);
            KBoard &= ~knight;
            knight = KBoard & -KBoard;
        }
        return list.toString();
    }

    private String getMoves(long board, GetMoves getMoves) {
        StringBuilder list = new StringBuilder();
        long piece = board & -board;
        while (piece != 0) {
            int location = Long.numberOfTrailingZeros(piece);
            long moves = getMoves.getMoves(location) & notMyPieces;
            addMove(list, location, moves);
            board &= ~piece;
            piece = board & -board;
        }
        return list.toString();
    }

    private void addMove(StringBuilder list, int location, long moves) {
        long move = moves & -moves;
        while (move != 0) {
            int nextLocation = Long.numberOfTrailingZeros(move);
            list.append(location / 8).append(location % 8).append(nextLocation / 8)
                    .append(nextLocation % 8);
            moves &= ~move;
            move = moves & -moves;
        }
    }

    private void checkArrayOut(StringBuilder list, int location, long moves) {
        moves = location % 8 < 4 ?
                moves & ~columnGH & notMyPieces :
                moves & ~columnAB & notMyPieces;
        addMove(list, location, moves);
    }

    public String possibleCW(boolean castleWK, boolean castleWQ) {
        StringBuilder list = new StringBuilder();
        // must empty places
        if (castleWK && (occupied & ((1L << 61) | (1L << 62))) == 0) {
            list.append("7476");
        }
        if (castleWQ && (occupied & ((1L << 57) | (1L << 58) | (1L << 59))) == 0) {
            list.append("7472");
        }
        return list.toString();
    }

    public String possibleCB(boolean castleBK, boolean castleBQ) {
        StringBuilder list = new StringBuilder();
        // must empty places
        if (castleBK && (occupied & ((1L << 5) | (1L << 6))) == 0) {
            list.append("0406");
        }
        if (castleBQ && (occupied & ((1L << 1) | (1L << 2) | (1L << 3))) == 0) {
            list.append("0402");
        }
        return list.toString();
    }

    public long unsafeForBlack(long[] pieces) {
        // pawn
        long unsafe = (pieces[WP] >> 7) & ~columnMasks8[0];
        unsafe |= (pieces[WP] >> 9) & ~columnMasks8[7];

        // rest
        unsafe |= isSafe(pieces, -6);
        return unsafe;
    }

    public long unsafeForWhite(long[] pieces) {
        // pawn
        long unsafe = (pieces[BP] << 7) & ~columnMasks8[7];
        unsafe |= (pieces[BP] << 9) & ~columnMasks8[0];

        // rest
        unsafe |= isSafe(pieces, 0);
        return unsafe;
    }

    private long isSafe(long[] pieces, int offset) {
        occupied = getOccupied(pieces); // for diagonal and straight moves
        long unsafe = 0L;
        // knight
        long board = pieces[BN + offset];
        long knight = board & -board;
        while (knight != 0) {
            int location = Long.numberOfTrailingZeros(knight);
            long moves = location > 18 ? KNIGHT_SPAN << (location - 18) : KNIGHT_SPAN >> (18 - location);
            moves = location % 8 < 4 ? moves & ~columnGH : moves & ~columnAB;
            unsafe |= moves;
            board &= ~knight;
            knight = board & -board;
        }
        // bishop | queen
        long QB = pieces[BQ + offset] | pieces[BB + offset];
        long bishopQueen = QB & -QB;
        while (bishopQueen != 0) {
            int location = Long.numberOfTrailingZeros(bishopQueen);
            long moves = diagonalMoves(location);
            unsafe |= moves;
            QB &= ~bishopQueen;
            bishopQueen = QB & -QB;
        }
        // rook | queen
        long QR = pieces[BQ + offset] | pieces[BR + offset];
        long rookQueen = QR & -QR;
        while (rookQueen != 0) {
            int location = Long.numberOfTrailingZeros(rookQueen);
            long moves = straightMoves(location);
            unsafe |= moves;
            QR &= ~rookQueen;
            rookQueen = QR & -QR;
        }
        // king
        int location = Long.numberOfTrailingZeros(pieces[BK + offset]);
        long moves = location > 9 ? KING_SPAN << (location - 9) : KING_SPAN >> (9 - location);
        moves = location % 8 < 4 ? moves & ~columnGH : moves & ~columnAB;
        unsafe |= moves;

        return unsafe;
    }

    private long getOccupied(long[] pieces) {
        long occupied = 0L;
        for (int i = 0; i < pieces.length - 1; i++)
            occupied |= pieces[i];
        return occupied;
    }

}
