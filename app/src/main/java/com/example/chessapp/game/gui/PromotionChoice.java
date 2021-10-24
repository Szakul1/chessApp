package com.example.chessapp.game.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import com.example.chessapp.R;

@SuppressLint("ViewConstructor")
public class PromotionChoice extends View {

    private final DialogManager dialogManager;
    private final Bitmap pieces;
    private final Rect[] piecesSource;
    private final boolean white;
    private Rect[][] piecesDst;
    private int blockSize;

    public PromotionChoice(DialogManager dialogManager, Context context, Rect[] piecesSource, boolean white) {
        super(context);
        this.dialogManager = dialogManager;
        pieces = BitmapFactory.decodeResource(context.getResources(), R.drawable.pieces);
        this.piecesSource = piecesSource;
        this.white = white;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint p = new Paint();

        p.setColor(Color.rgb(118, 150, 86));
        canvas.drawRect(0, 0, blockSize, blockSize, p);
        canvas.drawRect(blockSize, blockSize, blockSize * 2, blockSize * 2, p);

        if (white) {
            canvas.drawBitmap(pieces, piecesSource[1], piecesDst[0][0], p);
            canvas.drawBitmap(pieces, piecesSource[2], piecesDst[0][1], p);
            canvas.drawBitmap(pieces, piecesSource[3], piecesDst[1][0], p);
            canvas.drawBitmap(pieces, piecesSource[4], piecesDst[1][1], p);
        } else {
            canvas.drawBitmap(pieces, piecesSource[7], piecesDst[0][0], p);
            canvas.drawBitmap(pieces, piecesSource[8], piecesDst[0][1], p);
            canvas.drawBitmap(pieces, piecesSource[9], piecesDst[1][0], p);
            canvas.drawBitmap(pieces, piecesSource[10], piecesDst[1][1], p);
        }

        p.setColor(Color.WHITE);
        p.setTextSize((int) (blockSize / 6));
        p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Queen", (int) (blockSize / 2), blockSize - 20, p);
        canvas.drawText("Rook", (int) (blockSize / 2) + blockSize, blockSize - 20 + blockSize, p);
        p.setColor(Color.BLACK);
        canvas.drawText("Knight", (int) (blockSize / 2) + blockSize, blockSize - 20, p);
        canvas.drawText("Bishop", (int) (blockSize / 2), blockSize - 20 + blockSize, p);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        blockSize = h / 2;
        piecesDst = new Rect[2][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                piecesDst[i][j] = new Rect(i * blockSize + 80, j * blockSize + 80,
                        (i + 1) * blockSize - 80, (j + 1) * blockSize - 80);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int newX = (int) (event.getX() / getWidth() * 2);
        int newY = (int) (event.getY() / getHeight() * 2);

        char piece = ' ';
        switch (newY + "" + newX) {
            case "00":
                piece = 'Q';
                break;
            case "01":
                piece = 'N';
                break;
            case "10":
                piece = 'B';
                break;
            case "11":
                piece = 'R';
                break;
        }
        dialogManager.cancelDialog(white ? piece : Character.toLowerCase(piece));

        return false;
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }


}
