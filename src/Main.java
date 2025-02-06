import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {

    //region Constants and Variables
    // Board
    public static Square[] board = new Square[64];

    // Moves
    public static ArrayList<String> moves = new ArrayList<>();

    // Square names
    public static String[] squareName = {
            "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
            "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
            "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
            "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
            "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
            "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
            "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
            "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"
    };

    // Pieces
    public static final byte WHITE_PAWN = 1;
    public static final byte WHITE_KNIGHT = 2;
    public static final byte WHITE_BISHOP = 3;
    public static final byte WHITE_ROOK = 4;
    public static final byte WHITE_QUEEN = 5;
    public static final byte WHITE_KING = 6;

    public static final byte EMPTY = 0;

    public static final byte BLACK_PAWN = -1;
    public static final byte BLACK_KNIGHT = -2;
    public static final byte BLACK_BISHOP = -3;
    public static final byte BLACK_ROOK = -4;
    public static final byte BLACK_QUEEN = -5;
    public static final byte BLACK_KING = -6;

    // Rendering
    public static JPanel mainPanel = UI.mainPanel;

    // Piece images
    public static final Map<Byte, BufferedImage> pieceImages = new HashMap<>(12);

    static {
        // Load piece images into the dictionary
        ClassLoader classLoader = Main.class.getClassLoader();
        try {
            pieceImages.put(WHITE_PAWN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wP (Custom).png"))));
            pieceImages.put(WHITE_KNIGHT, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wN (Custom).png"))));
            pieceImages.put(WHITE_BISHOP, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wB (Custom).png"))));
            pieceImages.put(WHITE_ROOK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wR (Custom).png"))));
            pieceImages.put(WHITE_QUEEN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wQ (Custom).png"))));
            pieceImages.put(WHITE_KING, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wK (Custom).png"))));
            pieceImages.put(BLACK_PAWN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bP (Custom).png"))));
            pieceImages.put(BLACK_KNIGHT, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bN (Custom).png"))));
            pieceImages.put(BLACK_BISHOP, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bB (Custom).png"))));
            pieceImages.put(BLACK_ROOK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bR (Custom).png"))));
            pieceImages.put(BLACK_QUEEN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bQ (Custom).png"))));
            pieceImages.put(BLACK_KING, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bK (Custom).png"))));
        } catch (IOException e) {
            throw new RuntimeException("Error loading piece images: " + e.getMessage(), e);
        }
    }

    public static int offsetX, offsetY; // Offset of click within the square
    //endregion


    public static void setBoardFromFEN(String fen) {
        byte location = 0, piece_type;
        for (char c : fen.toCharArray()) {
            // Translate characters to pieces
            switch (c) {
                case 'p':
                    piece_type = BLACK_PAWN;
                    break;
                case 'n':
                    piece_type = BLACK_KNIGHT;
                    break;
                case 'b':
                    piece_type = BLACK_BISHOP;
                    break;
                case 'r':
                    piece_type = BLACK_ROOK;
                    break;
                case 'q':
                    piece_type = BLACK_QUEEN;
                    break;
                case 'k':
                    piece_type = BLACK_KING;
                    break;
                case 'P':
                    piece_type = WHITE_PAWN;
                    break;
                case 'N':
                    piece_type = WHITE_KNIGHT;
                    break;
                case 'B':
                    piece_type = WHITE_BISHOP;
                    break;
                case 'R':
                    piece_type = WHITE_ROOK;
                    break;
                case 'Q':
                    piece_type = WHITE_QUEEN;
                    break;
                case 'K':
                    piece_type = WHITE_KING;
                    break;
                case '/':
                    continue;
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                    location += (byte) Character.getNumericValue(c);
                    continue;
                default:
                    throw new RuntimeException("Error in FEN String");
            }

            board[location].setPiece(piece_type);
            location++;
        }

        UI.mainPanel.repaint();
    }


    /**
     * Move piece from square1 to square2
     * @Returns chess notation for move
     */
    public static String movePiece(Square square1, Square square2) {
        // Setup chess notation
        String movingPieceStr = String.valueOf(square1.getPieceChar());
        String takesString = "";
        String square2Name = squareName[square2.location];
        String toLocation;

        switch (square1.piece) {
            case WHITE_PAWN, BLACK_PAWN -> {
                // Location only is rank
                toLocation = String.valueOf(square2Name.toCharArray()[1]);
            }
            default -> {
                toLocation = square2Name;
            }
        }

        if (square2.piece != EMPTY) {
            UI.handleCapture(square2.piece);
            takesString = "x";
        }
        square2.piece = square1.piece;
        square1.piece = EMPTY;

        UI.selectedSquare = null;
        UI.mainPanel.repaint();

        String moveNotation = movingPieceStr + takesString + toLocation;
        System.out.println(moveNotation);

        moves.add(moveNotation);
        return moveNotation;
    }


    static class Square extends JPanel {
        private final Color color;
        public byte piece;
        public byte location;

        public Square(Color color, byte location) {
            this.color = color;
            this.location = location;
            this.piece = EMPTY;
        }

        public void setPiece(byte piece) {
            this.piece = piece;
        }

        public String toString() {
            return ("Location: " + location + ", Piece: " + piece);
        }

        public boolean isEmpty() {
            return (piece == EMPTY);
        }

        public char getPieceChar() {
            switch (this.piece) {
                case WHITE_PAWN, BLACK_PAWN -> {
                    return squareName[location].toCharArray()[0];
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

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Color square depending on if selected
            g.setColor(color);
            if (UI.isSelecting(Square.this)) {
                Color appliedColor = (color == UI.WHITE) ? UI.SELECTED_WHITE : UI.SELECTED_BLACK;
                g.setColor(appliedColor);
            }
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw piece if NOT being dragged
            if (piece != EMPTY && !UI.isDragging(this)) {
                BufferedImage img = pieceImages.get(piece);
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);

                // TODO add special rendering for pieces which are accessibleSquaresOf(selectedPiece)
            }

            // TODO render green circle of radius .27*square.getWidth() for empty squares which are accessibleSquaresOf(selectedPiece)
        }
    }


    public static void main(String[] args) {
        mainPanel = UI.handleGUI();
        setBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
    }

    //region Piece Logic
    public static ArrayList<Byte> accessibleSquaresOf(Square square) {
        ArrayList<Byte> accessibleSquares = new ArrayList<>();
        byte piece = square.piece;
        byte location = square.location;

        int squaresUp = location / 8;
        int squaresLeft = location % 8;
        int squaresDown = 7 - squaresUp;
        int squaresRight = 7 - squaresLeft;

        switch (piece) {
            case WHITE_PAWN -> {
                // Pawn has no moves if on 8th rank
                if (location <= 7) {
                    return accessibleSquares;
                }

                // If no pieces in front of pawn, it can move forward
                Square upperSquare = board[location - 8];
                if (upperSquare.isEmpty()) {
                    accessibleSquares.add(upperSquare.location);
                }

                // If not on 1st file and piece to upper left:
                // Can move upper left
                boolean onFirstFile = (location % 8 == 0);
                if (!onFirstFile) {
                    Square upperLeftSquare = board[location - 9];
                    if (!upperLeftSquare.isEmpty()) {
                        accessibleSquares.add(upperLeftSquare.location);
                    }
                }

                // If not on 8th file and piece to upper right:
                // Can move upper right
                boolean onLastFile = (location % 8 == 7);
                if (!onLastFile) {
                    Square upperRightSquare = board[location - 7];
                    if (!upperRightSquare.isEmpty()) {
                        accessibleSquares.add(upperRightSquare.location);
                    }
                }
            }
            case BLACK_PAWN -> {
                // Pawn has no moves if on first rank
                if (location > 55) {
                    return accessibleSquares;
                }

                // If no pieces below pawn, it can move down
                Square lowerSquare = board[location + 8];
                if (lowerSquare.isEmpty()) {
                    accessibleSquares.add(lowerSquare.location);
                }

                // If not on 1st file and piece to lower left:
                // Can move down-left
                boolean onFirstFile = (location % 8 == 0);
                if (!onFirstFile) {
                    Square lowerLeftSquare = board[location + 7];
                    if (!lowerLeftSquare.isEmpty()) {
                        accessibleSquares.add(lowerLeftSquare.location);
                    }
                }

                // If not on 8th file and piece to lower right:
                // Can move down-right
                boolean onLastFile = (location % 8 == 7);
                if (!onLastFile) {
                    Square lowerRightSquare = board[location + 9];
                    if (!lowerRightSquare.isEmpty()) {
                        accessibleSquares.add(lowerRightSquare.location);
                    }
                }
            }
            case WHITE_KNIGHT, BLACK_KNIGHT -> {
                // lu
                if (squaresLeft >= 2 && squaresUp >= 1) {
                    accessibleSquares.add((byte) (location - 10));
                }
                // ul
                if (squaresUp >= 2 && squaresLeft >= 1) {
                    accessibleSquares.add((byte) (location - 17));
                }
                // ur
                if (squaresUp >= 2 && squaresRight >= 1) {
                    accessibleSquares.add((byte) (location - 15));
                }
                // ru
                if (squaresRight >= 2 && squaresUp >= 1) {
                    accessibleSquares.add((byte) (location - 6));
                }
                // rd
                if (squaresRight >= 2 && squaresDown >= 1) {
                    accessibleSquares.add((byte) (location + 10));
                }
                // dr
                if (squaresDown >= 2 && squaresRight >= 1) {
                    accessibleSquares.add((byte) (location + 17));
                }
                // dl
                if (squaresDown >= 2 && squaresLeft >= 1) {
                    accessibleSquares.add((byte) (location + 15));
                }
                // ld
                if (squaresLeft >= 2 && squaresDown >= 1) {
                    accessibleSquares.add((byte) (location + 6));
                }
            }
            case WHITE_BISHOP, BLACK_BISHOP, WHITE_ROOK, BLACK_ROOK, WHITE_QUEEN, BLACK_QUEEN -> {

                if (Math.abs(piece) == WHITE_ROOK || Math.abs(piece) == WHITE_QUEEN) {
                    // Left
                    for (int i = 0; i < squaresLeft; i++) {
                        byte newLocation = (byte) (location + (i * -1));
                        tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Right
                    for (int i = 0; i < squaresRight; i++) {
                        byte newLocation = (byte) (location + (i));
                        tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Up
                    for (int i = 0; i < squaresUp; i++) {
                        byte newLocation = (byte) (location + (i * -8));
                        tryAdd(piece, newLocation, accessibleSquares);                    }

                    // Down
                    for (int i = 0; i < squaresDown; i++) {
                        byte newLocation = (byte) (location + (i * 8));
                        tryAdd(piece, newLocation, accessibleSquares);
                    }
                }

                if (Math.abs(piece) == WHITE_BISHOP || Math.abs(piece) == WHITE_QUEEN) {
                    // Left-Up
                    for (int i = 0; i < Math.min(squaresLeft, squaresUp); i++) {
                        byte newLocation = (byte) (location + (i - 9));
                        tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Right-Up
                    for (int i = 0; i < Math.min(squaresRight, squaresUp); i++) {
                        byte newLocation = (byte) (location + (i - 7));
                        tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Right-Down
                    for (int i = 0; i < Math.min(squaresRight, squaresDown); i++) {
                        byte newLocation = (byte) (location + (i + 9));
                        tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Left-Down
                    for (int i = 0; i < Math.min(squaresLeft, squaresDown); i++) {
                        byte newLocation = (byte) (location + (i + 7));
                        tryAdd(piece, newLocation, accessibleSquares);
                    }
                }
            }
            case WHITE_KING, BLACK_KING -> {
                if (squaresLeft > 0) {
                    accessibleSquares.add((byte) (location - 1));
                }
                if (squaresRight > 0) {
                    accessibleSquares.add((byte) (location + 1));
                }
                if (squaresUp > 0) {
                    accessibleSquares.add((byte) (location - 8));
                }
                if (squaresDown > 0) {
                    accessibleSquares.add((byte) (location + 8));
                }

                if (squaresLeft > 0 && squaresUp > 0) {
                    accessibleSquares.add((byte) (location - 9));
                }
                if (squaresRight > 0 && squaresUp > 0) {
                    accessibleSquares.add((byte) (location - 7));
                }
                if (squaresRight > 0 && squaresDown > 0) {
                    accessibleSquares.add((byte) (location + 9));
                }
                if (squaresRight > 0 && squaresDown > 0) {
                    accessibleSquares.add((byte) (location + 7));
                }
            }
        }
        return accessibleSquares;
    }
    //endregion

    public static void tryAdd(byte piece, byte newLocation, ArrayList<Byte> accessibleSquares) {
        // Stop if new square is occupied
        if (!board[newLocation].isEmpty()) {
            // If both pieces are different add it
            if (board[newLocation].piece * piece < 0) {
                accessibleSquares.add(newLocation);
            }
            return;
        }
        accessibleSquares.add(newLocation);
    }
}