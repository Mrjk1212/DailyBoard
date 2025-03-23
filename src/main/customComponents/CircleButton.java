import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class CircleButton extends JButton {

    private boolean mouseOver = false;
    private boolean mousePressed = false;

    public CircleButton(int diameter) {
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setPreferredSize(new Dimension(diameter, diameter));

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

    private int getDiameter() {
        return Math.min(getWidth(), getHeight());
    }

    @Override
    public boolean contains(int x, int y) {
        int radius = getDiameter() / 2;
        return Point2D.distance(x, y, getWidth() / 2, getHeight() / 2) < radius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int diameter = getDiameter();
        int radius = diameter / 2;

        g.setColor(mousePressed ? Color.LIGHT_GRAY : Color.WHITE);
        g.fillOval(getWidth() / 2 - radius, getHeight() / 2 - radius, diameter - 2, diameter - 2);

        g.setColor(mouseOver ? Color.BLUE : Color.BLACK);
        g.drawOval(getWidth() / 2 - radius, getHeight() / 2 - radius, diameter - 2, diameter - 2);
    }
}