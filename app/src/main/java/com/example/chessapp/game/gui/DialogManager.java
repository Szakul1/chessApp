package com.example.chessapp.game.gui;

import static android.view.View.GONE;
import static android.view.View.TEXT_ALIGNMENT_CENTER;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.example.chessapp.MainActivity;
import com.example.chessapp.R;
import com.example.chessapp.game.type.Move;
import com.example.chessapp.menu.GameFragment;

public class DialogManager {

    private final GameFragment gameFragment;
    private final Context context;

    private AlertDialog dialog;
    private WindowManager.LayoutParams lp;

    private Dialog endDialog;

    private Move promotionMove;

    public DialogManager(GameFragment gameFragment, Context context) {
        this.gameFragment = gameFragment;
        this.context = context;
    }

    /**
     * Show end dialog if game is finished
     */
    public void showEndDialog() {
        endDialog = new Dialog(context);
        endDialog.setContentView(R.layout.end_dialog);
        endDialog.setTitle("Game finished");
        ((TextView) endDialog.findViewById(R.id.result)).setText(gameFragment.finishedGame);
        endDialog.findViewById(R.id.back).setOnClickListener(view -> { // go to menu
            goBack();
            endDialog.dismiss();
        });
        endDialog.findViewById(R.id.analyzeButton).setOnClickListener(view -> analyze());
        endDialog.findViewById(R.id.replay).setOnClickListener(view -> {
            gameFragment.init();
            gameFragment.repaint();
            endDialog.dismiss();
        });
        // fullscreen dialog
        setParams(endDialog);
    }

    public void showPromotionDialog(Move promotionMove) {
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

        PromotionChoice promotionChoice = new PromotionChoice(this, context, gameFragment.getPieceSource(), gameFragment.whiteTurn);
        if (!gameFragment.whiteTurn) {
            title.setRotation(180);
            promotionChoice.setRotation(180);
        }
        layout.addView(promotionChoice, params);

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setCancelable(false);
        dialog = alertDialogBuilder.create();
        setParams(dialog);
    }

    public void cancelDialog(char piece) {
        dialog.cancel();
        promotionMove.promotionPiece = piece;
        gameFragment.makeMove(promotionMove);
    }

    private void analyze() {
        endDialog.setCancelable(false);
        endDialog.findViewById(R.id.end_normal).setVisibility(GONE);
        endDialog.findViewById(R.id.end_analyze).setVisibility(VISIBLE);
        gameFragment.startAnalyze(endDialog, endDialog.findViewById(R.id.loading_bar));
        //endDialog.getWindow().setAttributes(lp);

    }

    private void setParams(Dialog dialog) {
        lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void goBack() {
        ViewPager2 viewPager2 = ((MainActivity) context).getViewPager2();
        viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1);
    }
}
