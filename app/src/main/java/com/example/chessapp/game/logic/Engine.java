package com.example.chessapp.game.logic;

public class Engine {

    private final Game game;
    public final static int globalDepth = 4;

    public Engine(Game game) {
        this.game = game;
    }

    /**
     *
     * @return move (4 char) + value
     */
    public String alphaBeta(int depth, int beta, int alpha, String move, int player) {
        String list = game.possibleMoves();

        if (depth == 0 || list.length() == 0) {
            return move + rating() * player;
        }
        // TODO sort
        player = -player;
        for (int i = 0; i < list.length(); i += 5) {
            game.makeMove(list.substring(i, i + 5));
            game.flipBoard();
            String returnString = alphaBeta(depth - 1, beta, alpha, list.substring(i, i + 5),
                    player);
            int value = Integer.parseInt(returnString.substring(5));
            game.flipBoard();
            game.undoMove(list.substring(i, i + 5));
            if (player == -1 && value <= beta) {
                beta = value;
                if (depth == globalDepth) {
                    move = returnString.substring(0, 5);
                }
            } else if (value > alpha) {
                alpha = value;
                if (depth == globalDepth) {
                    move = returnString.substring(0, 5);
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

    public int rating() {
        return 0;
    }
}
