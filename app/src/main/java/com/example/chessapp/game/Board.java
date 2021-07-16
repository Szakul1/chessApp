package com.example.chessapp.game;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chessapp.MainActivity;
import com.example.chessapp.R;
import com.example.chessapp.game.logic.Game;
import com.example.chessapp.gui.PromotionChoice;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressLint("ViewConstructor")
public class Board extends SurfaceView implements SurfaceHolder.Callback {
    private final static int boardSize = 8;
    private float pieceWidth;
    private float pieceHeight;
    private final Bitmap pieces;
    private final boolean twoPlayers;
    public Rect[] piecesSource;
    private Game game;
    private Integer selectedX = null, selectedY = null;
    private boolean selection = false;
    private StringBuilder moves;
    private boolean animation = false;
    private float animationX, animationY;
    private final Context context;
    private AlertDialog dialog;
    private ProgressBar progressBar;
    private TextView progressText;
    private boolean promotion = false;
    private String promotionMove;
    private boolean whiteTurn = true;
    private String finishedGame = null;

    public Board(Context context, boolean twoPlayers) {
        super(context);
        pieces = BitmapFactory.decodeResource(context.getResources(), R.drawable.pieces);
        this.twoPlayers = twoPlayers;
        getHolder().addCallback(this);
        this.context = context;

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
        game = new Game(this, true);
        moves = new StringBuilder();
        RelativeLayout viewGroup = (RelativeLayout) getParent().getParent();
        progressBar = viewGroup.findViewById(R.id.positionBar);
        progressText = viewGroup.findViewById(R.id.positionValue);
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
            int x1 = Character.getNumericValue(moves.charAt(i + 1));
            if (moves.charAt(i + 4) == 'U') {
                x = x1;
                y = 0;
            } else if (moves.charAt(i + 4) == 'u') {
                x = x1;
                y = boardSize - 1;
            }

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
                    if (twoPlayers && Character.isLowerCase(game.chessBoard[i][j])) {
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
        if (animation || finishedGame != null)
            return false;
        moves = new StringBuilder();
        int newX = (int) (event.getX() / getWidth() * boardSize);
        int newY = (int) (event.getY() / getHeight() * boardSize);
        if (selection) {
            selection = game.chessBoard[newY][newX] != ' ' && game.canTake(newY, newX, !whiteTurn);
            if ((whiteTurn ? newY == 0 : newY == boardSize - 1) && promotion) { // promotion
                promotionMove = "" + selectedX + newX + game.chessBoard[newY][newX] +
                        (whiteTurn ? "Q" : "q") + (whiteTurn ? "U" : "u");
                if (game.checkMove(promotionMove, whiteTurn)) {
                    showDialog();
                }
            } else {
                String move = "" + selectedY + selectedX + newY + newX + game.chessBoard[newY][newX];
                if (game.checkMove(move, whiteTurn)) {
                    if (twoPlayers) {
                        game.makeMove(move);
                        whiteTurn = !whiteTurn;
                        if (game.possibleMoves(whiteTurn).isEmpty()) {
                            showEndDialog(whiteTurn ? "Black" : "White");
                        }
                    } else {
                        updateBar(game.makeMoveAndResponse(move, true));
                    }


                    selection = false;
                    animationX = selectedX * pieceWidth;
                    animationY = selectedY * pieceHeight;

                    //            animation(newY, newX);
                }

            }
        } else {
            selection = game.chessBoard[newY][newX] != ' ' && game.canTake(newY, newX, !whiteTurn);
        }
        if (selection) {
            game.checkMoveForPiece(newY * boardSize + newX, moves, whiteTurn);
        }
        selectedX = newX;
        selectedY = newY;
        promotion = game.chessBoard[selectedY][selectedX] == (whiteTurn ? 'P' : 'p');

        repaint();
        return false;
    }

    private void showDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);

        TextView title = new TextView(context);
        title.setText(R.string.promotion);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        title.setBackgroundColor(Color.BLACK);
        title.setTextColor(Color.WHITE);
        layout.addView(title, params);

        PromotionChoice promotionChoice = new PromotionChoice(context, this, whiteTurn);
        if (!whiteTurn) {
            title.setRotation(180);
            promotionChoice.setRotation(180);
        }
        layout.addView(promotionChoice, params);

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setCancelable(false);
        dialog = alertDialogBuilder.create();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    public void cancelDialog(char piece) {
        if (!whiteTurn)
            piece = Character.toLowerCase(piece);
        dialog.cancel();
        String move = promotionMove.substring(0, 3) + piece + promotionMove.charAt(4);
        whiteTurn = !whiteTurn;
        if (twoPlayers) {
            game.makeMove(move);
            if (game.possibleMoves(whiteTurn).isEmpty()) {
                showEndDialog(whiteTurn ? "Black" : "White");
            }
        } else {
            updateBar(game.makeMoveAndResponse(move, true));
        }
        selection = false;
        repaint();
    }

    @SuppressLint("SetTextI18n")
    private void updateBar(String value) {
        int val;
        if (value.equals("Black")) {
            val = 0;
            finishedGame = value;
        } else if (value.equals("White")) {
            val = Integer.MAX_VALUE;
            finishedGame = value;
        } else {
            val = -Integer.parseInt(value);
        }
        progressText.setText("" + val);
        ObjectAnimator.ofInt(progressBar, "progress", val / 2 + 500000)
                .setDuration(600)
                .start();
        if (finishedGame != null) {
            showEndDialog(value);
        }
    }

    private void showEndDialog(String result) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.end_dialog);
        dialog.setTitle("Game finished");
        ((TextView) dialog.findViewById(R.id.result)).setText(String.format("%s won", result));
        dialog.findViewById(R.id.back).setOnClickListener(view -> {
            ViewPager2 viewPager2 = ((MainActivity) getContext()).getViewPager2();
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1);
            dialog.dismiss();
        });
        dialog.findViewById(R.id.analyzeButton).setOnClickListener(view -> {
            analyze();
            dialog.dismiss();
        });
        dialog.findViewById(R.id.replay).setOnClickListener(view -> {
            dialog.dismiss();
            game=  new Game(this, true);
            selectedX = null;
            selectedY = null;
            selection = false;
            updateBar("0");
            promotion = false;
            whiteTurn = true;
            finishedGame = null;
            repaint();
        });

        dialog.show();
    }

    private void analyze() {
    }

}
