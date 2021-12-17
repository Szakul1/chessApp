package com.example.chessapp.game.logic;

import static com.example.chessapp.game.type.BitBoards.BB;
import static com.example.chessapp.game.type.BitBoards.BK;
import static com.example.chessapp.game.type.BitBoards.BN;
import static com.example.chessapp.game.type.BitBoards.BP;
import static com.example.chessapp.game.type.BitBoards.BQ;
import static com.example.chessapp.game.type.BitBoards.BR;
import static com.example.chessapp.game.type.BitBoards.WB;
import static com.example.chessapp.game.type.BitBoards.WK;
import static com.example.chessapp.game.type.BitBoards.WN;
import static com.example.chessapp.game.type.BitBoards.WP;
import static com.example.chessapp.game.type.BitBoards.WQ;
import static com.example.chessapp.game.type.BitBoards.WR;
import static com.example.chessapp.game.type.MoveType.CASTLE;
import static com.example.chessapp.game.type.MoveType.EN_PASSANT;
import static com.example.chessapp.game.type.MoveType.PROMOTION;

import android.widget.ProgressBar;

import com.example.chessapp.game.logic.engine.Analyze;
import com.example.chessapp.game.logic.engine.Engine;
import com.example.chessapp.game.type.Move;
import com.example.chessapp.menu.GameFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class Game {
    private final GameFragment gameFragment;
    private final Engine engine;
    // r - rook, k - knight, b - bishop, q - queen, a - king p - pawn
    // upper for white lower for black
    private final char[][] startingBoard = {
            {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
            {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
            {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}};
    private final char[][] chessBoard;
    private long[] boards = {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,};
    private boolean[] castleFlags = {true, true, true, true};
    private long hashKey;
    private final List<Move> moveHistory = new ArrayList<>();

    public Game(GameFragment gameFragment, char[][] chessBoard) {
        this.gameFragment = gameFragment;
        this.chessBoard = chessBoard;
        arrayToBitboards();
        engine = new Engine();
        hashKey = engine.zobrist.hashPosition(boards, castleFlags, true);
    }

    /**
     * Makes move
     * @param move move to make
     */
    public void makeMove(Move move) {
        hashKey = engine.zobrist.hashMove(hashKey, move, boards, castleFlags, gameFragment.whiteTurn);
        boolean capture = capture(move.endRow, move.endCol); // for sound playing
        castleFlags = MoveGenerator.updateCastling(move, boards, castleFlags);
        boards = MoveGenerator.makeMove(move, boards);
        move.capturePiece = updateBoard(move);
        moveHistory.add(move);
        gameFragment.playSound(capture);
    }

    /**
     * Makes move if is possible
     * @param startRow starting row
     * @param startCol starting column
     * @param endRow target row
     * @param endCol target column
     * @param white player to move
     * @return true if move is possible
     */
    public boolean checkMoveAndMake(int startRow, int startCol, int endRow, int endCol, boolean white) {
        List<Move> possibleMoves = possibleMoves(white);
        for (Move move : possibleMoves) {
            if (startRow == move.startRow && startCol == move.startCol && endRow == move.endRow && endCol == move.endCol) {
                if (move.type == PROMOTION) {
                    gameFragment.showPromotionDialog(move);
                    return false;
                }
                makeMove(move);
                return true;
            }
        }
        return false;
    }

    /**
     * Makes response after move
     * @param white player to move
     */
    public void response(boolean white) {
        int score = engine.findBestMove(boards, castleFlags, white, hashKey);
        score = white ? score : -score;
        Move move = engine.bestMove;

        boolean isMove = move != null;
        if (isMove) {
            makeMove(move);
        }
        finishGame(isMove, white);
        gameFragment.updateBar(score);
    }

    private List<Move> possibleMoves(boolean white) {
        return MoveGenerator.possibleMoves(white, boards, castleFlags);
    }

    /**
     * Returns possible move for the piece
     * @param row row of piece
     * @param col column of column
     * @param white player to move
     * @return list of moves for the piece
     */
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
        return engine.findBestMove(boards, castleFlags, white, hashKey);
    }


    public char updateBoard(Move move) {
        char piece = chessBoard[move.endRow][move.endCol];
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


    public Analyze startAnalyze(boolean white, ProgressBar bar) {
        arrayToBitboards();
        Analyze analyze = new Analyze(this, engine);
        hashKey = engine.zobrist.hashPosition(boards, castleFlags, true);
        analyze.analyzeGame(moveHistory, boards, castleFlags, white, bar, hashKey);
        resetBoard();
        return analyze;
    }


    public boolean kingSafe(boolean white) {
        return MoveGenerator.kingSafe(white, boards);
    }

    public boolean isMyPiece(int row, int col, boolean white) {
        int position = row * 8 + col;
        return (MoveGenerator.getMyPieces(white, boards) & (1L << position)) != 0;
    }

    public boolean capture(int row, int col) {
        return chessBoard[row][col] != ' ';
    }

    /**
     * Checks if game is finished
     * @param white player to move
     */
    public void gameFinished(boolean white) {
        List<Move> moves = possibleMoves(white);
        finishGame(!moves.isEmpty(), white);
    }


    private void finishGame(boolean moveCounter, boolean white) {
        if (!moveCounter) {
            if (kingSafe(white)) {
                gameFragment.finishedGame = "Draw Stalemate";
            } else {
                gameFragment.finishedGame = (white ? "Black" : "White") + " won";
            }
            gameFragment.showEndDialog();
        } else {
            long occupied = MoveGenerator.getOccupied(boards);
            if ((boards[WK] | boards[BK]) == occupied) {
                gameFragment.finishedGame = "Draw";
                gameFragment.showEndDialog();
            }
        }
    }

    private void resetBoard() {
        for (int i = 0; i < startingBoard.length; i++)
            chessBoard[i] = Arrays.copyOf(startingBoard[i], startingBoard.length);
    }

    private static int getBoardFromChar(char piece) {
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

    private void arrayToBitboards() {
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
}
