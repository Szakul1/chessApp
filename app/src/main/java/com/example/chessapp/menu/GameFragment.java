package com.example.chessapp.menu;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.chessapp.R;
import com.example.chessapp.game.gui.AnalyzeDesk;
import com.example.chessapp.game.gui.Board;
import com.example.chessapp.game.gui.DialogManager;
import com.example.chessapp.game.logic.Game;
import com.example.chessapp.game.logic.engine.Analyze;
import com.example.chessapp.game.type.Move;

import java.util.ArrayList;
import java.util.List;

public class GameFragment extends Fragment {

    private static final int boardSize = 8;

    // settings
    private final boolean twoPlayers;
    private final boolean color;

    // communication component
    public char[][] chessBoard;

    // components
    private Board board;
    private Game game;
    private AnalyzeDesk analyzeDesk;
    private ProgressBar progressBar;
    private TextView progressText;
    private ConstraintLayout.LayoutParams params;
    private DialogManager dialogManager;
    private LinearLayout linearLayout;

    // images
    private Bitmap pieces;
    private Rect[] piecesSource;

    // sounds
    private MediaPlayer captureSound;
    private MediaPlayer slideSound;

    // flags
    public boolean whiteTurn;
    public List<Move> moves;
    public boolean selection;
    private int selectedX;
    private int selectedY;
    private boolean analyzing;
    public String finishedGame;

    public GameFragment(boolean twoPlayers, boolean color) {
        this.twoPlayers = twoPlayers;
        this.color = color;
        whiteTurn = color;
        chessBoard = new char[boardSize][boardSize];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getComponents();
        addBoard();
        init();

        requireView().findViewById(R.id.main_restart).setOnClickListener(v -> {
            init();
            board.repaint();
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        captureSound.release();
        slideSound.release();
    }

    public void init() {
        selection = false;
        whiteTurn = color;
        moves = new ArrayList<>();
        game = new Game(this, chessBoard, color);
        analyzeDesk = new AnalyzeDesk(this, requireView());
        dialogManager = new DialogManager(this, requireActivity());
        if (!color) {
            game.response(true);
        }

        setBoardBias(0.5f);
    }

    public void sendInput(int newX, int newY) {
        if (analyzing) {
            return;
        }
        if (finishedGame != null) {
            dialogManager.showEndDialog();
            return;
        }

        if (!color) {
            newX = boardSize - 1 - newX;
            newY = boardSize - 1 - newY;
        }
        moves.clear();
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
    }

    public void makeMove(Move move) {
        game.makeRealMove(move);
        updateGame();
    }

    @SuppressLint("SetTextI18n")
    public void updateBar(int value, int mate) {
        if (mate == -1) {
            progressText.setText("" + value / 100.0);
        } else {
            progressText.setText("Mate in " + mate);
        }
        ObjectAnimator.ofInt(progressBar, "progress", (value + progressBar.getMax()) / 2)
                .setDuration(600)
                .start();
    }

    public void startAnalyze(Dialog endDialog, ProgressBar progressBar) {
        analyzing = true;
        setBoardBias(0.0f);
        new Thread(() -> {
            Analyze analyze = game.startAnalyze(true, progressBar);
            repaint();
            endDialog.dismiss();
            analyzeDesk.analyze(analyze);
        }).start();
        analyzeDesk.showAnalyzeDesk();
    }

    public void repaint() {
        board.repaint();
    }

    public boolean kingSafe(boolean white) {
        return game.kingSafe(white);
    }

    public void showPromotionDialog(Move move) {
        dialogManager.showPromotionDialog(move);
    }

    public void showEndDialog() {
        dialogManager.showEndDialog();
    }

    public Rect[] getPieceSource() {
        return piecesSource;
    }

    public Bitmap getPieces() {
        return pieces;
    }

    public void playSound(boolean capture) {
        if (capture)
            captureSound.start();
        else
            slideSound.start();
    }

    private void getComponents() {
        linearLayout = requireView().findViewById(R.id.board);
        progressBar = requireView().findViewById(R.id.positionBar);
        progressText = requireView().findViewById(R.id.positionValue);
        params = (ConstraintLayout.LayoutParams) linearLayout.getLayoutParams();

        // assets
        pieces = BitmapFactory.decodeResource(getResources(), R.drawable.pieces);
        int pieceImageSize = pieces.getHeight() / 2;
        piecesSource = new Rect[12]; // set piece sources from sprite sheet
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                piecesSource[i * 6 + j] = new Rect(pieceImageSize * j, pieceImageSize * i,
                        (j + 1) * pieceImageSize, (i + 1) * pieceImageSize);
            }
        }

        // sounds
        captureSound = MediaPlayer.create(requireContext(), R.raw.capture);
        slideSound = MediaPlayer.create(requireContext(), R.raw.slide);
    }

    private void addBoard() {
        board = new Board(this, chessBoard, getActivity(), twoPlayers, color, pieces, piecesSource);
        board.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.addView(board, 1);
    }

    private void checkMove(int newX, int newY) {
        selection = game.isMyPiece(newY, newX, whiteTurn);
        if (game.checkMoveAndMake(selectedY, selectedX, newY, newX, whiteTurn)) {
            updateGame();
        }
    }

    private void updateGame() {
        repaint();
        if (twoPlayers) {
            whiteTurn = !whiteTurn;
            new Thread(() -> {
                int score = game.scoreMove(whiteTurn);
                requireActivity().runOnUiThread(() -> updateBar(whiteTurn ? score : -score, -1));
            }).start();
        } else {
            game.response(!whiteTurn);
            repaint();
        }
        game.gameFinished(whiteTurn);

        selection = false;
    }

    private void setBoardBias(float bias) {
        params.verticalBias = bias;
        linearLayout.setLayoutParams(params);
    }
}