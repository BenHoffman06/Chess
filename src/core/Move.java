package core;


import static core.Main.WHITE_PAWN;

public class Move {
    public int number; // Counts black's first move as #2, double counting
    public String notation;
    public Square square1;
    public Square square2;

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

    public Move(String move) {
        String firstSquare = move.substring(0,2);
        String secondSquare = move.substring(2);
        this.square1 = Square.convertStringToSquare(firstSquare);
        this.square2 = Square.convertStringToSquare(secondSquare);
    }

    public int getDistance() {
        return Math.abs(square1.index - square2.index);
    }

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