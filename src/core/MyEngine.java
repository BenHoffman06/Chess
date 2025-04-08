package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static core.Board.isKingInCheck;
import static core.Board.simulateMove;

public class MyEngine extends Engine {


    void setIsWhite(boolean isWhite) {
        this.isWhite = isWhite;
    }

    boolean getIsWhite() {
        return isWhite;
    }



    private int leafNodesProcessed = 0;
    public int calledDepth = 0;
    public Move bestMove = null;
    // NOTE Performance (alongside possible board states) tends to get 50x slower for every time performance goes up by 1

    public String getBestMove(int depth) {
        return bestMove.getNotation();
    }

    /**
     * Uses minimax algorithm with alpha-beta pruning for efficiency in traversing position tree
     */
    @Override
    public String[] calculateBestMoveWithEvaluation(String fen, int depth) {
        String[] response = new String[2];
        calledDepth = depth;
        bestMove = null;
        leafNodesProcessed = 0;

        // If depth 0, skip multithreading and recursive calls
        // Just return instant depth 0 evaluation from calcEvalBaseCase()
        if (depth == 0) {
            double eval = calcEvalBaseCase(board);
            response[1] = String.valueOf(eval);
            response[0] = String.valueOf(board.getPossibleMoves().getFirst()); // No move to return at depth 0
            return response;
        }

        ArrayList<Move> possibleMoves = board.getPossibleMoves();
        if (possibleMoves.isEmpty()) {
            double eval = calcEvalBaseCase(board);
            response[1] = String.valueOf(eval);
            response[0] = ""; // No move to return if no moves are possible
            return response;
        }

        Move currentBestMove = possibleMoves.getFirst();
        double bestEval = isWhite ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        double alpha = Integer.MIN_VALUE;
        double beta = Integer.MAX_VALUE;

        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Double>> futures = new ArrayList<>();

        for (Move move : possibleMoves) {
            final Move currentMove = move;
            double finalAlpha = alpha;
            double finalBeta = beta;
            futures.add(executor.submit(() -> {
                Board childBoard = board.getBoardIfMoveHappened(currentMove);
                return calcEval(depth - 1, finalAlpha, finalBeta, childBoard, !isWhite, childBoard);
            }));
        }

        List<Double> childEvals = new ArrayList<>();
        for (Future<Double> future : futures) {
            try {
                childEvals.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                childEvals.add(isWhite ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            }
        }
        executor.shutdown();

        for (int i = 0; i < possibleMoves.size(); i++) {
            double childEval = childEvals.get(i);
            Move move = possibleMoves.get(i);

            if (isWhite) {
                if (childEval > bestEval) {
                    bestEval = childEval;
                    currentBestMove = move;
                    alpha = Math.max(alpha, bestEval);
                }
            } else {
                if (childEval < bestEval) {
                    bestEval = childEval;
                    currentBestMove = move;
                    beta = Math.min(beta, bestEval);
                }
            }

//                if (beta <= alpha) break; // Alpha-beta pruning
        }

        bestMove = currentBestMove;

        // Print debug data
        if (Main.debug && bestMove != null) {
            System.out.print("Move: " + bestMove.getNotation() + " Eval: " + String.format("%.2f", bestEval)  + ", best eval seen so far: " + String.format("%.2f", bestEval) );
            System.out.println("\tPositions Processed: " + leafNodesProcessed);
        }

        response[1] = String.valueOf(bestEval);
        if (bestMove != null) {
            response[0] = bestMove.getNotation();
        } else {
            response[0] = ""; // Handle case where no best move was found
        }
        return response;
    }

    /**
     * Uses minimax algorithm with alpha-beta pruning for efficiency in traversing position tree
     */
    public double calcEval(int depth, double alpha, double beta, Board current, boolean isMaximiser, Board board) {
        if (depth == 0) {
            return calcEvalBaseCase(board);
        }

        double eval = isMaximiser ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move move : current.getPossibleMoves()) {

//            // Nodes are pruned when beta <= alpha // TODO change this to <= when evaluation becomes more sophisticated
//            if (beta < alpha) {
//                break;
//            }

            // Get child eval
            Board possibleBoard = current.getBoardIfMoveHappened(move);
            double childEval =  calcEval(depth - 1, alpha, beta, possibleBoard, !isMaximiser, board);

            // Update alpha
            alpha = Math.max(alpha, childEval);

            // Update beta
            beta = Math.min(beta, childEval);

            // Update eval
            eval = isMaximiser ? alpha : beta;

//            System.out.println(move.notation + "\tAlpha: " + alpha + ", Beta: " + beta);
        }

        return eval;
    }

    public double calcEvalBaseCase(Board board) {
        leafNodesProcessed++;
        double materialEval = board.getMaterialDiff();
        double positionalEval = 0;
        for (Square s : board.squares) {
            if (s.piece > 0) positionalEval += board.accessibleSquaresOf(s).size();
            else if (s.piece < 0) positionalEval -= board.accessibleSquaresOf(s).size();
//            System.out.println(materialEval + positionalEval / 50);
        }
        return materialEval + positionalEval / 50;
    }

    private long nodes;

    // Full perft test
    public long perft(Board board, int depth) {
        nodes = 0;
        perftHelper(board, depth);
        return nodes;
    }

    // Perft with divide (prints move counts)
    public void perftDivide(Board board, int depth, HashMap<String, Long> stockfishPerft) {
        String RESET = "\u001B[0m";
        String RED = "\u001B[31m";
        String GREEN = "\u001B[32m";

        long total = 0;
        long stockfishTotal = 0;
        String currentWorstMove = board.getPossibleMoves().getFirst().notation;
        int currentWorstMoveDifferenceCount = 0;
        System.out.println("Perft Divide (depth " + depth + "):");

        // For all possible moves:
        for (Move move : board.getPossibleMoves()) {

            // Perform perft
            Board newBoard = board.getBoardIfMoveHappened(move);
            long count = perft(newBoard, depth - 1);

            // Stockfish count for comparison
            long stockfishCount = 0;

            // Try to retrieve stockfish perft
            String moveNotation = move.getNotation();
            if (stockfishPerft.containsKey(moveNotation)) {
                stockfishCount = stockfishPerft.get(moveNotation);
            }
            else {
                throw new RuntimeException("Program bug is allowing an illegal move according to Stockfish: " + moveNotation + "\nFEN: " + board.getCurrentFEN());
            }

            // Add individual counts to running totals
            total += count;
            stockfishTotal += stockfishCount;

            // Print out difference
            double average = (double) (stockfishCount + count) / 2;
            double percent = (stockfishCount - count) / average;
            System.out.print(move.notation + ": " + count + "\t\t\t\tDifference from Stockfish: ");
            if (count != stockfishCount) System.out.println(RED + (stockfishCount - count) + "\t(" + String.format("%.2f", percent) + "%)" + RESET);
            else System.out.println(GREEN + "0\t(" + String.format("%.2f", percent) + "%)" + RESET);

            // Keep track of move which is most different from stockfish
            if (currentWorstMoveDifferenceCount < Math.abs((stockfishCount - count))) {
                currentWorstMoveDifferenceCount = (int) Math.abs(stockfishCount - count);
                currentWorstMove = move.notation;
            }
        }
        System.out.println("\nTotal: " + total + "\t\t\tDifference from Stockfish: " + (stockfishTotal - total) + "\t(" + String.format("%.2f", (double) (stockfishTotal - total) / ((stockfishTotal + total) / 2)) + "%)\n");
        System.out.println("Move most different from stockfish: " + currentWorstMove +"\n\n");
    }

    private void perftHelper(Board board, int depth) {
        if (depth == 0) {
            nodes++;
            return;
        }

        ArrayList<Move> moves = board.getPossibleMoves();
        for (Move move : moves) {
            Board newBoard = board.getBoardIfMoveHappened(move);
            perftHelper(newBoard, depth - 1);
        }
    }

    private void printAllMovesFrom(Board b) {
        System.out.println("\n\n");
        System.out.println(b.getCurrentFEN());
        for (Move m : b.getPossibleMoves()) {
            System.out.println(m.getNotation());
            System.out.println(b.accessibleSquaresOf(b.squares[14]));
        }

        Move m = b.getPossibleMoves().getFirst();
        Board simulatedBoard = new Board(b, m.from.index, m.to.index);
        Square simulatedFrom = simulatedBoard.squares[15];
        Square simulatedTo = simulatedBoard.squares[7];

        // First, throw out moves that leave king in check afterwards
        simulateMove(simulatedBoard, new Move(simulatedBoard, simulatedFrom, simulatedTo));
        boolean isInCheck = isKingInCheck(simulatedBoard, b.isWhitesMove);
        System.out.println(isInCheck);

        System.out.println("\n\n");
    }
}