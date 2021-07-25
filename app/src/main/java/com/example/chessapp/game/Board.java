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
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
    private boolean color;
    public Rect[] piecesSource;
    private Game game;
    private Integer selectedX = null, selectedY = null;
    private boolean selection = false;
    private String moves = "";
    private boolean animation = false;
    private float animationX, animationY;
    private final Context context;
    private AlertDialog dialog;
    private ProgressBar progressBar;
    private TextView progressText;
    private String promotionMove;
    private boolean whiteTurn = true;
    public String finishedGame = null;

    public Board(Context context, boolean twoPlayers, boolean color) {
        super(context);
        pieces = BitmapFactory.decodeResource(context.getResources(), R.drawable.pieces);
        this.twoPlayers = twoPlayers;
        this.color = color;
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
        ConstraintLayout viewGroup = (ConstraintLayout) getParent().getParent();
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
        for (int i = 0; i < moves.length(); i += 2) {
            int y = Character.getNumericValue(moves.charAt(i));
            int x = Character.getNumericValue(moves.charAt(i + 1));
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
        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(10);
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
                    case 'K':
                        if (!game.kingSafe(true)) {
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
                        if (!game.kingSafe(false)) {
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
        if (finishedGame != null) {
            showEndDialog();
            return false;
        }
        if (animation)
            return false;
        moves = "";
        int newX = (int) (event.getX() / getWidth() * boardSize);
        int newY = (int) (event.getY() / getHeight() * boardSize);
        if (selection) {
            checkMove(newX, newY);
        } else {
            selection = game.isMyPiece(newY, newX, whiteTurn);
        }
        if (selection) {
            moves = game.getMovesForPiece(newY, newX, whiteTurn);
        }
        selectedX = newX;
        selectedY = newY;

        repaint();
        return false;
    }

    private void checkMove(int newX, int newY) {
        selection = game.isMyPiece(newY, newX, whiteTurn);
        if (game.checkMoveAndMake(selectedY, selectedX, newY, newX, whiteTurn)) {
            if (twoPlayers) {
                whiteTurn = !whiteTurn;
            } else {
                updateBar(game.response(!whiteTurn));
//                        updateBar(game.makeMoveAndResponse(move, true));
            }
            game.gameFinished(whiteTurn);


            selection = false;
            animationX = selectedX * pieceWidth;
            animationY = selectedY * pieceHeight;

            //            animation(newY, newX);
        }

    }

    public void showDialog(String promotionMove) {
        this.promotionMove = promotionMove;
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
        if (whiteTurn)
            piece = Character.toLowerCase(piece);
        dialog.cancel();
        String move = promotionMove.substring(0, 2) + piece + promotionMove.charAt(3);
        if (twoPlayers) {
            game.makeRealMove(move);
        } else {
            updateBar(game.response(!whiteTurn));
            //            updateBar(game.makeMoveAndResponse(move, true));
        }
        game.gameFinished(whiteTurn);
        repaint();
    }

    @SuppressLint("SetTextI18n")
    private void updateBar(int value) {
        progressText.setText("" + value);
        ObjectAnimator.ofInt(progressBar, "progress", value / 2 + 10000)
                .setDuration(600)
                .start();
    }

    public void showEndDialog() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.end_dialog);
        dialog.setTitle("Game finished");
        ((TextView) dialog.findViewById(R.id.result)).setText(finishedGame);
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
            game = new Game(this, true);
            selectedX = null;
            selectedY = null;
            selection = false;
            updateBar(0);
            whiteTurn = true;
            finishedGame = null;
            repaint();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void analyze() {
    }

}
