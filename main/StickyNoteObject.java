import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StickyNoteObject extends JPanel {

    private JTextField textField;
    private Point initialClick;

    public StickyNoteObject(int xPos, int yPos, int width, int length, Color color) {
        setBackground(color);
        setBounds(xPos, yPos, width, length);
        setLayout(null);

        // Create a JTextField instead of a JTextArea
        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setBackground(color);
        textField.setBounds(5, 5, width - 10, length - 10);
        textField.setBorder(null);  // Remove border for a cleaner look
        textField.setHorizontalAlignment(JTextField.CENTER); // Center text
        add(textField);

        // Enable dragging
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                textField.requestFocus(); // Ensure text input still works
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null) {
                    int thisX = getX();
                    int thisY = getY();
                    int deltaX = e.getX() - initialClick.x;
                    int deltaY = e.getY() - initialClick.y;
                    setLocation(thisX + deltaX, thisY + deltaY);
                }
            }
        });
    }
}