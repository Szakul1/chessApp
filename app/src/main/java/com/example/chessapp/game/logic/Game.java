package com.example.chessapp.game.logic;

import static com.example.chessapp.game.MoveType.CASTLE;
import static com.example.chessapp.game.MoveType.EN_PASSANT;
import static com.example.chessapp.game.MoveType.NORMAL;
import static com.example.chessapp.game.MoveType.PROMOTION;
import static com.example.chessapp.game.logic.BitBoards.BB;
import static com.example.chessapp.game.logic.BitBoards.BK;
import static com.example.chessapp.game.logic.BitBoards.BN;
import static com.example.chessapp.game.logic.BitBoards.BP;
import static com.example.chessapp.game.logic.BitBoards.BQ;
import static com.example.chessapp.game.logic.BitBoards.BR;
import static com.example.chessapp.game.logic.BitBoards.CBK;
import static com.example.chessapp.game.logic.BitBoards.CBQ;
import static com.example.chessapp.game.logic.BitBoards.CWK;
import static com.example.chessapp.game.logic.BitBoards.CWQ;
import static com.example.chessapp.game.logic.BitBoards.EP;
import static com.example.chessapp.game.logic.BitBoards.WB;
import static com.example.chessapp.game.logic.BitBoards.WK;
import static com.example.chessapp.game.logic.BitBoards.WN;
import static com.example.chessapp.game.logic.BitBoards.WP;
import static com.example.chessapp.game.logic.BitBoards.WQ;
import static com.example.chessapp.game.logic.BitBoards.WR;

import android.util.Log;
import android.widget.ProgressBar;

import com.example.chessapp.game.Move;
import com.example.chessapp.game.frontend.Board;
import com.example.chessapp.game.logic.engine.Analyze;
import com.example.chessapp.game.logic.engine.Engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class Game {
    private final Board board;
    private final Engine engine;
    // r - rook, k - knight, b - bishop, q - queen, a - king p - pawn
    // capital for white lower for black
    private final char[][] startingBoard = {
            {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
            {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
            {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}};
    public char[][] chessBoard;
    //    public char[][] chessBoard = {
//            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//            {' ', ' ', ' ', 'k', ' ', ' ', ' ', ' '},
//            {' ', ' ', 'q', ' ', ' ', ' ', ' ', ' '},
//            {' ', ' ', ' ', ' ', ' ', 'K', ' ', ' '}};
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

    public List<Move> moveHistory = new ArrayList<>();

    /*
        Initializing board
     */

    public void arrayToBitboards() {
        boards = new long[]{0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,};
        castleFlags = new boolean[]{true, true, true, true};
        resetBoard();
        long binary = 1L;
        for (int i = 0; i < 64; i++) {
            int board = getBoardFromChar(chessBoard[i / 8][i % 8]);
            if (board != -1) {
                boards[board] += binary;
            }
            binary = binary << 1;
        }
    }

    public static int getBoardFromChar(char piece) {
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

    public static void printBoard(char[][] board) {
        String output = "";
        for (char[] chars : board) {
            for (char aChar : chars) output += aChar;
            output += '\n';
        }
        Log.d("test", output);
    }

    private void resetBoard() {
        chessBoard = new char[8][8];
        for (int i = 0; i < startingBoard.length; i++)
            chessBoard[i] = Arrays.copyOf(startingBoard[i], startingBoard.length);
    }

    public Game(Board board, boolean white) {
        this.board = board;
        arrayToBitboards();
        engine = new Engine(this);
    }

    /*
        Making moves
     */

    public void makeRealMove(Move move) {
        castleFlags = updateCastling(move, boards, castleFlags);
        boards = makeMove(move, boards);
        move.capturePiece = updateBoard(move);
        moveHistory.add(move);
    }

    public boolean checkMoveAndMake(int startRow, int startCol, int endRow, int endCol, boolean white) {
        List<Move> possibleMoves = possibleMoves(white);
        for (Move move : possibleMoves) {
            if (startRow == move.startRow && startCol == move.startCol && endRow == move.endRow && endCol == move.endCol) {
                if (move.type == PROMOTION) {
                    board.showDialog(move);
                    return false;
                }
                makeRealMove(move);
                return true;
            }
        }
        return false;
    }

    public void response(boolean white) {
        board.repaint();

        int score = engine.findBestMove(boards, castleFlags, white);
        score = white ? score : -score;
        Move move = engine.bestMove;

        boolean isMove = move != null;
        if (isMove) {
            makeRealMove(move);
        }
        finishGame(isMove, white);
        board.updateBar(score, engine.mate);
    }

    public long[] makeMove(Move move, long[] pieces) {
        long tempWR = makeMoveForBoard(pieces[WR], move, 'R');
        long tempBR = makeMoveForBoard(pieces[BR], move, 'r');
        return new long[]{
                makeMoveForBoard(pieces[WP], move, 'P'),
                makeMoveForBoard(pieces[WN], move, 'N'),
                makeMoveForBoard(pieces[WB], move, 'B'),
                makeMoveCastle(tempWR, move, 'R'),
                makeMoveForBoard(pieces[WQ], move, 'Q'),
                makeMoveForBoard(pieces[WK], move, 'K'),
                makeMoveForBoard(pieces[BP], move, 'p'),
                makeMoveForBoard(pieces[BN], move, 'n'),
                makeMoveForBoard(pieces[BB], move, 'b'),
                makeMoveCastle(tempBR, move, 'r'),
                makeMoveForBoard(pieces[BQ], move, 'q'),
                makeMoveForBoard(pieces[BK], move, 'k'),
                makeMoveEP(pieces[WP] | pieces[BP], move),
        };
    }

    /*
        Getting moves
     */

    public List<Move> possibleMoves(boolean white) {
        return possibleMoves(white, boards, castleFlags);
    }

    public List<Move> possibleMoves(boolean white, long[] boards, boolean[] castleFlags) {
        List<Move> moves = white ? possibleMovesW(boards, castleFlags[CWK], castleFlags[CWQ]) :
                possibleMovesB(boards, castleFlags[CBK], castleFlags[CBQ]);
        ListIterator<Move> iterator = moves.listIterator();
        while (iterator.hasNext()) {
            Move move = iterator.next();
            long[] nextBoards = makeMove(move, boards);
            long unsafe = white ? unsafeForWhite(nextBoards) : unsafeForBlack(nextBoards);
            // checking castle square safe TODO
//            if (move.equals("CWQC") && (unsafe & (1L << 59)) != 0 ||
//                    move.equals("CWKC") && (unsafe & (1L << 61)) != 0 ||
//                    move.equals("CBQC") && (unsafe & (1L << 3)) != 0 ||
//                    move.equals("CBKC") && (unsafe & (1L << 5)) != 0) {
//                continue;
//            }
            if ((unsafe & (white ? nextBoards[WK] : nextBoards[BK])) != 0) {
                iterator.remove();
            }
        }
        return moves;
    }

    public List<Move> getMovesForPiece(int row, int col, boolean white) {
        List<Move> moves = possibleMoves(white);
        ListIterator<Move> iterator = moves.listIterator();

        while (iterator.hasNext()) {
            Move move = iterator.next();
            if (move.startRow != row || move.startCol != col) {
                iterator.remove();
            }
        }
        return moves;
    }

    public int scoreMove(boolean white) {
        return engine.scoreMove(boards, castleFlags, white);
    }

    /*
        Updating board
     */

    public char updateBoard(Move move) {
        char piece = chessBoard[move.endRow][move.endRow];
        if (move.type == PROMOTION) {
            chessBoard[move.endRow][move.endCol] = move.promotionPiece;
        } else {
            chessBoard[move.endRow][move.endCol] = chessBoard[move.startRow][move.startCol];
        }
        chessBoard[move.startRow][move.startCol] = ' ';
        if (move.type == CASTLE) {
            chessBoard[move.startRow][move.rookEndCol] = chessBoard[move.startRow][move.rookStartCol];
            chessBoard[move.startRow][move.rookStartCol] = ' ';
        } else if (move.type == EN_PASSANT) {
            chessBoard[move.startRow][move.endCol] = ' ';
        }
        return piece;
    }

    public void undoMove(Move move) {
        boolean white = Character.isUpperCase(chessBoard[move.endRow][move.endCol]);
        if (move.type == PROMOTION) {
            chessBoard[move.startRow][move.startCol] = white ? 'P' : 'p';
        } else {
            chessBoard[move.startRow][move.startCol] = chessBoard[move.endRow][move.endCol];
        }
        chessBoard[move.endRow][move.endCol] = move.capturePiece;
        if (move.type == CASTLE) {
            chessBoard[move.startRow][move.rookStartCol] = chessBoard[move.startRow][move.rookEndCol];
            chessBoard[move.startRow][move.rookEndCol] = ' ';
        } else if (move.type == EN_PASSANT) {
            chessBoard[move.startRow][move.endCol] = white ? 'p' : 'P';
        }
    }

    /*
        Analyze
     */

    public Analyze startAnalyze(boolean white, ProgressBar bar) {
        arrayToBitboards();
        Analyze analyze = new Analyze(this, engine);
        analyze.analyzeGame(moveHistory, boards, castleFlags, white, bar);
        resetBoard();
        return analyze;
    }

    /*
        Other functions
     */

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

    public boolean capture(int row, int col) {
        return chessBoard[row][col] != ' ';
    }

    public void gameFinished(boolean white) {
        List<Move> moves = possibleMoves(white);
        finishGame(!moves.isEmpty(), white);
    }

    private void finishGame(boolean moveCounter, boolean white) {
        if (!moveCounter) {
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

    /*
        Game logic
     */

    public boolean[] updateCastling(Move move, long[] pieces, boolean[] castleFlags) {
        boolean[] flags = Arrays.copyOf(castleFlags, castleFlags.length);
        if (move.type == NORMAL || move.type == CASTLE) {
            int start = move.startRow * 8 + move.startCol;
            if (((1L << start) & pieces[WK]) != 0) {
                flags[CWK] = false;
                flags[CWQ] = false;
            } else if (((1L << start) & pieces[BK]) != 0) {
                flags[CBK] = false;
                flags[CBQ] = false;
            } else if (((1L << start) & pieces[WR] & (1L << CASTLE_ROOKS[0])) != 0) {
                flags[CWK] = false;
            } else if (((1L << start) & pieces[WR] & (1L << CASTLE_ROOKS[1])) != 0) {
                flags[CWQ] = false;
            } else if (((1L << start) & pieces[BR] & (1L << CASTLE_ROOKS[2])) != 0) {
                flags[CBK] = false;
            } else if (((1L << start) & pieces[BR] & (1L << CASTLE_ROOKS[3])) != 0) {
                flags[CBQ] = false;
            }
        }
        return flags;
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

    public long makeMoveForBoard(long board, Move move, char promotionType) {
        int start = move.startRow * 8 + move.startCol;
        int end = move.endRow * 8 + move.endCol;
        if (move.type != PROMOTION) {
            if (((board >> start) & 1) == 1) { // it is this board
                board &= ~(1L << start); // remove from start
                board |= (1L << end); // add to end
            } else {
                board &= ~(1L << end); // remove from end (capture)
            }
        } else {
            if (promotionType == move.promotionPiece) { // promoted piece
                board |= (1L << end); // add to end
            } else {
                board &= ~(1L << start); // remove from start
                board &= ~(1L << end); // remove from end
            }
        }
        if (move.type == EN_PASSANT) {
            int enPassant = move.startRow * 8 + move.endCol;
            board &= ~(1L << enPassant); // remove en passant pawn
        }
        return board;
    }

    public long makeMoveCastle(long rookBoard, Move move, char type) {
        if (move.type == CASTLE) {
            int start = move.startRow * 8 + move.rookStartCol;
            int end = move.endRow * 8 + move.rookEndCol;
            if (type == 'R' && move.startRow == 7 || type == 'r' && move.startRow == 0) {
                rookBoard &= ~(1L << start); // remove from start
                rookBoard |= (1L << end); // add to end
            }
        }
        return rookBoard;
    }

    public long makeMoveEP(long board, Move move) {
        if (isDoublePush(move, board)) {
            return (move.startRow == 1 ? rowMasks8[3] : rowMasks8[4]) & columnMasks8[move.startCol];
        } else
            return 0;
    }

    public static boolean isDoublePush(Move move, long board) {
        int start = move.startRow * 8 + move.startCol;
        return (Math.abs(move.startRow - move.endRow) == 2) && (((board >> start) & 1) == 1);
    }

    public List<Move> possibleMovesW(long[] pieces, boolean CWK, boolean CWQ) {
        myPieces = getMyPieces(true, pieces);
        notMyPieces = ~(myPieces);
        occupied = getOccupied(pieces);
        empty = ~occupied;
        long unSafe = unsafeForWhite(pieces);
        List<Move> moves = new ArrayList<>();

        possibleWP(moves, pieces[WP], pieces[BP], pieces[EP]);
        possibleN(moves, pieces[WN]);
        possibleB(moves, pieces[WB]);
        possibleR(moves, pieces[WR]);
        possibleQ(moves, pieces[WQ]);
        possibleK(moves, pieces[WK]);
        possibleCW(moves, CWK, CWQ, unSafe, pieces[WK], pieces[WR]);
        return moves;
    }

    public List<Move> possibleMovesB(long[] pieces, boolean CBK, boolean CBQ) {
        myPieces = getMyPieces(false, pieces);
        notMyPieces = ~(myPieces);
        occupied = getOccupied(pieces);
        empty = ~occupied;
        long unSafe = unsafeForBlack(pieces);
        List<Move> moves = new ArrayList<>();

        possibleBP(moves, pieces[BP], pieces[WP], pieces[EP]);
        possibleN(moves, pieces[BN]);
        possibleB(moves, pieces[BB]);
        possibleR(moves, pieces[BR]);
        possibleQ(moves, pieces[BQ]);
        possibleK(moves, pieces[BK]);
        possibleCB(moves, CBK, CBQ, unSafe, pieces[BK], pieces[BR]);
        return moves;
    }

    public static long getMyPieces(boolean white, long[] pieces) {
        return white ? pieces[WP] | pieces[WN] | pieces[WB] | pieces[WR] | pieces[WQ] | pieces[WK] :
                pieces[BP] | pieces[BN] | pieces[BB] | pieces[BR] | pieces[BQ] | pieces[BK];
    }

    public void possibleWP(List<Move> moveList, long WP, long BP, long EP) {

        long pawnMoves = (WP >> 7) & notMyPieces & occupied & ~rowMasks8[0] & ~columnMasks8[0]; // capture right
        addPawnMoves(moveList, pawnMoves, 1, -1);

        pawnMoves = (WP >> 9) & notMyPieces & occupied & ~rowMasks8[0] & ~columnMasks8[7]; // capture left
        addPawnMoves(moveList, pawnMoves, 1, 1);

        pawnMoves = (WP >> 8) & empty & ~rowMasks8[0]; // move 1 up
        addPawnMoves(moveList, pawnMoves, 1, 0);

        pawnMoves = (WP >> 16) & empty & (empty >> 8) & rowMasks8[4]; // move 2 up
        addPawnMoves(moveList, pawnMoves, 2, 0);

        // promotion: y1, y2, promotion, 'P'

        pawnMoves = (WP >> 7) & notMyPieces & occupied & rowMasks8[0] & ~columnMasks8[0]; // capture right
        addPromotion(moveList, pawnMoves, 1, -1, true);

        pawnMoves = (WP >> 9) & notMyPieces & occupied & rowMasks8[0] & ~columnMasks8[7]; // capture left
        addPromotion(moveList, pawnMoves, 1, 1, true);

        pawnMoves = (WP >> 8) & notMyPieces & empty & rowMasks8[0]; // move 1 up
        addPromotion(moveList, pawnMoves, 1, 0, true);

        // el passant: y1, y2, Space, 'E'

        // right
        pawnMoves = (WP << 1) & BP & rowMasks8[3] & ~columnMasks8[0] & EP;
        if (pawnMoves != 0) {
            int i = Long.numberOfTrailingZeros(pawnMoves);
            moveList.add(new Move(3, i % 8 - 1, 2, i % 8, EN_PASSANT));
        }
        // left
        pawnMoves = (WP >> 1) & BP & rowMasks8[3] & ~columnMasks8[7] & EP;
        if (pawnMoves != 0) {
            int i = Long.numberOfTrailingZeros(pawnMoves);
            moveList.add(new Move(3, i % 8 + 1, 2, i % 8, EN_PASSANT));
        }

    }

    public void possibleBP(List<Move> moveList, long BP, long WP, long EP) {

        long pawnMoves = (BP << 7) & notMyPieces & occupied & ~rowMasks8[7] & ~columnMasks8[7]; // capture right
        addPawnMoves(moveList, pawnMoves, -1, 1);

        pawnMoves = (BP << 9) & notMyPieces & occupied & ~rowMasks8[7] & ~columnMasks8[0]; // capture left
        addPawnMoves(moveList, pawnMoves, -1, -1);

        pawnMoves = (BP << 8) & empty & ~rowMasks8[7]; // move 1 up
        addPawnMoves(moveList, pawnMoves, -1, 0);

        pawnMoves = (BP << 16) & empty & (empty << 8) & rowMasks8[3]; // move 2 up
        addPawnMoves(moveList, pawnMoves, -2, 0);

        // promotion

        pawnMoves = (BP << 7) & notMyPieces & occupied & rowMasks8[7] & ~columnMasks8[7]; // capture right
        addPromotion(moveList, pawnMoves, -1, 1, false);

        pawnMoves = (BP << 9) & notMyPieces & occupied & rowMasks8[7] & ~columnMasks8[0]; // capture left
        addPromotion(moveList, pawnMoves, -1, -1, false);

        pawnMoves = (BP << 8) & notMyPieces & empty & rowMasks8[7]; // move 1 up
        addPromotion(moveList, pawnMoves, -1, 0, false);

        // el passant

        // right
        pawnMoves = (BP >> 1) & WP & rowMasks8[4] & ~columnMasks8[7] & EP;
        if (pawnMoves != 0) {
            int i = Long.numberOfTrailingZeros(pawnMoves);
            moveList.add(new Move(4, i % 8 + 1, 5, i % 8, EN_PASSANT));
        }
        // left
        pawnMoves = (BP << 1) & WP & rowMasks8[4] & ~columnMasks8[0] & EP;
        if (pawnMoves != 0) {
            int i = Long.numberOfTrailingZeros(pawnMoves);
            moveList.add(new Move(4, i % 8 - 1, 5, i % 8, EN_PASSANT));
        }

    }

    private void addPromotion(List<Move> moveList, long pawnMoves, int addRow, int addCol, boolean white) {
        char[] promotion = white ? new char[]{'Q', 'R', 'B', 'N'} : new char[]{'q', 'r', 'b', 'n'};
        long firstPawn = pawnMoves & -pawnMoves; //&(pawnMoves-1)
        while (firstPawn != 0) {
            int i = Long.numberOfTrailingZeros(firstPawn);
            int row = i / 8, col = i % 8;
            for (char piece : promotion) {
                moveList.add(new Move(row + addRow, col + addCol, row, col, PROMOTION, piece));
            }
            pawnMoves &= ~firstPawn;
            firstPawn = pawnMoves & -pawnMoves; //&(pawnMoves-1)
        }
    }

    private void addPawnMoves(List<Move> moveList, long pawnMoves, int addRow, int addCol) {
        long pawn = pawnMoves & -pawnMoves; //&(pawnMoves-1)
        while (pawn != 0) {
            int i = Long.numberOfTrailingZeros(pawn);
            int row = i / 8, col = i % 8;
            moveList.add(new Move(row + addRow, col + addCol, row, col, NORMAL));
            pawnMoves &= ~pawn;
            pawn = pawnMoves & -pawnMoves; //&(pawnMoves-1)
        }
    }

    private void possibleB(List<Move> moveList, long BBoard) {
        getSlidingMoves(moveList, BBoard, this::diagonalMoves);
    }

    public void possibleR(List<Move> moveList, long RBoard) {
        getSlidingMoves(moveList, RBoard, this::straightMoves);
    }

    public void possibleQ(List<Move> moveList, long QBoard) {
        getSlidingMoves(moveList, QBoard, this::getQueenMoves);
    }

    private long getQueenMoves(int position) {
        return straightMoves(position) | diagonalMoves(position);
    }

    private interface GetMoves {
        long getMoves(int location);
    }

    public void possibleK(List<Move> moveList, long KBoard) {
        int location = Long.numberOfTrailingZeros(KBoard);
        long moves = location > 9 ?
                KING_SPAN << (location - 9) :
                KING_SPAN >> (9 - location);
        checkArrayOut(moveList, location, moves);
    }

    public void possibleN(List<Move> moveList, long KBoard) {
        long knight = KBoard & -KBoard;
        while (knight != 0) {
            int location = Long.numberOfTrailingZeros(knight);
            long moves = location > 18 ?
                    KNIGHT_SPAN << (location - 18) :
                    KNIGHT_SPAN >> (18 - location);
            checkArrayOut(moveList, location, moves);
            KBoard &= ~knight;
            knight = KBoard & -KBoard;
        }
    }

    private void getSlidingMoves(List<Move> moveList, long board, GetMoves getMoves) {
        long piece = board & -board;
        while (piece != 0) {
            int location = Long.numberOfTrailingZeros(piece);
            long moves = getMoves.getMoves(location) & notMyPieces;
            addMove(moveList, location, moves);
            board &= ~piece;
            piece = board & -board;
        }
    }

    private void addMove(List<Move> moveList, int location, long moves) {
        long move = moves & -moves;
        while (move != 0) {
            int nextLocation = Long.numberOfTrailingZeros(move);
            moveList.add(new Move(location / 8, location % 8, nextLocation / 8, nextLocation % 8, NORMAL));
            moves &= ~move;
            move = moves & -moves;
        }
    }

    private void checkArrayOut(List<Move> moveList, int location, long moves) {
        moves = location % 8 < 4 ?
                moves & ~columnGH & notMyPieces :
                moves & ~columnAB & notMyPieces;
        addMove(moveList, location, moves);
    }

    public void possibleCW(List<Move> moves, boolean castleWK, boolean castleWQ, long unsafe, long king, long rook) {
        // must empty places
        if ((unsafe & king) == 0) { // not in check
            if (castleWK && (((1L << CASTLE_ROOKS[0]) & rook) != 0) && ((occupied | unsafe) & ((1L << 61) | (1L << 62))) == 0) {
                moves.add(new Move(7, 4, 7, 6, CASTLE, 7, 5));
            }
            if (castleWQ && (((1L << CASTLE_ROOKS[1]) & rook) != 0) && ((occupied | (unsafe & ~(1L << 57))) & ((1L << 57) | (1L << 58) | (1L << 59))) == 0) {
                moves.add(new Move(7, 4, 7, 2, CASTLE, 0, 3));
            }
        }
    }

    public void possibleCB(List<Move> moves, boolean castleBK, boolean castleBQ, long unsafe, long king, long rook) {
        // must empty places
        if ((unsafe & king) == 0) { // not in check
            if (castleBK && (((1L << CASTLE_ROOKS[2]) & rook) != 0) && ((occupied | unsafe) & ((1L << 5) | (1L << 6))) == 0) {
                moves.add(new Move(0, 4, 0, 6, CASTLE, 7, 5));
            }
            if (castleBQ && (((1L << CASTLE_ROOKS[3]) & rook) != 0) && ((occupied | (unsafe & ~(1L << 1))) & ((1L << 1) | (1L << 2) | (1L << 3))) == 0) {
                moves.add(new Move(0, 4, 0, 2, CASTLE, 0, 3));
            }
        }
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

    public static long getOccupied(long[] pieces) {
        long occupied = 0L;
        for (int i = 0; i < pieces.length - 1; i++)
            occupied |= pieces[i];
        return occupied;
    }

    public long[] getBoards() {
        return boards;
    }

    public void setBoards(long[] boards) {
        this.boards = boards;
    }

    public boolean[] getCastleFlags() {
        return castleFlags;
    }

    public void setCastleFlags(boolean[] castleFlags) {
        this.castleFlags = castleFlags;
    }
}
