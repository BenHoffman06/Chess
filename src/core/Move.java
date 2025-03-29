package core;


import static core.Main.WHITE_PAWN;

public class Move {
    public int number; // Counts half-moves
    public String notation;
    public Square square1; // From
    public Square square2; // To
    public char promotedTo = 'Z'; // Only referenced when move involves a promotion

    public Move(int number, String notation, Square square1, Square square2) {
        this.number = number;
        this.notation = notation;
        this.square1 = square1;
        this.square2 = square2;
    }

    public Move(String notation, Square square1, Square square2) {
        this.number = Main.moves.size() + 1;
        this.notation = notation;
        this.square1 = square1;
        this.square2 = square2;
    }

    public Move(String moveNotation) {
        String firstSquare = moveNotation.substring(0,2);
        String secondSquare = moveNotation.substring(2, 4);
        this.square1 = Square.convertStringToSquare(firstSquare);
        this.square2 = Square.convertStringToSquare(secondSquare);
        notation = firstSquare + secondSquare;
        if (moveNotation.length() == 5) {
            promotedTo = moveNotation.charAt(4);
            notation += promotedTo;
        }

    }

    /**
     * Returns true if it resets 50-move count, by capturing or moving pawn
     */
    public boolean isHalfMoveReset() {
        if (Math.abs(square2.piece) == WHITE_PAWN) {
            return true;
        }
        if (notation.contains("x")) {
            return true;
        }
        return false;
    }
}