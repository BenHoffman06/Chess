import javax.swing.*;
import java.awt.*;

public class Main {

    private static final Color BACKGROUND_COLOR = Color.decode("#161512");
    private static final Color SQUARE_COLOR1 = Color.decode("#f0d9b5");
    private static final Color SQUARE_COLOR2 = Color.decode("#b58863");



    static class Square extends JPanel {

        private Color color;

        public Square(Color color) {
            this.color = color;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(color);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static JFrame handleGUI() {
        JFrame frame = new JFrame("Chess");
        JPanel mainPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BACKGROUND_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 550);
        frame.setLocationRelativeTo(null);

        for (int i = 0; i < 64; i++) {
            Color color = (i + i / 8) % 2 == 0 ? SQUARE_COLOR1 : SQUARE_COLOR2;
            Square square = new Square(color);
            mainPanel.add(square);

        }

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                resizeSquares(mainPanel);
            }
        });

        frame.setVisible(true);

        resizeSquares((JPanel) frame.getContentPane());

        return frame;
    }

    // TODO make board
//    public static class Board {
//        public Square[] squares = new Square[64];
//
//        public void addSquare(Square s) {
//            s
//        }
//    }

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

    public static void main(String[] args) {
        handleGUI();


    }
}