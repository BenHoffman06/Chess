package ui;

import core.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static core.Piece.*;

import static core.Piece.WHITE_PAWN;

public class BoardRenderer {
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
    public static Square redSquare;

    // Piece images
    public static final Map<Byte, BufferedImage> pieceImages = new HashMap<>(12);


    // core.Main panel
    public static JPanel mainPanel;

    static {
        // Load piece images into the dictionary
        ClassLoader classLoader = BoardRenderer.class.getClassLoader();
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

            // Non-pieces
            pieceImages.put(TAKEABLE_WHITE, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/takeable-white.png"))));
            pieceImages.put(TAKEABLE_BLACK, ImageIO.read(Objects.requireNonNull(classLoader.getResourceAsStream("images/takeable-black.png"))));



        } catch (IOException e) {
            throw new RuntimeException("Error loading piece images: " + e.getMessage(), e);
        }
    }
    //endregion
}