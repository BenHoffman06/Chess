package core;

public class MoveExecutor {
    public static Move execute(Square from, Square to, Board board) {
        byte piece = from.piece;
        from.setPiece(Piece.EMPTY);
        to.setPiece(piece);

        // Generate notation (simplified for now)
        String notation = from.index + "->" + to.index;

        return new Move(notation, from, to);
    }
}