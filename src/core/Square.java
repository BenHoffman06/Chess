package core;

import java.awt.*;

public class Square {
    public byte piece;
    public byte index;
    public boolean hasNotChanged;

    private static String[] squareName = {
            "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
            "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
            "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
            "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
            "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
            "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
            "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
            "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"
    };

    public Square(byte index) {
        this.index = index;
        this.piece = Main.EMPTY;
        this.hasNotChanged = true;
    }

    public Square(Square s) {
        this.piece = s.piece;
        this.index = s.index;
        this.hasNotChanged = s.hasNotChanged;
    }

    public void setPiece(byte piece) {
        this.piece = piece;
    }

    public void removePiece() {
        setPiece(Main.EMPTY);
    }

    public boolean isIn(Board b) {
        for (Square s : b.squares) {
            if (this == s) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return (" Location: " + index + ", Piece: " + piece);
    }

    public boolean isEmpty() {
        return (piece == Main.EMPTY);
    }

    public boolean isWhite() {
        return (index + (index / 8)) % 2 == 0;
    }

    String getSquareName() {
        return squareName[index];
    }

    public byte getPromotionOption() {
        byte[] promotionOptions = { Main.WHITE_QUEEN, Main.WHITE_KNIGHT, Main.WHITE_ROOK, Main.WHITE_BISHOP, Main.BLACK_BISHOP, Main.BLACK_ROOK, Main.BLACK_KNIGHT, Main.BLACK_QUEEN };
        int row = index / 8;
        return promotionOptions[row];
    }

    /**
     * Returns a Color based on if white/black and if selected
     */
    public Color getCurrentColor() {

        // Calculate relevant flags
        boolean isSelected = UI.isSelecting(Square.this);
        boolean isMisclicked = (UI.redCountdown > 0 && Square.this == UI.redSquare);
        boolean wasJustMoved = !Main.moves.isEmpty() && (this == Main.moves.getLast().from || this == Main.moves.getLast().to);

        // Apply misclick color if so
        if (isMisclicked) return UI.RED;

        // Apply selection and recent move color mixing
        Color color = isWhite() ? UI.WHITE : UI.BLACK;
        if (isSelected) color = UI.mix(color, UI.SELECTED);
        if (wasJustMoved) color = UI.mix(color, UI.JUST_MOVED);

        return color;
    }

    public Color getCurrentComplementaryColor() {
        Color current = getCurrentColor();
        return (isWhite()) ? UI.BLACK : UI.WHITE;
    }

    public char getPieceChar() {
        switch (this.piece) {
            case Main.WHITE_PAWN, Main.BLACK_PAWN -> {
                return getSquareName().toCharArray()[0];
            }
            case Main.WHITE_KNIGHT, Main.BLACK_KNIGHT -> {
                return 'N';
            }
            case Main.WHITE_BISHOP, Main.BLACK_BISHOP -> {
                return 'B';
            }
            case Main.WHITE_ROOK, Main.BLACK_ROOK -> {
                return 'R';
            }
            case Main.WHITE_QUEEN, Main.BLACK_QUEEN -> {
                return 'Q';
            }
            case Main.WHITE_KING, Main.BLACK_KING -> {
                return 'K';
            }
            default -> throw new RuntimeException("Piece char calc failed. Is this piece 0: " + piece +
                    "? If so you caused a empty piece to move...");
        }
    }

    public char getPieceCharForFEN() {
        switch (this.piece) {
            case Main.WHITE_PAWN -> {
                return 'P';
            }
            case Main.BLACK_PAWN -> {
                return 'p';
            }
            case Main.WHITE_KNIGHT -> {
                return 'N';
            }
            case Main.BLACK_KNIGHT -> {
                return 'n';
            }
            case Main.WHITE_BISHOP -> {
                return 'B';
            }
            case Main.BLACK_BISHOP -> {
                return 'b';
            }
            case Main.WHITE_ROOK -> {
                return 'R';
            }
            case Main.BLACK_ROOK -> {
                return 'r';
            }
            case Main.WHITE_QUEEN -> {
                return 'Q';
            }
            case Main.BLACK_QUEEN -> {
                return 'q';
            }
            case Main.WHITE_KING -> {
                return 'K';
            }
            case Main.BLACK_KING -> {
                return 'k';
            }
            default -> throw new RuntimeException("Piece char calculation failed. Is this piece: " + piece);
        }
    }

    public boolean hasNotChanged() {
        for (Move m : Main.moves) {
            // If square has been moved to or from, it has changed
            if (index == m.from.index || index == m.to.index) {
                return false;
            }
        }
        return true;
    }

    public boolean isDragging() {
        return (this == UI.selectedSquare && UI.beingDragged);
    }
}