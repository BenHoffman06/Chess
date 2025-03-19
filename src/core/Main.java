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

        if (currentEngine.isWhite) {
            currentEngine.tryPlay(12);
        }

        repaint();
    }

    public static void main(String[] args) {

        UI.mainPanel = UI.handleGUI();

        // Default piece setup
//        board.reset();

        board.setFromFEN("rnbqkbnr/p2ppppp/8/8/8/8/PPpPPPPP/RNBQKBNR w KQkq - 0 1");
        currentEngine = new MyEngine();
        currentEngine.isPlaying = true;
        currentEngine.isWhite = true;
        currentEngine.tryPlay(12);
//        setBoardFromFEN("rnbqkbnr/ppppp1pp/8/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 1");

    }

}
