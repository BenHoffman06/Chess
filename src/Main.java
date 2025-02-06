import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Array;
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
            "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
            "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
            "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
            "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
            "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
            "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
            "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
            "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"
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

    //endregion
    public static ArrayList<Byte> accessibleMoves = new ArrayList<>();
    public static boolean moveNotPossible = false;

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
     * Attempts to call movePiece(), <br>
     * Updates moveNotPossible if requested move is impossible
     */
    public static void tryMovePiece(Square square1, Square square2) {
        boolean canMakeMove = (accessibleSquaresOf(square1).contains(square2.index));

        if (canMakeMove) {
            System.out.println("Calling movePiece");
            movePiece(square1, square2);
        }
        else {
            moveNotPossible = true;
            UI.redCountdown = 25;
            UI.redSquare = square2;
        }
    }

    /**
     * Move piece from square1 to square2
     * @Returns chess notation for move
     */
    public static String movePiece(Square square1, Square square2) {
        // Setup chess notation
        String movingPieceStr = String.valueOf(square1.getPieceChar());
        String takesString = "";
        String square2Name = squareName[square2.index];
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
        public byte index;

        public Square(Color color, byte index) {
            this.color = color;
            this.index = index;
            this.piece = EMPTY;
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
            return color == UI.WHITE;
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

        @Override
        protected void paintComponent(Graphics g) {
//            System.out.println("Square.paintComponent called for location: " + this.location + ", selectedSquare: " + (UI.selectedSquare != null ? UI.selectedSquare.location : "null") + ", accessibleMoves: " + accessibleMoves); // ADDED DEBUG PRINT

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

            // Draw piece if NOT being dragged
            if (piece != EMPTY && !UI.isDragging(this)) {
                BufferedImage img = pieceImages.get(piece);
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }


    public static void main(String[] args) {
        mainPanel = UI.handleGUI();
        setBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
    }

    //region Piece Logic
    public static int timesAccessibleSquaresOfCalled = 0;
    public static ArrayList<Byte> accessibleSquaresOf(Square square) {
//        System.out.println(++timesAccessibleSquaresOfCalled);
        ArrayList<Byte> accessibleSquares = new ArrayList<>();
        byte piece = square.piece;
        byte location = square.index;

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
                    accessibleSquares.add(upperSquare.index);

                    // Pawn can also move up two if on 2nd rank
                    if (location / 8 == 6) {
                        accessibleSquares.add((byte) (location - 16));
                    }
                }

                // If not on 1st file and piece to upper left:
                // Can move upper left
                boolean onFirstFile = (location % 8 == 0);
                if (!onFirstFile) {
                    Square upperLeftSquare = board[location - 9];
                    if (upperLeftSquare.piece  < 0) {
                        accessibleSquares.add(upperLeftSquare.index);
                    }
                }

                // If not on 8th file and piece to upper right:
                // Can move upper right
                boolean onLastFile = (location % 8 == 7);
                if (!onLastFile) {
                    Square upperRightSquare = board[location - 7];
                    if (upperRightSquare.piece < 0) {
                        accessibleSquares.add(upperRightSquare.index);
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
                    accessibleSquares.add(lowerSquare.index);

                    // Pawn can also move up two if on 7th rank
                    if (location / 8 == 1) {
                        accessibleSquares.add((byte) (location + 16));
                    }
                }

                // If not on 1st file and piece to lower left:
                // Can move down-left
                boolean onFirstFile = (location % 8 == 0);
                if (!onFirstFile) {
                    Square lowerLeftSquare = board[location + 7];
                    if (lowerLeftSquare.piece > 0) {
                        accessibleSquares.add(lowerLeftSquare.index);
                    }
                }

                // If not on 8th file and piece to lower right:
                // Can move down-right
                boolean onLastFile = (location % 8 == 7);
                if (!onLastFile) {
                    Square lowerRightSquare = board[location + 9];
                    if (lowerRightSquare.piece > 0) {
                        accessibleSquares.add(lowerRightSquare.index);
                    }
                }
            }
            case WHITE_KNIGHT, BLACK_KNIGHT -> {
                // lu
                if (squaresLeft >= 2 && squaresUp >= 1) {
                    // If they are opposite colors or it is empty
                    if (board[location - 10].piece * piece <= 0) {
                        accessibleSquares.add((byte) (location - 10));
                    }
                }
                // ul
                if (squaresUp >= 2 && squaresLeft >= 1) {
                    // If they are opposite colors or it is empty
                    if (board[location - 17].piece * piece <= 0) {
                        accessibleSquares.add((byte) (location - 17));
                    }
                }
                // ur
                if (squaresUp >= 2 && squaresRight >= 1) {
                    // If they are opposite colors or it is empty
                    if (board[location - 15].piece * piece <= 0) {
                        accessibleSquares.add((byte) (location - 15));
                    }
                }
                // ru
                if (squaresRight >= 2 && squaresUp >= 1) {
                    // If they are opposite colors or it is empty
                    if (board[location - 6].piece * piece <= 0) {
                        accessibleSquares.add((byte) (location - 6));
                    }
                }
                // rd
                if (squaresRight >= 2 && squaresDown >= 1) {
                    // If they are opposite colors or it is empty
                    if (board[location + 10].piece * piece <= 0) {
                        accessibleSquares.add((byte) (location + 10));
                    }
                }
                // dr
                if (squaresDown >= 2 && squaresRight >= 1) {
                    // If they are opposite colors or it is empty
                    if (board[location + 17].piece * piece <= 0) {
                        accessibleSquares.add((byte) (location + 17));
                    }
                }
                // dl
                if (squaresDown >= 2 && squaresLeft >= 1) {
                    // If they are opposite colors or it is empty
                    if (board[location + 15].piece * piece <= 0) {
                        accessibleSquares.add((byte) (location + 15));
                    }
                }
                // ld
                if (squaresLeft >= 2 && squaresDown >= 1) {
                    // If they are opposite colors or it is empty
                    if (board[location + 6].piece * piece <= 0) {
                        accessibleSquares.add((byte) (location + 6));
                    }
                }
            }
            case WHITE_BISHOP, BLACK_BISHOP, WHITE_ROOK, BLACK_ROOK, WHITE_QUEEN, BLACK_QUEEN -> {
                boolean stop;
                int i;

                if (Math.abs(piece) == WHITE_ROOK || Math.abs(piece) == WHITE_QUEEN) {
                    // Left
                    stop = false; i = 1;
                    while ((i <= squaresLeft) && !stop) {
                        byte newLocation = (byte) (location + (i++ * -1));
                        stop = tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Right
                    stop = false; i = 1;
                    while ((i <= squaresRight) && !stop) {
                        byte newLocation = (byte) (location + (i++));
                        stop = tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Up                    }
                    stop = false; i = 1;
                    while ((i <= squaresUp) && !stop) {
                        byte newLocation = (byte) (location + (i++ * -8));
                        stop = tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Down
                    stop = false; i = 1;
                    while ((i <= squaresDown) && !stop) {
                        byte newLocation = (byte) (location + (i++ * 8));
                        stop = tryAdd(piece, newLocation, accessibleSquares);
                    }
                }

                if (Math.abs(piece) == WHITE_BISHOP || Math.abs(piece) == WHITE_QUEEN) {
                    // Left-Up
                    stop = false; i = 1;
                    while ((i <= Math.min(squaresLeft, squaresUp)) && !stop) {
                        byte newLocation = (byte) (location + (-9 * i++));
                        stop = tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Right-Up
                    stop = false; i = 1;
                    while ((i <= Math.min(squaresRight, squaresUp)) && !stop) {
                        byte newLocation = (byte) (location + (-7 * i++));
                        stop = tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Right-Down
                    stop = false; i = 1;
                    while ((i <= Math.min(squaresRight, squaresDown)) && !stop) {
                        byte newLocation = (byte) (location + (9 * i++));
                        stop = tryAdd(piece, newLocation, accessibleSquares);
                    }

                    // Left-Down
                    stop = false; i = 1;
                    while ((i <= Math.min(squaresLeft, squaresDown)) && !stop) {
                        byte newLocation = (byte) (location + (7 * i++));
                        stop = tryAdd(piece, newLocation, accessibleSquares);
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
//        System.out.println("accessibleSquaresOf for piece: " + square.piece + " at location: " + square.location + " returning: " + accessibleSquares); // ADDED

        return accessibleSquares;
    }
    //endregion

    public static boolean tryAdd(byte piece, byte newLocation, ArrayList<Byte> accessibleSquares) {
        // Stop if new square is occupied
        if (!board[newLocation].isEmpty()) {
            // If both pieces are different add it
            if (board[newLocation].piece * piece < 0) {
                accessibleSquares.add(newLocation);
            }
            return true;
        }
        accessibleSquares.add(newLocation);
        return false;
    }
}