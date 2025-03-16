import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class WhiteBoardObject extends JPanel {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private int prevX, prevY;  // Stores the last position of the mouse
    private boolean drawing = false; // Track if the user is drawing

    public WhiteBoardObject(int xPos, int yPos,int width, int height, Color color) {
        
        setBackground(color);
        setOpaque(false);
        setBounds(xPos, yPos, width, height);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(null);

        // Create the canvas image where drawing will happen
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2)); // Set line thickness
        g2d.setColor(Color.BLACK); // Default drawing color

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                drawing = true;
                prevX = e.getX();
                prevY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drawing = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawing) {
                    int x = e.getX();
                    int y = e.getY();
                    
                    // Draw a line from the previous point to the new one
                    g2d.drawLine(prevX, prevY, x, y);
                    prevX = x;
                    prevY = y;

                    repaint(); // Refresh the panel to show the new drawing
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, 0, 0, null); // Draw the stored canvas onto the panel
    }
}