package com.example.chessapp.game.logic;

import android.util.Log;

public class Engine {

    private final Game game;
    public final static int globalDepth = 4;

    public Engine(Game game) {
        this.game = game;
    }

    /**
     * @return move (4 char) + value
     */
    public String alphaBeta(int depth, int beta, int alpha, String move, int player, boolean white) {
        String list = game.possibleMoves(white);

        if (depth == 0 || list.length() == 0) {
            return move + rating(list, depth, white) * player;
        }
        // TODO sort
        player = -player;
        for (int i = 0; i < list.length(); i += 5) {
            game.makeMove(list.substring(i, i + 5));
            String returnString = alphaBeta(depth - 1, beta, alpha, list.substring(i, i + 5),
                    player, !white);
            int value = Integer.parseInt(returnString.substring(5));
            game.undoMove(list.substring(i, i + 5));
            if (player == -1) {
                if (value <= beta) {
                    beta = value;
                    if (depth == globalDepth) {
                        move = returnString.substring(0, 5);
                    }
                }
            } else {
                if (value > alpha) {
                    alpha = value;
                    if (depth == globalDepth) {
                        move = returnString.substring(0, 5);
                    }
                }
            }
            if (alpha >= beta) {
                if (player == -1) {
                    return move + beta;
                } else {
                    return move + alpha;
                }
            }
        }

        if (player == -1) {
            return move + beta;
        } else {
            return move + alpha;
        }
    }

    public int rating(String moves, int depth, boolean white) {
        int whiteRating = rateAttack(true) + rateMaterial(true) + rateMobility(true) +
                ratePositional(true);
        int blackRating = rateAttack(false) + rateMaterial(false) + rateMobility(false) +
                ratePositional(false);
        int rating = (whiteRating - blackRating) * (globalDepth - depth) * 50;
//        Log.d("test", "" + -rating);
        return white ? -rating : rating;
    }

    private int rateAttack(boolean white) {
        return 0;
    }

    private int rateMaterial(boolean white) {
        int counter = 0, bishopCounter = 0;
        for (int i = 0; i < 64; i++) {
            char piece = game.chessBoard[i / 8][i % 8];
            if (white ? Character.isLowerCase(piece) : Character.isUpperCase(piece)) {
                continue;
            }
            if (white)
                piece = Character.toUpperCase(piece);
            switch (Character.toUpperCase(piece)) {
                case 'P':
                    counter += 100;
                    break;
                case 'R':
                    counter += 500;
                    break;
                case 'K':
                case 'B':
                    bishopCounter++;
                    counter += 300;
                    break;
                case 'Q':
                    counter += 900;
                    break;
            }
        }
        if (bishopCounter >= 2) {
            counter += 300 * bishopCounter;
        } else if (bishopCounter == 1) {
            counter += 250;
        }
        return counter;
    }

    private int rateMobility(boolean white) {
        return 0;
    }

    private int ratePositional(boolean white) {
        return 0;
    }
}
