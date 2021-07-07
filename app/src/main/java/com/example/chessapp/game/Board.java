package com.example.chessapp.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.chessapp.R;

public class Board extends SurfaceView implements SurfaceHolder.Callback {

    private final static int boardSize = 8;
    private float pieceWidth;
    private float pieceHeight;
    private final Bitmap pieces;
    private Rect[] piecesSource;


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
    }

    public void repaint() {
        Canvas canvas = getHolder().lockCanvas();
        draw(canvas);
        getHolder().unlockCanvasAndPost(canvas);
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
    }

}
