package core;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UI {

    //region Implemented global static variables
    public static Square promotionFrom;
    public static Square promotionTo;

    public static boolean endGamePanelShouldBeShown = false;

    public static Rectangle promotionEscapeBounds;
    public static boolean promotionEscapeHover;

    // Variables to handle promotions
    public static boolean isPromoting = false;
    public static Square promotionSquare = null;
    public static Rectangle[] clickablePromotionRegions = new Rectangle[4];

    //region Colors
    public static final Color BACKGROUND = Color.decode("#262421");
    public static final Color WHITE = Color.decode("#f0d9b5");
    public static final Color BLACK = Color.decode("#b58863");
    public static final Color SELECTED = new Color(16, 82, 26, 125);
    public static final Color JUST_MOVED = new Color(25, 151, 2, 83);
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
    public static Square redSquare;
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

    public static EndGamePanel endGamePanel = new EndGamePanel();
//endregion

    //region Mouse Interaction Variables
    public static Square selectedSquare = null;
    public static boolean beingDragged = false;
    public static int dragX, dragY;
    //endregion

    // UI Components
    public static JPanel mainPanel;

    //endregion

    //region Square Selection and Dragging Logic
    public static boolean isSelecting(Square s) {
        return (s == selectedSquare);
    }

    private static void select(Square s) {
        // Catch clicking outside the board
        if (s == null) return;

        // If no square is previously selected
        if (selectedSquare == null) {

            // Select this square only if it has a piece
            if (s.piece != Main.EMPTY) {

                // Select piece if it is that player's turn
                if (s.hasPieceWhoseTurnItIsToMove() && !Main.currentEngine.isTurn()) {

                    // Select piece and show accessible squares
                    selectedSquare = s;
                    Main.accessibleMoves.addAll(Main.board.accessibleSquaresOf(s));
                }
            }
        } else {

            // If new square to select has your piece
            if (s.hasPieceWhoseTurnItIsToMove()) {

                // If you clicked on the selected square again, deselect it
                if (selectedSquare == s) {
                    selectedSquare = null;
                }

                // Otherwise, select that piece instead
                else selectedSquare = s;

            }

            // Otherwise
            else {

                // Try to move to the second square
                Main.board.attemptMove(selectedSquare, s);

                // Clear selected square
                selectedSquare = null;
            }

        }
        repaint();
    }
    //endregion

    public static void handleInvalidMoveTo(Move move) {
        UI.selectedSquare = null; // TODO CHANGE

        UI.redCountdown = 50;
        UI.redSquare = move.to;

        repaint();
    }

    //region GUI and MouseListeners Setup and Rendering
    public static JPanel handleGUI() {
        System.setProperty("sun.java2d.opengl", "true");

        MouseAdapter mouseHandler = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                // Process button clicks on endgame panel
                if (endGamePanelShouldBeShown) {
                    endGamePanel.handleMouseClick(e.getPoint());
                    return;
                }

                // If promoting, handle clicks on promotion UI
                else if (isPromoting) {
                    // Handle all promotion-related clicks in one place
                    Point clickPoint = e.getPoint();

                    // 1. First check promotion pieces
                    for (int i = 0; i < clickablePromotionRegions.length; i++) {
                        Rectangle r = clickablePromotionRegions[i];
                        if (r.contains(clickPoint)) {
                            // Handle promotion piece selection
                            byte selectedPiece = getPromotionPieceForIndex(i);
                            Main.board.executeMove(UI.promotionFrom, UI.promotionTo, selectedPiece, false);
                            resetPromotionState();
                            repaint();
                            return;
                        }
                    }

                    // 2. If clicked elsewhere on board (undo button or not):
                    // Undo promotion
                    boolean clickedOnBoard = getSquareAt(e.getPoint()) != null;
                    if (clickedOnBoard) {
                        System.out.println("Clicked undo");
                        resetPromotionState();
                        repaint();
                        return;
                    }
//
//                    // 3. If clicked outside, do nothing
//                    return;
                }

                //region Handle square selection/deselection

                Square clickedSquare = getSquareAt(e.getPoint());
                select(clickedSquare);

                // If not selecting square, clear accessible moves
                if (selectedSquare == null || selectedSquare.isEmpty()) {
                    Main.accessibleMoves.clear();
                }

                // If selecting square, keep accessible moves updated
                else {
                    Main.accessibleMoves = Main.board.accessibleSquaresOf(UI.selectedSquare);
                }
                //endregion
            }

            public void mouseMoved(MouseEvent e) {
                //region Keep track of button hovering
                Point mousePos = e.getPoint();

                // Update game end panel hovering
                if (endGamePanelShouldBeShown) {
                    endGamePanel.handleMouseMove(mousePos);
                    repaint();
                }

                // Update promotion UI button hovering
                if (isPromoting && promotionEscapeBounds != null) {
                    promotionEscapeHover = promotionEscapeBounds.contains(mousePos) && isPromoting && promotionEscapeBounds != null;
                }

                // endregion
                if (endGamePanelShouldBeShown || isPromoting) {
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
                if (beingDragged) {
                    select(getSquareAt(e.getPoint()));

                    // Stop showing accessible squares
                    Main.accessibleMoves.clear();

                    // Update being dragged
                    beingDragged = false;
                }

                // Ignore if game is over
                if (!Main.gameOngoing) {
                    return;
                }

//                // If clicked on undo button
//                if (promotionEscapeHover) {
//                    System.out.println("Clicked undo");
//
//                    //region Reset promotion variables
//                    UI.isPromoting = false;
//                    promotionSquare = null;
//                    UI.promotionFrom = null;
//                    UI.promotionTo = null;
//                    promotionEscapeHover = false;
//                    //endregion
//
//                    repaint();
//                    return;
//                }

                // If previously selected a different, move to new point
                Square targetSquare = getSquareAt(e.getPoint());
//                boolean
                if (targetSquare != null && targetSquare != selectedSquare && selectedSquare != null) {
                    Main.board.attemptMove(selectedSquare, targetSquare);
                }
            }

            public Square getSquareAt(Point p) {
                for (Square square : Main.board.squares) {
                    if (getSquareBounds(square).contains(p)) {
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
                Rectangle boardBounds = getBoardBounds();

                drawSquares(g, mainPanel);

                drawMaterialDifference(g, mainPanel, boardBounds);

                drawEvalBar(g, mainPanel, boardBounds);

                tryDrawPromotionOverlay(g, mainPanel);

                tryDrawingDraggingPieces(g, mainPanel);

                tryDrawingAccessibleSquares(g, mainPanel);

                drawSquareNames(g);

                tryDrawGameEndOverlay(g, mainPanel);
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
        frame.setSize(1000, 550);
        frame.setLocationRelativeTo(null);

        // Set up listener to resize board on window resize
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
//                resizeSquares(mainPanel);
                repaint();
            }
        });
//        resizeSquares((JPanel) frame.getContentPane());
        repaint();

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

    public static void tryDrawGameEndOverlay(Graphics g, JPanel panel) {
        if (endGamePanelShouldBeShown) {
            // Darken background
            g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
            g.fillRect(0, 0, panel.getWidth(), panel.getHeight());

            // Draw end game content
            Graphics2D g2d = (Graphics2D) g;
            endGamePanel.draw(g2d, panel.getWidth(), panel.getHeight());
        }
    }

    public static void drawMaterialDifference(Graphics g, JPanel panel, Rectangle boardBounds) {
        // Sort lost pieces: white pieces in natural order, black pieces in reverse order.
        Main.board.capturedWhitePieces.sort(Byte::compare);
        Main.board.capturedBlackPieces.sort(Comparator.reverseOrder());

        // Calculate relevant variables
        int materialDiff = Main.board.getMaterialDiff();
        boolean materialOnWhiteSide = (materialDiff > 0);
        int pieceWidth = boardBounds.width / 24;
        int heightOffset = (int) (boardBounds.width / 2.2);
        byte previous = 0;
        int boardRightEdge = boardBounds.x + boardBounds.width;

        //region Draw white lost pieces
        // Start drawing to the right of the board, vertically centered using heightOffset.
        Point whiteStart = new Point(boardRightEdge + boardBounds.width / 16, (boardBounds.y + heightOffset) - (pieceWidth / 2));
        int newX = whiteStart.x;
        for (int i = 0; i < Main.board.capturedWhitePieces.size(); i++) {
            byte piece = Main.board.capturedWhitePieces.get(i);
            // If the same type as the previous piece, adjust the x position for a tighter grouping.
            if (previous == piece) {
                newX -= (int)(pieceWidth / 1.5);
            }
            Rectangle bounds = new Rectangle(newX, whiteStart.y, pieceWidth, pieceWidth);
            g.drawImage(pieceImages.get(piece), bounds.x, bounds.y, bounds.width, bounds.height, panel);
            previous = piece;
            newX += pieceWidth;
        }

        // Draw material difference number
        if (materialOnWhiteSide && materialDiff != 0) {
            g.setColor(Color.WHITE);
            g.drawString("+" + materialDiff, newX, whiteStart.y + pieceWidth);
        }
        //endregion

        //region Draw black lost pieces

        // First, create a white background for each slot.
        Point blackStart = new Point(boardRightEdge + boardBounds.width / 16,
                (boardBounds.y + boardBounds.height - heightOffset - (pieceWidth / 2)));
        newX = blackStart.x;
        for (int i = 0; i < Main.board.capturedBlackPieces.size(); i++) {
            byte piece = Main.board.capturedBlackPieces.get(i);
            if (previous == piece) {
                newX -= (int)(pieceWidth / 1.5);
            }
            Rectangle bounds = new Rectangle(newX, blackStart.y, pieceWidth, pieceWidth);
            g.setColor(Color.WHITE);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            previous = piece;
            newX += pieceWidth;
        }

        // Draw material difference number
        if (!materialOnWhiteSide && materialDiff != 0) {
            g.drawString("+" + -1 * materialDiff, newX, blackStart.y + pieceWidth);
        }

        // Then, draw the black pieces over the background.
        // Reset positioning variables for a clean draw pass.
        previous = 0;
        blackStart = new Point(boardRightEdge + boardBounds.width / 16,
                (boardBounds.y + boardBounds.height - heightOffset - (pieceWidth / 2)));
        newX = blackStart.x;
        for (int i = 0; i < Main.board.capturedBlackPieces.size(); i++) {
            byte piece = Main.board.capturedBlackPieces.get(i);
            if (previous == piece) {
                newX -= (int)(pieceWidth / 1.5);
            }
            Rectangle bounds = new Rectangle(newX, blackStart.y, pieceWidth, pieceWidth);
            g.drawImage(pieceImages.get(piece), bounds.x, bounds.y, bounds.width, bounds.height, panel);
            previous = piece;
            newX += pieceWidth;
        }

        //endregion
    }

    public static void drawEvalBar(Graphics g, JPanel panel, Rectangle boardBounds) {
        double stored = Main.board.currentEval;
        double eval = stored;
        if (Main.currentEngine == null) return;

        // Calculate eval bar bounds
        double analysisBarOffset = 4 * eval / (Math.abs(eval) + 4); // in units of squares
        int divider = (int) (boardBounds.y + (double) boardBounds.height / 2 + analysisBarOffset * boardBounds.width / 8);
        Rectangle evalBar = new Rectangle(boardBounds.x - 30 * boardBounds.height / 256, boardBounds.y, boardBounds.height / 16, boardBounds.height);

        // Draw white part
        g.setColor(Color.WHITE);
        int whiteHeight = evalBar.y + evalBar.height - divider;
        g.fillRect(evalBar.x, divider, evalBar.width, whiteHeight);

        // Draw black part
        g.setColor(new Color(64, 61, 57));
        g.fillRect(evalBar.x, evalBar.y, evalBar.width, evalBar.height - whiteHeight);

        // Draw eval number
        g.setColor(BACKGROUND);
        g.drawString(String.format("%.2f", -1 * eval), boardBounds.x - (int) (boardBounds.height / 9.5), evalBar.y + evalBar.height - 24);
    }

    // Helper method to get promotion piece based on index
    private static byte getPromotionPieceForIndex(int index) {
        boolean isWhite = promotionSquare.index < 8;
        return new byte[] {
                isWhite ? Main.WHITE_QUEEN : Main.BLACK_QUEEN,
                isWhite ? Main.WHITE_KNIGHT : Main.BLACK_KNIGHT,
                isWhite ? Main.WHITE_ROOK : Main.BLACK_ROOK,
                isWhite ? Main.WHITE_BISHOP : Main.BLACK_BISHOP
        }[index];
    }

    // Helper method to reset promotion state
    private static void resetPromotionState() {
        UI.isPromoting = false;
        promotionSquare = null;
        UI.promotionFrom = null;
        UI.promotionTo = null;
        promotionEscapeBounds = null;
        promotionEscapeHover = false;
    }

    public static void tryDrawPromotionOverlay(Graphics g, JPanel panel) {
        if (isPromoting) {
            // Calculate relevant variables
            int squareSize = getSquareSize();

            Point promotionPos = UI.getSquarePosition(promotionSquare.index); // getScreenPosition
            boolean isWhitePromotion = promotionSquare.index < 8;
            int direction = isWhitePromotion ? 1 : -1;
            int y = (promotionPos.y + 4 * squareSize * direction);
            int Undoheight = (int) (squareSize * (69.0 / 137));
            if (!isWhitePromotion) {
                y = y - Undoheight + squareSize;
            }
            promotionEscapeBounds = new Rectangle(promotionPos.x, y, squareSize, Undoheight);

            //region Draw promotion UI background
            BufferedImage whitePromoImg = promotionEscapeHover ? pieceImages.get(PROMO_BACKGROUND_UNDO_SELECTED) : pieceImages.get(PROMO_BACKGROUND);
            BufferedImage blackPromoImg = flipImageVertically(whitePromoImg, (Graphics2D) g);

            int height = squareSize * whitePromoImg.getHeight(panel) / whitePromoImg.getWidth(panel);

            if (isWhitePromotion) {
                g.drawImage(whitePromoImg, promotionPos.x, promotionPos.y, squareSize, height, panel);
            } else {
                g.drawImage(blackPromoImg, promotionPos.x, promotionPos.y - height + squareSize, squareSize, height, panel);
            }

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
                g.drawImage(pieceImages.get(promotionPieces[i]), promotionPos.x, optionY, squareSize, squareSize, panel);
            }
        }
    }

    public static void tryDrawingDraggingPieces(Graphics g, JPanel panel) {
        boolean aPieceIsDragging = selectedSquare != null && selectedSquare.piece != Main.EMPTY && selectedSquare.isDragging();
        if (aPieceIsDragging) {

            // Calculate bounds
            int squareBounds = getSquareSize();
            int x = dragX - squareBounds / 2;
            int y = dragY - squareBounds / 2;

            // Draw dragging piece
            BufferedImage img = pieceImages.get(selectedSquare.piece);
            g.drawImage(img, x, y, squareBounds, squareBounds, panel);
        }
    }

    public static void tryDrawingAccessibleSquares(Graphics g, JPanel panel) {
        for (Integer index : Main.accessibleMoves) {
            // Fetch square we are drawing
            Square s = Main.board.squares[index];

            // Calculate proportions
            double width = (.27 * getSquareSize());
            int topLeft = (int) (((double) getSquareSize() / 2) - (width / 2));
            Point pos = getSquarePosition(s.index);

            // Set color
            Color color = (s.isWhite()) ? UI.mix(UI.WHITE, SELECTED) : UI.mix(UI.BLACK, SELECTED);
            g.setColor(color);

            // Draw oval to represent empty accessible squares
            if (s.piece == Main.EMPTY) {
                g.fillOval(pos.x + topLeft, pos.y + topLeft, (int) width, (int) width);
            }

            // Draw image for accessible squares with pieces
            else {
                BufferedImage img = (s.isWhite()) ? pieceImages.get(TAKEABLE_WHITE) : pieceImages.get(TAKEABLE_BLACK);
                g.drawImage(img, pos.x, pos.y, getSquareSize(), getSquareSize(), panel);
            }
        }
    }

    public static void drawSquares(Graphics g, JPanel panel) {
        for (Square s : Main.board.squares) {

            Rectangle board = getBoardBounds();
            int squareSize = Math.min(board.width, board.height) / 8;

            g.setColor(s.getCurrentColor());
            Rectangle squareBounds = getSquareBounds(s);
            g.fillRect(squareBounds.x, squareBounds.y, squareBounds.width, squareBounds.height);

            // Draw checkmate image if checkmated
            if (s.piece == Main.WHITE_KING && Main.board.isWhitesMove || s.piece == Main.BLACK_KING && !Main.board.isWhitesMove) {
                if (Main.board.checkGameOutcome() == 1) {
                    BufferedImage img = UI.pieceImages.get(CHECKMATE);
                    g.drawImage(img, 0, 0, squareSize, squareSize, panel);
                }
            }

            // Draw piece if NOT being dragged
            if (s.piece != Main.EMPTY && !s.isDragging()) {
                BufferedImage img = pieceImages.get(s.piece);
                g.drawImage(img, squareBounds.x, squareBounds.y, squareBounds.width, squareBounds.height, panel);

            }
        }
    }

    public static void drawSquareNames(Graphics g) {
        // Calculate labeled squares
        Square[] squares = Main.board.squares;

        ArrayList<Square> squaresWithFileLabels = new ArrayList<>();
        ArrayList<Square> squaresWithRankLabels = new ArrayList<>();
        String[] fileLabels = {"a", "b", "c", "d", "e", "f", "g", "h"};

        int fontSize = (int) (getBoardBounds().height * 0.0698138297873 * 0.4);
        Font current = g.getFont();
        g.setFont(new Font("Arial", Font.BOLD, fontSize));
        for (int i = 0; i < 8; i++) {

            String fileLabel = fileLabels[i];
            String rankLabel = String.valueOf(8 - i);

            Square fileLabelSquare = squares[56 + i];
            Square rankLabelSquare = squares[8 * (i + 1) - 1];

            Rectangle fileLabelSquareBounds = getSquareBounds(fileLabelSquare);
            Rectangle rankLabelSquareBounds = getSquareBounds(rankLabelSquare);

            int offset = (int) (fileLabelSquareBounds.width * .75);

            g.setColor(fileLabelSquare.getCurrentComplementaryColor());
            g.drawString(fileLabel, (int) (fileLabelSquareBounds.x + (0.13 * offset)), (int) (fileLabelSquareBounds.y + (offset * 1.25)));
            g.setColor(rankLabelSquare.getCurrentComplementaryColor());
            g.drawString(rankLabel, (int) (rankLabelSquareBounds.x + (offset * 1.1)), (int) (rankLabelSquareBounds.y + (0.4 * offset)));
        }
        g.setFont(current);
    }

    private static BufferedImage flipImageVertically(BufferedImage img, Graphics2D g2d) {
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

    public static void repaint() {
        mainPanel.repaint();
    }

    // Modify summonEndGamePanel
    public static void summonEndGamePanel(String title, String subtitle) {
        endGamePanelShouldBeShown = true;
        endGamePanel.setTitle(title);
        endGamePanel.setSubtitle(subtitle);
        mainPanel.repaint();
    }

    private static void resizeSquares(JPanel panel) { // TODO remove this method
        Rectangle board = getBoardBounds();
        int squareSize = Math.min(board.width, board.height) / 8;

        for (int i = 0; i < 64; i++) {
            int row = i / 8;
            int col = i % 8;
            Component square = panel.getComponent(i);
            square.setBounds(board.x + col * squareSize, board.y + row * squareSize, squareSize, squareSize);
        }

        // Repaint only the chessboard area
        panel.repaint(board.x, board.y, board.width, board.height);
    }

    public static Rectangle getSquareBounds(Square s) {
        Rectangle board = getBoardBounds();
        int squareSize = Math.min(board.width, board.height) / 8; // TODO can we make this simpler by assuming the board is always a square?
        int row = s.index / 8;
        int col = s.index % 8;
        return new Rectangle(board.x + col * squareSize, board.y + row * squareSize, squareSize, squareSize);
    }

    public static int getSquareSize() {
        Rectangle board = getBoardBounds();
        return Math.min(board.width, board.height) / 8;
    }
    //endregion

    public static Rectangle getBoardBounds() {
        int width = mainPanel.getWidth();
        int height = mainPanel.getHeight();
        int size = Math.min(width, height) / 8;

        int startX = (width - size * 8) / 2;
        int startY = (height - size * 8) / 2;

        return new Rectangle(startX, startY, size * 8, size * 8);
    }
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

    /**
     * Mixes color2 into color1, as if color2 were laid on top of color1.
     * Will yield color2 if color2 alpha is 1
     * Modifies color1
     */
    public static Color mix(Color color1, Color color2) {
        float alpha2 = color2.getAlpha() / 255.0f;

        // Ensure final alpha is always 1
        float combinedAlpha = 1.0f;

        int red = (int) (color1.getRed() * (1 - alpha2) + color2.getRed() * alpha2);
        int green = (int) (color1.getGreen() * (1 - alpha2) + color2.getGreen() * alpha2);
        int blue = (int) (color1.getBlue() * (1 - alpha2) + color2.getBlue() * alpha2);

        return new Color(red, green, blue, (int) (combinedAlpha * 255));
    }

}