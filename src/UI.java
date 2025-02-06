import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UI {
    //region Constants and Variables
    // Colors
    public static final Color BACKGROUND = Color.decode("#161512");
    public static final Color WHITE = Color.decode("#f0d9b5");
    public static final Color BLACK = Color.decode("#b58863");
    public static final Color SELECTED_WHITE = Color.decode("#829769");
    public static final Color SELECTED_BLACK = Color.decode("#646f40");

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
        if (selectedSquare == null) {
            // Select this square only if it has a piece
            if (s.piece != Main.EMPTY) {
                selectedSquare = s;
            }
        } else {
            if (selectedSquare != s) {
                // If the selected square has a piece, move it to this square
                if (selectedSquare.piece != Main.EMPTY) {
                    Main.movePiece(selectedSquare, s);
                }
                // Clear selection after move or if selected square was empty
                selectedSquare = null;
            } else {
                // Clicking the same square again: deselect
                selectedSquare = null;
            }
        }
        mainPanel.repaint();
    }
    //endregion

    public static void handleCapture(byte piece) {
        playCaptureSound();
    }

    //region GUI and MouseListeners Setup and Rendering
    public static JPanel handleGUI() {
        System.setProperty("sun.java2d.opengl", "true");

        // Mouse handler for square selection and dragging
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Main.Square clickedSquare = getSquareAt(e.getPoint());
                if (clickedSquare != null && clickedSquare.piece != Main.EMPTY) {
                    handleSquareSelection(clickedSquare);

                    // Calculate offset
                    offsetX = e.getX() - clickedSquare.getX();
                    offsetY = e.getY() - clickedSquare.getY();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                dragX = e.getX();
                dragY = e.getY();
                beingDragged = true;
                mainPanel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Handle piece movement on release
                Main.Square targetSquare = getSquareAt(e.getPoint());
                if (targetSquare != null && targetSquare != selectedSquare && selectedSquare != null) {
                    Main.movePiece(selectedSquare, targetSquare);
                }
                beingDragged = false;
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
                if (selectedSquare != null && selectedSquare.piece != Main.EMPTY && UI.isDragging(selectedSquare)) {
                    int x = dragX - offsetX; // Use the offset here
                    int y = dragY - offsetY;
                    BufferedImage img = pieceImages.get(selectedSquare.piece);
                    g.drawImage(img, x, y, selectedSquare.getWidth(), selectedSquare.getHeight(), this);
                }
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

        frame.setVisible(true);

        return mainPanel;
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

    //region Sound Effects
    private static void playCaptureSound() {
        // thanks DeepSeek for uh my code

        // sound from https://github.com/lichess-org/lila/blob/master/public/sound/standard/Capture.mp3

        // Play capture sound in a separate thread
        new Thread(() -> {
            try {
                // Load the WAV file from resources
                InputStream soundFile = Main.class.getClassLoader().getResourceAsStream("sounds/Capture.wav");
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
    //endregion
}