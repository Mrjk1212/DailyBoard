import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StickyNoteObject extends JPanel {
    private int originalX, originalY, originalWidth, originalHeight;
    private JTextField textField;
    private Point initialClick;

    public StickyNoteObject(int xPos, int yPos, int width, int height, Color color) {
        this.originalX = xPos;
        this.originalY = yPos;
        this.originalWidth = width;
        this.originalHeight = height;

        setBackground(color);
        setBounds(xPos, yPos, width, height);
        setLayout(null);

        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setBackground(color);
        textField.setBounds(5, 5, width - 10, height - 10);
        textField.setBorder(null);
        textField.setHorizontalAlignment(JTextField.CENTER);
        add(textField);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                textField.requestFocus();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null) {
                    int newX = getX() + e.getX() - initialClick.x;
                    int newY = getY() + e.getY() - initialClick.y;
                    setLocation(newX, newY);
                }
            }
        });
    }

    public int getOriginalX() { return originalX; }
    public int getOriginalY() { return originalY; }
    public int getOriginalWidth() { return originalWidth; }
    public int getOriginalHeight() { return originalHeight; }

    public void updateOriginalPosition() {
        this.originalX = getX();
        this.originalY = getY();
    }

}