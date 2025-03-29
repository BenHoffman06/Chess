package core;

import java.util.ArrayList;

public class MyEngine extends Engine {
    private int leafNodesProcessed = 0;
    public int calledDepth = 0;

    public String getBestMove(int depth) {
        // Choose first move that comes to mind (just like me frfr)
        for (Square s : board.squares) {
            ArrayList<Byte> accessibleSquares = Board.accessibleSquaresOf(s, board, true);
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

            // Nodes are pruned when beta <= alpha // TODO change this to <= when evaluation becomes more sophisticated
            if (beta < alpha) {
                break;
            }

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
}