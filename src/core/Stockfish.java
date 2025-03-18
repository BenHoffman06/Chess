package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Stockfish extends Engine {

    /**
     * Gets best move and evaluation from Stockfish API.
     *
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
    @Override
    public String[] calculateBestMoveWithEvaluation(String fen, int depth) throws IOException {
        if (depth < 1 || depth >= 16) {
            throw new IllegalArgumentException("Depth must be between 1 and 15");
        }

        String encodedFen = URLEncoder.encode(fen, StandardCharsets.UTF_8);
        String urlStr = String.format(
                "https://stockfish.online/api/s/v2.php?fen=%s&depth=%d",
                encodedFen,
                depth
        );

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP request failed with code: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        String responseStr = response.toString();

        // Check for success
        if (!responseStr.contains("\"success\":true")) {
            int dataIndex = responseStr.indexOf("\"data\":\"");
            if (dataIndex > 0) {
                int endIndex = responseStr.indexOf("\"", dataIndex + 8);
                String errorMsg = responseStr.substring(dataIndex + 8, endIndex);
                throw new IOException("API error: " + errorMsg);
            }
            throw new IOException("API request failed with unknown error\n\nFEN input: " + fen + "\n\n");
        }

        // Extract best move
        int bestMoveIndex = responseStr.indexOf("\"bestmove\":\"");
        if (bestMoveIndex == -1) {
            throw new IOException("Invalid response format - missing bestmove");
        }
        int bmStart = bestMoveIndex + 12;
        int bmEnd = responseStr.indexOf("\"", bmStart);
        String bestMoveLine = responseStr.substring(bmStart, bmEnd);
        String[] bmParts = bestMoveLine.split(" ");
        if (bmParts.length < 2) {
            throw new IOException("Invalid bestmove format: " + bestMoveLine);
        }
        String bestMove = bmParts[1];

        // Extract evaluation and mate
        String evaluation = extractJsonValue(responseStr, "\"evaluation\":");
        String mate = extractJsonValue(responseStr, "\"mate\":");

        // Prioritize mate if evaluation is null
        String evaluationResult;
        if ("null".equals(evaluation)) {
            evaluationResult = "Mate in " + mate;
        } else {
            evaluationResult = String.format("%+.2f", Double.parseDouble(evaluation));
        }

        return new String[]{bestMove, evaluationResult};
    }

    private static String extractJsonValue(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) return "null";

        int valueStart = json.indexOf(':', keyIndex) + 1;
        int valueEnd = -1;

        // Find the next comma or closing brace/bracket
        for (int i = valueStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ',' || c == '}' || c == ']') {
                valueEnd = i;
                break;
            }
        }

        if (valueEnd == -1) return "null";
        return json.substring(valueStart, valueEnd).trim();
    }
}