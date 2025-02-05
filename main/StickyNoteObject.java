import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;


public class StickyNoteObject extends MoveableObject {
    private String text = "";
    private boolean selected = false;
    private JTextField textField;
    private final int MIN_WIDTH = 30;
    private final int MIN_HEIGHT = 30;
    private JPanel parentPanel; // Store reference to parent panel for repainting

    public StickyNoteObject(int x, int y, int width, int height, Color color, JPanel parentPanel) {
        super(x, y, width, height, color);
        this.parentPanel = parentPanel;

        // Create and configure the text field
        textField = new JTextField();
        textField.setBounds(x + 5, y + 5, width - 10, height - 10);
        textField.setBackground(Color.YELLOW.brighter());
        textField.setVisible(false);  // Initially hidden

        // Save text on Enter
        textField.addActionListener(e -> saveText());

        // Hide the text field when focus is lost
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                saveText();
            }
        });

        // Add mouse listener to make the text field editable when clicked
        parentPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (contains(e.getPoint())) {
                    setSelected(true);
                } else {
                    setSelected(false);
                }
            }
        });

        parentPanel.add(textField);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            textField.setText(text);
            textField.setBounds(getX() + 5, getY() + 5, getWidth() - 10, getHeight() - 10);
            textField.setVisible(true);
            textField.requestFocus();
        } else {
            saveText();
        }
    }

    private void saveText() {
        text = textField.getText();
        textField.setVisible(false);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        g.setColor(Color.BLACK);

        int padding = 5;
        int maxWidth = getWidth() - (2 * padding);
        int maxHeight = getHeight() - (2 * padding);

        if (text.isEmpty()) return; // No need to draw anything if text is empty

        // Get best font size that fits
        Font font = getFittingFont(g, text, maxWidth, maxHeight);
        g.setFont(font);

        // Split text into lines that fit within the width
        FontMetrics metrics = g.getFontMetrics(font);
        java.util.List<String> lines = wrapText(text, metrics, maxWidth);

        // Start drawing from the upper-left corner
        int y = getY() + padding + metrics.getAscent();

        for (String line : lines) {
            g.drawString(line, getX() + padding, y);
            y += metrics.getHeight(); // Move down for the next line
        }
    }

private Font getFittingFont(Graphics g, String text, int maxWidth, int maxHeight) {
    int fontSize = 20; // Start with a reasonable size
    Font font = new Font("Arial", Font.PLAIN, fontSize);

    return new Font("Arial", Font.PLAIN, fontSize); // Step back to last fitting size
}

private java.util.List<String> wrapText(String text, FontMetrics metrics, int maxWidth) {
    java.util.List<String> lines = new java.util.ArrayList<>();
    String[] words = text.split(" ");
    StringBuilder currentLine = new StringBuilder();

    for (String word : words) {
        String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
        if (metrics.stringWidth(testLine) > maxWidth) {
            lines.add(currentLine.toString());
            currentLine = new StringBuilder(word);
        } else {
            currentLine.append(currentLine.isEmpty() ? word : " " + word);
        }
    }
    
    if (!currentLine.isEmpty()) {
        lines.add(currentLine.toString());
    }

    return lines;
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
        // Move the text field along with the sticky note
        textField.setBounds(getX() + 5, getY() + 5, getWidth() - 10, getHeight() - 10);

        // Force repaint to remove ghosting
        parentPanel.repaint();
    }

public void setWidth(int width) {
    if (width >= MIN_WIDTH) {
        super.setWidth(width); // Ensure parent class has a valid method
    } else {
        super.setWidth(MIN_WIDTH); // Enforce minimum width
    }
}

public void setHeight(int height) {
    if (height >= MIN_HEIGHT) {
        super.setHeight(height); // Ensure parent class has a valid method
    } else {
        super.setHeight(MIN_HEIGHT); // Enforce minimum height
    }
}

    // 🔥 Destroy the object properly
    public void destroy() {
        parentPanel.remove(textField); // Remove text field from panel
        parentPanel.repaint();         // Refresh UI to remove artifacts
    }
}
