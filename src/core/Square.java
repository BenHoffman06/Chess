package core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static core.UI.CHECKMATE;
import static core.UI.pieceImages;

public class Square extends JPanel {
    public final Color color;
    public byte piece;
    public byte index;

    public Square(Color color, byte index) {
        this.color = color;
        this.index = index;
        this.piece = Main.EMPTY;
    }

    public Square(Square s) {
        this.color = s.color;
        this.piece = s.piece;
        this.index = s.index;
    }

    public void setPiece(byte piece) {
        this.piece = piece;
    }

    public String toString() {
        return ("Location: " + index + ", Piece: " + piece);
    }

    public boolean isEmpty() {
        return (piece == Main.EMPTY);
    }

    public boolean isWhite() {
        return color == UI.WHITE;
    }

    String getSquareName() {
        return squareName[index];
    }

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

    public static Square convertStringToSquare(String s) {
        s.trim();
        for (int i = 0; i < squareName.length; i++) {
            String name = squareName[i];
            if (name.equals(s)) {
                return Main.board.squares[i];
            }
        }
        System.out.println("returning null for " + s);
        return null;
    }

    public byte getPromotionOption() {
        byte[] promotionOptions = { Main.WHITE_QUEEN, Main.WHITE_KNIGHT, Main.WHITE_ROOK, Main.WHITE_BISHOP, Main.BLACK_BISHOP, Main.BLACK_ROOK, Main.BLACK_KNIGHT, Main.BLACK_QUEEN };
        int row = index / 8;
        return promotionOptions[row];
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
            if (index == m.square1.index || index == m.square2.index) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        //region Enable rendering optimizations
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        System.setProperty("awt.useSystemAAFontSettings", "on");
        //endregion

        super.paintComponent(g);

        // Color square depending on if selected
        g.setColor(color);
        if (UI.isSelecting(Square.this)) {
            Color appliedColor = (color == UI.WHITE) ? UI.SELECTED_WHITE : UI.SELECTED_BLACK;
            g.setColor(appliedColor);
        }
        if (UI.redCountdown > 0 && Square.this == UI.redSquare) {
            System.out.println(UI.redCountdown);
            g.setColor(UI.RED);
        }
        g.fillRect(0, 0, getWidth(), getHeight());


        // Draw checkmate image if checkmated
        if (piece == Main.WHITE_KING && Main.board.isWhitesMove || piece == Main.BLACK_KING && !Main.board.isWhitesMove) {
            if (Main.board.checkGameOutcome() == 1) {
                BufferedImage img = UI.pieceImages.get(CHECKMATE);
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        }

        // Draw piece if NOT being dragged
        if (piece != Main.EMPTY && !this.isDragging()) {
            BufferedImage img = pieceImages.get(piece);
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);

        }
    }

    public boolean isDragging() {
        return (this == UI.selectedSquare && UI.beingDragged);
    }
}