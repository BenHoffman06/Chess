package core;

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
        this.number = Game.moves.size() + 1;
        this.notation = notation;
        this.square1 = square1;
        this.square2 = square2;
    }

    public int getDistance() {
        return Math.abs(square1.index - square2.index);
    }
}