import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

/* 
TODO
-Add an option to re-color each note so there can be distinction between them when zoomed out.
-Add a title option for each note (the outlook board had this option, I just found it sort of nice.)
-Add the functionality for actually creating new lines inside the note, example:

--------------------------------------------------------------------
|this is a sticky note                                             |
|-I don't want to rely on the size of the note to denote new lines |
|-Because I want to show things on multiple lines IF I WANT TO!!!  |
--------------------------------------------------------------------

--------BUGS----------
-There is no way to effectively make a blank sticky note after it has been
written in. If you try and backspace all the text in it and then hit enter
the note will not be overwritten with the blank input but will instead
stay the same. 
- this is fixed now I think, but i'll leave this here as a reminder to check this.

*/

/**
 * StickyNoteObject represents a resizable and draggable component that allows
 * users to input and display text. The text can be edited by clicking inside the note,
 * resizing is done by clicking and dragging the bottom left corner of the note,
 * moving the note is done by clicking an edge and draggin the note to a new location,
 * and deleting the note is done by clicking the top right corner of the note.
 * @author Aaron Cherney
 * @version 1.0
 */
public class WhiteBoardObject extends JPanel {
    private JTextField textField;
    private JLabel displayLabel;
    private Point initialClick;
    private boolean isMoving;
    private static final int RESIZE_MARGIN = 10;
    private static final int DELETE_MARGIN = 10;
    private static final Color RESIZE_COLOR = Color.GRAY;
    private int originalWidth;
    private int originalHeight;
    private double scale = 1.0;
    private int ARC_RADIUS = 10;

    private BufferedImage canvas;
    private Graphics2D g2d;
    private int prevX, prevY;  // Stores the last position of the mouse
    private boolean drawing = false; // Track if the user is drawing
    private int canvasWidth; 
    private int canvasHeight; 

    public WhiteBoardObject(int xPos, int yPos,int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        canvasWidth = width - 10;
        canvasHeight = height - 10;
        setBackground(Color.WHITE);
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
                if (drawing && !isMoving) {
                    int x = e.getX();
                    int y = e.getY();
                    
                    // Draw a line from the previous point to the new one
                    g2d.drawLine(prevX, prevY, x, y);
                    prevX = x;
                    prevY = y;

                    repaint(); // Refresh the panel to show the new drawing
                }else{
                    int thisX = getX();
                    int thisY = getY();
                    int deltaX = e.getX() - initialClick.x;
                    int deltaY = e.getY() - initialClick.y;
                    setLocation(thisX + deltaX, thisY + deltaY);
                }
                
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();

                if(isInDeleteZone(e.getPoint())){
                    delete();
                }else if(isInMoveZone(e.getPoint())){
                    isMoving = true;
                }else{
                    isMoving = false;
                }

                
            }
        });

        

    }


    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public void setScale(double newScale) {
        this.scale = newScale;
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
    
        setBounds(getX(), getY(), newWidth, newHeight);
        
        // Scale the drawing canvas
        BufferedImage newCanvas = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D newG2d = newCanvas.createGraphics();
        newG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
        // Scale the previous drawing
        newG2d.drawImage(canvas, 0, 0, newWidth, newHeight, null);
        newG2d.setStroke(new BasicStroke((float) (2 * scale))); // Scale stroke thickness
    
        canvas = newCanvas;
        g2d = newG2d;
    
        repaint();
    }

    //Literally does nothing yet
    private void updateTextStyle() {
        int fontSize = Math.max(1, (int) Math.round(12 * scale));
    }

    private boolean isInMoveZone(Point p) {
        int w = getWidth();
        int h = getHeight();
        return (p.x >= w - RESIZE_MARGIN && p.y >= h - RESIZE_MARGIN);
    }

    private boolean isInDeleteZone(Point p) {
        int w = getWidth();
        return (p.x >= w - DELETE_MARGIN && p.y <= DELETE_MARGIN);
    }

    
    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rounded rectangle background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_RADIUS, ARC_RADIUS);

        // Draw resize box
        g2.setColor(RESIZE_COLOR);
        g2.fillRoundRect(getWidth() - RESIZE_MARGIN, getHeight() - RESIZE_MARGIN, RESIZE_MARGIN, RESIZE_MARGIN, ARC_RADIUS, ARC_RADIUS);

        // Draw delete box
        g2.setColor(Color.RED);
        g2.fillRoundRect(getWidth() - DELETE_MARGIN, 0, DELETE_MARGIN, DELETE_MARGIN, ARC_RADIUS, ARC_RADIUS);

        // Calculate center position
        int x = (getWidth() - canvasWidth) / 2;
        int y = (getHeight() - canvasHeight) / 2;

        g.drawImage(canvas, x, y, null); // Draw the stored canvas onto the panel
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.GRAY);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC_RADIUS, ARC_RADIUS);
    }

    public void repaintInside() {
        updateTextStyle();
        repaint();
    }

    //get a click in top right corner and delete all components inside and then delete the sticky note....
    public void delete(){
        Container parent = getParent();
        if (parent instanceof CanvasPanel) {
            ((CanvasPanel) parent).removeWhiteBoardObject(this); // Notify CanvasPanel
        }
    }

}
