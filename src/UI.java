import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UI {

    public static boolean moveNotPossible = false;

    //region Constants and Variables
    // Colors
    public static final Color BACKGROUND = Color.decode("#161512");
    public static final Color WHITE = Color.decode("#f0d9b5");
    public static final Color BLACK = Color.decode("#b58863");
    public static final Color SELECTED_WHITE = Color.decode("#829769");
    public static final Color SELECTED_BLACK = Color.decode("#646f40");
    public static final Color RED = Color.decode("#af5f5f");

    public static final byte TAKEABLE_WHITE = 50;
    public static final byte TAKEABLE_BLACK = -50;


    public static int redCountdown = 0;
    public static Main.Square redSquare;

    // Piece images
    public static final Map<Byte, BufferedImage> pieceImages = new HashMap<>(12);

    // Mouse movement and selection variables
    public static Main.Square selectedSquare = null;
    public static boolean beingDragged = false;
    public static int dragX, dragY;
    public static int offsetX, offsetY; // Offset of click within the square

    // Main panel
    public static JPanel mainPanel;

    static {
        // Load piece images into the dictionary
        ClassLoader classLoader = Main.class.getClassLoader();
        try {
            pieceImages.put(Main.WHITE_PAWN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wP (Custom).png"))));
            pieceImages.put(Main.WHITE_KNIGHT, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wN (Custom).png"))));
            pieceImages.put(Main.WHITE_BISHOP, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wB (Custom).png"))));
            pieceImages.put(Main.WHITE_ROOK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wR (Custom).png"))));
            pieceImages.put(Main.WHITE_QUEEN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wQ (Custom).png"))));
            pieceImages.put(Main.WHITE_KING, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wK (Custom).png"))));
            pieceImages.put(Main.BLACK_PAWN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bP (Custom).png"))));
            pieceImages.put(Main.BLACK_KNIGHT, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bN (Custom).png"))));
            pieceImages.put(Main.BLACK_BISHOP, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bB (Custom).png"))));
            pieceImages.put(Main.BLACK_ROOK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bR (Custom).png"))));
            pieceImages.put(Main.BLACK_QUEEN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bQ (Custom).png"))));
            pieceImages.put(Main.BLACK_KING, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bK (Custom).png"))));
            pieceImages.put(TAKEABLE_WHITE, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/takeable-white.png"))));
            pieceImages.put(TAKEABLE_BLACK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/takeable-black.png"))));

        } catch (IOException e) {
            throw new RuntimeException("Error loading piece images: " + e.getMessage(), e);
        }
    }
    //endregion

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

            // Clicking the same square again: deselect
            if (selectedSquare == s) {
                selectedSquare = null;
            }
            // If clicking new square
            else {

                // If previously selected square has a piece
                if (selectedSquare.piece != Main.EMPTY) {

                    // Try to move to new square
                    Main.tryMovePiece(selectedSquare, s);
                }
                // Clear selection after move or if selected square was empty
                selectedSquare = null;
            }
        }
        repaint();
    }
    //endregion

    public static void handleCapture(byte piece) {
        playSound("sounds/Capture.wav");
    }

    public static void handleInvalidMoveTo(Main.Square s) {
        moveNotPossible = true;
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
                Main.Square clickedSquare = getSquareAt(e.getPoint());

                if (clickedSquare != null && clickedSquare.piece != Main.EMPTY) {
                    handleSquareSelection(clickedSquare);

                    // Calculate offset
                    offsetX = e.getX() - clickedSquare.getX();
                    offsetY = e.getY() - clickedSquare.getY();
                } else {
                    handleSquareSelection(clickedSquare); // Call handleSquareSelection anyways to handle deselection
                }

                if (selectedSquare == null || selectedSquare.isEmpty()) {
                    Main.accessibleMoves.clear();
                }
                else {
                    Main.accessibleMoves = Main.accessibleSquaresOf(UI.selectedSquare, Main.board, true);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                dragX = e.getX();
                dragY = e.getY();
                beingDragged = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Handle piece movement on release
                Main.Square targetSquare = getSquareAt(e.getPoint());
                if (targetSquare != null && targetSquare != selectedSquare && selectedSquare != null) {
                    Main.tryMovePiece(selectedSquare, targetSquare);
                }
                beingDragged = false;

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
            // Background
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BACKGROUND);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            // Stationary pieces and dragged piece
            @Override
            public void paint(Graphics g) {
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
                // TODO add special rendering for pieces which are accessibleSquaresOf(selectedPiece)

            }
        };

        mainPanel.addMouseListener(mouseHandler);
        mainPanel.addMouseMotionListener(mouseHandler);

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
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                resizeSquares(mainPanel);
            }
        });
        resizeSquares((JPanel) frame.getContentPane());

        //region redCountdown decrementer
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {

            if (redCountdown > 0) {
//                System.out.println("doing red countdown");
                redCountdown -= 1;
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



    private static void playSound(String filename) {
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