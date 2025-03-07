package core;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static core.Main.*;
import static core.UI.*;
import static core.UI.chosenPieceToPromoteTo;

public class Board {

    //region Attributes
    public Square[] squares = new Square[64];
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

    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

    // Pieces
    public ArrayList<Byte> capturedWhitePieces = new ArrayList<>();
    public ArrayList<Byte> capturedBlackPieces = new ArrayList<>();

    // Turn to move
    public boolean isWhitesMove = true;

    // En passant handling
    public Square enPassantTarget = null;
    //endregion

    //region Initialization and Setup
    public Board() {
        // Fill with squares
        for (byte i = 0; i < 64; i++) {
            Color color = (i + i / 8) % 2 == 0 ? UI.WHITE : UI.BLACK;
            Square square = new Square(color, i);
            squares[i] = square;
        }
    }

    public Board(Board b) {
        // Squares
        for (int i = 0; i < 64; i++) {
            squares[i] = new Square(b.squares[i]);
        }
        // Lost Pieces
        this.capturedWhitePieces = new ArrayList<>(b.capturedWhitePieces);
        this.capturedBlackPieces = new ArrayList<>(b.capturedBlackPieces);

        // Turn to move
        this.isWhitesMove = b.isWhitesMove;

        // En passant
        Square enPassantTarget = b.enPassantTarget;
    }

    public void addSquaresToPanel(JPanel mainPanel) {
        for (Square s : squares) {
            mainPanel.add(s);
        }
    }

    public void reset() {
        setFromFEN(STARTING_FEN);
        capturedWhitePieces.clear();
        capturedBlackPieces.clear();
    }
    //endregion

    //region FEN handling
    public void setFromFEN(String fen) {
        byte location = 0, piece_type;

        // Clear
        for (Square s : squares) {
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

            squares[location].setPiece(piece_type);
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

        // Handle abnormal FEN
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
                    ArrayList<Byte> capturedPiecesList = (piece > 0) ? capturedWhitePieces : capturedBlackPieces;
                    capturedPiecesList.add(piece);
                }
            }
            //endregion
        }

        // Repaint screen
        UI.repaint();
    }

    public String getCurrentFEN() {
        StringBuilder stringBuilder = new StringBuilder();

        //region Fill in piece locations part of FEN
        int emptyCounter = 0;
        for (Square s : squares) {
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
        Square blackKingSquare = squares[4];
        Square whiteKingSquare = squares[60];

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
            Square left = squares[ePTarget.index - 1];
            Square right = squares[ePTarget.index - 1];

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
    //endregion

    //region Helper Functions
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < squares.length; i++) {
            Square s = squares[i];
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

            sb.append(c).append(" ");

            // Print a newline after every 8 squares to create rows
            if ((i + 1) % 8 == 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public Square convertStringToSquare(String s) {
        s.trim();
        for (int i = 0; i < squareName.length; i++) {
            String name = squareName[i];
            if (name.equals(s)) {
                return squares[i];
            }
        }
        System.out.println("returning null for " + s);
        return null;
    }

    private int getPieceCount(byte piece) {
        int count = 0;
        for (Square s : squares) {
            if (s.piece == piece) {
                count++;
            }
        }
        return count;
    }

    public int getMaterialDiff() {
        int whiteMaterial = 0;
        int blackMaterial = 0;

        for (byte piece : capturedWhitePieces) {
            int value = pieceValues.get(piece);
            whiteMaterial += value;
        }

        for (byte piece : capturedBlackPieces) {
            int value = pieceValues.get(piece);
            blackMaterial += value;
        }

        return whiteMaterial - blackMaterial;
    }
    //endregion

    //region enPassantTarget Access Methods
    public void setEnPassantTarget(Square s) {
        enPassantTarget = s;
    }

    public void resetEnPassantTarget() {
        enPassantTarget = null;
    }

    public Square getEnPassantTarget() {
        return enPassantTarget;
    }
    //endregion

    //region Move Execution
    /**
     * Attempts to call movePiece()
     * In case of promotions: only call movePiece once piece to promote to is selected
     */
    public void attemptMove(Square square1, Square square2) {
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

            boolean isWhitePromotion = board.squares[from].piece == WHITE_PAWN && to < 8;
            boolean isBlackPromotion = board.squares[from].piece == BLACK_PAWN && to >= 56;
            if (!UI.isPromoting && (isWhitePromotion || isBlackPromotion)) {
//                System.out.println("Promotion in tryMovePiece");
                UI.isPromoting = true;
                promotionSquare = board.squares[to];
                // Store the from and to squares for the promotion move
                UI.promotionFrom = square1;
                UI.promotionTo = square2;
                repaint();
                return;
            }

            //endregion

            // Make and display move
            String moveNotation = executeMove(square1, square2);
            UI.repaint();

            // Add it to move list
            moves.add(new Move(moveNotation, square1, square2));

            // Get Stockfish's response if possible
            Stockfish.tryPlay(12);
        }
        else {
            UI.handleInvalidMoveTo(square2);
//            System.out.println(accessibleMoves);
        }
    }

    /**
     * Move piece from square1 to square2 <br>
     * returns chess notation for move
     */
    public String executeMove(Square square1, Square square2) {

        byte from = square1.index;
        byte to = square2.index;

        // Update variables
        board.isWhitesMove = !board.isWhitesMove;
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
            board.handleCapture(square2.piece);
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
                Square rookSquare = board.squares[square2.index + 1];
                Square otherSide = board.squares[square2.index - 1];
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
                Square rookSquare = board.squares[square2.index - 2];
                Square otherSide = board.squares[square2.index + 1];
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
        boolean isWhitePromotion = board.squares[from].piece == WHITE_PAWN && to < 8;
        boolean isBlackPromotion = board.squares[from].piece == BLACK_PAWN && to >= 56;
        if (isWhitePromotion || isBlackPromotion) {
            // Set promotion variables
            promotionSquare = square2;
            isPromoting = false;

            // Update pieces on board
            board.squares[to].piece = chosenPieceToPromoteTo;
            board.squares[from].piece = EMPTY;

            // Reset chosen piece to promote to
            chosenPieceToPromoteTo = 0;
        }
        //endregion

        //region Handle en-passant

        if (square2.equals(board.getEnPassantTarget())) {
            Square behind = board.squares[square2.index + Integer.signum(square1.piece) * 8];
            behind.piece = EMPTY;
            takesString = "x";
            board.handleCapture((byte) (square1.piece * -1));
        }
        //endregion

        //region Update en passant targets if pawn moved two squares
        if (Math.abs(square1.piece) == WHITE_PAWN && Math.abs(from - to) == 16) {
            int enPassantSquareIndex = (from + to) / 2;
            board.setEnPassantTarget(board.squares[enPassantSquareIndex]);
        } else {
            board.resetEnPassantTarget();
        }
        //endregion

        // If a non-promotion and non-castling move, update new square
        if (!isBlackPromotion && !isWhitePromotion) {
            board.squares[to].piece = board.squares[from].piece;
        }

        // Remove piece which moved from its original square
        board.squares[from].piece = EMPTY;


        return movingPieceStr + takesString + toLocation;
    }


    public void handleCapture(byte piece) {
        if (piece == EMPTY) {
            System.out.println("Cannot capture an empty piece!!");
        }
        ArrayList<Byte> capturedPiecesList = (piece > 0) ? capturedWhitePieces : capturedBlackPieces;
        capturedPiecesList.add(piece);
        UI.playSound("sounds/Capture.wav");
    }
    //endregion

    //region Piece Movement Logic
    private ArrayList<Byte> getRawMoves(Square square, Board board) {
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
                Square upperSquare = board.squares[location - 8];
                if (upperSquare.isEmpty()) {
                    byteMoves.add((byte) (location - 8));

                    // Pawn can also move two squares if on the 2nd rank and no piece on the 4th rank
                    if (location / 8 == 6 && board.squares[location - 16].isEmpty()) {
                        byteMoves.add((byte) (location - 16));
                    }
                }

                // If not on the 1st file
                if (squaresLeft > 0) {
                    Square upperLeftSquare = board.squares[location - 9];
                    Square leftSquare = board.squares[location - 1];

                    // And if there's a black piece to the upper left:
                    if (upperLeftSquare.piece < 0) {

                        // The pawn can capture diagonally to the upper left
                        byteMoves.add((byte) (location - 9));
                    }

                    // If there's a black pawn to the left
                    if (leftSquare.piece == BLACK_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (upperLeftSquare.equals(board.getEnPassantTarget()))  {

                            // The pawn can capture diagonally to the upper left
                            byteMoves.add((byte) (location - 9));
                        }
                    }
                }

                // If not on the 8th file
                if (squaresRight > 0) {
                    Square upperRightSquare = board.squares[location - 7];
                    Square rightSquare = board.squares[location + 1];

                    // And if there's a black piece to the upper right:
                    if (upperRightSquare.piece < 0) {

                        // The pawn can capture diagonally to the upper right
                        byteMoves.add((byte) (location - 7));
                    }

                    // If there's a black pawn to the right
                    if (rightSquare.piece == BLACK_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (upperRightSquare.equals(board.getEnPassantTarget()))  {

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
                Square lowerSquare = board.squares[location + 8];
                if (lowerSquare.isEmpty()) {
                    byteMoves.add((byte) (location + 8));

                    // Pawn can also move two squares if on the 7th rank and no piece on the 5th rank
                    if (location / 8 == 1 && board.squares[location + 16].isEmpty()) {
                        byteMoves.add((byte) (location + 16));
                    }
                }

                // If not on the 1st file
                if (squaresLeft > 0) {
                    Square lowerLeftSquare = board.squares[location + 7];
                    Square leftSquare = board.squares[location - 1];

                    // And if there's a white piece to the lower left:
                    if (lowerLeftSquare.piece > 0) {

                        // The pawn can capture diagonally to the lower left
                        byteMoves.add((byte) (location + 7));
                    }

                    // If there's a white pawn to the left
                    if (leftSquare.piece == WHITE_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (lowerLeftSquare.equals(board.getEnPassantTarget()))  {

                            // The pawn can capture diagonally to the lower left
                            byteMoves.add((byte) (location + 7));
                        }
                    }
                }

                // If not on the 8th file
                if (squaresRight > 0) {
                    Square lowerRightSquare = board.squares[location + 9];
                    Square rightSquare = board.squares[location + 1];

                    // And if there's a white piece to the lower right:
                    if (lowerRightSquare.piece > 0) {

                        // The pawn can capture diagonally to the lower right
                        byteMoves.add((byte) (location + 9));
                    }

                    // If there's a white pawn to the right
                    if (rightSquare.piece == WHITE_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (lowerRightSquare.equals(board.getEnPassantTarget()))  {
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
                            if ((board.squares[target].piece * piece <= 0)) {

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
                        if (board.squares[target].isEmpty()) {
                            byteMoves.add((byte) target);
                        }
                        // If the square has an enemy piece, add it and stop
                        else if (board.squares[target].piece * piece < 0) {
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
                            if ((board.squares[target].piece * piece <= 0)) {

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
                    Square ks1 = board.squares[location + 1];
                    Square ks2 = board.squares[location + 2];
                    if (ks1.isEmpty() && ks2.isEmpty()) {

                        // And if rook has not moved
                        if (board.squares[location + 3].hasNotChanged()) {

                            // Add to moves
                            byteMoves.add((byte) (location + 2));
                        }
                    }

                    // Queenside castling:

                    // If there is space
                    Square qs1 = board.squares[location - 1];
                    Square qs2 = board.squares[location - 2];
                    Square qs3 = board.squares[location - 3];
                    if (qs1.isEmpty() && qs2.isEmpty() && qs3.isEmpty()) {

                        // And rook has not moved
                        if (board.squares[location - 4].hasNotChanged())  {

                            // Add to moves
                            byteMoves.add((byte) (location - 2));

                        }
                    }
                }
            }
        }

        return byteMoves;
    }

    public ArrayList<Byte> accessibleSquaresOf(Square square, Board board, boolean checkForChecks) {
        ArrayList<Byte> validMoves = new ArrayList<>();
        byte piece = square.piece;
        byte location = square.index;

        // Get all potential moves without considering checks
        ArrayList<Byte> rawMoves = getRawMoves(square, board);

        if (checkForChecks) {
            // Verify each move doesn't leave king in check
            for (Byte move : rawMoves) {
                // Save the current enPassantTarget
                Square originalEnPassant = board.getEnPassantTarget();

                Board simulatedBoard = new Board(board);
                simulateMove(simulatedBoard, location, move);
                boolean isInCheck = isKingInCheck(simulatedBoard, piece > 0);

                // Restore enPassantTarget after simulation
                if (originalEnPassant != null) {
                    board.setEnPassantTarget(originalEnPassant);
                }
                else {
                    board.resetEnPassantTarget();
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
                        Board simulatedCastlingBoard = new Board(board);
                        simulateMove(simulatedCastlingBoard, location, intermediateSquare);

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

    private void simulateMove(Board on, byte from, byte to) {
        //region Handle promotions
        if (on.squares[from].piece == WHITE_PAWN && to < 8) {
            on.squares[to].piece = chosenPieceToPromoteTo;
//            System.out.println("New piece: " + board[to].piece);
        }
        else if (on.squares[from].piece == BLACK_PAWN && to >= 56) {
            on.squares[to].piece = chosenPieceToPromoteTo;
//            System.out.println("New piece: " + board[to].piece);
        }
        //endregion

        //region Handle normal movement (piece deleted from old square and placed on new one)
        else {
            on.squares[to].piece = on.squares[from].piece;
        }
        on.squares[from].piece = EMPTY;
        //endregion

        //region Update en passant targets

        boolean pawnJustMoved = Math.abs(on.squares[to].piece) == WHITE_PAWN;
        boolean moved16Indices = Math.abs(from - to) == 16;

        Square newEnPassantTarget = (pawnJustMoved && moved16Indices) ? on.squares[(from + to) / 2] : null;
        if (newEnPassantTarget != null) {
            on.setEnPassantTarget(newEnPassantTarget);
        }
        else {
            on.resetEnPassantTarget();
        }
        //endregion

    }
    //endregion

    //region Game State Management
    /**
     * Returns 0 if stalemated, 1 if checkmated, -1 if pieces can move
     */
    public int checkGameOutcome() {
        if (piecesCanMove()) {
            return -1;
        }

        // Handle checkmate
        // only triggers once before gameOngoing flag is set to false
        if (isKingInCheck(board, board.isWhitesMove)) {

            if (gameOngoing) {
                String winner = board.isWhitesMove ? "Black" : "White";
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
    //endregion

    //region Move Validation
    public boolean isKingInCheck(Board board, boolean isWhite) {
        byte king = (isWhite ? WHITE_KING : BLACK_KING);
        Square kingSquare = null;

        // Find king position
        for (Square s : board.squares) {
            if (s.piece == king) {
                kingSquare = s;
                break;
            }
        }
        if (kingSquare == null) return false;

        // Check all opposing pieces
        for (Square s : board.squares) {
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
     * Can take as input kingSquare which does not have a king on it, and will return false
     */
    public boolean canCastle(boolean isKingside, Square kingSquare) {
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
        if (squares[location + 3].hasNotChanged()) {

            // And it is asking about kings
            if (isKingside) {

                return true;
            }
        }

        // If rook has not moved
        if (squares[location - 4].hasNotChanged())  {

            // And if it is asking about queenside
            if (!isKingside) {

                return true;
            }
        }

        // Otherwise:
        return false;
    }

    public boolean piecesCanMove() {
        for (Square s : board.squares) {
            if (s.piece == EMPTY) {
                continue;
            }
            boolean isWhite = Math.signum(s.piece) > 0;
            boolean isPiecesTurnToMove =  isWhite && board.isWhitesMove || !isWhite && !board.isWhitesMove;

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
    //endregion
}
