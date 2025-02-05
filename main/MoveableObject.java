import java.awt.*;

public class MoveableObject {
    private int x;
    private int y;
    private int width;
    private int height;
    private Color color;

    public MoveableObject(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public MoveableObject(int x2, int y2, int width2, int height2, Color color2, String text) {
        //TODO Auto-generated constructor stub
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    public boolean contains(Point p) {
        return (p.x >= x && p.x <= x + width && p.y >= y && p.y <= y + height);
    }

    // New move method that handles resizing as well
    public void move(int dx, int dy, boolean resizing) {
        if (resizing) {
            // Update width and height for resizing
            this.width += dx;
            this.height += dy;
        } else {
            // Regular movement
            this.x += dx;
            this.y += dy;
        }
    }

    public void selected(Graphics g, boolean selectedFlag) {
        if (selectedFlag) {
            g.setColor(Color.BLACK);
            g.draw3DRect(x, y, width, height, true);
        } else {
            g.setColor(Color.BLACK);
            g.draw3DRect(x, y, width, height, false);
        }
    }

    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }


}