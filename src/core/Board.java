package core;

import java.util.*;

import static core.Main.*;
import static core.UI.*;

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
    private final Map<String, Square> nameToSquare = new HashMap<>();

    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // Pieces
    public ArrayList<Byte> capturedWhitePieces = new ArrayList<>();
    public ArrayList<Byte> capturedBlackPieces = new ArrayList<>();

    public byte chosenPieceToPromoteTo = 0;

    // Turn to move
    public boolean isWhitesMove = true;

    public int halfMoveCounter = 0;

    // En passant handling
    public Square enPassantTarget = null;

    //endregion

    public double currentEval;


    //region Initialization and Setup
    public Board() {
        // Fill with squares
        for (byte i = 0; i < 64; i++) {
            Square square = new Square(i);
            squares[i] = square;
            nameToSquare.put(squareName[i], square);
        }

        // Hardcode starting eval
        currentEval = -0.2;
    }

    //
//    public Board(Board original, byte from, byte to) {
//        byte movingPieceType = (byte) Math.abs(original.squares[from].piece);
//
//        // Shallow copy squares
//        System.arraycopy(original.squares, 0, this.squares, 0, 64);
//
//        // Deep copy from and to squares
//        this.squares[from] = new Square(original.squares[from]);
//        this.squares[to] = new Square(original.squares[from]);
//
//        //region Deep copy castling squares if necessary
//        boolean isCastling = movingPieceType == WHITE_KING && Math.abs(from - to) == 2;
//        if (isCastling) {
//            for (int i = -4; i <= 3; i++) {
//                this.squares[from + i] = new Square(original.squares[from + i]);
//            }
//        }
//        //endregion
//
//        //region Deep copy possible en passant locations if necessary
//        boolean isPawnCaptureFormation = (Math.abs(from - to) == 7 || Math.abs(from - to) == 9);
//        if (isPawnCaptureFormation && movingPieceType == WHITE_PAWN) {
//            // Deep copy 2 other squares in 2x2 area to also deep copy en passant squares
//            if (from - to == 7 || to - from == 9) {
//                this.squares[from + 1] = new Square(original.squares[from + 1]);
//            }
//            if (to - from == 7 || from - to == 9) {
//                this.squares[from - 1] = new Square(original.squares[from - 1]);
//            }
//        }
//        //endregion
//
//        // Copy game state
//        this.isWhitesMove = original.isWhitesMove;
//        this.enPassantTarget = original.enPassantTarget != null ?
//                new Square(original.enPassantTarget) : null;
//        this.capturedWhitePieces = new ArrayList<>(original.capturedWhitePieces);
//        this.capturedBlackPieces = new ArrayList<>(original.capturedBlackPieces);
//        this.chosenPieceToPromoteTo = original.chosenPieceToPromoteTo;
//    }

    public Board(Board original) {
        // Deep copy all squares
        for (byte i = 0; i < 64; i++) {
            this.squares[i] = new Square(original.squares[i]);
        }

        // Copy other fields
        this.isWhitesMove = original.isWhitesMove;
        this.enPassantTarget = original.enPassantTarget != null ?
                this.squares[original.enPassantTarget.index] : null;
        this.capturedWhitePieces = new ArrayList<>(original.capturedWhitePieces);
        this.capturedBlackPieces = new ArrayList<>(original.capturedBlackPieces);
        this.chosenPieceToPromoteTo = original.chosenPieceToPromoteTo;
    }

    public Board(Board original, byte from, byte to) {
        byte movingPieceType = (byte) Math.abs(original.squares[from].piece);

        // Shallow copy squares
        System.arraycopy(original.squares, 0, this.squares, 0, 64);

        // Deep copy from and to squares
        this.squares[from] = new Square(original.squares[from]);
        this.squares[to] = new Square(original.squares[from]);

        //region Deep copy castling squares if necessary
        boolean isCastling = movingPieceType == WHITE_KING && Math.abs(from - to) == 2;
        if (isCastling) {
            for (int i = -4; i <= 3; i++) {
                this.squares[from + i] = new Square(original.squares[from + i]);
            }
        }
        //endregion

        //region Deep copy possible en passant locations if necessary
        boolean isPawnCaptureFormation = (Math.abs(from - to) == 7 || Math.abs(from - to) == 9);
        if (isPawnCaptureFormation && movingPieceType == WHITE_PAWN) {
            // Deep copy 2 other squares in 2x2 area to also deep copy en passant squares
            if (from - to == 7 || to - from == 9) {
                this.squares[from + 1] = new Square(original.squares[from + 1]);
            }
            if (to - from == 7 || from - to == 9) {
                this.squares[from - 1] = new Square(original.squares[from - 1]);
            }
        }
        //endregion

        // Copy game state
        this.isWhitesMove = original.isWhitesMove;
        this.enPassantTarget = original.enPassantTarget != null ?
                new Square(original.enPassantTarget) : null;
        this.capturedWhitePieces = new ArrayList<>(original.capturedWhitePieces);
        this.capturedBlackPieces = new ArrayList<>(original.capturedBlackPieces);
        this.chosenPieceToPromoteTo = original.chosenPieceToPromoteTo;
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
//            s.piece = EMPTY;
            s.removePiece();
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

        //region Advanced FEN handling
        // if 3rd to last section has an alphabetic character that's the en passant-able pawn
        String thirdToLastSection = fenSections[fenSections.length - 3];
        if (thirdToLastSection.chars().anyMatch(Character::isLowerCase)) {
            Square newEnPassantTarget = convertStringToSquare(thirdToLastSection);
            if (newEnPassantTarget != null) {
                setEnPassantTarget(newEnPassantTarget);
            }
            else {
                resetEnPassantTarget();
            }
        }

        // Parse 2nd to last section to halfMoveCounter
        String secondToLastSection = fenSections[fenSections.length - 2];
        halfMoveCounter = Integer.parseInt(secondToLastSection);
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
            stringBuilder.append(" -");
        }
//        else {
//            stringBuilder.append(" -");
//        }

        //endregion

        //region Add en passant targets only if an en passant could be performed
        Square ePTarget = getEnPassantTarget();
        if (ePTarget != null) {
            int destinationIndex;
            // Determine destination square based on whose turn it is
            if (isWhitesMove) {
                // enPassantTarget was set by a black pawn's move, destination is ePTarget.index + 8
                destinationIndex = ePTarget.index + 8;
            } else {
                // enPassantTarget was set by a white pawn's move, destination is ePTarget.index - 8
                destinationIndex = ePTarget.index - 8;
            }

            // Check if destination is within bounds
            if (destinationIndex < 0 || destinationIndex >= 64) {
                stringBuilder.append(" -");
            } else {
                int destFile = destinationIndex % 8;
                int leftDest = destinationIndex - 1;
                int rightDest = destinationIndex + 1;
                byte expectedPawn = isWhitesMove ? WHITE_PAWN : BLACK_PAWN;
                boolean enPassPossible = false;

                // Check left adjacent square if valid
                if (destFile > 0) {
                    Square leftSquare = squares[leftDest];
                    if (leftSquare.piece == expectedPawn) {
                        enPassPossible = true;
                    }
                }

                // Check right adjacent square if valid
                if (destFile < 7) {
                    Square rightSquare = squares[rightDest];
                    if (rightSquare.piece == expectedPawn) {
                        enPassPossible = true;
                    }
                }

                if (enPassPossible) {
                    stringBuilder.append(' ').append(ePTarget.getSquareName());
                } else {
                    stringBuilder.append(" -");
                }
            }
        } else {
            stringBuilder.append(" -");
        }
//endregion

        //region Add half moves
        stringBuilder.append(" ").append(halfMoveCounter);
        //endregion

        //region Add full-move number
        stringBuilder.append(" ").append(1 + moves.size() / 2);
        //endregion

        return stringBuilder.toString();
    }

    /**
     * Returns current FEN without last two fields (half-moves for threefold counting and full-moves in game)
     */
    public String getBoardStateForThreefoldChecking() {
        String fen = getCurrentFEN();
        String[] decomposedFEN = fen.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < decomposedFEN.length - 2; i++) {
            sb.append(decomposedFEN[i]);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static boolean isDrawFromThreefold() {
        for (String boardstate : threefoldStates) {
            if (Collections.frequency(threefoldStates, boardstate) >= 3) {
                return true;
            }
        }
        return false;
    }

    public boolean isInsufficientMaterial() {
        int whiteMaterial = 0;
        int blackMaterial = 0;
        for (Square  s : squares) {
            switch (s.piece) {
                case EMPTY, WHITE_KING, BLACK_KING -> {}
                case WHITE_BISHOP, WHITE_KNIGHT -> whiteMaterial += s.piece;
                case BLACK_BISHOP, BLACK_KNIGHT -> blackMaterial -= s.piece;
                default -> {
                    return false;
                }
            }
        }
        // If each side has >1 bishop or bishop+knight
        if (whiteMaterial >= 5 || blackMaterial >= 5) {
            return false;
        }
        // If one side has two knights only insufficient material if other side material = 0
        boolean oneSideHasTwoKnights = whiteMaterial == 4 || blackMaterial == 4;
        if (oneSideHasTwoKnights && whiteMaterial + blackMaterial != 4) {
            return false;
        }

        return true;

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
        return nameToSquare.getOrDefault(s, null);
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

    public void updateHalfMoves(Move m) {
        if (m.isHalfMoveReset()) {
            halfMoveCounter = 0;
        }
        else {
            halfMoveCounter++;
        }
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

        boolean canMakeMove = accessibleSquaresOf(square1).contains((int) square2.index);
        if (canMakeMove) {

            //region Set up promotion prompt if promoting and prompt is not already up

            boolean isWhitePromotion = squares[from].piece == WHITE_PAWN && to < 8;
            boolean isBlackPromotion = squares[from].piece == BLACK_PAWN && to >= 56;
            if (!UI.isPromoting && (isWhitePromotion || isBlackPromotion)) {
                UI.isPromoting = true;
                promotionSquare = squares[to];
                // Store the from and to squares for the promotion move
                UI.promotionFrom = square1;
                UI.promotionTo = square2;
                repaint();
                return;
            }

            //endregion

            executeMove(square1, square2, chosenPieceToPromoteTo, false);

            // Get engine's response if possible
            Main.currentEngine.tryPlay(3);
        }
        else {
            Move invalid = new Move(board, square1.getSquareName() + square2.getSquareName());
            UI.handleInvalidMoveTo(invalid);
        }
    }

    /**
     * Move piece from square1 to square2 <br>
     * returns chess notation for move
     */
    public void executeMove(Square square1, Square square2, byte chosenPieceToPromoteTo, boolean isEngineMove) {
//        System.out.println("Trying to execute " + square1 + square2);
        //region Handle board and move notation
        byte from = square1.index;
        byte to = square2.index;

        square1.hasNotChanged = false;
        square2.hasNotChanged = false;

        String moveNotation = null;

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
            handleCapture(square2.piece, true);
            takesString = "x";
        }

        //region Castling
        boolean kingMove = Math.abs(square1.piece) == WHITE_KING;
        int distance = Math.abs(square1.index % 8 - square2.index % 8);

        boolean isKingsideCastle = (kingMove && distance > 1) && (square2.index > square1.index);
        boolean isQueensideCastle = (kingMove && distance > 1) && (square2.index < square1.index);

        // If doing kingside castle
        if (isKingsideCastle) {

            // bring rookSquare other side
            Square rookSquare = squares[square2.index + 1];
            Square otherSide = squares[square2.index - 1];
            otherSide.piece = rookSquare.piece;
//            rookSquare.piece = EMPTY;
            rookSquare.removePiece();

            // Move king
            square2.piece = square1.piece;
//            square1.piece = EMPTY;
            square1.removePiece();

            // Update
            repaint();

            // Set kingside castling notation
            moveNotation = "O-O";
        }

        // If doing queenside castle
        if (isQueensideCastle) {

            // bring rook other side
            Square rookSquare = squares[square2.index - 2];
            Square otherSide = squares[square2.index + 1];
            otherSide.piece = rookSquare.piece;
            rookSquare.piece = EMPTY;

            // Move kind
            square2.piece = square1.piece;
            square1.piece = EMPTY;

            // Update
            repaint();

            // Set queenside castling notation
            moveNotation = "O-O-O";
        }

        //endregion

        //region Handle promotions
        boolean isWhitePromotion = squares[from].piece == WHITE_PAWN && to < 8;
        boolean isBlackPromotion = squares[from].piece == BLACK_PAWN && to >= 56;
        if (isWhitePromotion || isBlackPromotion) {
            // Set promotion variables
            promotionSquare = square2;
            isPromoting = false;

            // Update pieces on board
            squares[to].piece = chosenPieceToPromoteTo;
            squares[from].piece = EMPTY;

            // Reset chosen piece to promote to
            chosenPieceToPromoteTo = 0;
        }
        //endregion

        //region Handle en-passant

        if (square2.equals(getEnPassantTarget())) {
            Square behind = squares[square2.index + Integer.signum(square1.piece) * 8];
            behind.piece = EMPTY;
            takesString = "x";
            handleCapture((byte) (square1.piece * -1), true);
        }
        //endregion

        //region Update en passant targets if pawn moved two squares
        if (Math.abs(square1.piece) == WHITE_PAWN && Math.abs(from - to) == 16) {
            int enPassantSquareIndex = (from + to) / 2;
            setEnPassantTarget(squares[enPassantSquareIndex]);
        } else {
            resetEnPassantTarget();
        }
        //endregion

        // If a non-promotion and non-castling move, update new square
        if (!isBlackPromotion && !isWhitePromotion && !isKingsideCastle && !isQueensideCastle) {
            squares[to].piece = squares[from].piece;
        }

        // Remove piece which moved from its original square
        squares[from].piece = EMPTY;

        // Finally calculate move notation
        if (!takesString.isEmpty()) {
            toLocation = square2.getSquareName();
        }
        if (moveNotation == null) {
            moveNotation = movingPieceStr + takesString + toLocation;
        }
        if (isWhitePromotion || isBlackPromotion) {
            moveNotation += String.valueOf(square2.getPieceChar());
        }

        //endregion

        //region Handle game


        // Add it to move list and position to threefold repetition storage
//        Move m = new Move(this, square1, square2);
        Move m = new Move(this, square1.getSquareName() + square2.getSquareName());
        moves.add(m);
        threefoldStates.add(getBoardStateForThreefoldChecking());
        updateHalfMoves(m);

        // Print out move made
        if (currentEngine.isTurn()) System.out.print("HUMAN: ");
        else System.out.print("COMPUTER: ");
        System.out.println("made move " + m.getNotation());

        repaint();
//        runPerft(2, getCurrentFEN());
        //endregion
    }


    public void handleCapture(byte piece, boolean playSound) {
        // Handle edge cases of empty squares getting captured
        if (piece == EMPTY) System.out.println("Cannot capture an empty piece!!");

        // Play capture sound
        if (playSound) UI.playSound("sounds/Capture.wav");

        // Add captured piece to be displayed beside board
        ArrayList<Byte> capturedPiecesList = (piece > 0) ? capturedWhitePieces : capturedBlackPieces;
        capturedPiecesList.add(piece);
    }
    //endregion

    //region Piece Movement Logic // TODO change this to use Move()
    public LinkedList<Integer> getRawMoves(Square square) {
        LinkedList<Integer> accessibleSquareIndices = new LinkedList<>();
        byte piece = square.piece;
        byte location = square.index;

        int squaresLeft = location % 8;          // Squares to the left edge
        int squaresRight = 7 - squaresLeft;      // Squares to the right edge

        switch (piece) {
            case WHITE_PAWN -> {
                // Pawn has no moves if on the 8th rank (for white)
                if (location <= 7) {
                    return accessibleSquareIndices;
                }

                // If no pieces are in front of the pawn, it can move forward
                Square upperSquare = squares[location - 8];
                if (upperSquare.isEmpty()) {
                    accessibleSquareIndices.add(location - 8);

                    // Pawn can also move two squares if on the 2nd rank and no piece on the 4th rank
                    if (location / 8 == 6 && squares[location - 16].isEmpty()) {
                        accessibleSquareIndices.add(location - 16);
                    }
                }

                // If not on the 1st file
                if (squaresLeft > 0) {
                    Square upperLeftSquare = squares[location - 9];
                    Square leftSquare = squares[location - 1];

                    // And if there's a black piece to the upper left:
                    if (upperLeftSquare.piece < 0) {

                        // The pawn can capture diagonally to the upper left
                        accessibleSquareIndices.add(location - 9);
                    }

                    // If there's a black pawn to the left
                    if (leftSquare.piece == BLACK_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (upperLeftSquare.equals(getEnPassantTarget()))  {

                            // The pawn can capture diagonally to the upper left
                            accessibleSquareIndices.add(location - 9);
                        }
                    }
                }

                // If not on the 8th file
                if (squaresRight > 0) {
                    Square upperRightSquare = squares[location - 7];
                    Square rightSquare = squares[location + 1];

                    // And if there's a black piece to the upper right:
                    if (upperRightSquare.piece < 0) {

                        // The pawn can capture diagonally to the upper right
                        accessibleSquareIndices.add(location - 7);
                    }

                    // If there's a black pawn to the right
                    if (rightSquare.piece == BLACK_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (upperRightSquare.equals(getEnPassantTarget()))  {

                            // The pawn can capture diagonally to the upper right
                            accessibleSquareIndices.add(location - 7);
                        }
                    }
                }
            }
            case BLACK_PAWN -> {
                // Pawn has no moves if on the 1st rank (for black)
                if (location >= 56) {
                    return accessibleSquareIndices;
                }

                // If no pieces are below the pawn, it can move down
                Square lowerSquare = squares[location + 8];
                if (lowerSquare.isEmpty()) {
                    accessibleSquareIndices.add(location + 8);

                    // Pawn can also move two squares if on the 7th rank and no piece on the 5th rank
                    if (location / 8 == 1 && squares[location + 16].isEmpty()) {
                        accessibleSquareIndices.add(location + 16);
                    }
                }

                // If not on the 1st file
                if (squaresLeft > 0) {
                    Square lowerLeftSquare = squares[location + 7];
                    Square leftSquare = squares[location - 1];

                    // And if there's a white piece to the lower left:
                    if (lowerLeftSquare.piece > 0) {

                        // The pawn can capture diagonally to the lower left
                        accessibleSquareIndices.add(location + 7);
                    }

                    // If there's a white pawn to the left
                    if (leftSquare.piece == WHITE_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (lowerLeftSquare.equals(getEnPassantTarget()))  {

                            // The pawn can capture diagonally to the lower left
                            accessibleSquareIndices.add(location + 7);
                        }
                    }
                }

                // If not on the 8th file
                if (squaresRight > 0) {
                    Square lowerRightSquare = squares[location + 9];
                    Square rightSquare = squares[location + 1];

                    // And if there's a white piece to the lower right:
                    if (lowerRightSquare.piece > 0) {

                        // The pawn can capture diagonally to the lower right
                        accessibleSquareIndices.add(location + 9);
                    }

                    // If there's a white pawn to the right
                    if (rightSquare.piece == WHITE_PAWN) {

                        // And if the space behind it is an En Passant Target
                        if (lowerRightSquare.equals(getEnPassantTarget()))  {
                            // The pawn can capture diagonally to the lower right
                            accessibleSquareIndices.add(location + 9);
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
                            if ((squares[target].piece * piece <= 0)) {

                                // Add it to moves
                                accessibleSquareIndices.add(target);
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
                        if (squares[target].isEmpty()) {
                            accessibleSquareIndices.add(target);
                        }
                        // If the square has an enemy piece, add it and stop
                        else if (squares[target].piece * piece < 0) {
                            accessibleSquareIndices.add(target);
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
                            if ((squares[target].piece * piece <= 0)) {

                                // Add to moves
                                accessibleSquareIndices.add(target);
                            }
                        }
                    }
                }

                // If the king hasn't moved
                if (square.hasNotChanged && squaresLeft == 4) {

                    // Kingside castling:

                    // If there is space
                    Square ks1 = squares[location + 1];
                    Square ks2 = squares[location + 2];
                    if (ks1.isEmpty() && ks2.isEmpty()) {

                        // And if rook has not moved
                        if (squares[location + 3].hasNotChanged) {

                            // Add to moves
                            accessibleSquareIndices.add(location + 2);
                        }
                    }

                    // Queenside castling:

                    // If there is space
                    Square qs1 = squares[location - 1];
                    Square qs2 = squares[location - 2];
                    Square qs3 = squares[location - 3];
                    if (qs1.isEmpty() && qs2.isEmpty() && qs3.isEmpty()) {

                        // And rook has not moved
                        if (squares[location - 4].hasNotChanged)  {

                            // Add to moves
                            accessibleSquareIndices.add(location - 2);

                        }
                    }
                }
            }
        }

        return accessibleSquareIndices;
    }

    public ArrayList<Integer> accessibleSquaresOf(Square square) {
        ArrayList<Integer> validMoves = new ArrayList<>();
        byte piece = square.piece;
        byte from = square.index;

        LinkedList<Integer> rawMoves = getRawMoves(square);

        rawMovesLoop:
        for (int to : rawMoves) {
            // Create simulated board for this move
            Board simulatedBoard = new Board(this, from, (byte) to);
            Square simulatedFrom = simulatedBoard.squares[from];
            Square simulatedTo = simulatedBoard.squares[to];

            // First, throw out moves that leave king in check afterwards
            simulateMove(simulatedBoard, new Move(simulatedBoard, simulatedFrom, simulatedTo));
            boolean isInCheck = isKingInCheck(simulatedBoard, isWhitesMove);

            if (isInCheck) continue;

            // Additional castling checks:
            int diff = to - from;
            boolean isCastling = Math.abs(diff) == 2 && Math.abs(piece) == WHITE_KING;

            if (isCastling) {
                // 1. Check initial king position
                if (isKingInCheck(this, isWhitesMove)) {
                    continue;
                }

                //region 2. Check intermediate squares
                int direction = diff > 0 ? 1 : -1;
                int[] intermediates = direction == 1 ?
                        new int[]{from + 1} :           // Kingside: 1 square
                        new int[]{from - 1, from - 2};  // Queenside: 2 squares

                for (int i : intermediates) {
                    Board tempBoard = new Board(this, from, (byte) to);
                    simulateMove(tempBoard, new Move(tempBoard, Board.squareName[from] + Board.squareName[i]));

                    if (isKingInCheck(tempBoard, isWhitesMove)) {
                        continue rawMovesLoop;
                    }
                }
                //endregion
            }

            validMoves.add(to);
        }
        return validMoves;
    }

    // It's a problem with board not getting deep copied when this is called
    public static void simulateMove(Board on, Move move) {
        byte piece = move.from.piece;
        move.from.hasNotChanged = false;
        move.to.hasNotChanged = false;

        // Handle promotion piece update
        if (move.isWhitePromotion() || move.isBlackPromotion()) {
            // TODO make this cleaner
            move.to.piece = (move.isWhitePromotion()) ? (byte) Math.abs(on.chosenPieceToPromoteTo) : (byte) (-1 * Math.abs(on.chosenPieceToPromoteTo));
        } else {
            // Handle all other piece updates
            move.to.piece = move.from.piece;
        }
        move.from.removePiece();

        //region Also move rook when castling
        boolean isCastling = Math.abs(piece) == WHITE_KING && Math.abs(move.from.index - move.to.index) == 2;
        if (isCastling) {
            int direction = (move.to.index > move.from.index) ? 1 : -1; // Kingside: +2, Queenside: -2
            int rookFromIndex, rookToIndex;

            if (direction == 1) { // Kingside
                rookFromIndex = move.to.index + 1;
                rookToIndex = move.to.index - 1;
            } else { // Queenside
                rookFromIndex = move.to.index - 2;
                rookToIndex = move.to.index + 1;
            }

            // Move rook
            Square rookFrom = on.squares[rookFromIndex];
            Square rookTo = on.squares[rookToIndex];
            rookTo.piece = rookFrom.piece;
            rookFrom.removePiece();
        }
        //endregion

        //region En Passant logic

        // Update squares after en passant performed
        boolean enPassantHappened = (Math.abs(piece) == WHITE_PAWN && move.to.equals(on.getEnPassantTarget()));
        if (enPassantHappened) {

            // Remove captured pawn
            int direction = Integer.signum(piece); // 1 for white, -1 for black
            int capturedIndex = move.to.index + (direction * 8);
            Square capturedSquare = on.squares[capturedIndex];
            capturedSquare.removePiece();
        }

        // Update en passant targets
        boolean pawnJustMoved = Math.abs(move.to.piece) == WHITE_PAWN;
        boolean moved16Indices = Math.abs(move.from.index - move.to.index) == 16;

        if (pawnJustMoved && moved16Indices) {
            // Update en passant target
            on.enPassantTarget = on.squares[(move.from.index + move.to.index) / 2];
        } else {
            on.enPassantTarget = null;
        }
        //endregion

        on.isWhitesMove = !on.isWhitesMove;
    }

    //endregion

    //region Game State Management
    /**
     * Returns 0 if stalemated, 1 if checkmated, -1 if pieces can move
     */
    public int checkGameOutcome() {
        // Check draw from threefold
        if (isDrawFromThreefold() && gameOngoing) {
            UI.summonEndGamePanel("Draw", "By threefold");

            playSound("sounds/beep.wav");
            gameOngoing = false;
            return 0;
        }

        // Check draw from insufficient material
        if (isInsufficientMaterial() && gameOngoing) {
            UI.summonEndGamePanel("Draw", "By insufficient material");

            playSound("sounds/beep.wav");
            gameOngoing = false;
            return 0;
        }

        // Check draw by 50 move rule
        if (halfMoveCounter >= 100 && gameOngoing) {
            System.out.println("DRAW...");
            UI.summonEndGamePanel("Stalemate", "By 50-move rule");

            playSound("sounds/beep.wav");
            gameOngoing = false;
            return 0;
        }

        // Check the game has not finished
        if (piecesCanMove()) {
            return -1;
        }

        // Check checkmate
        if (isKingInCheck(board, isWhitesMove) && gameOngoing) {
            String winner = isWhitesMove ? "Black" : "White";
            UI.summonEndGamePanel(winner + " Wins", "by checkmate");

            playSound("sounds/beep.wav");
            gameOngoing = false;

            return 1;
        }

        // Handle stalemate
        if (gameOngoing) {
            System.out.println("DRAW...");
            UI.summonEndGamePanel("Draw", "By stalemate");

            playSound("sounds/beep.wav");
            gameOngoing = false;
        }
        return 0;
    }
    //endregion

    //region Move Validation
    public static boolean isKingInCheck(Board board, boolean isWhite) {
        byte king = (isWhite ? WHITE_KING : BLACK_KING);
        int kingPos = -1;

        // Find king position
        for (int i = 0; i < 64; i++) {
            if (board.squares[i].piece == king) {
                kingPos = i;
                break;
            }
        }

        // Check all enemy pieces
        for (int i = 0; i < 64; i++) {
            Square s = board.squares[i];
            if (s.piece == EMPTY) continue;
            if ((s.piece > 0) == isWhite) continue;

            LinkedList<Integer> moves = board.getRawMoves(s);
            if (moves.contains(kingPos)) {
                return true;
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
            if (m.from == kingSquare) {
                return false;
            }
        }

        // If rook has not moved
        if (squares[location + 3].hasNotChanged) {

            // And it is asking about kings
            if (isKingside) {

                return true;
            }
        }

        // If rook has not moved
        if (squares[location - 4].hasNotChanged)  {

            // And if it is asking about queenside
            if (!isKingside) {

                return true;
            }
        }

        // Otherwise:
        return false;
    }

    public boolean piecesCanMove() {
        for (Square s : squares) {
            if (s.piece == EMPTY) {
                continue;
            }
            boolean isWhite = Math.signum(s.piece) > 0;
            boolean isPiecesTurnToMove =  isWhite && isWhitesMove || !isWhite && !isWhitesMove;

            // If it is the correct color
            if (isPiecesTurnToMove) {

                // And if it can move somewhere
                if (!accessibleSquaresOf(s).isEmpty()) {

                    return true;
                }
            }
        }
        return false; // if there does not exist a piece of the right color which can move
    }
    //endregion

    //region Engine
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        for (Square from : squares) {

            // Skip this square if empty or has piece of wrong color
            if (from.piece <= 0 == isWhitesMove) {
                continue;
            }

            // Loop over every square we can move to, adding a move to there to the possibilities
            ArrayList<Integer> accessibleDestinations = accessibleSquaresOf(from);
            for (int to : accessibleDestinations) {

                // If move is a promotion, include all 4 variants
                boolean isWhitePromotion = from.piece == WHITE_PAWN && to < 8;
                boolean isBlackPromotion = from.piece == BLACK_PAWN && to >= 56;
                if (isWhitePromotion || isBlackPromotion) {
                    char[] promotionOptions = isWhitesMove ? new char[]{'Q', 'N', 'R', 'B'} : new char[]{'q', 'n', 'r', 'b'};
                    possibleMoves.add(new Move(board, from.getSquareName() + squares[to].getSquareName() + promotionOptions[0]));
                    possibleMoves.add(new Move(board, from.getSquareName() + squares[to].getSquareName() + promotionOptions[1]));
                    possibleMoves.add(new Move(board, from.getSquareName() + squares[to].getSquareName() + promotionOptions[2]));
                    possibleMoves.add(new Move(board, from.getSquareName() + squares[to].getSquareName() + promotionOptions[3]));
                }

                else {
                    possibleMoves.add(new Move(board, from.getSquareName() + squares[to].getSquareName()));
                }
            }

        }
        return possibleMoves;
    }

    // In Board.java
    public Board getBoardIfMoveHappened(Move m) {
        Board possible = new Board(this); // TODO figure out why new constructor doesn't work

        // Get the from/to squares from the COPIED BOARD (not original)
        Square newFrom = possible.squares[m.from.index];
        Square newTo = possible.squares[m.to.index];
//        Move newMove = new Move(possible, newFrom.getSquareName() + newTo.getSquareName());
        Move newMove = new Move(possible, newFrom, newTo);

        // Handle promotions
        if (m.notation.length() == 5) {
            possible.chosenPieceToPromoteTo = m.getPromotionChoice();
        }

        simulateMove(possible, newMove);
        return possible;
    }
    //endregion

}
