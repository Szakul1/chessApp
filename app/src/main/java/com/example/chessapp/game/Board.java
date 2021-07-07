package com.example.chessapp.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chessapp.R;
import com.example.chessapp.game.logic.Game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Board extends SurfaceView implements SurfaceHolder.Callback {

    private final static int boardSize = 8;
    private float pieceWidth;
    private float pieceHeight;
    private final Bitmap pieces;
    private Rect[] piecesSource;
    private Game game;
    private Integer selectedX = null, selectedY = null;
    private boolean selection = false;
    private StringBuilder moves;
    private boolean animation = false;
    private float animationX, animationY;


    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        pieces = BitmapFactory.decodeResource(context.getResources(), R.drawable.pieces);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        int pieceImageSize = pieces.getHeight() / 2;
        pieceWidth = getWidth() / (float) boardSize;
        pieceHeight = getHeight() / (float) boardSize;
        piecesSource = new Rect[12];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                piecesSource[i * 6 + j] = new Rect(pieceImageSize * j, pieceImageSize * i,
                        (j + 1) * pieceImageSize, (i + 1) * pieceImageSize);
            }
        }
        game = new Game();
        moves = new StringBuilder();
        repaint();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        drawBoard(canvas);
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
        String moves = this.moves.toString();
        for (int i = 0; i < moves.length(); i += 5) {
            int x = Character.getNumericValue(moves.charAt(i + 3));
            int y = Character.getNumericValue(moves.charAt(i + 2));
            if (game.chessBoard[y][x] != ' ') {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(10);
                radius = 60;
            } else {
                p.setStyle(Paint.Style.FILL);
                radius = 20;
            }

            canvas.drawCircle(x * pieceWidth + pieceWidth / 2,
                    y * pieceHeight + pieceWidth / 2, radius, p);

        }
    }

    private void drawPieces(Canvas canvas) {
        Rect dst;
        Rect src = null;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boolean draw = true;
                if (animation && selectedX == j && selectedY == i) {
                    dst = new Rect((int) animationX, (int) animationY,
                            (int) (animationX + pieceWidth), (int) (animationY + pieceHeight));
                } else
                    dst = new Rect((int) pieceWidth * j, (int) pieceHeight * i,
                            (j + 1) * (int) pieceWidth, (i + 1) * (int) pieceHeight);
                switch (game.chessBoard[i][j]) {
                    case 'A':
                        src = piecesSource[0];
                        break;
                    case 'Q':
                        src = piecesSource[1];
                        break;
                    case 'B':
                        src = piecesSource[2];
                        break;
                    case 'K':
                        src = piecesSource[3];
                        break;
                    case 'R':
                        src = piecesSource[4];
                        break;
                    case 'P':
                        src = piecesSource[5];
                        break;
                    case 'a':
                        src = piecesSource[6];
                        break;
                    case 'q':
                        src = piecesSource[7];
                        break;
                    case 'b':
                        src = piecesSource[8];
                        break;
                    case 'k':
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
                    canvas.drawBitmap(pieces, src, dst, null);
                }
            }
        }
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    private void drawBoard(Canvas canvas) {
        Paint p = new Paint();

        boolean color = false;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                color = !color;
                if (color)
                    p.setColor(Color.rgb(238, 238, 210));
                else
                    p.setColor(Color.rgb(118, 150, 86));
                canvas.drawRect(j * pieceWidth, i * pieceWidth, (j + 1) * pieceWidth,
                        (i + 1) * pieceHeight, p);
            }
            color = !color;
        }
        if (selection) {
            p.setARGB(128, 255, 255, 0);
            canvas.drawRect(selectedX * pieceWidth, selectedY * pieceWidth,
                    (selectedX + 1) * pieceWidth,
                    (selectedY + 1) * pieceHeight, p);
        }
    }

    private void animation(int row, int col) {
        animation = true;
        selectedY = row;
        selectedX = col;
        float numberOfFrames = 10;
        float speedX = (col * pieceWidth - animationX) / numberOfFrames;
        float speedY = (row * pieceHeight - animationY) / numberOfFrames;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Runnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter == numberOfFrames) {
                    animation = false;
                    repaint();
                    executor.shutdown();
                    return;
                }

                animationX += speedX;
                animationY += speedY;

                repaint();
                counter++;
            }
        }, 0, (long) (50 / numberOfFrames), TimeUnit.MILLISECONDS);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation)
            return false;
        moves = new StringBuilder();
        int newX = (int) (event.getX() / getWidth() * boardSize);
        int newY = (int) (event.getY() / getHeight() * boardSize);
        if (selection && game.checkMove("" + selectedY + selectedX + newY + newX)) {
            selection = false;
            animationX = selectedX * pieceWidth;
            animationY = selectedY * pieceHeight;
            animation(newY, newX);
        } else
            selection = game.chessBoard[newY][newX] != ' ';
        if (selection) {
            game.checkMoveForPiece(newY * boardSize + newX, moves);
        }
        selectedX = newX;
        selectedY = newY;

        repaint();
        return false;
    }


}
