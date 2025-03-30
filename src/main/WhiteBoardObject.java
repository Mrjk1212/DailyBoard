import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * WhiteBoardObject represents a resizable and draggable component that allows
 * users to draw on a canvas. The component can be moved, resized, deleted,
 * and properly scales its content when zoomed.
 * @author Aaron Cherney
 * @version 1.1
 */
public class WhiteBoardObject extends JPanel {
    private Point initialClick;
    private boolean isMoving;
    private static final int RESIZE_MARGIN = 10;
    private static final int DELETE_MARGIN = 10;
    private static final Color RESIZE_COLOR = Color.GRAY;
    private int originalWidth;
    private int originalHeight;
    private double scale = 1.0;
    private int ARC_RADIUS = 10;

    private BufferedImage originalCanvas; // Stores the original unscaled drawing
    private BufferedImage displayCanvas;  // The scaled canvas for display
    private Graphics2D g2d;
    private int prevX, prevY;
    private boolean drawing = false;
    private int canvasWidth; 
    private int canvasHeight;
    private float strokeWidth = 2.0f; // Default stroke width 
    private String imageLocation;

    public WhiteBoardObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        canvasWidth = width - 10;
        canvasHeight = height - 10;
        setBackground(Color.WHITE);
        setOpaque(false);
        setBounds(xPos, yPos, width, height);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(null);

        // Create the original canvas at full resolution
        originalCanvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        g2d = originalCanvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.setColor(Color.BLACK);
        
        // Initialize the display canvas
        displayCanvas = originalCanvas;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();

                if (isInDeleteZone(e.getPoint())) {
                    delete();
                } else if (isInMoveZone(e.getPoint())) {
                    isMoving = true;
                } else {
                    isMoving = false;
                    // Start drawing
                    drawing = true;
                    // Convert coordinates based on scale and panel positioning
                    prevX = (int)((e.getX() - (getWidth() - canvasWidth * scale) / 2) / scale);
                    prevY = (int)((e.getY() - (getHeight() - canvasHeight * scale) / 2) / scale);
                    
                    // Ensure we're within the canvas bounds
                    prevX = Math.max(0, Math.min(prevX, canvasWidth - 1));
                    prevY = Math.max(0, Math.min(prevY, canvasHeight - 1));
                }
            } 

            @Override
            public void mouseReleased(MouseEvent e) {
                drawing = false;
                isMoving = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawing && !isMoving) {
                    // Convert coordinates based on scale and panel positioning
                    int x = (int)((e.getX() - (getWidth() - canvasWidth * scale) / 2) / scale);
                    int y = (int)((e.getY() - (getHeight() - canvasHeight * scale) / 2) / scale);
                    
                    // Ensure we're within the canvas bounds
                    x = Math.max(0, Math.min(x, canvasWidth - 1));
                    y = Math.max(0, Math.min(y, canvasHeight - 1));
                    
                    // Draw on the original canvas
                    g2d.drawLine(prevX, prevY, x, y);
                    prevX = x;
                    prevY = y;
                    
                    // Update the display canvas
                    updateDisplayCanvas();
                    repaint();
                } else if (isMoving) {
                    int thisX = getX();
                    int thisY = getY();
                    int deltaX = e.getX() - initialClick.x;
                    int deltaY = e.getY() - initialClick.y;
                    setLocation(thisX + deltaX, thisY + deltaY);
                }
            }
        });
    }

    /**
     * Updates the display canvas based on the original canvas and current scale.
     */
    private void updateDisplayCanvas() {
        int scaledWidth = (int)(canvasWidth * scale);
        int scaledHeight = (int)(canvasHeight * scale);
        
        displayCanvas = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = displayCanvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalCanvas, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public void setOriginalWidth(int newWidth){
        originalWidth = newWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(int newHeight){
        originalHeight = newHeight;
    }

    public void setScale(double newScale) {
        // Only update if scale has changed
        if (this.scale != newScale) {
            this.scale = newScale;
            
            int newWidth = (int)(originalWidth * scale);
            int newHeight = (int)(originalHeight * scale);
            
            setBounds(getX(), getY(), newWidth, newHeight);
            
            // Update the display canvas with the new scale
            updateDisplayCanvas();
            
            repaint();
        }
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

        // Calculate center position for the canvas
        int x = (getWidth() - displayCanvas.getWidth()) / 2;
        int y = (getHeight() - displayCanvas.getHeight()) / 2;

        // Draw the scaled canvas
        g.drawImage(displayCanvas, x, y, null);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.GRAY);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC_RADIUS, ARC_RADIUS);
    }

    public void repaintInside() {
        // Update the display canvas and repaint
        updateDisplayCanvas();
        repaint();
    }

    public void saveImage(){
        String filePath = "whiteBoard.png";// Add with static number increasing everytime constuctor is run.
        File outputFile = new File(filePath);
        try {
            ImageIO.write(originalCanvas, "png", outputFile);
            System.out.println("Image saved successfully to: " + filePath);
            imageLocation = filePath;
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }

    public String getImageLocation(){
        return this.imageLocation;
    }

    public void loadImage(String savedImageLocation){
        //set buffered image to image at the saved Image Location
        if(savedImageLocation != null){
            try {
                // Specify the path to the PNG file
                File pngFile = new File(savedImageLocation);

                // Read the PNG file into a BufferedImage
                originalCanvas = ImageIO.read(pngFile);

                // Check if the image was loaded successfully
                if (originalCanvas != null) {
                    System.out.println("PNG image loaded successfully!");
                    // Update the display canvas with the new scale
                    updateDisplayCanvas();
                    // You can now work with the bufferedImage
                } else {
                    System.err.println("Failed to load PNG image. Check the file path.");
                }
            } catch (IOException e) {
                System.err.println("An error occurred while loading the image: " + e.getMessage());
            }
        }
    }

    public void delete() {
        Container parent = getParent();
        if (parent instanceof CanvasPanel) {
            ((CanvasPanel) parent).removeWhiteBoardObject(this);
        }
    }

    /**
     * Changes the stroke width for drawing
     * @param width The new stroke width
     */
    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        g2d.setStroke(new BasicStroke(strokeWidth));
    }
    
    /**
     * Changes the drawing color
     * @param color The new color
     */
    public void setDrawingColor(Color color) {
        g2d.setColor(color);
    }
}