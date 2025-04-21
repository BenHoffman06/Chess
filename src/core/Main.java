package core;

import java.util.*;

import static core.UI.*;

public class Main {

    public static boolean debug = true;

    public static Board board = new Board();


    public static boolean gameOngoing = true;
//    public static Engine currentEngine = null;
    public static MyEngine currentEngine = null;

    // Move and position tracking
    public static ArrayList<Move> moves = new ArrayList<>(); // Stores all moves made in the game
    public static ArrayList<String> threefoldStates = new ArrayList<>(); // Stores all moves made in the game

    // Accessible moves for a specific piece
    public static ArrayList<Integer> accessibleMoves = new ArrayList<>(); // Stores accessible squares (their index) for a selected piece


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
            currentEngine.tryPlay(3);
        }

        repaint();
    }

    public static void main(String[] args) {
        // Set up UI
        UI.mainPanel = UI.handleGUI();

        // FEN
        String MIDGAME_TESTING_FEN = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
        String EN_PASSANT_TESTING_FEN = "k1K5/1p1p1p2/8/2P1P1P1/1pPp1p2/8/4P1P1/8 b - c3 0 1";
        String EN_PASSANT_TESTING_FEN_2 = "k1K5/1p3p2/8/2PpP1P1/1pPp1p2/8/4P1P1/8 w - d6 0 1";
        String EN_PASSANT_TESTING_FEN_3 = "k1K5/1p1p1p2/8/2P3P1/1pPp1p2/8/4P1P1/8 b - c3 0 1";
        String PROMOTION_TESTING_FEN = "8/KPPP1PP1/8/k7/8/8/1ppp1pp1/8 w - - 0 1";
        String PROMOTION_TESTING_FEN_2 = "K7/6P1/8/k7/8/8/1p6/8 b - - 0 1";
        String KNIGHT_CHECKMATE_TESTING_FEN = "1nnn4/2nnn3/2knnn2/8/nnnnnnnn/n3nnnn/8/2K5 b - - 0 1";
        String ROOK_BISHOP_CHECKMATE_TESTING_FEN = "bbbbbb1b/6b1/bb1bbbbb/4bbbb/bkbbbb1b/rpppprrr/8/2K5 b - - 0 1";
        String KING_AND_PAWN_MIRRORED_ENDGAME_TESTING_FEN = "1k6/2p5/8/8/8/8/2PK4/8 b - - 0 1";
        String DEVELOPMENT_TESTING_FEN = "rnbqkbnr/pppppppp/8/8/2BPPB2/2N2N2/1PP2PPP/R2QR1K1 w Qkq - 0 1";

        String selectedFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        board.setFromFEN(selectedFEN);

        // Set up engine
        currentEngine = new MyEngine();

        // Set up engine parameters
        ArrayList<String> engineArgs = new ArrayList<>();
        engineArgs.add("print");

        // Call and time engine's eval analysis of current position
        long start = System.currentTimeMillis();
//        currentEngine.calcEval(5, engineArgs);
//        currentEngine.makeBestMove(2);
        System.out.println("\nTime taken: " + (System.currentTimeMillis() - start) + "ms");

        // Set up engine to play as black
        currentEngine.isPlaying = true;
        currentEngine.isWhite = false;

    }

    /**
     * Using testing discussed here: <a href="https://www.chessprogramming.org/Perft_Results#:~:text=81076-,Position%205,-This%20position%20was">...</a>
     */
    public static void runPerft(int depth, String selectedFEN) {
        // Init
        currentEngine = new MyEngine();
        board.setFromFEN(selectedFEN);
        System.out.println(board.getCurrentFEN());
        repaint();

        // Get and store stockfish perft
        HashMap<String, Long> stockfishPerftReturn = StockfishPerftParser.get("C:/Users/Benem/Downloads/stockfish/stockfish-windows-x86-64-avx2.exe", selectedFEN, depth);

        // Start time logging
        long time = 0;
        long start = System.currentTimeMillis();
        System.out.println("Starting perft.");
        System.out.print("Depth: " + depth);

        currentEngine.perftDivide(board, depth, stockfishPerftReturn);

        // Finish time logging
        long end = System.currentTimeMillis();
        time = (end - start);
        System.out.println("Time taken: " + time + "ms");

    }

}
