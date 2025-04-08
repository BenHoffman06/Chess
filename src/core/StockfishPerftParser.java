package core;

import java.io.*;
import java.util.HashMap;

public class StockfishPerftParser {
    public static HashMap<String, Long> get(String stockfishPath, String inputFEN, int depth) {
        HashMap<String, Long> resultMap = new HashMap<>();

        try {
            Process engine = new ProcessBuilder(stockfishPath).start();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(engine.getOutputStream()));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(engine.getInputStream()));

            // UCI initialization
            sendCommand(writer, "uci");
            sendCommand(writer, "isready");
            waitForReady(reader);

            // Send perft commands
            sendCommand(writer, "position fen " + inputFEN);
            sendCommand(writer, ("go perft " + depth));

            // Parse output
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("info")) {
                    reader.readLine();
                    continue;
                }

                if (line.contains(": ")) {
                    String[] parts = line.split(": ");
                    if (parts.length == 2) {
                        String move = parts[0].trim();
                        long count = Long.parseLong(parts[1].trim());
                        resultMap.put(move, count);
                    }
                }
                else {
                    return resultMap;
                }

            }
            sendCommand(writer, "quit");
            engine.waitFor();

        } catch (IOException | InterruptedException | NumberFormatException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    private static void sendCommand(BufferedWriter writer, String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
    }

    private static void waitForReady(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if ("readyok".equals(line)) return;
        }
    }
}
