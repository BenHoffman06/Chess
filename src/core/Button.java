package core;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Button {
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

    public boolean isHovered() {
        return isHovered;
    }
}