package core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import static core.UI.*;

public class Main {

    // Represent chessboard as 1D array of 64 squares
    public static Square[] board = new Square[64];

    public static Square enPassantTarget = null;

    //region Game State
    public static boolean isWhitesMove = true; // Tracks whose turn it is (true = White's turn, false = Black's turn)
    public static boolean gameOngoing = true;
    //endregion

    //region Moves
    public static ArrayList<Move> moves = new ArrayList<>(); // Stores all moves made in the game
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

    // Lost pieces lists
    public static ArrayList<Byte> lostWhitePieces = new ArrayList<>();
    public static ArrayList<Byte> lostBlackPieces = new ArrayList<>();

    public static HashMap<Byte, Integer> pieceValues = new HashMap<>();

    static {
        pieceValues.put(WHITE_PAWN, 1);
        pieceValues.put(WHITE_KNIGHT, 3);
        pieceValues.put(WHITE_BISHOP, 3);
        pieceValues.put(WHITE_ROOK, 5);
        pieceValues.put(WHITE_QUEEN, 9);

        pieceValues.put(BLACK_PAWN, 1);
        pieceValues.put(BLACK_KNIGHT, 3);
        pieceValues.put(BLACK_BISHOP, 3);
        pieceValues.put(BLACK_ROOK, 5);
        pieceValues.put(BLACK_QUEEN, 9);
    }

    //region enPassantTarget Access Methods
    public static void setEnPassantTarget(Square s) {
//        System.out.println("New En Passant Target: " + s.getSquareName());
        enPassantTarget = s;
    }

    public static void resetEnPassantTarget() {
//        System.out.println("En Passant Target reset");
        enPassantTarget = null;
    }

    public static Square getEnPassantTarget() {
//        System.out.println("Requested en passant target");
        return enPassantTarget;
    }
    //endregion

    /**
     * Attempts to call movePiece()
     * In case of promotions: only call movePiece once piece to promote to is selected
     */
    public static void tryMovePiece(Square square1, Square square2) {
        int from = square1.index;
        int to = square2.index;

        // Inside the promotion check in tryMovePiece
        UI.promotionFrom = square1;
        UI.promotionTo = square2;

        // Reset accessible moves
        accessibleMoves.clear();

        boolean canMakeMove = (accessibleSquaresOf(square1, board, true).contains(square2.index));
        if (canMakeMove) {

            //region Set up promotion prompt if promoting and prompt is not already up

            boolean isWhitePromotion = board[from].piece == WHITE_PAWN && to < 8;
            boolean isBlackPromotion = board[from].piece == BLACK_PAWN && to >= 56;
            if (!UI.isPromoting && (isWhitePromotion || isBlackPromotion)) {
//                System.out.println("Promotion in tryMovePiece");
                UI.isPromoting = true;
                promotionSquare = board[to];
                // Store the from and to squares for the promotion move
                UI.promotionFrom = square1;
                UI.promotionTo = square2;
                repaint();
                return;
            }

            //endregion

            // Make and display move
            String moveNotation = movePiece(square1, square2);
            UI.repaint();

            // Add it to move list
            moves.add(new Move(moveNotation, square1, square2));

//            System.out.println(Main.getCurrentFEN());
//            printBestMove();
            Stockfish.tryPlay(12);
        }
        else {
            UI.handleInvalidMoveTo(square2);
//            System.out.println(accessibleMoves);
        }
    }

    public static void printBestMove() {
        try {
            String[] result = Stockfish.getBestMoveWithEvaluation(Main.getCurrentFEN(), 14);
            System.out.println("bzz bzz, best move is: " + result[0] + "\t\t Eval: " + result[1]);
        }
        catch (IOException e) {
            System.out.println("Error getting stockfish eval" + e);
        }
    }

    public static void rematch() {
        UI.gameEnded = false;
        gameOngoing = true;
        isWhitesMove = true;
        accessibleMoves.clear();

        resetBoard();

        repaint();
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
            handleCapture(square2.piece);
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
        if (isWhitePromotion || isBlackPromotion) {
            // Set promotion variables
            promotionSquare = square2;
            isPromoting = false;

            // Update pieces on board
            board[to].piece = chosenPieceToPromoteTo;
            board[from].piece = EMPTY;

            // Reset chosen piece to promote to
            chosenPieceToPromoteTo = 0;
        }
        //endregion

        //region Handle en-passant

        if (square2.equals(getEnPassantTarget())) {
            Square behind = board[square2.index + Integer.signum(square1.piece) * 8];
            behind.piece = EMPTY;
            takesString = "x";
            handleCapture((byte) (square1.piece * -1));
        }
        //endregion

        //region Update en passant targets if pawn moved two squares
        if (Math.abs(square1.piece) == WHITE_PAWN && Math.abs(from - to) == 16) {
            int enPassantSquareIndex = (from + to) / 2;
            setEnPassantTarget(board[enPassantSquareIndex]);
        } else {
            resetEnPassantTarget();
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

    public static void handleCapture(byte piece) {
        if (piece == EMPTY) {
            System.out.println("Cannot capture an empty piece!!");
        }
        ArrayList<Byte> capturedPiecesList = (piece > 0) ? lostWhitePieces : lostBlackPieces;
        capturedPiecesList.add(piece);
        UI.playSound("sounds/Capture.wav");
    }
    /**
     * Returns 0 if stalemated, 1 if checkmated, -1 if pieces can move
     */
    public static int getEndState() {
        if (piecesCanMove()) {
            return -1;
        }

        // Handle checkmate
        // only triggers once before gameOngoing flag is set to false
        if (isKingInCheck(board, isWhitesMove)) {

            if (gameOngoing) {
                String winner = isWhitesMove ? "Black" : "White";
                UI.summonEndGamePanel(winner + " Wins", "by checkmate");

                playSound("sounds/beep.wav");
                gameOngoing = false;
            }

            return 1;
        }

        // Handle draw
        if (gameOngoing) {
            System.out.println("DRAW...");
            UI.summonEndGamePanel("Stalemate", "");

            playSound("sounds/beep.wav");
            gameOngoing = false;
        }
        return 0;
    }

    private static void makeMove(Square[] board, byte from, byte to) {
        //region Handle promotions
        if (board[from].piece == WHITE_PAWN && to < 8) {
            board[to].piece = chosenPieceToPromoteTo;
//            System.out.println("New piece: " + board[to].piece);
        }
        else if (board[from].piece == BLACK_PAWN && to >= 56) {
            board[to].piece = chosenPieceToPromoteTo;
//            System.out.println("New piece: " + board[to].piece);
        }
        //endregion
        // If a non-promotion move
        else {
            board[to].piece = board[from].piece;
        }
        // Remove piece which moved from its original square
        board[from].piece = EMPTY;

        //region Update en passant targets

        boolean pawnJustMoved = Math.abs(board[to].piece) == WHITE_PAWN;
        boolean moved16Indices = Math.abs(from - to) == 16;

        Square newEnPassantTarget = (pawnJustMoved && moved16Indices) ? board[(from + to) / 2] : null;
        if (newEnPassantTarget != null) {
            setEnPassantTarget(newEnPassantTarget);
        }
        else {
            resetEnPassantTarget();
        }
        //endregion

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
                    return board[i];
                }
            }
            System.out.println("returning null for " + s);
            return null;
        }

        public byte getPromotionOption() {
            byte[] promotionOptions = { WHITE_QUEEN, WHITE_KNIGHT, WHITE_ROOK, WHITE_BISHOP, BLACK_BISHOP, BLACK_ROOK, BLACK_KNIGHT, BLACK_QUEEN };
            int row = index / 8;
            return promotionOptions[row];
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

        public char getPieceCharForFEN() {
            switch (this.piece) {
                case WHITE_PAWN -> {
                    return 'P';
                }
                case BLACK_PAWN -> {
                    return 'p';
                }
                case WHITE_KNIGHT -> {
                    return 'N';
                }
                case BLACK_KNIGHT -> {
                    return 'n';
                }
                case WHITE_BISHOP -> {
                    return 'B';
                }
                case BLACK_BISHOP -> {
                    return 'b';
                }
                case WHITE_ROOK -> {
                    return 'R';
                }
                case BLACK_ROOK -> {
                    return 'r';
                }
                case WHITE_QUEEN -> {
                    return 'Q';
                }
                case BLACK_QUEEN -> {
                    return 'q';
                }
                case WHITE_KING -> {
                    return 'K';
                }
                case BLACK_KING -> {
                    return 'k';
                }
                default -> throw new RuntimeException("Piece char calculation failed. Is this piece: " + piece);
            }
        }


        public boolean hasNotChanged() {
            for (Move m : moves) {
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
            if (piece == WHITE_KING && isWhitesMove || piece == BLACK_KING && !isWhitesMove) {
                if (Main.getEndState() == 1) {
                    BufferedImage img = UI.pieceImages.get(CHECKMATE);
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                }
            }

            // Draw piece if NOT being dragged
            if (piece != EMPTY && !this.isDragging()) {
                BufferedImage img = pieceImages.get(piece);
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);

            }
        }

        public boolean isDragging() {
            return (this == UI.selectedSquare && UI.beingDragged);
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
                // Save the current enPassantTarget
                Square originalEnPassant = getEnPassantTarget();

                Square[] simulatedBoard = copyBoard(board, location, move);
                makeMove(simulatedBoard, location, move);
                boolean isInCheck = isKingInCheck(simulatedBoard, piece > 0);

                // Restore enPassantTarget after simulation
                if (originalEnPassant != null) {
                    setEnPassantTarget(originalEnPassant);
                }
                else {
                    resetEnPassantTarget();
                }

                if (!isInCheck) {

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
                    if (leftSquare.piece == BLACK_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (upperLeftSquare.equals(getEnPassantTarget()))  {

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
                    if (rightSquare.piece == BLACK_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (upperRightSquare.equals(getEnPassantTarget()))  {

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
                    if (leftSquare.piece == WHITE_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (lowerLeftSquare.equals(getEnPassantTarget()))  {

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
                    if (rightSquare.piece == WHITE_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (lowerRightSquare.equals(getEnPassantTarget()))  {
                            // The pawn can capture diagonally to the lower right
                            byteMoves.add((byte) (location + 9));
                        }
                    }
                }
            }
            case WHITE_KNIGHT, BLACK_KNIGHT -> {
                int[] knightMoves = {-17, -15, -10, -6, 6, 10, 15, 17};

                // For all possible knight moves
                for (int offset : knightMoves) {

                    int target = location + offset;

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

    /**
     * Can take as input kingSquare which does not have a king on it, and will return false
     */
    public static boolean canCastle(boolean isKingside, Square kingSquare) {
        int location = kingSquare.index;

        // Return false early if king not on correct square
        if (Math.abs(kingSquare.piece) != WHITE_KING) {
            return false;
        }

        // Return false early if king square has changed
        for (Move m : moves) {
            if (m.square1 == kingSquare) {
                return false;
            }
        }

        // If rook has not moved
        if (board[location + 3].hasNotChanged()) {

            // And it is asking about kings
            if (isKingside) {

                return true;
            }
        }

        // If rook has not moved
        if (board[location - 4].hasNotChanged())  {

            // And if it is asking about queenside
            if (!isKingside) {

                return true;
            }
        }

        // Otherwise:
        return false;
    }

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

        // Clear board
        for (Square s : board) {
            s.piece = EMPTY;
        }

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

        String[] fenSections = fen.split(" ");

        // If FEN only has one section, we should not change anything else and return early
        if (fenSections.length == 1) {
            return;
        }

        // Set black to move if specified
        if (fenSections[1].contains("b")) {
            isWhitesMove = false;
        }

        //region Get FEN String
        // if 3rd to last section has an alphabetic character that's the en passant-able pawn
        String thirdToLastSection = fenSections[fenSections.length - 3];
        if (thirdToLastSection.chars().anyMatch(Character::isLowerCase)) {
            Square newEnPassantTarget = Square.convertStringToSquare(thirdToLastSection);
            if (newEnPassantTarget != null) {
                setEnPassantTarget(newEnPassantTarget);
            }
            else {
                resetEnPassantTarget();
            }
        }


        //endregion

        // Handle abnormal boards
        if (!fen.equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")) {
            // Print them
//            printBoard();

            //region Update lost pieces
            byte[] pieceConstants = {Main.WHITE_PAWN, Main.WHITE_KNIGHT, Main.WHITE_BISHOP, Main.WHITE_ROOK, Main.WHITE_QUEEN, Main.WHITE_KING, Main.BLACK_PAWN, Main.BLACK_KNIGHT, Main.BLACK_BISHOP, Main.BLACK_ROOK, Main.BLACK_QUEEN, Main.BLACK_KING};
            int[] pieceInitialCounts = {8, 2, 2, 2, 1, 1, 8, 2, 2, 2, 1, 1};

            for (int i = 0; i < pieceConstants.length; i++) {
                byte piece = pieceConstants[i];
                int actualCount = getPieceCount(piece);
                int supposedCount = pieceInitialCounts[i];
                int countLost = supposedCount - actualCount;

                // Update lost pieces arrayList
                for (int j = 0; j < countLost; j++) {
                    ArrayList<Byte> capturedPiecesList = (piece > 0) ? lostWhitePieces : lostBlackPieces;
                    capturedPiecesList.add(piece);
                }
            }
            //endregion
        }

        // Repaint screen
        UI.repaint();
    }
    //endregion

    private static int getPieceCount(byte piece) {
        int count = 0;
        for (Square s : board) {
            if (s.piece == piece) {
                count++;
            }
        }
        return count;
    }

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

    public static void resetBoard() {
        setBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        lostWhitePieces.clear();
        lostBlackPieces.clear();
    }

    public static int getMaterialDiff() {
        int whiteMaterial = 0;
        int blackMaterial = 0;

        for (byte piece : lostWhitePieces) {
            int value = pieceValues.get(piece);
            whiteMaterial += value;
        }

        for (byte piece : lostBlackPieces) {
            int value = pieceValues.get(piece);
            blackMaterial += value;
        }

        return whiteMaterial - blackMaterial;
    }

    public static String getCurrentFEN() {
        StringBuilder stringBuilder = new StringBuilder();

        //region Fill in piece locations part of FEN
        int emptyCounter = 0;
        for (Square s : board) {
            if (s.isEmpty()) {
                emptyCounter++;
            }
            else {
                if (emptyCounter != 0) {
                    stringBuilder.append(emptyCounter);
                    emptyCounter = 0;
                }
                stringBuilder.append(s.getPieceCharForFEN());
            }

            // After all checks, if the square is at the end of its row add a '/'
            if (s.index % 8 == 7) {
                if (emptyCounter != 0) {
                    stringBuilder.append(emptyCounter);
                    emptyCounter = 0;
                }
                stringBuilder.append('/');
            }
        }
        stringBuilder.delete(stringBuilder.length()-1, stringBuilder.length());
        //endregion

        //region Fill in side to move part of FEN
        stringBuilder.append(' ');
        char sideToMoveChar = (isWhitesMove) ? 'w' : 'b';
        stringBuilder.append(sideToMoveChar);
        //endregion

        //region Fill in castling rights part of FEN
        stringBuilder.append(' ');
        Square blackKingSquare = board[4];
        Square whiteKingSquare = board[60];

        boolean whiteCanCastleKingside = canCastle(true, whiteKingSquare);
        boolean whiteCanCastleQueenside = canCastle(false, whiteKingSquare);
        boolean blackCanCastleKingside = canCastle(true, blackKingSquare);
        boolean blackCanCastleQueenside = canCastle(false, blackKingSquare);

        if (whiteCanCastleKingside) { stringBuilder.append("K"); }
        if (whiteCanCastleQueenside) { stringBuilder.append("Q"); }
        if (blackCanCastleKingside) { stringBuilder.append("k"); }
        if (blackCanCastleQueenside) { stringBuilder.append("q"); }

        if (!whiteCanCastleKingside && !whiteCanCastleQueenside && !blackCanCastleKingside && !blackCanCastleQueenside) {
            stringBuilder.append("- -");
        }
        else {
            stringBuilder.append(" -");
        }
        //endregion

        //region Add en passant targets only if an en passant could be performed
        Square ePTarget = getEnPassantTarget();

        if (ePTarget != null) {
            Square left = board[ePTarget.index - 1];
            Square right = board[ePTarget.index - 1];

            byte enemyPawn = isWhitesMove ? WHITE_PAWN : BLACK_PAWN;

            boolean enPassPossible = left.piece == enemyPawn || right.piece == enemyPawn;

            if (enPassPossible) {
                stringBuilder.append(' ');
                stringBuilder.append(ePTarget.getSquareName());
            }
        }
        //endregion

        //region Add half moves
        // TODO after implementing 50 move rule, add it to FEN
        stringBuilder.append(" 0");
        //endregion

        //region Add full-move number
        stringBuilder.append(" ").append(1 + moves.size() / 2);
        //endregion

        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        UI.mainPanel = UI.handleGUI();

        // Default piece setup
        setBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

        // Endgame with black winning
//        setBoardFromFEN("3k4/8/8/8/8/2q3q1/8/3K4 b - - 0 1");

        // Endgame with white winning
//        setBoardFromFEN("3K4/8/8/8/8/2Q3Q1/8/3k4 w - - 0 1");

//        setBoardFromFEN("8/4PPP1/2k5/8/2K5/8/4pp1p/8 w - - 0 1");
//        setBoardFromFEN("r5rk/2p1Nppp/3p3P/pp2p1P1/4P3/2qnPQK1/8/R6R w - - 1 0");
        Stockfish.isPlaying = true;
        Stockfish.isWhite = false;
//        setBoardFromFEN("rnbqkbnr/ppppp1pp/8/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 1");

    }


}