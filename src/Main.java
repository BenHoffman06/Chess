import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    //region Init static variables

    // Board
    public static Square[] board = new Square[64];

    // Moves
    public static ArrayList<String> moves = new ArrayList<>();

    public static String[] squareName = {
            "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
            "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
            "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
            "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
            "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
            "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
            "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
            "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"
    };

    public static Square selectedSquare = null;
    public static boolean beingDragged = false;
    public static int dragX, dragY;

    // Pieces
    public static final byte WHITE_PAWN = 1;
    public static final byte WHITE_KNIGHT = 2;
    public static final byte WHITE_BISHOP = 3;
    public static final byte WHITE_ROOK = 4;
    public static final byte WHITE_QUEEN = 5;
    public static final byte WHITE_KING = 6;

    public static final byte EMPTY = 0;

    public static final byte BLACK_PAWN = -1;
    public static final byte BLACK_KNIGHT = -2;
    public static final byte BLACK_BISHOP = -3;
    public static final byte BLACK_ROOK = -4;
    public static final byte BLACK_QUEEN = -5;
    public static final byte BLACK_KING = -6;

    // Colors
    public static final Color BACKGROUND = Color.decode("#161512");
    public static final Color WHITE = Color.decode("#f0d9b5");
    public static final Color BLACK = Color.decode("#b58863");
    public static final Color SELECTED_WHITE = Color.decode("#829769");
    public static final Color SELECTED_BLACK = Color.decode("#646f40");

    public static JPanel mainPanel;

    //region Load piece images into a dictionary
    public static final Map<Byte, BufferedImage> pieceImages = new HashMap<>(12);
    static {
        ClassLoader classLoader = Main.class.getClassLoader();
        try {
            pieceImages.put(WHITE_PAWN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wP (Custom).png"))));
            pieceImages.put(WHITE_KNIGHT, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wN (Custom).png"))));
            pieceImages.put(WHITE_BISHOP, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wB (Custom).png"))));
            pieceImages.put(WHITE_ROOK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wR (Custom).png"))));
            pieceImages.put(WHITE_QUEEN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wQ (Custom).png"))));
            pieceImages.put(WHITE_KING, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/wK (Custom).png"))));
            pieceImages.put(BLACK_PAWN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bP (Custom).png"))));
            pieceImages.put(BLACK_KNIGHT, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bN (Custom).png"))));
            pieceImages.put(BLACK_BISHOP, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bB (Custom).png"))));
            pieceImages.put(BLACK_ROOK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bR (Custom).png"))));
            pieceImages.put(BLACK_QUEEN, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bQ (Custom).png"))));
            pieceImages.put(BLACK_KING, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/bK (Custom).png"))));
        } catch (IOException e) {
            throw new RuntimeException("Error loading piece images: " + e.getMessage(), e);
        }
    }
    //endregion

    public static int offsetX, offsetY; // Offset of click within the square
    //endregion

    public static void setBoardFromFEN(String fen) {
        byte location = 0, piece_type;
        for (char c : fen.toCharArray()) {

            // Translate characters to pieces
            switch (c) {
                case 'p':
                    piece_type = Main.BLACK_PAWN;
                    break;
                case 'n':
                    piece_type = Main.BLACK_KNIGHT;
                    break;
                case 'b':
                    piece_type = Main.BLACK_BISHOP;
                    break;
                case 'r':
                    piece_type = Main.BLACK_ROOK;
                    break;
                case 'q':
                    piece_type = Main.BLACK_QUEEN;
                    break;
                case 'k':
                    piece_type = Main.BLACK_KING;
                    break;
                case 'P':
                    piece_type = Main.WHITE_PAWN;
                    break;
                case 'N':
                    piece_type = Main.WHITE_KNIGHT;
                    break;
                case 'B':
                    piece_type = Main.WHITE_BISHOP;
                    break;
                case 'R':
                    piece_type = Main.WHITE_ROOK;
                    break;
                case 'Q':
                    piece_type = Main.WHITE_QUEEN;
                    break;
                case 'K':
                    piece_type = Main.WHITE_KING;
                    break;
                case '/':
                    continue;
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                    location += (byte) Character.getNumericValue(c);
                    continue;
                default:
                    throw new RuntimeException("Error in FEN String");
            }

            board[location].setPiece(piece_type);
            location++;
        }
    }

    /**
     * Move piece from square1 to square2
     * @Returns chess notation for move
     */
    public static String movePiece(Square square1, Square square2) {
        //region Setup chess notation
        String movingPieceStr = String.valueOf(square1.getPieceChar());
        String takesString = "";
        String square2Name = squareName[square2.location];
        String toLocation;

        switch (square1.piece) {
            case WHITE_PAWN, BLACK_PAWN -> {
                // Location only is rank
                toLocation = String.valueOf(square2Name.toCharArray()[1]);
            }
            default -> {
                toLocation = square2Name;
            }
        }
        //endregion

        if (square2.piece != EMPTY) {
            handleCapture(square2.piece);
            takesString = "x";
        }
        square2.piece = square1.piece;
        square1.piece = EMPTY;

        selectedSquare = null;
        mainPanel.repaint();

        String moveNotation = movingPieceStr + takesString + toLocation;
        System.out.println(moveNotation);

        return moveNotation;
    }


    public static void handleCapture(byte piece) {
        // TODO play sound, display piece on side of board
        playCaptureSound();
    }

    static class Square extends JPanel {
        private final Color color;
        public byte piece;
        public byte location;

        public Square(Color color, byte name) {
            this.color = color;
            this.location = name;
            this.piece = EMPTY;

        }

        public void setPiece(byte piece) {
            this.piece = piece;
        }

        public String toString() {
            return ("Location: " + location + ", Piece: " + piece);
        }

        public boolean isSelected() {
            return (this == selectedSquare);
        }

        public boolean isBeingDragged() {
            return (this == selectedSquare && beingDragged);
        }

        public char getPieceChar() {
            switch (this.piece) {
                case WHITE_PAWN, BLACK_PAWN -> {
                    return squareName[location].toCharArray()[0];
                }
                case WHITE_KNIGHT, BLACK_KNIGHT -> {
                    return 'N';
                }
                case WHITE_BISHOP, BLACK_BISHOP -> {
                    return 'B';
                }
                case WHITE_ROOK, BLACK_ROOK -> {
                    return 'R';
                }
                case WHITE_QUEEN, BLACK_QUEEN -> {
                    return 'Q';
                }
                case WHITE_KING, BLACK_KING -> {
                    return 'K';
                }
                default -> throw new RuntimeException("Piece char calc failed. Is this piece 0: " + piece +
                        "? If so you caused a empty piece to move...");
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Color square depending on if selected
            g.setColor(color);
            if (isSelected()) {
                Color appliedColor = (color == WHITE) ? SELECTED_WHITE : SELECTED_BLACK;
                g.setColor(appliedColor);
            }
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw piece if NOT being dragged
            if (piece != EMPTY && !isBeingDragged()) {
                BufferedImage img = pieceImages.get(piece);
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    /**
     * Also sets up mouselistener for program and links it to GUI
     * @return
     */
    private static JPanel handleGUI() {
        System.setProperty("sun.java2d.opengl", "true");

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Square clickedSquare = getSquareAt(e.getPoint());
                if (clickedSquare != null && clickedSquare.piece != EMPTY) {
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
                // This works for both drag and drop and point and click
                Square targetSquare = getSquareAt(e.getPoint());
                if (targetSquare != null && targetSquare != selectedSquare && selectedSquare != null) {
                    movePiece(selectedSquare, targetSquare);
                }

                beingDragged = false;

            }

            private Square getSquareAt(Point p) {
                for (Square square : board) {
                    if (square.getBounds().contains(p)) {
                        return square;
                    }
                }
                return null;
            }
        };

        // Setup mainPanel
        mainPanel = new JPanel(null) {
            //region Override paint and paintComponent to render background and stationary pieces

            // Background
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BACKGROUND);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            // Stationary pieces
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (selectedSquare != null && selectedSquare.piece != EMPTY && selectedSquare.isBeingDragged()) {
                    int x = dragX - offsetX; // Use the offset here
                    int y = dragY - offsetY;
                    BufferedImage img = pieceImages.get(selectedSquare.piece);
                    g.drawImage(img, x, y, selectedSquare.getWidth(), selectedSquare.getHeight(), this);
                }
            }
            //endregion
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
            Square square = new Square(color, i);
            mainPanel.add(square);
            board[i] = square;
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

    private static void handleSquareSelection(Square s) {
        System.out.println("Selected square: " + selectedSquare + ", this: " + s);
        if (selectedSquare == null) {
            // Select this square only if it has a piece
            if (s.piece != EMPTY) {
                selectedSquare = s;
            }
        } else {
            if (selectedSquare != s) {
                // If the selected square has a piece, move it to this square
                if (selectedSquare.piece != EMPTY) {
                    movePiece(selectedSquare, s);
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

    private static void playCaptureSound() {
        // thanks DeepSeek for uh my code

        // sound from https://github.com/lichess-org/lila/blob/master/public/sound/standard/Capture.mp3
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

    public static void main(String[] args) {
        JPanel mainPanel = handleGUI();
        setBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        mainPanel.repaint();
    }
}