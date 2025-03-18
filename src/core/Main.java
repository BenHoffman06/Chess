package core;

import java.util.*;

import static core.UI.*;

public class Main {

    public static Board board = new Board();

    public static boolean gameOngoing = true;
    public static Engine currentEngine = null;

    // Move and position tracking
    public static ArrayList<Move> moves = new ArrayList<>(); // Stores all moves made in the game
    public static ArrayList<String> threefoldStates = new ArrayList<>(); // Stores all moves made in the game

    // Accessible moves for a specific piece
    public static ArrayList<Byte> accessibleMoves = new ArrayList<>(); // Stores accessible squares (their index) for a selected piece


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

    public static void rematch() {
        UI.endGamePanelShouldBeShown = false;
        gameOngoing = true;
        board.isWhitesMove = true;
        accessibleMoves.clear();
        threefoldStates.clear();
        board.halfMoveCounter = 0;
        board.reset();

        repaint();
    }

    public static void main(String[] args) {
        UI.mainPanel = UI.handleGUI();

        // Default piece setup
        board.setFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

        // Endgame with black winning
//        setBoardFromFEN("3k4/8/8/8/8/2q3q1/8/3K4 b - - 0 1");

        // Endgame with white winning
//        setBoardFromFEN("3K4/8/8/8/8/2Q3Q1/8/3k4 w - - 0 1");

//        setBoardFromFEN("8/4PPP1/2k5/8/2K5/8/4pp1p/8 w - - 0 1");
//        board.setFromFEN("rnb1kb1r/pppppppp/8/5n2/8/6Q1/PPPPPPPP/RNB1K2R w KQkq - 0 1");

        // Test en passant after double pawn move
//        board.setFromFEN("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 1");

// Test castling through check
//        board.setFromFEN("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
//
//// Test insufficient material
//        board.setFromFEN("8/8/8/8/8/8/8/KB5k w - - 0 1");

        currentEngine = new MyEngine();

        currentEngine.isPlaying = true;
        currentEngine.isWhite = false;
        currentEngine.tryPlay(12);
//        setBoardFromFEN("rnbqkbnr/ppppp1pp/8/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 1");

    }

}
