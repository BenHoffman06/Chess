package core;

import ui.BoardRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static core.Board.isDragging;
import static core.Piece.*;
import static ui.BoardRenderer.*;

public class Square extends JPanel {
    public final Color color;
    public byte piece;
    public byte index;

    // Square names
    public static String[] squareName = {
            "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
            "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
            "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
            "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
            "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
            "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
            "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
            "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"
    };

    public Square(Color color, byte index) {
        this.color = color;
        this.index = index;
        this.piece = EMPTY;
    }

    public Square(Main.Square s) {
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
        return (piece == EMPTY);
    }

    public boolean isWhite() {
        return color == WHITE;
    }

    public char getPieceChar() {
        switch (this.piece) {
            case WHITE_PAWN, BLACK_PAWN -> {
                return squareName[index].toCharArray()[0];
            }
            case WHITE_KNIGHT, BLACK_KNIGHT -> {
                return 'N';
            }
            case WHITE_BISHOP, BLACK_BISHOP -> {
                return 'B';
            }
            case WHITE_ROOK, BLACK_ROOK -> {
                return 'R';
            }
            case WHITE_QUEEN, BLACK_QUEEN -> {
                return 'Q';
            }
            case WHITE_KING, BLACK_KING -> {
                return 'K';
            }
            default -> throw new RuntimeException("Piece char calc failed. Is this piece 0: " + piece +
                    "? If so you caused a empty piece to move...");
        }
    }

    public boolean hasChanged() {
        for (Move m : Game.moves) {
            // If square has been moved to or from, it has changed
            if (index == m.square1.index || index == m.square2.index) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        // Color square depending on if selected
        g.setColor(color);
        if (Board.isSelecting(Square.this)) {
            Color appliedColor = (color == WHITE) ? SELECTED_WHITE : SELECTED_BLACK;
            g.setColor(appliedColor);
        }
        if (redCountdown > 0 && Square.this == redSquare) {
            System.out.println(redCountdown);
            g.setColor(RED);
        }
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw piece if NOT being dragged
        if (piece != EMPTY && !isDragging(this)) {
            BufferedImage img = pieceImages.get(piece);
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public byte getPiece() {
        return this.piece;
    }
}
