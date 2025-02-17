package core;

import java.awt.*;

public class Board {
    public static Square[] board = new Square[64];

    // Mouse movement and selection variables
    public static Square selectedSquare = null;
    public static boolean beingDragged = false;
    public static int dragX, dragY;
    public static int offsetX, offsetY; // Offset of click within the square


    public static boolean isSelecting(Square s) {
        return (s == selectedSquare);
    }

    public static boolean isDragging(Square s) {
        return (s == selectedSquare && beingDragged);
    }
}