package core;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Piece {

    //region Piece Constants
    // White Pieces
    public static final byte WHITE_PAWN = 1;
    public static final byte WHITE_KNIGHT = 2;
    public static final byte WHITE_BISHOP = 3;
    public static final byte WHITE_ROOK = 4;
    public static final byte WHITE_QUEEN = 5;
    public static final byte WHITE_KING = 6;

    // Black Pieces
    public static final byte BLACK_PAWN = -1;
    public static final byte BLACK_KNIGHT = -2;
    public static final byte BLACK_BISHOP = -3;
    public static final byte BLACK_ROOK = -4;
    public static final byte BLACK_QUEEN = -5;
    public static final byte BLACK_KING = -6;

    // Empty Square
    public static final byte EMPTY = 0; // Represents an empty square on the board
    //endregion

    public boolean isWhite;
    public String filePath;

    /**
     * Accepts types:
     * "King", "Queen", "Rook", "Knight", "Bishop", "Pawn",
     */
    public boolean isType(String type) {
        return false;
    }

    public ArrayList<Byte> getRawMoves() {
        return new ArrayList<Byte>();
    }

    public BufferedImage getPieceImage() throws IOException {
        ClassLoader classLoader = Main.class.getClassLoader();
        return ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream(filePath)));
    }
}
