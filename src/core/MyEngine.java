package core;

import java.util.ArrayList;

public class MyEngine extends Engine {

    public double calcEval(int depth) {
//        System.out.println("Returning eval: " + board.getMaterialDiff());
        return board.getMaterialDiff();
    }

    public String getBestMove(int depth) {
        // Choose first move that comes to mind (just like me frfr)
        for (Square s : board.squares) {
            ArrayList<Byte> accessibleSquares = Board.accessibleSquaresOf(s, board, true);
            if (s.piece > 0 == board.isWhitesMove && !accessibleSquares.isEmpty()) {
                String from = s.getSquareName();
                String to = board.squares[accessibleSquares.getFirst()].getSquareName();
//                System.out.println("Returning best move: " + from + to);
                // TODO figure out checks when promoting by capturing their queen while promoting
                return from + to;
            }
        }
        return "ERROR";
    }


    @Override
    public String[] calculateBestMoveWithEvaluation(String fen, int depth) {
        String[] response = new String[2];
        response[0] = getBestMove(12);
        response[1] = String.valueOf(calcEval(12));
        return response;
    }
}