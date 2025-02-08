public class Move {
    public int number; // Counts black's first move as #2, double counting
    public String notation;
    public Main.Square square1;
    public Main.Square square2;

    public Move(int number, String notation, Main.Square square1, Main.Square square2) {
        this.number = number;
        this.notation = notation;
        this.square1 = square1;
        this.square2 = square2;
    }

    public Move(String notation, Main.Square square1, Main.Square square2) {
        this.number = Main.moves.size() + 1;
        this.notation = notation;
        this.square1 = square1;
        this.square2 = square2;
    }
}
