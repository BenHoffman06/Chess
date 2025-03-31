package core;

import java.util.ArrayList;
import java.util.HashMap;

public class MyEngine extends Engine {
    private int leafNodesProcessed = 0;
    public int calledDepth = 0;
    // NOTE Performance (alongside possible board states) tends to get 50x slower for every time performance goes up by 1

    public String getBestMove(int depth) {
        // Choose first move that comes to mind (just like me frfr)
        for (Square s : board.squares) {
            ArrayList<Byte> accessibleSquares = board.accessibleSquaresOf(s);
            if (s.piece > 0 == board.isWhitesMove && !accessibleSquares.isEmpty()) {
                String from = s.getSquareName();
                String to = board.squares[accessibleSquares.getFirst()].getSquareName();
//                System.out.println("Returning best move: " + from + to);
                return from + to;
            }
        }
        return "ERROR";
    }


    @Override
    public String[] calculateBestMoveWithEvaluation(String fen, int depth) {
        String[] response = new String[2];
        response[0] = getBestMove(12);
        response[1] = String.valueOf(calcEval(1));
        return response;
    }

    //    public double calcEval(int depth) {
////        System.out.println("Returning eval: " + board.getMaterialDiff());
//        return board.getMaterialDiff();
//    }

    public double calcEval(int depth) {
        calledDepth = depth;
        double eval = calcEval(depth, Integer.MIN_VALUE, Integer.MAX_VALUE, board, isWhite);
        System.out.print(", Positions Processed: " + leafNodesProcessed);
        leafNodesProcessed = 0;
        return eval;
    }

    /**
     * Uses minimax algorithm with alpha-beta pruning for efficiency in traversing position tree
     */
    public double calcEval(int depth, double alpha, double beta, Board current, boolean isMaximiser) {
        if (depth == 0) {
            return calcEvalBaseCase();
        }

        double eval = isMaximiser ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move move : current.getPossibleMoves()) {

//            // Nodes are pruned when beta <= alpha // TODO change this to <= when evaluation becomes more sophisticated
//            if (beta < alpha) {
//                break;
//            }

            // Get child eval
            Board possibleBoard = current.getBoardIfMoveHappened(move);
            double childEval =  calcEval(depth - 1, alpha, beta, possibleBoard, !isMaximiser);

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

    public double calcEvalBaseCase() {
        leafNodesProcessed++;
        return board.getMaterialDiff();
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
        long total = 0;
        ArrayList<Move> moves = board.getPossibleMoves();

        System.out.println("Perft Divide (depth " + depth + "):");
        for (Move move : moves) {
            Board newBoard = board.getBoardIfMoveHappened(move);
            long count = perft(newBoard, depth - 1);

            total += count;

            // Stockfish count for comparison
            long stockfishCount = 0;
            if (stockfishPerft.containsKey(move.square1.getSquareName() + move.square2.getSquareName())) {
                stockfishCount = stockfishPerft.get(move.square1.getSquareName() + move.square2.getSquareName());
            }
            else {
                System.out.println("Stockfish Keys: " + stockfishPerft.keySet());
                System.out.println("Attempted key retrieval: " + move.square1.getSquareName() + move.square2.getSquareName());
            }
            double average = (double) (stockfishCount + count) / 2;
            double percent = (stockfishCount - count) / average;
            System.out.println(move.notation + ": " + count +"\t\t\t\tDifference from Stockfish: " + (stockfishCount - count) + "\t(" + String.format("%.2f", percent) + "%)");
        }
        System.out.println("Total: " + total + "\n");
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
}