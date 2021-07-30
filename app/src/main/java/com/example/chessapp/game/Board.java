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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chessapp.MainActivity;
import com.example.chessapp.R;
import com.example.chessapp.game.logic.Analyze;
import com.example.chessapp.game.logic.Game;
import com.example.chessapp.gui.PromotionChoice;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

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
    private boolean analyzing = false;
    private Button forwardButton;
    private Button backButton;
    private Dialog endDialog;
    private LinearLayout analyzeDesk;
    private WindowManager.LayoutParams lp;
    private TextView bestScore;
    private TextView bestMove;
    private TextView actualScore;
    private TextView actualMove;

    public Board(Context context, boolean twoPlayers, boolean color) {
        super(context);
        pieces = BitmapFactory.decodeResource(context.getResources(), R.drawable.pieces);
        this.twoPlayers = twoPlayers;
        this.color = color;
        getHolder().addCallback(this);
        this.context = context;
        if (!color) {
            whiteTurn = false;
        }
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
        game = new Game(this);
        LinearLayout viewGroup = (LinearLayout) getParent();
        progressBar = viewGroup.findViewById(R.id.positionBar);
        progressText = viewGroup.findViewById(R.id.positionValue);
        forwardButton = viewGroup.findViewById(R.id.forwardAnalyze);
        backButton = viewGroup.findViewById(R.id.backAnalyze);
        analyzeDesk = viewGroup.findViewById(R.id.analyze_desk);
        bestScore = viewGroup.findViewById(R.id.best_value);
        bestMove = viewGroup.findViewById(R.id.best_move);
        actualScore = viewGroup.findViewById(R.id.actual_value);
        actualMove = viewGroup.findViewById(R.id.actual_move);
        if (!color) {
            game.response(true);
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

        if (!color) {
            canvas.rotate(180, canvas.getWidth() / 2, canvas.getHeight() / 2);
        }
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
                    if (!color || twoPlayers && Character.isLowerCase(game.chessBoard[i][j])) {
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
        if (analyzing) {
            return false;
        }
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
                game.response(!whiteTurn);
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
        setParams(dialog);
    }

    private void setParams(Dialog dialog) {
        lp = new WindowManager.LayoutParams();
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
            game.makeRealMove(move, whiteTurn);
        } else {
            game.response(!whiteTurn);
        }
        game.gameFinished(whiteTurn);
        repaint();
    }

    @SuppressLint("SetTextI18n")
    public void updateBar(int value, int mate) {
        if (mate == -1) {
            progressText.setText("" + value);
        } else {
            progressText.setText("Mate in " + mate);
        }
        ObjectAnimator.ofInt(progressBar, "progress", (value + progressBar.getMax()) / 2)
                .setDuration(600)
                .start();
    }

    public void showEndDialog() {
        endDialog = new Dialog(context);
        endDialog.setContentView(R.layout.end_dialog);
        endDialog.setTitle("Game finished");
        ((TextView) endDialog.findViewById(R.id.result)).setText(finishedGame);
        endDialog.findViewById(R.id.back).setOnClickListener(view -> {
            ViewPager2 viewPager2 = ((MainActivity) getContext()).getViewPager2();
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1);
            endDialog.dismiss();
        });
        endDialog.findViewById(R.id.analyzeButton).setOnClickListener(view -> {
            analyze();
        });
        endDialog.findViewById(R.id.replay).setOnClickListener(view -> {
            game = new Game(this);
            selectedX = null;
            selectedY = null;
            selection = false;
            updateBar(0, -1);
            analyzing = false;
            whiteTurn = true;
            finishedGame = null;
            repaint();
            endDialog.dismiss();
        });

        setParams(endDialog);
    }

    private void analyze() {
        endDialog.setCancelable(false);
        endDialog.findViewById(R.id.end_normal).setVisibility(GONE);
        endDialog.findViewById(R.id.end_analyze).setVisibility(VISIBLE);
        analyzeDesk.setVisibility(VISIBLE);
        endDialog.getWindow().setAttributes(lp);
        analyzing = true;
        LinearLayout viewGroup = (LinearLayout) getParent();
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) viewGroup.getLayoutParams();
        params.verticalBias = 0.0f;
        viewGroup.setLayoutParams(params);

        new Thread(() -> {
            Analyze analyze;
            analyze = game.startAnalyze(color, endDialog.findViewById(R.id.loading_bar));
            repaint();
            forwardButton.setOnClickListener(view -> {
                int currentMove = analyze.moveForward();
                if (currentMove == 0) {
                    bestScore.setVisibility(VISIBLE);
                    bestMove.setVisibility(VISIBLE);
                    actualScore.setVisibility(VISIBLE);
                    actualMove.setVisibility(VISIBLE);
                }
                if (currentMove != -1) {
                    updateAnalyze(analyze, currentMove);
                }
            });
            backButton.setOnClickListener(view -> {
                int currentMove = analyze.moveBack();
                if (currentMove == -2) {
                    repaint();
                    bestScore.setVisibility(INVISIBLE);
                    bestMove.setVisibility(INVISIBLE);
                    actualScore.setVisibility(INVISIBLE);
                    actualMove.setVisibility(INVISIBLE);
                    updateBar(0, -1);
                } else if (currentMove > -1) {
                    updateAnalyze(analyze, currentMove);
                }
            });
            endDialog.dismiss();
        }).start();
    }

    private void updateAnalyze(Analyze analyze, int currentMove) {
        repaint();
        bestMove.setText(analyze.bestMoves[currentMove]);
        int score = analyze.bestScores[currentMove];
        bestScore.setText("" + score);
        if (score >= 0) {
            bestScore.setTextColor(Color.BLACK);
            bestScore.setBackgroundColor(Color.WHITE);
        } else {
            bestScore.setTextColor(Color.WHITE);
            bestScore.setBackgroundColor(Color.BLACK);
        }
        actualMove.setText(analyze.getMove(currentMove * 5));
        score = analyze.moveScores[currentMove];
        updateBar(score, -1);
        actualScore.setText("" + score);
        if (score >= 0) {
            actualScore.setTextColor(Color.BLACK);
            actualScore.setBackgroundColor(Color.WHITE);
        } else {
            actualScore.setTextColor(Color.WHITE);
            actualScore.setBackgroundColor(Color.BLACK);
        }
    }
}
