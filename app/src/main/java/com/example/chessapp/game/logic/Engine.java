package com.example.chessapp.game.logic;

public class Engine {
    static int[][] pawnBoard = {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {5, 5, 10, 25, 25, 10, 5, 5},
            {0, 0, 0, 20, 20, 0, 0, 0},
            {5, -5, -10, 0, 0, -10, -5, 5},
            {5, 10, 10, -20, -20, 10, 10, 5},
            {0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] rookBoard = {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {5, 10, 10, 10, 10, 10, 10, 5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {0, 0, 0, 5, 5, 0, 0, 0}};
    static int[][] knightBoard = {
            {-50, -40, -30, -30, -30, -30, -40, -50},
            {-40, -20, 0, 0, 0, 0, -20, -40},
            {-30, 0, 10, 15, 15, 10, 0, -30},
            {-30, 5, 15, 20, 20, 15, 5, -30},
            {-30, 0, 15, 20, 20, 15, 0, -30},
            {-30, 5, 10, 15, 15, 10, 5, -30},
            {-40, -20, 0, 5, 5, 0, -20, -40},
            {-50, -40, -30, -30, -30, -30, -40, -50}};
    static int[][] bishopBoard = {
            {-20, -10, -10, -10, -10, -10, -10, -20},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 10, 10, 5, 0, -10},
            {-10, 5, 5, 10, 10, 5, 5, -10},
            {-10, 0, 10, 10, 10, 10, 0, -10},
            {-10, 10, 10, 10, 10, 10, 10, -10},
            {-10, 5, 0, 0, 0, 0, 5, -10},
            {-20, -10, -10, -10, -10, -10, -10, -20}};
    static int[][] queenBoard = {
            {-20, -10, -10, -5, -5, -10, -10, -20},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 5, 5, 5, 0, -10},
            {-5, 0, 5, 5, 5, 5, 0, -5},
            {0, 0, 5, 5, 5, 5, 0, -5},
            {-10, 5, 5, 5, 5, 5, 0, -10},
            {-10, 0, 5, 0, 0, 0, 0, -10},
            {-20, -10, -10, -5, -5, -10, -10, -20}};
    static int[][] kingMidBoard = {
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-20, -30, -30, -40, -40, -30, -30, -20},
            {-10, -20, -20, -20, -20, -20, -20, -10},
            {20, 20, 0, 0, 0, 0, 20, 20},
            {20, 30, 10, 0, 0, 10, 30, 20}};
    static int[][] kingEndBoard = {
            {-50, -40, -30, -20, -20, -30, -40, -50},
            {-30, -20, -10, 0, 0, -10, -20, -30},
            {-30, -10, 20, 30, 30, 20, -10, -30},
            {-30, -10, 30, 40, 40, 30, -10, -30},
            {-30, -10, 30, 40, 40, 30, -10, -30},
            {-30, -10, 20, 30, 30, 20, -10, -30},
            {-30, -30, 0, 0, 0, 0, -30, -30},
            {-50, -30, -30, -30, -30, -30, -30, -50}};

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
            return move + rating(list.length(), depth, white) * player;
        }
//        list = sortMoves(list, white);

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

    private String sortMoves(String list, boolean white) {
        int[] ratings = new int[list.length() / 5];
        for (int i = 0; i < list.length(); i += 5) {
            game.makeMove(list.substring(i, i + 5));
            ratings[i / 5] = rating(-1, 0, white); // todo check
            game.undoMove(list.substring(i, i + 5));
        }
        StringBuilder newListA = new StringBuilder();
        String newListB = list;
        for (int i = 0; i < Math.min(6, ratings.length / 5); i++) {
            int max = Integer.MIN_VALUE, maxLocation = 0;
            for (int j = 0; j < ratings.length / 5; j++) {
                if (ratings[j] > max) {
                    max = ratings[j];
                    maxLocation = j;
                }
            }
            ratings[maxLocation] = Integer.MIN_VALUE;
            String move = list.substring(maxLocation * 5, maxLocation * 5 + 5);
            newListA.append(move);
            newListB = newListB.replace(move, "");
        }

        return newListA + newListB;
    }

    private int rating(int movesNumber, int depth, boolean white) {
        int whiteMaterial = rateMaterial(true);
        int blackMaterial = rateMaterial(false);

        int whiteRating = rateAttack(true) + whiteMaterial +
                rateMobility(movesNumber, depth, true) +
                ratePositional(whiteMaterial, true);
        int blackRating = rateAttack(false) + blackMaterial +
                rateMobility(movesNumber, depth, false) +
                ratePositional(blackMaterial, false);
        int rating = (whiteRating - blackRating) * (globalDepth - depth) * 50;
//        Log.d("test", "" + -rating);
        return white ? -rating : rating;
    }

    private int rateAttack(boolean white) {
        int counter = 0, tempKing = white ? game.kingPosWhite : game.kingPosBlack;
        for (int i = 0; i < 64; i++) {
            char piece = game.chessBoard[i / 8][i % 8];
            if (white ? Character.isLowerCase(piece) : Character.isUpperCase(piece)) {
                continue;
            }
            switch (Character.toUpperCase(piece)) {
                case 'P':
                    counter += pieceSafe(i, 64, white);
                    break;
                case 'R':
                    counter += pieceSafe(i, 500, white);
                    break;
                case 'K':
                case 'B':
                    counter += pieceSafe(i, 300, white);
                    break;
                case 'Q':
                    counter += pieceSafe(i, 900, white);
                    break;
            }
        }
        if (white) {
            game.kingPosWhite = tempKing;
        } else {
            game.kingPosBlack = tempKing;
        }
        if (!game.kingSafe(white))
            counter -= 200;
        return counter / 2;
    }

    private int pieceSafe(int position, int value, boolean white) {
        if (white) {
            game.kingPosWhite = position;
        } else {
            game.kingPosBlack = position;
        }
        if (!game.kingSafe(white)) {
            return -value;
        } else {
            return 0;
        }
    }

    private int rateMaterial(boolean white) {
        int counter = 0, bishopCounter = 0;
        for (int i = 0; i < 64; i++) {
            char piece = game.chessBoard[i / 8][i % 8];
            if (white ? Character.isLowerCase(piece) : Character.isUpperCase(piece)) {
                continue;
            }
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

    private int rateMobility(int movesNumber, int depth, boolean white) {
        int counter = 0;
        counter += movesNumber;
        if (movesNumber == 0) {
            if (game.kingSafe(white)) { // stalemate
                counter -= 200_000 * depth;
            } else { // checkmate
                counter -= 150_000 * depth;
            }
        }
        return counter;
    }

    private int ratePositional(int material, boolean white) {
        int counter = 0, kingPos = game.possibleA(white ? game.kingPosWhite :
                game.kingPosBlack, white).length() * 10;
        for (int i = 0; i < 64; i++) {
            int row = i / 8, col = i % 8;
            char piece = game.chessBoard[row][col];
            if (white ? Character.isLowerCase(piece) : Character.isUpperCase(piece)) {
                continue;
            }
            if (!white) {
                row = 7 - row;
                col = 7 - col;
            }
            switch (Character.toUpperCase(piece)) {
                case 'P':
                    counter += pawnBoard[row][col];
                    break;
                case 'R':
                    counter += rookBoard[row][col];
                    break;
                case 'K':
                    counter += knightBoard[row][col];
                case 'B':
                    counter += bishopBoard[row][col];
                    break;
                case 'Q':
                    counter += queenBoard[row][col];
                    break;
                case 'A':
                    if (material >= 1750) {
                        counter += kingMidBoard[row][col];
                        counter += kingPos;
                    } else {
                        counter += kingEndBoard[row][col];
                        counter += kingPos * 3;
                    }
                    break;
            }
        }
        return counter;
    }
}
