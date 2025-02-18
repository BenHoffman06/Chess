package core;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UI {

    //region Colors
    public static final Color BACKGROUND = Color.decode("#161512");
    public static final Color WHITE = Color.decode("#f0d9b5");
    public static final Color BLACK = Color.decode("#b58863");
    public static final Color SELECTED_WHITE = Color.decode("#829769");
    public static final Color SELECTED_BLACK = Color.decode("#646f40");
    public static final Color RED = Color.decode("#af5f5f");
    //endregion

    //region Indicator Image Access Constants
    public static final byte TAKEABLE_WHITE = 50;
    public static final byte TAKEABLE_BLACK = -50;
    public static final byte CHECKMATE = 99;
    //endregion

    //region Selection Mistake Variables
    public static int redCountdown = 0;
    public static Main.Square redSquare;
    //endregion

    //region Piece Images
    public static final Map<Byte, BufferedImage> pieceImages = new HashMap<>(12);

    static {
        // Load piece images into the dictionary
        ClassLoader classLoader = Main.class.getClassLoader();
        try {
            // White Pieces
            pieceImages.put(Main.WHITE_PAWN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wP (Custom).png"))));
            pieceImages.put(Main.WHITE_KNIGHT, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wN (Custom).png"))));
            pieceImages.put(Main.WHITE_BISHOP, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wB (Custom).png"))));
            pieceImages.put(Main.WHITE_ROOK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wR (Custom).png"))));
            pieceImages.put(Main.WHITE_QUEEN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wQ (Custom).png"))));
            pieceImages.put(Main.WHITE_KING, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wK (Custom).png"))));

            // Black Pieces
            pieceImages.put(Main.BLACK_PAWN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bP (Custom).png"))));
            pieceImages.put(Main.BLACK_KNIGHT, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bN (Custom).png"))));
            pieceImages.put(Main.BLACK_BISHOP, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bB (Custom).png"))));
            pieceImages.put(Main.BLACK_ROOK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bR (Custom).png"))));
            pieceImages.put(Main.BLACK_QUEEN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bQ (Custom).png"))));
            pieceImages.put(Main.BLACK_KING, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bK (Custom).png"))));

            // Non-pieces (e.g., indicators)
            pieceImages.put(TAKEABLE_WHITE, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/takeable-white.png"))));
            pieceImages.put(TAKEABLE_BLACK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/takeable-black.png"))));
            pieceImages.put(CHECKMATE, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/checkmateOutline.png"))));

        } catch (IOException e) {
            throw new RuntimeException("Error loading piece images: " + e.getMessage(), e);
        }
    }
//endregion

    //region Mouse Interaction Variables
    public static Main.Square selectedSquare = null;
    public static boolean beingDragged = false;
    public static int dragX, dragY;
    //endregion

    // UI Components
    public static JPanel mainPanel;

    //region Square Selection and Dragging Logic
    public static boolean isSelecting(Main.Square s) {
        return (s == selectedSquare);
    }

    public static boolean isDragging(Main.Square s) {
        return (s == selectedSquare && beingDragged);
    }

    private static void handleSquareSelection(Main.Square s) {

        // If no square is previously selected
        if (selectedSquare == null) {

            // Select this square only if it has a piece
            if (s.piece != Main.EMPTY) {

                // That piece has to be the color of the person whose turn it is to move
                boolean isRightColor = (Main.isWhitesMove && s.piece > 0) || (!Main.isWhitesMove && s.piece < 0);
                if (isRightColor) {
                    selectedSquare = s;
                }
            }
        } else {

            // If clicking different square
            if (selectedSquare != s) {

                // And if previously selected square has a piece
                if (selectedSquare.piece != Main.EMPTY) {

                    // Try to move to new square
                    Main.tryMovePiece(selectedSquare, s);
                }
                // Clear selection after move or if selected square was empty
            }

            // Clicking the same square again: deselect
            selectedSquare = null;
        }
        repaint();
    }
    //endregion

    public static void handleCapture() {
        playSound("sounds/Capture.wav");
    }

    public static void handleInvalidMoveTo(Main.Square s) {
        UI.selectedSquare = null;

        UI.redCountdown = 25;
        UI.redSquare = s;

        s.repaint();
    }

    public static void handleInvalidMoveTo(Main.Square s, String message) {
        System.out.println(message);
        handleInvalidMoveTo(s);
    }


    //region GUI and MouseListeners Setup and Rendering
    public static JPanel handleGUI() {
        System.setProperty("sun.java2d.opengl", "true");

        MouseAdapter mouseHandler = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                // Ignore if game is over
                if (!Main.gameOngoing) {
                    return;
                }

                // Handle selection/deselection
                Main.Square clickedSquare = getSquareAt(e.getPoint());
                handleSquareSelection(clickedSquare);

                // If not selecting square, clear accessible moves
                if (selectedSquare == null || selectedSquare.isEmpty()) {
                    Main.accessibleMoves.clear();
                }

                // If selecting square, keep accessible moves updated
                else {
                    Main.accessibleMoves = Main.accessibleSquaresOf(UI.selectedSquare, Main.board, true);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Ignore if game is over
                if (!Main.gameOngoing) {
                    return;
                }

                dragX = e.getX();
                dragY = e.getY();
                beingDragged = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Ignore if game is over
                if (!Main.gameOngoing) {
                    return;
                }

                // If previously selected a different, move to new point
                Main.Square targetSquare = getSquareAt(e.getPoint());
                if (targetSquare != null && targetSquare != selectedSquare && selectedSquare != null) {
                    Main.tryMovePiece(selectedSquare, targetSquare);
                }
                beingDragged = false;

                // If doing drag and drop, clear accessible moves after making move
                if (selectedSquare == null || selectedSquare.isEmpty()) {
                    Main.accessibleMoves.clear();
                }
            }

            private Main.Square getSquareAt(Point p) {
                for (Main.Square square : Main.board) {
                    if (square.getBounds().contains(p)) {
                        return square;
                    }
                }
                return null;
            }
        };

        // Setup mainPanel
        mainPanel = new JPanel(null) {
            @Override
            public void paint(Graphics g) {
                //region Enable rendering optimizations
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                //endregion

                super.paint(g);

                // Draw dragging pieces
                if (selectedSquare != null && selectedSquare.piece != Main.EMPTY && UI.isDragging(selectedSquare)) {
                    int x = dragX - selectedSquare.getWidth() / 2;
                    int y = dragY - selectedSquare.getWidth() / 2;
                    BufferedImage img = pieceImages.get(selectedSquare.piece);
                    g.drawImage(img, x, y, selectedSquare.getWidth(), selectedSquare.getHeight(), this);
                }

                // Draw accessible moves
                for (Main.Square s : Main.board) {
                    for (Byte b : Main.accessibleMoves ) {
                        if ((int) b == (int) s.index){
                            double width = (.27 * s.getWidth());
                            int topLeft = (int) (((double) s.getWidth() / 2) - (width / 2));

                            Point pos = getSquarePosition(s.index);
                            Color color = (s.isWhite()) ? SELECTED_WHITE : SELECTED_BLACK;
                            g.setColor(color);

                            if (s.piece == Main.EMPTY) {
                                g.fillOval(pos.x + topLeft, pos.y + topLeft, (int) width, (int) width);
                            } else {
                                BufferedImage img = (s.isWhite()) ? pieceImages.get(TAKEABLE_WHITE) : pieceImages.get(TAKEABLE_BLACK);
                                g.drawImage(img, pos.x, pos.y, selectedSquare.getWidth(), selectedSquare.getHeight(), this);
                            }
                        }
                    }
                }
            }
        };

        // Add mouse listeners
        mainPanel.addMouseListener(mouseHandler);
        mainPanel.addMouseMotionListener(mouseHandler);

        // Add background to mainPanel
        mainPanel.setBackground(BACKGROUND);

        // Setup frame
        JFrame frame = new JFrame("Chess");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 550);
        frame.setLocationRelativeTo(null);

        // Fill board with squares
        for (byte i = 0; i < 64; i++) {
            Color color = (i + i / 8) % 2 == 0 ? WHITE : BLACK;
            Main.Square square = new Main.Square(color, i);
            mainPanel.add(square);
            Main.board[i] = square;
        }

        // Set up listener to resize board on window resize
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                resizeSquares(mainPanel);
            }
        });
        resizeSquares((JPanel) frame.getContentPane());

        //region redCountdown decrementer
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            if (redCountdown-- > 0) {
                if (redCountdown == 0) {
                    repaint();
                }
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        //endregion

        frame.setVisible(true);

        return mainPanel;
    }

    public static void repaint() {
        mainPanel.repaint();
    }

    private static void resizeSquares(JPanel panel) {
        int width = panel.getWidth();
        int height = panel.getHeight();
        int size = Math.min(width, height) / 8;

        int startX = (width - size * 8) / 2;
        int startY = (height - size * 8) / 2;

        for (int i = 0; i < 64; i++) {
            int row = i / 8;
            int col = i % 8;
            Component square = panel.getComponent(i);
            square.setBounds(startX + col * size, startY + row * size, size, size);
        }

        // Repaint only the chessboard area
        panel.repaint(startX, startY, size * 8, size * 8);
    }
    //endregion

    public static Point getSquarePosition(int index) {
        int row = index / 8;
        int col = index % 8;

        int size = Math.min(mainPanel.getWidth(), mainPanel.getHeight()) / 8;
        int startX = (mainPanel.getWidth() - size * 8) / 2;
        int startY = (mainPanel.getHeight() - size * 8) / 2;

        int x = startX + col * size;
        int y = startY + row * size;

        return new Point(x, y);
    }


    /**
     * Works only with WAV files
     */
    public static void playSound(String filename) {
        // thanks DeepSeek for uh my code

        // sound from https://github.com/lichess-org/lila/blob/master/public/sound/standard/Capture.mp3

        // Play capture sound in a separate thread
        new Thread(() -> {
            try {
                // Load the WAV file from resources
                InputStream soundFile = Main.class.getClassLoader().getResourceAsStream(filename);
                if (soundFile == null) {
                    System.err.println("Could not find sound file!");
                    return;
                }

                // Get an AudioInputStream from the file
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);

                // Get a Clip (sound player) and open it with the audio stream
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);

                // Start playing the sound
                clip.start();

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }
}