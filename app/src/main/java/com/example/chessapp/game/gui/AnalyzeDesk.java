package com.example.chessapp.game.gui;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chessapp.R;
import com.example.chessapp.game.logic.engine.Analyze;
import com.example.chessapp.menu.GameFragment;

public class AnalyzeDesk {

    private final GameFragment gameFragment;
    private final LinearLayout analyzeDesk;

    private final TextView bestScore;
    private final TextView bestMove;
    private final TextView actualScore;
    private final TextView actualMove;

    private final Button forwardButton;
    private final Button backButton;

    public AnalyzeDesk(GameFragment gameFragment, View view) {
        this.gameFragment = gameFragment;
        analyzeDesk = view.findViewById(R.id.analyze_desk);

        bestScore = view.findViewById(R.id.best_value);
        bestMove = view.findViewById(R.id.best_move);
        actualScore = view.findViewById(R.id.actual_value);
        actualMove = view.findViewById(R.id.actual_move);

        forwardButton = view.findViewById(R.id.forwardAnalyze);
        backButton = view.findViewById(R.id.backAnalyze);
    }

    public void analyze(Analyze analyze) {
        forwardButton.setOnClickListener(view -> {
            int currentMove = analyze.moveForward();
            if (currentMove == 0) {
                showAnalyze();
            }
            updateAnalyze(analyze, currentMove);
        });
        backButton.setOnClickListener(view -> {
            int currentMove = analyze.moveBack();
            if (currentMove == -1) {
                gameFragment.repaint();
                hideAnalyze();
                gameFragment.updateBar(0, -1);
            } else {
                updateAnalyze(analyze, currentMove);
            }
        });
    }

    private void updateAnalyze(Analyze analyze, int currentMove) {
        gameFragment.repaint();
        bestMove.setText(analyze.bestMoves[currentMove]);
        int score = analyze.bestScores[currentMove];
        if (score >= 0) {
            bestScore.setText("+" + score / 100.0);
            bestScore.setTextColor(Color.BLACK);
            bestScore.setBackgroundColor(Color.WHITE);
        } else {
            bestScore.setText("" + score / 100.0);
            bestScore.setTextColor(Color.WHITE);
            bestScore.setBackgroundColor(Color.BLACK);
        }
        actualMove.setText(analyze.moves.get(currentMove).toString());
        score = analyze.moveScores[currentMove];
        gameFragment.updateBar(score, -1);
        if (score >= 0) {
            actualScore.setText("+" + score / 100.0);
            actualScore.setTextColor(Color.BLACK);
            actualScore.setBackgroundColor(Color.WHITE);
        } else {
            actualScore.setText("" + score / 100.0);
            actualScore.setTextColor(Color.WHITE);
            actualScore.setBackgroundColor(Color.BLACK);
        }
    }

    private void hideAnalyzeDesk() {
        analyzeDesk.setVisibility(GONE);
    }

    public void showAnalyzeDesk() {
        analyzeDesk.setVisibility(VISIBLE);
        hideAnalyze();
    }

    private void hideAnalyze() {
        bestScore.setVisibility(INVISIBLE);
        bestMove.setVisibility(INVISIBLE);
        actualScore.setVisibility(INVISIBLE);
        actualMove.setVisibility(INVISIBLE);
    }

    private void showAnalyze() {
        bestScore.setVisibility(VISIBLE);
        bestMove.setVisibility(VISIBLE);
        actualScore.setVisibility(VISIBLE);
        actualMove.setVisibility(VISIBLE);
    }
}
