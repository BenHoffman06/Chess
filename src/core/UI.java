package core;

import org.w3c.dom.css.Rect;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
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

    public static Main.Square promotionFrom;
    public static Main.Square promotionTo;

    public static boolean gameEnded = false;
    public static String endGameTitle = "";
    public static String endGameSubtitle = "";

    // Add these to track button positions
    public static Rectangle rematchButtonBounds;
    public static Rectangle exitButtonBounds;
    public static Rectangle hideButtonBounds;
    public static Rectangle promotionEscapeBounds;

    // Variables to see which button image should be drawn
    public static boolean rematchHover;
    public static boolean exitHover;
    public static boolean hideHover;
    public static boolean promotionEscapeHover;

    // Variables to handle promotions
    public static boolean isPromoting = false;
    public static Main.Square promotionSquare = null;
    public static byte chosenPieceToPromoteTo = 0;
    public static Rectangle[] clickablePromotionRegions = new Rectangle[4];

    //region Colors
    public static final Color BACKGROUND = Color.decode("#262421");
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

    public static final byte EXIT_BUTTON = 100;
    public static final byte EXIT_BUTTON_SELECTED = 101;
    public static final byte HIDE_BUTTON = 102;
    public static final byte HIDE_BUTTON_SELECTED = 103;
    public static final byte REMATCH_BUTTON = 104;
    public static final byte REMATCH_BUTTON_SELECTED = 105;
    public static final byte END_PANEL_BASE = 106;
    public static final byte PROMO_BACKGROUND = 107;
    public static final byte PROMO_BACKGROUND_UNDO_SELECTED = 108;
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

            // End game panel svgs
            pieceImages.put(EXIT_BUTTON, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/exitButton.png"))));
            pieceImages.put(EXIT_BUTTON_SELECTED, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/exitButtonSelected.png"))));
            pieceImages.put(HIDE_BUTTON, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/hideButton.png"))));
            pieceImages.put(HIDE_BUTTON_SELECTED, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/hideButtonSelected.png"))));
            pieceImages.put(REMATCH_BUTTON, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/rematchButton.png"))));
            pieceImages.put(REMATCH_BUTTON_SELECTED, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/rematchButtonSelected.png"))));
            pieceImages.put(END_PANEL_BASE, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/endPanelBase.png"))));
            pieceImages.put(PROMO_BACKGROUND, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/promoBG.png"))));
            pieceImages.put(PROMO_BACKGROUND_UNDO_SELECTED, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/promoBGUndoSelected.png"))));


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

    private static void handleSquareSelection(Main.Square s) {

        // If no square is previously selected
        if (selectedSquare == null && s != null) {

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
                if (gameEnded) {
                    System.out.println("Game ended, mouse pressed");

                    //region Only look for interaction on end game panel
                    if (rematchButtonBounds.contains(e.getPoint())) {
                        Main.rematch();
                    }
                    else if (exitButtonBounds.contains(e.getPoint())) {
                        System.exit(0);
                    }
                    // Add this condition for hide button
                    else if (hideButtonBounds.contains(e.getPoint())) {
                        gameEnded = false;
                        mainPanel.repaint();
                    }
                    //endregion
                    return;
                }

                // Focus on promoting piece UI if promotion is ongoing
                else if (isPromoting) {

                    //region Handle clicking on promotion options
                    for (Rectangle r : clickablePromotionRegions) {
                        // Inside the loop where promotion regions are checked
                        if (r.contains(e.getPoint())) {
                            // Find piece clicked on
                            byte selectedPiece = Objects.requireNonNull(getSquareAt(e.getPoint())).getPromotionOption();
                            System.out.println("You clicked on piece value: " + selectedPiece);

                            // Process promotion by calling movePiece with stored from and to
                            chosenPieceToPromoteTo = selectedPiece;
                            String moveNotation = Main.movePiece(UI.promotionFrom, UI.promotionTo);
                            Main.moves.add(new Move1(moveNotation, UI.promotionFrom, UI.promotionTo));

                            // Reset promotion state
                            isPromoting = false;
                            promotionFrom = null;
                            promotionTo = null;
                            promotionEscapeBounds = null;

                            // Redraw updated board
                            repaint();
                            return;
                        }
                    }
                    //endregion

                    // Ignore any mouse presses until a UI selection is made
                    return;
                }

                //region Handle square selection/deselection

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
                //endregion
            }

            public void mouseMoved(MouseEvent e) {
                //region Keep track of button hovering
                Point mousePos = e.getPoint();

                // Update game end panel hovering
                if (gameEnded) {
                    rematchHover = rematchButtonBounds.contains(mousePos);
                    exitHover = exitButtonBounds.contains(mousePos);
                    hideHover = hideButtonBounds.contains(mousePos);
                }

                // Update promotion UI button hovering
                if (isPromoting && promotionEscapeBounds != null) {
                    promotionEscapeHover = promotionEscapeBounds.contains(mousePos) && isPromoting && promotionEscapeBounds != null;
                }

                // endregion
                if (gameEnded || isPromoting) {
                    repaint();
                }

            }

            @Override
            public void mouseDragged(MouseEvent e) {

                // Update piece dragging variables
                dragX = e.getX();
                dragY = e.getY();
                beingDragged = true;

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Update being dragged
                beingDragged = false;

                // Ignore if game is over
                if (!Main.gameOngoing) {
                    return;
                }

                // If clicked on undo button
                if (promotionEscapeHover) {
                    System.out.println("Clicked undo");

                    //region Reset promotion variables
                    UI.isPromoting = false;
                    promotionSquare = null;
                    UI.promotionFrom = null;
                    UI.promotionTo = null;
                    promotionEscapeHover = false;
                    //endregion

                    repaint();
                    return;
                }

                // If previously selected a different, move to new point
                Main.Square targetSquare = getSquareAt(e.getPoint());
//                boolean
                if (targetSquare != null && targetSquare != selectedSquare && selectedSquare != null) {
                    Main.tryMovePiece(selectedSquare, targetSquare);
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
        // Modified paint method in mainPanel
        mainPanel = new JPanel(null) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);

                if (gameEnded) {
                    //region Draw game end UI
                    // Darken background
                    g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
                    g.fillRect(0, 0, getWidth(), getHeight());

                    // Draw end game content
                    Graphics2D g2d = (Graphics2D) g;
                    drawEndGamePanel(g2d);

                    //endregion
                    return;
                }


                if (isPromoting) {
                    //region Draw promotion UI
//                    System.out.println("Trying to draw promotion UI! 🤞");

                    // Calculate relevant variables
                    int squareSize = Main.board[0].getWidth();
                    Point promotionPos = UI.getSquarePosition(promotionSquare.index); // getScreenPosition
                    boolean isWhitePromotion = promotionSquare.index < 8;
                    int direction = isWhitePromotion ? 1 : -1;
                    int y = (promotionPos.y + 4 * squareSize * direction);
                    int Undoheight = (int) (squareSize * (69.0 / 137));
                    if (!isWhitePromotion) {
                        y = y - Undoheight + squareSize;
                    }
                    promotionEscapeBounds = new Rectangle(promotionPos.x, y, squareSize, Undoheight);
//                    System.out.println("Promotion escape bounds: " + promotionEscapeBounds);
                    //region Draw promotion UI background
                    BufferedImage whitePromoImg = promotionEscapeHover ? pieceImages.get(PROMO_BACKGROUND_UNDO_SELECTED) : pieceImages.get(PROMO_BACKGROUND);
                    BufferedImage blackPromoImg = flipImageVertically(whitePromoImg, (Graphics2D) g);

                    int height = squareSize * whitePromoImg.getHeight(this) / whitePromoImg.getWidth(this);

                    if (isWhitePromotion) {
                        g.drawImage(whitePromoImg, promotionPos.x, promotionPos.y, squareSize, height, this);
                    } else {
                        g.drawImage(blackPromoImg, promotionPos.x, promotionPos.y - height + squareSize, squareSize, height, this);
                    }
                    //endregion

                    // Define the order of pieces to display
                    byte[] promotionPieces = isWhitePromotion
                            ? new byte[] { Main.WHITE_QUEEN, Main.WHITE_KNIGHT, Main.WHITE_ROOK, Main.WHITE_BISHOP }
                            : new byte[] { Main.BLACK_QUEEN, Main.BLACK_KNIGHT, Main.BLACK_ROOK, Main.BLACK_BISHOP };

                    // For each piece option:
                    for (int i = 0; i < 4; i++) {

                        // Calculate updated y location for drawing
                        int optionY = promotionPos.y + i * direction * squareSize;

                        // Add rectangle to clickable regions
                        Rectangle rect = new Rectangle(promotionPos.x, optionY, squareSize, squareSize);
                        clickablePromotionRegions[i] = rect;

                        // Draw piece
                        g.drawImage(pieceImages.get(promotionPieces[i]), promotionPos.x, optionY, squareSize, squareSize, this);
                    }

                    //endregion
                    return;
                }

                // Draw dragging pieces last (on top of everything)
                if (selectedSquare != null && selectedSquare.piece != Main.EMPTY && selectedSquare.isDragging()) {
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
                                g.drawImage(img, pos.x, pos.y, s.getWidth(), s.getHeight(), this);
                            }
                        }
                    }
                }
            }

            private BufferedImage flipImageVertically(BufferedImage img, Graphics2D g2d) {
                // Create an AffineTransform to flip the image vertically
                AffineTransform transform = new AffineTransform();
                transform.translate(0, img.getHeight(null)); // Move the image down by its height
                transform.scale(1, -1); // Flip vertically

                // Create a new BufferedImage to hold the flipped image
                BufferedImage flippedImg = new BufferedImage(
                        img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

                // Draw the original image onto the new image with the applied transformation
                Graphics2D g2dFlipped = flippedImg.createGraphics();
                g2dFlipped.drawImage(img, transform, null);
                g2dFlipped.dispose();

                return flippedImg;
            }

            private void drawEndGamePanel(Graphics2D g2d) {
                int squareSize = Main.board[0].getWidth();
                int panelWidth = (int)(squareSize * 3.25);
                int panelHeight = (int)(panelWidth * (334.0/323.0));
                int x = (getWidth() - panelWidth) / 2;
                int y = (getHeight() - panelHeight) / 2;

                // Draw base panel
                g2d.drawImage(pieceImages.get(END_PANEL_BASE), x, y, panelWidth, panelHeight, this);

                // Calculate bounding boxes and trust they're right
                rematchButtonBounds = new Rectangle(x + (int)(27.0/323.0 * panelWidth), y + (int)(164.0/334.0 * panelHeight), (int)(269.0/323.0 * panelWidth), (int)(64.0/334.0 * panelHeight));
                exitButtonBounds = new Rectangle(rematchButtonBounds.x,  y + (int)(238.0/334.0 * panelHeight),rematchButtonBounds.width,rematchButtonBounds.height);
                hideButtonBounds = new Rectangle(x + (int)(295.0/323.0 * panelWidth),y + (int)(16.0/334.0 * panelHeight),(int)(16.0/323.0 * panelWidth),(int)(16.0/323.0 * panelWidth));

                // Get appropriate button sprites
                BufferedImage hideImg = hideHover ? pieceImages.get(HIDE_BUTTON_SELECTED) : pieceImages.get(HIDE_BUTTON);
                BufferedImage rematchImg = rematchHover ? pieceImages.get(REMATCH_BUTTON_SELECTED) : pieceImages.get(REMATCH_BUTTON);
                BufferedImage exitImg = exitHover ? pieceImages.get(EXIT_BUTTON_SELECTED) : pieceImages.get(EXIT_BUTTON);

                // Draw buttons using bounds
                g2d.drawImage(hideImg, hideButtonBounds.x, hideButtonBounds.y, hideButtonBounds.width, hideButtonBounds.height, this);
                g2d.drawImage(rematchImg, rematchButtonBounds.x, rematchButtonBounds.y, rematchButtonBounds.width, rematchButtonBounds.height, this);
                g2d.drawImage(exitImg, exitButtonBounds.x, exitButtonBounds.y, exitButtonBounds.width, exitButtonBounds.height, this);

                // Scale fonts
                int titleFontSize = (int)(panelHeight * (24.0/334.0));
                int subTitleFontSize = (int)(panelHeight * (14.0/334.0));

                g2d.setFont(new Font("Arial", Font.BOLD, titleFontSize));
                drawCenteredString(g2d, endGameTitle, new Rectangle(x, y + (int)(panelHeight * 0.06), panelWidth, (int)(panelHeight * 0.09)));

                g2d.setFont(new Font("Arial", Font.PLAIN, subTitleFontSize));
                drawCenteredString(g2d, endGameSubtitle, new Rectangle(x, y + (int)(panelHeight * 0.15), panelWidth, (int)(panelHeight * 0.06)));
            }
            private void drawCenteredString(Graphics g, String text, Rectangle rect) {
                FontMetrics metrics = g.getFontMetrics();
                int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
                int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
                g.setColor(Color.WHITE);
                g.drawString(text, x, y);
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

    // Modified summonEndGamePanel
    public static void summonEndGamePanel(String title, String subtitle) {
        gameEnded = true;
        endGameTitle = title;
        endGameSubtitle = subtitle;
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

    /**
     * Returns the screen coordinates of the top left corner of the square for rendering
     */
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