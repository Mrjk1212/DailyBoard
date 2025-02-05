import java.awt.*;


public class StickyNoteObject extends MoveableObject {
    private String text = "";
    private boolean isResizing = false;
    private Point lastDrag = null;

    public StickyNoteObject(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);
    }

    // Method to set the text of the sticky note
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        // Draw the text inside the sticky note
        g.setColor(Color.BLACK);
        g.drawString(text, getX() + 10, getY() + 20);  // Position the text with padding

        // Draw resize handle at the bottom-right corner
        g.setColor(Color.BLACK);
        g.fillRect(getX() + getWidth() - 10, getY() + getHeight() - 10, 10, 10);
    }

    // Method to check if a point is within the resizing corner
    public boolean isResizing(Point p) {
        int cornerX = this.getX() + this.getWidth() - 10;
        int cornerY = this.getY() + this.getHeight() - 10;
        return (p.x >= cornerX && p.x <= cornerX + 10 && p.y >= cornerY && p.y <= cornerY + 10);
    }

    // Override move method to include resizing functionality
    public void move(int dx, int dy, boolean resizing) {
        if (resizing) {
            // If resizing, adjust width and height
            setWidth(getWidth() + dx);
            setHeight(getHeight() + dy);
        } else {
            // Regular dragging
            super.move(dx, dy, false);
        }
    }

    // Getters and setters for width and height (needed for resizing)
    public void setWidth(int width) {
        if(getWidth() >= 30 && getHeight()  >= 30){
        super.setWidth(width); // Assuming MoveableObject has a setWidth method
        }
    }

    public void setHeight(int height) {
        if(getWidth() >= 30 && getHeight()  >= 30){
            super.setHeight(height); // Assuming MoveableObject has a setWidth method
            }
    }

    public String getText() {
        return text;
    }
}
