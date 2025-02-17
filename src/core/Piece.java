package core;

public class Piece {
    public static final byte EMPTY = 0;

    public static final byte WHITE_PAWN = 1;
    public static final byte WHITE_KNIGHT = 2;
    public static final byte WHITE_BISHOP = 3;
    public static final byte WHITE_ROOK = 4;
    public static final byte WHITE_QUEEN = 5;
    public static final byte WHITE_KING = 6;

    public static final byte BLACK_PAWN = -1;
    public static final byte BLACK_KNIGHT = -2;
    public static final byte BLACK_BISHOP = -3;
    public static final byte BLACK_ROOK = -4;
    public static final byte BLACK_QUEEN = -5;
    public static final byte BLACK_KING = -6;

    public static boolean isWhite(byte piece) {
        return piece > 0;
    }

    public static boolean isBlack(byte piece) {
        return piece < 0;
    }

    public boolean sameColors(byte p1, byte p2) {
        return (p1 * p2 > 0);
    }
}