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
import com.example.chessapp.game.logic.engine.Engine;
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

        hideAnalyzeDesk();
    }

    /**
     * Sets buttons for navigation in analyze
     * @param analyze class with data for analyzing
     */
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
                gameFragment.updateBar(0);
            } else {
                updateAnalyze(analyze, currentMove);
            }
        });
    }

    private void updateAnalyze(Analyze analyze, int currentMove) {
        gameFragment.repaint();
        bestMove.setText(analyze.bestMoves[currentMove]);
        int score = analyze.bestScores[currentMove];
        setProperties(bestScore, score);

        actualMove.setText(analyze.moves.get(currentMove).toString());
        score = analyze.moveScores[currentMove];
        gameFragment.updateBar(score);
        setProperties(actualScore, score);
    }

    private void setProperties(TextView view, int score) {
        String mate = Engine.isMate(score);
        if (score >= 0) {
            view.setText(mate != null ? mate : "+" + score / 100.0);
            view.setTextColor(Color.BLACK);
            view.setBackgroundColor(Color.WHITE);
        } else {
            view.setText(mate != null ? mate : "" + score / 100.0);
            view.setTextColor(Color.WHITE);
            view.setBackgroundColor(Color.BLACK);
        }
    }

    private void hideAnalyzeDesk() {
        analyzeDesk.setVisibility(GONE);
    }

    /**
     * Shows analyze desk
     */
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
