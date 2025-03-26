import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class RoundedButton extends JButton {

    private boolean mouseOver = false;
    private boolean mousePressed = false;

    public RoundedButton(int width, int height, String text) {
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setPreferredSize(new Dimension(width, height));
        setText(text);
        
        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (contains(me.getX(), me.getY())) {
                    mousePressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                mousePressed = false;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent me) {
                mouseOver = false;
                mousePressed = false;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent me) {
                mouseOver = contains(me.getX(), me.getY());
                repaint();
            }
        };

        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
    }

    public Color getContrastColor(Color newColor){
        double brightness = (0.299 * newColor.getRed()) + (0.587 * newColor.getGreen()) + (0.114 * newColor.getBlue());
        Color contrastColor = (brightness > 128) ? Color.BLACK : Color.WHITE;
        return contrastColor;
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Determine button color based on state
        Color backgroundColor = mousePressed ? getBackground() : getBackground();
        Color borderColor = mouseOver ? Color.BLUE : getContrastColor(getBackground());

        // Fill rounded rectangle
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        // Draw border
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        // Draw text in the center
        g2.setColor(getContrastColor(getBackground()));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(getText());
        int textHeight = fm.getAscent();
        int x = (getWidth() - textWidth) / 2;
        int y = (getHeight() + textHeight) / 2 - 2;
        g2.drawString(getText(), x, y);
    }
}