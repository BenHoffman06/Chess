package core;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Engine {

    boolean isPlaying = false;
    boolean isWhite = false;
    Board board = Main.board;
    HashMap<String, String[]> storedEvals = new HashMap<>(); // Maps FEN to evaluations including eval bar and best move in string form
    boolean isAwaitingResponse = true;

    void tryPlay(int depth) {
        if (isTurn()) {
            makeBestMove(depth);
        }
    }

    boolean isTurn() {
        return isPlaying && isWhite == board.isWhitesMove;
    }

    void makeBestMove(int depth) {
        CompletableFuture<String> futureMove = getBestMoveInNewThread(depth);
        long startTime = System.currentTimeMillis();
        AtomicBoolean completed = new AtomicBoolean(false);

        // Start a timer thread
        Thread timerThread = new Thread(() -> {
            try {
                while (!completed.get()) {
                    Thread.sleep(5000); // Wait for 5 seconds
                    if (!completed.get()) {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        System.out.println("Still waiting for best move... " + elapsedTime + " milliseconds elapsed");
                    }
                }
            } catch (InterruptedException e) {
                // Thread interrupted, exit silently
            }
        });
        timerThread.start();

        // Use callback to handle the result when it's ready
        futureMove.thenAccept(bestMove -> {
            completed.set(true);
            long totalTime = System.currentTimeMillis() - startTime;
            if (bestMove != null) {
                System.out.println("Making best move 😈 (" + bestMove + ") after " + totalTime + " milliseconds");
                Move best = new Move(bestMove);
                board.attemptMove(best.square1, best.square2);
            } else {
                System.out.println("Failed to get best move after " + totalTime + " milliseconds");
            }
        });
    }

    // TODO simplify
    public CompletableFuture<String> getBestMoveInNewThread(int depth) {
        return CompletableFuture.supplyAsync(() -> {
            String fen = board.getCurrentFEN();
            return getBestMoveWithEvaluation(fen, depth)[0];
        });
    }

    public double getEval(int depth) {
        String fen = board.getCurrentFEN();
        String[] moveAndEval = getBestMoveWithEvaluation(fen, depth);

        return moveAndEval.length > 1 ? Double.parseDouble(moveAndEval[1]) : 0.0;
    }


    /**
     * @param fen   Chess position in FEN format
     * @param depth Search depth (1-15)
     * @return String array: [best move, evaluation]<br>
     *         - Best move: Coordinate notation (e.g. "e2e4")<br>
     *         - Evaluation: Centipawn score (e.g. "0.45") or mate count (e.g. "+3" for mate in 3)
     * @throws IOException If API call fails
     * @Example Basic usage:
     * <pre>
     * String[] result = getBestMoveWithEvaluation("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 10);
     * System.out.println("Best move: " + result[0]); // e.g. "e2e4"
     * System.out.println("Evaluation: " + result[1]); // e.g. "0.45" or "-3" (mate in 3 for black)
     * </pre>
     */
    public abstract String[] calculateBestMoveWithEvaluation(String fen, int depth) throws IOException;

    /**
     * Tries to retrieve stored eval, if it doesn't exist it returns calculateBestMoveWithEvaluation()
     */
    public String[] getBestMoveWithEvaluation(String fen, int depth) {
        System.out.println("Checking if storedEvals contains " + fen);
        System.out.println("StoredEvals keys: " + storedEvals.keySet());
        if (storedEvals.containsKey(fen)) {
            return storedEvals.get(fen);
        }
        String[] s = new String[0];
        try {
            isAwaitingResponse = true;
            s = calculateBestMoveWithEvaluation(fen, depth);
            isAwaitingResponse = false;
            storedEvals.put(fen, s);
            board.currentEval = Double.parseDouble(s[1]);
        } catch (Exception e) {
            System.out.println("Couldn't calculate best move with eval..." + e);
        }
        return s;
    }

    public boolean isReadyToRender() {
        return !isAwaitingResponse;
    }

}
