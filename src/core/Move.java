package core;


import java.util.HashMap;
import java.util.Map;

import static core.Main.*;

public class Move {
    public int number; // Counts half-moves
    public String notation;
    public Square from; // From
    public Square to; // To
    public boolean isCapture;
    public char promotedTo; // Only referenced when move involves a promotion
    public Board on; // TODO force constructors to use this

//    public Move(int number, String notation, Square from, Square to) {
//        this.number = number;
//        this.notation = notation;
//        this.from = from;
//        this.to = to;
//    }

//    public Move(String notation, Square from, Square to) {
//        this.number = Main.moves.size() + 1;
//        this.notation = notation;
//        this.from = from;
//        this.to = to;
//    }

    public Move(Board on, Square from, Square to) {
        this.from = from;
        this.to = to;
        this.on = on;
        if (!from.isIn(on) || !to.isIn(on)) {
            System.out.println(from.isIn(on) + ", " +  to.isIn(on));
            System.out.println("ERROR: Board and Square mismatch in Move constructor");
            throw new RuntimeException("Board and Square mismatch in Move constructor");
        }
        if (to.piece != EMPTY) {
            isCapture = true;
        }
    }

//    public Move(String moveNotation) {
//        String firstSquare = moveNotation.substring(0,2);
//        String secondSquare = moveNotation.substring(2, 4);
//        this.from = Main.board.convertStringToSquare(firstSquare);
//        this.to = Main.board.convertStringToSquare(secondSquare);
//        notation = firstSquare + secondSquare;
//        if (moveNotation.length() == 5) {
//            promotedTo = moveNotation.charAt(4);
//            notation += promotedTo;
//        }
//    }

    public Move(Board on, String moveNotation) {
        String firstSquare = moveNotation.substring(0,2);
        String secondSquare = moveNotation.substring(2, 4);
        this.from = on.convertStringToSquare(firstSquare);
        this.to = on.convertStringToSquare(secondSquare);
        this.notation = moveNotation;

        if (notation.contains("x")) isCapture = true;

        if (moveNotation.length() == 5) {
            promotedTo = moveNotation.charAt(4);
            notation += promotedTo;
        } else {
            promotedTo = to.piece > 1 ? 'Q' : 'q';
        }

        if (!from.isIn(on) || !to.isIn(on)) {
            System.out.println(from.isIn(on) + ", " +  to.isIn(on));
            System.out.println("ERROR: Board and Square mismatch in Move constructor");
            throw new RuntimeException("Board and Square mismatch in Move constructor");
        }
    }

    /**
     * Returns true if it resets 50-move count, by capturing or moving pawn
     */
    public boolean isHalfMoveReset() {
        if (Math.abs(to.piece) == WHITE_PAWN || isCapture) {
            return true;
        }
        return false;
    }

    public byte getPromotionChoice() {
        char promoChar = promotedTo;
        return switch (promoChar) {
            case 'Q' -> WHITE_QUEEN;
            case 'R' -> WHITE_ROOK;
            case 'B' -> WHITE_BISHOP;
            case 'N' -> WHITE_KNIGHT;
            case 'q' -> BLACK_QUEEN;
            case 'r' -> BLACK_ROOK;
            case 'b' -> BLACK_BISHOP;
            case 'n' -> BLACK_KNIGHT;
            default -> {
                System.out.println("Incorrect promotion");
                throw new RuntimeException("Incorrect promotion");
            }
        };
    }

    public boolean isWhitePromotion() {
        return from.piece == WHITE_PAWN && to.index < 8;
    }

    public boolean isBlackPromotion() {
        return from.piece == BLACK_PAWN && to.index >= 56;
    }

    public String getNotation() {
        String promoNotation = ((isWhitePromotion() || isBlackPromotion())) ? String.valueOf(Character.toLowerCase(promotedTo)) :  "";
        return from.getSquareName() + to.getSquareName() + promoNotation;
    }

}