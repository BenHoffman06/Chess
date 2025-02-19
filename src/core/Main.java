package core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

import static core.UI.*;

public class Main {

    // Represent chessboard as 1D array of 64 squares
    public static Square[] board = new Square[64];

    //region Game State
    public static boolean isWhitesMove = true; // Tracks whose turn it is (true = White's turn, false = Black's turn)
    public static boolean gameOngoing = true;
    //endregion

    //region Moves
    public static ArrayList<Move1> moves = new ArrayList<>(); // Stores all moves made in the game
    public static ArrayList<Byte> accessibleMoves = new ArrayList<>(); // Stores accessible squares (their index) for a selected piece
    //endregion

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

    /**
     * Attempts to call movePiece()
     */
    public static void tryMovePiece(Square square1, Square square2) {

        boolean canMakeMove = (accessibleSquaresOf(square1, board, true).contains(square2.index));
        if (canMakeMove) {
            // Make and display move
            String moveNotation = movePiece(square1, square2);
            UI.repaint();

            // Add it to move list
            moves.add(new Move1(moveNotation, square1, square2));
        }
        else {
            UI.handleInvalidMoveTo(square2, "Move invalid since it isn't in accessibleSquares");
            System.out.println(accessibleMoves);
        }
    }


    /**
     * Move piece from square1 to square2 <br>
     * returns chess notation for move
     */
    public static String movePiece(Square square1, Square square2) {
        byte from = square1.index;
        byte to = square2.index;

        // Update variables
        isWhitesMove = !isWhitesMove;
        UI.selectedSquare = null;

        // Setup chess notation
        String movingPieceStr = String.valueOf(square1.getPieceChar());
        String takesString = "";
        String toLocation = square2.getSquareName();

        // If pawn move, just use file
        if (Math.abs(square1.piece) == WHITE_PAWN) {
            toLocation = String.valueOf(square2.getSquareName().toCharArray()[1]);
        }

        if (square2.piece != EMPTY) {
            UI.handleCapture();
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

        //region Handle promotions
        boolean isWhitePromotion = board[from].piece == WHITE_PAWN && to < 8;
        boolean isBlackPromotion = board[from].piece == BLACK_PAWN && to >= 56;
        if (isWhitePromotion) {
            board[to].piece = WHITE_QUEEN;
        }
        else if (isBlackPromotion) {
            board[to].piece = BLACK_QUEEN;
        }
        //endregion

        //region Handle en-passant

        // isEnPassant if pawn is moving diagonally and square2 is empty
        boolean isPawnMove = Math.abs(square1.piece) == WHITE_PAWN;
        boolean isDiagonal = Math.abs(from - to) % 8 != 0;
        boolean isEnPassant = square2.isEmpty() && isPawnMove && isDiagonal;

        if (isEnPassant) {
            // Remove piece from behind square2
            Square behind = board[square2.index + Integer.signum(square1.piece) * 8];
            behind.piece = EMPTY;
            takesString = "x";
            UI.handleCapture();
        }
        //endregion

        // If a non-promotion and non-castling move, update new square
        if (!isBlackPromotion && !isWhitePromotion) {
            board[to].piece = board[from].piece;
        }

        // Remove piece which moved from its original square
        board[from].piece = EMPTY;


        return movingPieceStr + takesString + toLocation;
    }

    public static boolean piecesCanMove() {
        for (Square s : board) {
            if (s.piece == EMPTY) {
                continue;
            }
            boolean isWhite = Math.signum(s.piece) > 0;
            boolean isPiecesTurnToMove =  isWhite && isWhitesMove || !isWhite && !isWhitesMove;

            // If it is the correct color
            if (isPiecesTurnToMove) {

                // And if it can move somewhere
                if (!accessibleSquaresOf(s, board, true).isEmpty()) {

                    return true;
                }
            }
        }
        return false; // if there does not exist a piece of the right color which can move
    }

    /**
     * Returns 0 if stalemated, 1 if checkmated, -1 if pieces can move
     */
    public static int getEndState() {
        if (piecesCanMove()) {
            return -1;
        }

        // When game over:
        playSound("sounds/beep.wav");
        gameOngoing = false;

        // Handle checkmate
        if (isKingInCheck(board, isWhitesMove)) {
            System.out.println("CHECKMATE!");
            return 1;
        }

        // Handle draw
        System.out.println("DRAW...");
        return 0;
    }

    private static void makeMove(Square[] board, byte from, byte to) {
        //region Handle promotions
        if (board[from].piece == WHITE_PAWN && to < 8) {
            board[to].piece = WHITE_QUEEN;
        }
        else if (board[from].piece == BLACK_PAWN && to >= 56) {
            board[to].piece = BLACK_QUEEN;
        }
        //endregion
        // If a non-promotion move
        else {
            board[to].piece = board[from].piece;
        }
        // Remove piece which moved from its original square
        board[from].piece = EMPTY;
    }

    public static class Square extends JPanel {
        public final Color color;
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

        private String getSquareName() {
            String[] squareName = {
                    "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                    "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                    "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                    "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                    "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                    "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                    "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                    "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"
            };
            return squareName[index];
        }

        public char getPieceChar() {
            switch (this.piece) {
                case WHITE_PAWN, BLACK_PAWN -> {
                    return getSquareName().toCharArray()[0];
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

        public boolean hasNotChanged() {
            for (Move1 m : moves) {
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
            if (piece == WHITE_KING && isWhitesMove || piece == BLACK_KING && !isWhitesMove) {
                if (Main.getEndState() == 1) {
                    BufferedImage img = UI.pieceImages.get(CHECKMATE);
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                }
            }

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
                Square[] simulatedBoard = copyBoard(board, location, move);
                makeMove(simulatedBoard, location, move);
                if (!isKingInCheck(simulatedBoard, piece > 0)) {


                    // If castling:
                    int diff = move - location;
                    if (Math.abs(diff) == 2 && Math.abs(piece) == WHITE_KING) {

                        //region Handle no castling through check
                        // Find square between pre-castled and post-castled king square (where rook will be), calling it intermediateSquare
                        int direction = (diff > 0) ? 1 : -1;
                        byte intermediateSquare = (byte)(location + direction);

                        // Simulate moving the king to the intermediate square
                        Square[] simulatedCastlingBoard = copyBoard(board, location, intermediateSquare);
                        makeMove(simulatedCastlingBoard, location, intermediateSquare);

                        // If the king would be in check on the intermediate square, discard this castling move.
                        if (isKingInCheck(simulatedCastlingBoard, piece > 0)) {
                            continue;
                        }
                        //endregion

                        //region Handle no castling while in check
                        if (isKingInCheck(board, piece > 0)) {
                            continue;
                        }
                        //endregion
                    }

                    // Add it to validMoves if not castling or passes castling check
                    validMoves.add(move);
                }
            }
        } else {
            validMoves.addAll(rawMoves);
        }

        return validMoves;
    }

    private static ArrayList<Byte> getRawMoves(Square square, Square[] board) {
        ArrayList<Byte> byteMoves = new ArrayList<>();
        byte piece = square.piece;
        byte location = square.index;


        int squaresLeft = location % 8;          // Squares to the left edge
        int squaresRight = 7 - squaresLeft;      // Squares to the right edge

        switch (piece) {
            case WHITE_PAWN -> {
                // Pawn has no moves if on the 8th rank (for white)
                if (location <= 7) {
                    return byteMoves;
                }

                // If no pieces are in front of the pawn, it can move forward
                Square upperSquare = board[location - 8];
                if (upperSquare.isEmpty()) {
                    byteMoves.add((byte) (location - 8));

                    // Pawn can also move two squares if on the 2nd rank and no piece on the 4th rank
                    if (location / 8 == 6 && board[location - 16].isEmpty()) {
                        byteMoves.add((byte) (location - 16));
                    }
                }

                // If not on the 1st file
                if (squaresLeft > 0) {
                    Square upperLeftSquare = board[location - 9];
                    Square leftSquare = board[location - 1];

                    // And if there's a black piece to the upper left:
                    if (upperLeftSquare.piece < 0) {

                        // The pawn can capture diagonally to the upper left
                        byteMoves.add((byte) (location - 9));
                    }

                    // If there's a black pawn to the left
                    if (leftSquare.piece == BLACK_PAWN && moves.size() > 1) {

                        // And if the pawn just moved 16 indices
                        if (moves.getLast().getDistance() == 16 && moves.getLast().square2 == leftSquare) {

                            // The pawn can capture diagonally to the upper left
                            byteMoves.add((byte) (location - 9));
                        }
                    }
                }

                // If not on the 8th file
                if (squaresRight > 0) {
                    Square upperRightSquare = board[location - 7];
                    Square rightSquare = board[location + 1];

                    // And if there's a black piece to the upper right:
                    if (upperRightSquare.piece < 0) {

                        // The pawn can capture diagonally to the upper right
                        byteMoves.add((byte) (location - 7));
                    }

                    // If there's a black pawn to the right
                    if (rightSquare.piece == BLACK_PAWN && moves.size() > 1) {

                        // And if the pawn just moved 16 indices
                        if (moves.getLast().getDistance() == 16 && moves.getLast().square2 == rightSquare) {

                            // The pawn can capture diagonally to the upper right
                            byteMoves.add((byte) (location - 7));
                        }
                    }
                }
            }
            case BLACK_PAWN -> {
                // Pawn has no moves if on the 1st rank (for black)
                if (location >= 56) {
                    return byteMoves;
                }

                // If no pieces are below the pawn, it can move down
                Square lowerSquare = board[location + 8];
                if (lowerSquare.isEmpty()) {
                    byteMoves.add((byte) (location + 8));

                    // Pawn can also move two squares if on the 7th rank and no piece on the 5th rank
                    if (location / 8 == 1 && board[location + 16].isEmpty()) {
                        byteMoves.add((byte) (location + 16));
                    }
                }

                // If not on the 1st file
                if (squaresLeft > 0) {
                    Square lowerLeftSquare = board[location + 7];
                    Square leftSquare = board[location - 1];

                    // And if there's a white piece to the lower left:
                    if (lowerLeftSquare.piece > 0) {

                        // The pawn can capture diagonally to the lower left
                        byteMoves.add((byte) (location + 7));
                    }

                    // If there's a white pawn to the left
                    if (leftSquare.piece == WHITE_PAWN && moves.size() > 1) {

                        // And if the pawn just moved 16 indices
                        if (moves.getLast().getDistance() == 16 && moves.getLast().square2 == leftSquare) {

                            // The pawn can capture diagonally to the lower left
                            byteMoves.add((byte) (location + 7));
                        }
                    }
                }

                // If not on the 8th file
                if (squaresRight > 0) {
                    Square lowerRightSquare = board[location + 9];
                    Square rightSquare = board[location + 1];

                    // And if there's a white piece to the lower right:
                    if (lowerRightSquare.piece > 0) {

                        // The pawn can capture diagonally to the lower right
                        byteMoves.add((byte) (location + 9));
                    }

                    // If there's a white pawn to the right
                    if (rightSquare.piece == WHITE_PAWN && moves.size() > 1) {

                        // And if the pawn just moved 16 indices
                        if (moves.getLast().getDistance() == 16 && moves.getLast().square2 == rightSquare) {

                            // The pawn can capture diagonally to the lower right
                            byteMoves.add((byte) (location + 9));
                        }
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
                                byteMoves.add((byte) target);
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
                            byteMoves.add((byte) target);
                        }
                        // If the square has an enemy piece, add it and stop
                        else if (board[target].piece * piece < 0) {
                            byteMoves.add((byte) target);
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
                                byteMoves.add((byte) target);
                            }
                        }
                    }
                }

                // If the king hasn't moved
                if (square.hasNotChanged() && squaresLeft == 4) {

                    // Kingside castling:

                    // If there is space
                    Square ks1 = board[location + 1];
                    Square ks2 = board[location + 2];
                    if (ks1.isEmpty() && ks2.isEmpty()) {

                        // And if rook has not moved
                        if (board[location + 3].hasNotChanged()) {

                            // Add to moves
                            byteMoves.add((byte) (location + 2));
                        }
                    }

                    // Queenside castling:

                    // If there is space
                    Square qs1 = board[location - 1];
                    Square qs2 = board[location - 2];
                    Square qs3 = board[location - 3];
                    if (qs1.isEmpty() && qs2.isEmpty() && qs3.isEmpty()) {

                        // And rook has not moved
                        if (board[location - 4].hasNotChanged())  {

                            // Add to moves
                            byteMoves.add((byte) (location - 2));

                        }
                    }
                }
            }
        }

        return byteMoves;
    }
    //endregion

    //region Helper methods
    private static Square[] copyBoard(Square[] original, byte from, byte to) {
        Square[] copy = new Square[64];
        for (int i = 0; i < original.length; i++) {
            // Create deep copy if will be changed
//            if (i == from || i == to) {
                copy[i] = new Square(original[i]);
//            }
//            // Create shallow copy otherwise
//            else {
//                copy[i] = original[i];
//            }
        }
        return copy;
    }

    public static boolean isKingInCheck(Square[] board, boolean isWhite) {
        byte king = (isWhite ? WHITE_KING : BLACK_KING);
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

    public static void setBoardFromFEN(String fen) {
        byte location = 0, piece_type;

        // Place pieces
        stringIteratorLoop:
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
                case ' ':
                    break stringIteratorLoop;
                default:
                    throw new RuntimeException("Error in FEN String");
            }

            board[location].setPiece(piece_type);
            location++;
        }

        // Set black to move if specified
        if (fen.contains(" b")) {
            isWhitesMove = false;
        }

        // Print abnormal boards
        if (!fen.equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")) {
            printBoard();
        }

        // Repaint screen
        UI.repaint();
    }
    //endregion

    public static void printBoard() {
        for (int i = 0; i < board.length; i++) {
            Square s = board[i];
            char c = switch (s.piece) {
                case WHITE_KING -> 'K';
                case WHITE_QUEEN -> 'Q';
                case WHITE_ROOK -> 'R';
                case WHITE_BISHOP -> 'B';
                case WHITE_KNIGHT -> 'N';
                case WHITE_PAWN -> 'P';
                case BLACK_KING -> 'k';
                case BLACK_QUEEN -> 'q';
                case BLACK_ROOK -> 'r';
                case BLACK_BISHOP -> 'b';
                case BLACK_KNIGHT -> 'n';
                case BLACK_PAWN -> 'p';
                default -> '.';
            };

            System.out.print(c + " ");

            // Print a newline after every 8 squares to create rows
            if ((i + 1) % 8 == 0) {
                System.out.println();
            }
        }
    }


    public static void main(String[] args) {
        UI.mainPanel = UI.handleGUI();

        // Default piece setup
        setBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

        // Endgame
//        setBoardFromFEN("3k4/8/8/8/8/2q3q1/8/3K4 b - - 0 1");
    }
}