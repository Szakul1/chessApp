package com.example.chessapp.game.logic;

import static com.example.chessapp.game.type.BitBoards.BB;
import static com.example.chessapp.game.type.BitBoards.BK;
import static com.example.chessapp.game.type.BitBoards.BN;
import static com.example.chessapp.game.type.BitBoards.BP;
import static com.example.chessapp.game.type.BitBoards.BQ;
import static com.example.chessapp.game.type.BitBoards.BR;
import static com.example.chessapp.game.type.BitBoards.CBK;
import static com.example.chessapp.game.type.BitBoards.CBQ;
import static com.example.chessapp.game.type.BitBoards.CWK;
import static com.example.chessapp.game.type.BitBoards.CWQ;
import static com.example.chessapp.game.type.BitBoards.EP;
import static com.example.chessapp.game.type.BitBoards.WB;
import static com.example.chessapp.game.type.BitBoards.WK;
import static com.example.chessapp.game.type.BitBoards.WN;
import static com.example.chessapp.game.type.BitBoards.WP;
import static com.example.chessapp.game.type.BitBoards.WQ;
import static com.example.chessapp.game.type.BitBoards.WR;
import static com.example.chessapp.game.type.MoveType.CASTLE;
import static com.example.chessapp.game.type.MoveType.EN_PASSANT;
import static com.example.chessapp.game.type.MoveType.NORMAL;
import static com.example.chessapp.game.type.MoveType.PROMOTION;

import com.example.chessapp.game.type.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class MoveGenerator {

    private static long notMyPieces;
    private static long myPieces;
    private static long occupied;
    private static long empty;
    private static final long columnAB = 217020518514230019L;
    private static final long columnGH = -4557430888798830400L;
    private static final long KING_SPAN = 460039L;
    private static final long KNIGHT_SPAN = 43234889994L;
    private static final long[] CASTLE_ROOKS = {63, 56, 7, 0};
    private static final long[] rowMasks8 = /* from rank1 to rank8 */
            {0xFFL, 0xFF00L, 0xFF0000L, 0xFF000000L, 0xFF00000000L, 0xFF0000000000L, 0xFF000000000000L,
                    0xFF00000000000000L};
    private static  final long[] columnMasks8 = /* from fileA to FileH */
            {0x101010101010101L, 0x202020202020202L, 0x404040404040404L, 0x808080808080808L, 0x1010101010101010L,
                    0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L};
    private static  final long[] DiagonalMasks8 = /* from top left to bottom right */
            {0x1L, 0x102L, 0x10204L, 0x1020408L, 0x102040810L, 0x10204081020L, 0x1020408102040L, 0x102040810204080L,
                    0x204081020408000L, 0x408102040800000L, 0x810204080000000L, 0x1020408000000000L,
                    0x2040800000000000L, 0x4080000000000000L, 0x8000000000000000L};
    private static  final long[] AntiDiagonalMasks8 = /* from top right to bottom left */
            {0x80L, 0x8040L, 0x804020L, 0x80402010L, 0x8040201008L, 0x804020100804L, 0x80402010080402L,
                    0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L, 0x1008040201000000L,
                    0x804020100000000L, 0x402010000000000L, 0x201000000000000L, 0x100000000000000L};

    public static int startPiece, targetPiece;
    public static int startSquare, targetSquare;

    /*
        Communication
     */

    public static boolean kingSafe(boolean white, long[] pieces) {
        return white ? (pieces[WK] & unsafeForWhite(pieces)) == 0 :
                (pieces[BK] & unsafeForBlack(pieces)) == 0;
    }

    public static List<Move> possibleMoves(boolean white, long[] boards, boolean[] castleFlags) {
        List<Move> moves = white ? possibleMovesW(boards, castleFlags[CWK], castleFlags[CWQ]) :
                possibleMovesB(boards, castleFlags[CBK], castleFlags[CBQ]);
        ListIterator<Move> iterator = moves.listIterator();
        while (iterator.hasNext()) {
            Move move = iterator.next();
            long[] nextBoards = makeMove(move, boards);
            long unsafe = white ? unsafeForWhite(nextBoards) : unsafeForBlack(nextBoards);
            // checking castle square safe TODO
            if ((unsafe & (white ? nextBoards[WK] : nextBoards[BK])) != 0) {
                iterator.remove();
            }
        }
        return moves;
    }

    public static long[] makeMove(Move move, long[] pieces) {
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

    public static boolean[] updateCastling(Move move, long[] pieces, boolean[] castleFlags) {
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

    public static long getMyPieces(boolean white, long[] pieces) {
        return white ? pieces[WP] | pieces[WN] | pieces[WB] | pieces[WR] | pieces[WQ] | pieces[WK] :
                pieces[BP] | pieces[BN] | pieces[BB] | pieces[BR] | pieces[BQ] | pieces[BK];
    }

    /**
     * Assigns moving piece to startPiece and target piece to targetPiece
     * @param move move
     * @param boards bit boards
     */
    public static void getPieces(Move move, long[] boards) {
        startSquare = move.startRow * 8 + move.startCol;
        targetSquare = move.endRow * 8 + move.endCol;
        for (int i = 0; i < boards.length - 1; i++) {
            if ((boards[i] & (1L << startSquare)) != 0) {
                startPiece = i;
            } else if ((boards[i] & (1L << targetSquare)) != 0) {
                targetPiece = i;
            }
        }
    }

    /**
     * Checks if move is capture
     * @param move move
     * @param opponentPieces bitboards of opponent pieces
     * @return true if capture
     */
    public static boolean captureMove(Move move, long opponentPieces) {
        int position = move.endRow * 8 + move.endCol;
        return ((1L << position) & opponentPieces) != 0;
    }

    /**
     * Returns occupied squares
     * @param pieces pieces
     * @return occupied bitboard
     */
    public static long getOccupied(long[] pieces) {
        long occupied = 0L;
        for (int i = 0; i < pieces.length - 1; i++) // or of all pieces
            occupied |= pieces[i];
        return occupied;
    }

    /*
        Private methods
     */

    private static long makeMoveForBoard(long board, Move move, char promotionType) {
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

    private static long makeMoveCastle(long rookBoard, Move move, char type) {
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

    private static long makeMoveEP(long board, Move move) {
        if (isDoublePush(move, board)) {
            return (move.startRow == 1 ? rowMasks8[3] : rowMasks8[4]) & columnMasks8[move.startCol];
        } else
            return 0;
    }

    private static boolean isDoublePush(Move move, long board) {
        int start = move.startRow * 8 + move.startCol;
        return (Math.abs(move.startRow - move.endRow) == 2) && (((board >> start) & 1) == 1);
    }

    private static List<Move> possibleMovesW(long[] pieces, boolean CWK, boolean CWQ) {
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

    private static List<Move> possibleMovesB(long[] pieces, boolean CBK, boolean CBQ) {
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

    private static void possibleWP(List<Move> moveList, long WP, long BP, long EP) {

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

    private static void possibleBP(List<Move> moveList, long BP, long WP, long EP) {

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

    private static void addPromotion(List<Move> moveList, long pawnMoves, int addRow, int addCol, boolean white) {
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

    /**
     * Adds pawn moves to list
     * @param moveList list to add
     * @param pawnMoves moves bitboard
     * @param addRow offset to get starting row
     * @param addCol offset to get starting col
     */
    private static void addPawnMoves(List<Move> moveList, long pawnMoves, int addRow, int addCol) {
        long pawn = pawnMoves & -pawnMoves; // get fir pawn
        while (pawn != 0) { // until no more pawns
            int location = Long.numberOfTrailingZeros(pawn); // location of pawn
            int row = location / 8, col = location % 8;
            moveList.add(new Move(row + addRow, col + addCol, row, col, NORMAL));
            pawnMoves &= ~pawn; // remove pawn from bitboard
            pawn = pawnMoves & -pawnMoves; // get next pawn
        }
    }

    /**
     * Adds move for bishop to list
     * @param moveList list ot add
     * @param BBoard bishop bitboard
     */
    private static void possibleB(List<Move> moveList, long BBoard) {
        getSlidingMoves(moveList, BBoard, MoveGenerator::diagonalMoves);
    }

    /**
     * Adds move for rook to list
     * @param moveList list ot add
     * @param RBoard rook bitboard
     */
    private static void possibleR(List<Move> moveList, long RBoard) {
        getSlidingMoves(moveList, RBoard, MoveGenerator::straightMoves);
    }

    /**
     * Adds move for queen to list
     * @param moveList list ot add
     * @param QBoard queen bitboard
     */
    private static void possibleQ(List<Move> moveList, long QBoard) {
        getSlidingMoves(moveList, QBoard, MoveGenerator::getQueenMoves);
    }

    /**
     * Returns moves for queen
     * @param position position of queen
     * @return bitboard of moves
     */
    private static long getQueenMoves(int position) {
        return straightMoves(position) | diagonalMoves(position);
    }

    /**
     * interface for function for getting moves for different pieces
     */
    private interface GetMoves {
        long getMoves(int location);
    }

    /**
     * Adds moves to list for sliding pieces
     * @param moveList list ot add
     * @param board bitboard of piece
     * @param getMoves function for getting moves for different pieces
     */
    private static void getSlidingMoves(List<Move> moveList, long board, GetMoves getMoves) {
        long piece = board & -board; // get first piece -b = ~(b-1)
        while (piece != 0) { // until no more pieces
            int location = Long.numberOfTrailingZeros(piece); // location of piece
            // get moves but only for empty squares and captures (not my pieces)
            long moves = getMoves.getMoves(location) & notMyPieces;
            addMove(moveList, location, moves); // add moves
            board &= ~piece; // remove pieces from bitboard
            piece = board & -board; // get next piece
        }
    }

    /**
     * Adds possible moves for knight to list
     * @param moveList list to add
     * @param KBoard bitboard of kings
     */
    public static void possibleK(List<Move> moveList, long KBoard) {
        int location = Long.numberOfTrailingZeros(KBoard); // location of king
        // 9 - location of king with all possible moves at most left bottom position
        long moves = location > 9 ?
                KING_SPAN << (location - 9) : // move forward
                KING_SPAN >> (9 - location); // move back
        checkArrayOut(moveList, location, moves); // check if move is out of board and add moves
    }

    /**
     * Adds possible moves for knight to list
     * @param moveList list to add
     * @param NBoard bitboard of knights
     */
    public static void possibleN(List<Move> moveList, long NBoard) {
        long knight = NBoard & -NBoard; // get first knight -b = ~(pawnMoves-1)
        while (knight != 0) { // until no more knights
            int location = Long.numberOfTrailingZeros(knight); // location of knight
            // 18 - location of knight with all possible moves at most left bottom position
            long moves = location > 18 ?
                    KNIGHT_SPAN << (location - 18) : // move forward
                    KNIGHT_SPAN >> (18 - location); // move back
            checkArrayOut(moveList, location, moves); // check if move is out of board and add moves
            NBoard &= ~knight; // remove check knight
            knight = NBoard & -NBoard; // get next knight
        }
    }

    /**
     * Checks if move is out of board for king and knight and calls addMove
     * @param moveList list to add moves
     * @param location starting location
     * @param moves bitboard of moves
     */
    private static void checkArrayOut(List<Move> moveList, int location, long moves) {
        moves = location % 8 < 4 ?
                moves & ~columnGH & notMyPieces :
                moves & ~columnAB & notMyPieces;
        addMove(moveList, location, moves);
    }

    /**
     * Creates moves from bitboard and adds to list
     * @param moveList list to add
     * @param location starting location
     * @param moves bitboard of possible moves
     */
    private static void addMove(List<Move> moveList, int location, long moves) {
        long move = moves & -moves;
        while (move != 0) {
            int nextLocation = Long.numberOfTrailingZeros(move);
            moveList.add(new Move(location / 8, location % 8, nextLocation / 8, nextLocation % 8, NORMAL));
            moves &= ~move;
            move = moves & -moves;
        }
    }

    static long straightMoves(int position) {
        int row = position / 8, col = position % 8;
        long binaryPiece = 1L << position;
        long movesHorizontal = (occupied - 2 * binaryPiece) ^
                Long.reverse(Long.reverse(occupied) - 2 * Long.reverse(binaryPiece));
        long movesVertical = ((occupied & columnMasks8[col]) - 2 * binaryPiece) ^
                Long.reverse(Long.reverse(occupied & columnMasks8[col]) -
                        (2 * Long.reverse(binaryPiece)));
        return (movesHorizontal & rowMasks8[row]) | (movesVertical & columnMasks8[col]);
    }

    static long diagonalMoves(int position) {
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


    /**
     * Adds possible castle moves for white to list
     * checks castle flags, rook in place
     * @param moves list to add
     * @param castleWK able to castle white king
     * @param castleWQ able to castle white queen
     * @param unsafe unsafe squares
     * @param king king bitboard
     * @param rook rook bitboard
     */
    private static void possibleCW(List<Move> moves, boolean castleWK, boolean castleWQ, long unsafe, long king, long rook) {
        if ((unsafe & king) == 0) { // not in check
            if (castleWK && // castle king side
                    (((1L << CASTLE_ROOKS[0]) & rook) != 0) && // rook is in correct place
                    ((occupied | unsafe) & ((1L << 61) | (1L << 62))) == 0) { // squares are empty and safe
                moves.add(new Move(7, 4, 7, 6, CASTLE, 7, 5));
            }
            if (castleWQ && // castle queen side
                    (((1L << CASTLE_ROOKS[1]) & rook) != 0) && // rook in correct place
                    // 3 squares must be empty, path for king must be safe
                    ((occupied | (unsafe & ~(1L << 57))) & ((1L << 57) | (1L << 58) | (1L << 59))) == 0) {
                moves.add(new Move(7, 4, 7, 2, CASTLE, 0, 3));
            }
        }
    }

    /**
     * Adds possible castle moves for black to list
     * checks castle flags, rook in place
     * @param moves list to add
     * @param castleBK able to castle black king
     * @param castleBQ able to castle black queen
     * @param unsafe unsafe squares
     * @param king king bitboard
     * @param rook rook bitboard
     */
    private static void possibleCB(List<Move> moves, boolean castleBK, boolean castleBQ, long unsafe, long king, long rook) {
        if ((unsafe & king) == 0) { // not in check
            if (castleBK && // castle king side
                    (((1L << CASTLE_ROOKS[2]) & rook) != 0) && // rook is in correct place
                    ((occupied | unsafe) & ((1L << 5) | (1L << 6))) == 0) { // squares are empty and safe
                moves.add(new Move(0, 4, 0, 6, CASTLE, 7, 5));
            }
            if (castleBQ && // castle queen side
                    (((1L << CASTLE_ROOKS[3]) & rook) != 0) && // rook is in correct place
                    // 3 squares must be empty, path for king must be safe
                    ((occupied | (unsafe & ~(1L << 1))) & ((1L << 1) | (1L << 2) | (1L << 3))) == 0) {
                moves.add(new Move(0, 4, 0, 2, CASTLE, 0, 3));
            }
        }
    }

    /**
     * Returns unsafe squares for black
     * @param pieces pieces
     * @return unsafe squares bitboard
     */
    public static long unsafeForBlack(long[] pieces) {
        // pawn
        long unsafe = (pieces[WP] >> 7) & ~columnMasks8[0]; // left
        unsafe |= (pieces[WP] >> 9) & ~columnMasks8[7]; // right

        // rest
        unsafe |= isSafe(pieces, -6);
        return unsafe;
    }

    /**
     * Returns unsafe squares for white
     * @param pieces pieces
     * @return unsafe squares bitboard
     */
    private static long unsafeForWhite(long[] pieces) {
        // pawn
        long unsafe = (pieces[BP] << 7) & ~columnMasks8[7]; // left
        unsafe |= (pieces[BP] << 9) & ~columnMasks8[0]; // right

        // rest
        unsafe |= isSafe(pieces, 0);
        return unsafe;
    }

    /**
     * Returns squares attacked by player without pawns
     * @param pieces pieces
     * @param offset 0 - for white, -6 for black to get pieces from array
     * @return attacked pieces bitboard
     */
    private static long isSafe(long[] pieces, int offset) {
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

}
