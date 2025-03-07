package core;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EndGamePanel {
    private final Button rematchButton;
    private final Button exitButton;
    private final Button hideButton;
    private String title = "TITLE";
    private String subtitle = "SUBTITLE";
    private Rectangle panelBounds;

    public static class Button {
        private Rectangle bounds;
        private final BufferedImage normalImage;
        private final BufferedImage hoverImage;
        private boolean isHovered;

        public Button(Rectangle bounds, BufferedImage normalImage, BufferedImage hoverImage) {
            this.bounds = bounds;
            this.normalImage = normalImage;
            this.hoverImage = hoverImage;
            this.isHovered = false;
        }

        public void draw(Graphics2D g2d) {
            BufferedImage img = isHovered ? hoverImage : normalImage;
            g2d.drawImage(img, bounds.x, bounds.y, bounds.width, bounds.height, null);
        }

        public boolean contains(Point p) {
            return bounds.contains(p);
        }

        public void setBounds(Rectangle bounds) {
            this.bounds = bounds;
        }

        public void setHovered(boolean hovered) {
            isHovered = hovered;
        }
    }

    public EndGamePanel() {
        // Initialize buttons with placeholder bounds (updated when drawn)
        rematchButton = new Button(
                new Rectangle(0, 0, 1, 1),
                UI.pieceImages.get(UI.REMATCH_BUTTON),
                UI.pieceImages.get(UI.REMATCH_BUTTON_SELECTED)
        );

        exitButton = new Button(
                new Rectangle(0, 0, 1, 1),
                UI.pieceImages.get(UI.EXIT_BUTTON),
                UI.pieceImages.get(UI.EXIT_BUTTON_SELECTED)
        );

        hideButton = new Button(
                new Rectangle(0, 0, 1, 1),
                UI.pieceImages.get(UI.HIDE_BUTTON),
                UI.pieceImages.get(UI.HIDE_BUTTON_SELECTED)
        );
    }

    public void draw(Graphics2D g2d, int parentWidth, int parentHeight) {
        // Calculate panel dimensions based on chess square size
        int squareSize = Main.board.squares[0].getWidth();
        int panelWidth = (int)(squareSize * 3.25);
        int panelHeight = (int)(panelWidth * (334.0/323.0));
        int x = (parentWidth - panelWidth) / 2;
        int y = (parentHeight - panelHeight) / 2;
        panelBounds = new Rectangle(x, y, panelWidth, panelHeight);

        // Darken background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, parentWidth, parentHeight);

        // Draw base panel
        g2d.drawImage(UI.pieceImages.get(UI.END_PANEL_BASE), x, y, panelWidth, panelHeight, null);

        // Update button positions
        updateButtonBounds(x, y, panelWidth, panelHeight);

        // Draw buttons
        rematchButton.draw(g2d);
        exitButton.draw(g2d);
        hideButton.draw(g2d);

        // Draw text
        drawText(g2d, panelWidth, panelHeight, x, y);
    }

    private void updateButtonBounds(int panelX, int panelY, int panelWidth, int panelHeight) {
        // Rematch button bounds
        Rectangle rematchBounds = new Rectangle(
                panelX + (int)(27.0/323.0 * panelWidth),
                panelY + (int)(164.0/334.0 * panelHeight),
                (int)(269.0/323.0 * panelWidth),
                (int)(64.0/334.0 * panelHeight)
        );
        rematchButton.setBounds(rematchBounds);

        // Exit button bounds
        Rectangle exitBounds = new Rectangle(
                rematchBounds.x,
                panelY + (int)(238.0/334.0 * panelHeight),
                rematchBounds.width,
                rematchBounds.height
        );
        exitButton.setBounds(exitBounds);

        // Hide button bounds
        Rectangle hideBounds = new Rectangle(
                panelX + (int)(295.0/323.0 * panelWidth),
                panelY + (int)(16.0/334.0 * panelHeight),
                (int)(16.0/323.0 * panelWidth),
                (int)(16.0/323.0 * panelWidth)
        );
        hideButton.setBounds(hideBounds);
    }

    private void drawText(Graphics2D g2d, int panelWidth, int panelHeight, int x, int y) {
        // Calculate font sizes based on panel height
        int titleFontSize = (int)(panelHeight * (24.0/334.0));
        int subTitleFontSize = (int)(panelHeight * (14.0/334.0));

        // Draw title
        g2d.setFont(new Font("Arial", Font.BOLD, titleFontSize));
        drawCenteredString(g2d, title,
                new Rectangle(x, y + (int)(panelHeight * 0.06), panelWidth, (int)(panelHeight * 0.09)));

        // Draw subtitle
        g2d.setFont(new Font("Arial", Font.PLAIN, subTitleFontSize));
        drawCenteredString(g2d, subtitle,
                new Rectangle(x, y + (int)(panelHeight * 0.15), panelWidth, (int)(panelHeight * 0.06)));
    }

    private void drawCenteredString(Graphics g, String text, Rectangle rect) {
        FontMetrics metrics = g.getFontMetrics();
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    public void handleMouseMove(Point mousePos) {
        rematchButton.setHovered(rematchButton.contains(mousePos));
        exitButton.setHovered(exitButton.contains(mousePos));
        hideButton.setHovered(hideButton.contains(mousePos));
    }

    public void handleMouseClick(Point mousePos) {
        if (rematchButton.contains(mousePos)) {
            Main.rematch();
        } else if (exitButton.contains(mousePos)) {
            System.exit(0);
        } else if (hideButton.contains(mousePos)) {
            UI.endGamePanelShouldBeShown = false;
            UI.repaint();
        }
    }

    // Setters for text content
    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}