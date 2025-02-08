import javax.imageio.ImageIO;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class Main {

    //region Constants and Variables
    // Board
    public static Square[] board = new Square[64];

    // Moves
    public static ArrayList<Move> moves = new ArrayList<>();

    public static boolean isWhitesMove = true;

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

        UI.repaint();
    }

    /**
     * Attempts to call movePiece(), <br>
     * Updates moveNotPossible if requested move is impossible
     */
    public static void tryMovePiece(Square square1, Square square2) {

        boolean canMakeMove = (accessibleSquaresOf(square1, board, true).contains(square2.index));
        if (canMakeMove) {
            // Make and display move
            String moveNotation = movePiece(square1, square2);
            UI.repaint();

            // Add it to move list
            moves.add(new Move(moveNotation, square1, square2));
        }
        else {
            handleInvalidMoveTo(square2, "Move invalid since it isn't in accessibleSquares");
            System.out.println(accessibleMoves);
        }
    }

    public static void handleInvalidMoveTo(Square s) {
        moveNotPossible = true;
        UI.selectedSquare = null;

        UI.redCountdown = 25;
        UI.redSquare = s;

        s.repaint();
    }

    public static void handleInvalidMoveTo(Square s, String message) {
        System.out.println(message);
        handleInvalidMoveTo(s);
    }

    public static boolean isKingInCheck(Square[] board, boolean isWhite) {
        byte king = (byte) (isWhite ? WHITE_KING : BLACK_KING);
        Square kingSquare = null;

        // Find king position
        for (Square s : board) {
            if (s.piece == king) {
                kingSquare = s;
                break;
            }
        }
        if (kingSquare == null) return false;

        // Check all opposing pieces
        for (Square s : board) {
            if (s.piece * king < 0) { // Enemy piece
                ArrayList<Byte> moves = accessibleSquaresOf(s, board, false);
                if (moves.contains(kingSquare.index)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Move piece from square1 to square2
     * @Returns chess notation for move
     */
    public static String movePiece(Square square1, Square square2) {
        // Update variables
        isWhitesMove = !isWhitesMove;
        UI.selectedSquare = null;

        // Setup chess notation
        String movingPieceStr = String.valueOf(square1.getPieceChar());
        String takesString = "";
        String toLocation = squareName[square2.index];

        // If pawn move, just use file
        if (Math.abs(square1.piece) == WHITE_PAWN) {
            toLocation = String.valueOf(squareName[square2.index].toCharArray()[1]);
        }

        if (square2.piece != EMPTY) {
            UI.handleCapture(square2.piece);
            takesString = "x";
        }

        //region Castling
        boolean kingMove = Math.abs(square1.piece) == WHITE_KING;
        int distance = Math.abs(square1.index % 8 - square2.index % 8);

        // Check king moved and it wasn't just his normal movement
        if (kingMove && distance > 1) {

            // If doing kingside castle
            if (square2.index > square1.index) {
                // bring rookSquare other side
                Square rookSquare = board[square2.index + 1];
                Square otherSide = board[square2.index - 1];
                otherSide.piece = rookSquare.piece;
                rookSquare.piece = EMPTY;

                // Move king
                square2.piece = square1.piece;
                square1.piece = EMPTY;

                // Update
                System.out.println(rookSquare);
                rookSquare.repaint();
                otherSide.repaint();

                // Return notation
                return "O-O";
            }

            // If doing queenside castle
            else {
                // bring rook other side
                Square rookSquare = board[square2.index - 2];
                Square otherSide = board[square2.index + 1];
                otherSide.piece = rookSquare.piece;
                rookSquare.piece = EMPTY;

                // Move kind
                square2.piece = square1.piece;
                square1.piece = EMPTY;

                // Update
                rookSquare.repaint();
                otherSide.repaint();

                // Return notation
                return "O-O-O";
            }
        }

        //endregion
        else {
            square2.piece = square1.piece;
            square1.piece = EMPTY;
        }

        return movingPieceStr + takesString + toLocation;
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

        public boolean hasChanged() {
            for (Move m : moves) {
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

    //region Piece Logic
    public static ArrayList<Byte> accessibleSquaresOf(Square square, Square[] board, boolean checkForChecks) {
        ArrayList<Byte> validMoves = new ArrayList<>();
        byte piece = square.piece;
        byte location = square.index;

        // Get all potential moves without considering checks
        ArrayList<Byte> rawMoves = getRawMoves(square, board);

        if (checkForChecks) {
            // Verify each move doesn't leave king in check
            for (Byte move : rawMoves) {
                Square[] simulatedBoard = copyBoard(board);
                makeMove(simulatedBoard, location, move);
                if (!isKingInCheck(simulatedBoard, piece > 0)) {
                    validMoves.add(move);
                }
            }
        } else {
            validMoves.addAll(rawMoves);
        }

        return validMoves;
    }

    private static ArrayList<Byte> getRawMoves(Square square, Square[] board) {
        ArrayList<Byte> moves = new ArrayList<>();
        byte piece = square.piece;
        byte location = square.index;


        int squaresLeft = location % 8;          // Squares to the left edge
        int squaresRight = 7 - squaresLeft;      // Squares to the right edge

        switch (piece) {
            case WHITE_PAWN -> {
                // Pawn has no moves if on the 8th rank (for white)
                if (location <= 7) {
                    return moves;
                }

                // If no pieces are in front of the pawn, it can move forward
                Square upperSquare = board[location - 8];
                if (upperSquare.isEmpty()) {
                    moves.add((byte) (location - 8));

                    // Pawn can also move two squares if on the 2nd rank and no piece on the 4th rank
                    if (location / 8 == 6 && board[location - 16].isEmpty()) {
                        moves.add((byte) (location - 16));
                    }
                }

                // If not on the 1st file and there's an enemy piece to the upper left:
                // The pawn can capture diagonally to the upper left
                if (squaresLeft > 0) {
                    Square upperLeftSquare = board[location - 9];
                    if (upperLeftSquare.piece < 0) { // Enemy piece (black)
                        moves.add((byte) (location - 9));
                    }
                }

                // If not on the 8th file and there's an enemy piece to the upper right:
                // The pawn can capture diagonally to the upper right
                if (squaresRight > 0) {
                    Square upperRightSquare = board[location - 7];
                    if (upperRightSquare.piece < 0) { // Enemy piece (black)
                        moves.add((byte) (location - 7));
                    }
                }
            }
            case BLACK_PAWN -> {
                // Pawn has no moves if on the 1st rank (for black)
                if (location >= 56) {
                    return moves;
                }

                // If no pieces are below the pawn, it can move down
                Square lowerSquare = board[location + 8];
                if (lowerSquare.isEmpty()) {
                    moves.add((byte) (location + 8));

                    // Pawn can also move two squares if on the 7th rank and no piece on the 5th rank
                    if (location / 8 == 1 && board[location + 16].isEmpty()) {
                        moves.add((byte) (location + 16));
                    }
                }

                // If not on the 1st file and there's an enemy piece to the lower left:
                // The pawn can capture diagonally to the lower left
                if (squaresLeft > 0) {
                    Square lowerLeftSquare = board[location + 7];
                    if (lowerLeftSquare.piece > 0) { // Enemy piece (white)
                        moves.add((byte) (location + 7));
                    }
                }

                // If not on the 8th file and there's an enemy piece to the lower right:
                // The pawn can capture diagonally to the lower right
                if (squaresRight > 0) {
                    Square lowerRightSquare = board[location + 9];
                    if (lowerRightSquare.piece > 0) { // Enemy piece (white)
                        moves.add((byte) (location + 9));
                    }
                }
            }
            case WHITE_KNIGHT, BLACK_KNIGHT -> {
                int[] knightMoves = {-17, -15, -10, -6, 6, 10, 15, 17};

                // For all possible knight moves
                for (int move : knightMoves) {

                    int target = location + move;

                    // If target is on the board
                    if (target >= 0 && target < 64) { // Ensure the target is on the board
                        int fileDiff = Math.abs((location % 8) - (target % 8)); // Horizontal distance
                        int rankDiff = Math.abs((location / 8) - (target / 8)); // Vertical distance

                        // And if move is in L-shape
                        if (fileDiff + rankDiff == 3) {

                            // And if the target square is empty or has an enemy piece
                            if ((board[target].piece * piece <= 0)) {

                                // Add it to moves
                                moves.add((byte) target);
                            }
                        }
                    }
                }
            }
            case WHITE_BISHOP, BLACK_BISHOP, WHITE_ROOK, BLACK_ROOK, WHITE_QUEEN, BLACK_QUEEN -> {
                int[][] directions;

                // Rook directions
                if (Math.abs(piece) == WHITE_ROOK) {
                    directions = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                }
                // Bishop directions
                else if (Math.abs(piece) == WHITE_BISHOP) {
                    directions = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                }
                // Queen directions
                else {
                    directions = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                }

                // For each direction, move until the edge of the board or a blocking piece
                for (int[] dir : directions) {
                    int dx = dir[0]; // Change in file
                    int dy = dir[1]; // Change in rank
                    int steps = 1;

                    while (true) {
                        int newX = (location % 8) + dx * steps;
                        int newY = (location / 8) + dy * steps;

                        // Stop if we go off the board
                        if (newX < 0 || newX >= 8 || newY < 0 || newY >= 8) {
                            break;
                        }
                        int target = newY * 8 + newX;

                        // If the square is empty, add it to moves
                        if (board[target].isEmpty()) {
                            moves.add((byte) target);
                        }
                        // If the square has an enemy piece, add it and stop
                        else if (board[target].piece * piece < 0) {
                            moves.add((byte) target);
                            break;
                        }
                        // If the square has a friendly piece, stop
                        else {
                            break;
                        }
                        steps++;
                    }
                }
            }
            case WHITE_KING, BLACK_KING -> {
                int[] kingMoves = {-9, -8, -7, -1, 1, 7, 8, 9};

                // For all possible king moves
                for (int move : kingMoves) {
                    int target = location + move;

                    // If target is on the board
                    if (target >= 0 && target < 64) {
                        int fileDiff = Math.abs((location % 8) - (target % 8)); // Horizontal distance
                        int rankDiff = Math.abs((location / 8) - (target / 8)); // Vertical distance

                        // And if the move is only 1 square away
                        if (fileDiff <= 1 && rankDiff <= 1) {

                            // And if the target square is empty or has an enemy piece
                            if ((board[target].piece * piece <= 0)) {

                                // Add to moves
                                moves.add((byte) target);
                            }
                        }
                    }
                }

                // If the king hasn't moved
                if (!square.hasChanged()) {

                    // Kingside castling:

                    // If there is space
                    if (board[location + 1].isEmpty() && board[location + 2].isEmpty()) {

                        // And rook has not moved
                        if (!board[location + 3].hasChanged()) {

                            // Add to moves
                            moves.add((byte) (location + 2));
                        }
                    }

                    // Queenside castling:

                    // If there is space
                    if (board[location - 1].isEmpty() && board[location - 2].isEmpty() && board[location - 3].isEmpty()) {

                        // And rook has not moved
                        if (!board[location - 4].hasChanged())  {

                            // Add to moves
                            moves.add((byte) (location - 2));
                        }
                    }
                }
            }
        }

        return moves;
    }
    //endregion

    //region Helper methods
    private static Square[] copyBoard(Square[] original) {
        Square[] copy = new Square[64];
        for (int i = 0; i < original.length; i++) {
            copy[i] = new Square(original[i]);
        }
        return copy;
    }

    private static void makeMove(Square[] board, byte from, byte to) {
        board[to].piece = board[from].piece;
        board[from].piece = EMPTY;
    }
    //endregion

    public static void main(String[] args) {
        mainPanel = UI.handleGUI();
        setBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
    }
}