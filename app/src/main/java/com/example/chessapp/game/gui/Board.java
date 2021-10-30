package com.example.chessapp.game.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.chessapp.game.type.Move;
import com.example.chessapp.menu.GameFragment;

@SuppressLint("ViewConstructor")
public class Board extends SurfaceView implements SurfaceHolder.Callback {
    // constants
    private final static int boardSize = 8;
    private static final int WHITE_BOARD_COLOR = Color.rgb(238, 238, 210);
    private static final int GREEN_BOARD_COLOR = Color.rgb(118, 150, 86);

    private final GameFragment gameFragment;

    // communication component
    private final char[][] chessBoard;

    // drawable values
    private float pieceWidth;
    private float pieceHeight;
    private final Bitmap pieces;
    private final Rect[] piecesSource;

    // flags
    private final boolean twoPlayers;
    private final boolean color;

    public Board(GameFragment gameFragment, char[][] chessBoard, Context context, boolean twoPlayers,
                 boolean color, Bitmap pieces, Rect[] piecesSource) {
        super(context);
        this.gameFragment = gameFragment;
        this.chessBoard = chessBoard;
        this.twoPlayers = twoPlayers;
        this.color = color;
        this.pieces = pieces;
        this.piecesSource = piecesSource;
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        pieceWidth = getWidth() / (float) boardSize;
        pieceHeight = getHeight() / (float) boardSize;
        repaint();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        drawBoard(canvas);
        if (!color) {
            canvas.rotate(180, (float) getWidth() / 2, (float) getHeight() / 2);
        }
        drawPieces(canvas);
        drawMoves(canvas);
    }

    public void repaint() {
        Canvas canvas = getHolder().lockCanvas();
        draw(canvas);
        getHolder().unlockCanvasAndPost(canvas);
    }

    private void drawMoves(Canvas canvas) {
        Paint p = new Paint();
        p.setARGB(255, 211, 211, 211);
        int radius;
        for (Move move : gameFragment.moves) {
            if (gameFragment.chessBoard[move.endRow][move.endCol] != ' ') {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(10);
                radius = 60;
            } else {
                p.setStyle(Paint.Style.FILL);
                radius = 20;
            }

            canvas.drawCircle(move.endCol * pieceWidth + pieceWidth / 2,
                    move.endRow * pieceHeight + pieceWidth / 2, radius, p);
        }
    }

    private void drawPieces(Canvas canvas) {
        Rect dst;
        Rect src = null;
        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(10);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boolean draw = true;
                dst = new Rect((int) pieceWidth * j, (int) pieceHeight * i,
                        (j + 1) * (int) pieceWidth, (i + 1) * (int) pieceHeight);
                switch (chessBoard[i][j]) {
                    case 'K':
                        if (!gameFragment.kingSafe(true)) {
                            canvas.drawRect(dst, p);
                        }
                        src = piecesSource[0];
                        break;
                    case 'Q':
                        src = piecesSource[1];
                        break;
                    case 'B':
                        src = piecesSource[2];
                        break;
                    case 'N':
                        src = piecesSource[3];
                        break;
                    case 'R':
                        src = piecesSource[4];
                        break;
                    case 'P':
                        src = piecesSource[5];
                        break;
                    case 'k':
                        if (!gameFragment.kingSafe(false)) {
                            canvas.drawRect(dst, p);
                        }
                        src = piecesSource[6];
                        break;
                    case 'q':
                        src = piecesSource[7];
                        break;
                    case 'b':
                        src = piecesSource[8];
                        break;
                    case 'n':
                        src = piecesSource[9];
                        break;
                    case 'r':
                        src = piecesSource[10];
                        break;
                    case 'p':
                        src = piecesSource[11];
                        break;
                    default:
                        draw = false;
                }
                if (draw) {
                    if (!color || twoPlayers && Character.isLowerCase(chessBoard[i][j])) {
                        canvas.save();
                        canvas.rotate(180, dst.centerX(), dst.centerY());
                        canvas.drawBitmap(pieces, src, dst, null);
                        canvas.restore();
                    } else {
                        canvas.drawBitmap(pieces, src, dst, null);
                    }
                }
            }
        }
    }

    private void drawBoard(Canvas canvas) {
        Paint p = new Paint();
        p.setTextSize(pieceWidth / 3);
        p.setAntiAlias(true);
        Rect bounds = new Rect();

        boolean color = false;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                color = !color;
                p.setColor(getPaintColor(color));
                canvas.drawRect(j * pieceWidth, i * pieceWidth, (j + 1) * pieceWidth,
                        (i + 1) * pieceHeight, p);
                p.setColor(getPaintColor(!color));
                if (i == boardSize - 1) {
                    String text = "" + (char) (this.color ? 'a' + j : 'h' - j);
                    p.getTextBounds(text, 0, 1, bounds);
                    canvas.drawText(text, (j + 1) * pieceWidth - (bounds.width() + 3), getHeight() - bounds.bottom - 3, p);
                }
            }
            p.setColor(getPaintColor(color));
            String text = "" + (this.color ? 8 - i : i + 1);
            p.getTextBounds(text, 0, 1, bounds);
            canvas.drawText(text, 3, i * pieceWidth + bounds.height() + 3, p);
            color = !color;
        }
        if (gameFragment.selection) {
            p.setARGB(128, 255, 255, 0);
            canvas.drawRect(gameFragment.selectedX * pieceWidth, gameFragment.selectedY * pieceWidth,
                    (gameFragment.selectedX + 1) * pieceWidth,
                    (gameFragment.selectedY + 1) * pieceHeight, p);
        }
    }

    private int getPaintColor(boolean color) {
        if (!color)
            return WHITE_BOARD_COLOR;
        else
            return GREEN_BOARD_COLOR;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int newX = (int) (event.getX() / getWidth() * boardSize);
        int newY = (int) (event.getY() / getHeight() * boardSize);
        gameFragment.sendInput(newX, newY);

        repaint();
        return false;
    }
}
