import java.awt.*;
import java.awt.event.*;
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
public class StickyNoteObject extends JPanel {
    private JTextField textField;
    private JLabel displayLabel;
    private Point initialClick;
    private boolean isResizing;
    private static final int RESIZE_MARGIN = 10;
    private static final int DELETE_MARGIN = 10;
    private static final Color RESIZE_COLOR = Color.GRAY;
    private int originalWidth;
    private int originalHeight;
    private double scale = 1.0;

    public StickyNoteObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        setBackground(color);
        setBounds(xPos, yPos, width, height);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setLayout(null);

        // Create a JLabel to display text
        displayLabel = new JLabel("", SwingConstants.CENTER);
        displayLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        displayLabel.setBounds(5, 5, width - 10, height - 10);
        displayLabel.setHorizontalAlignment(JLabel.LEFT);
        displayLabel.setVerticalAlignment(JLabel.TOP);
        add(displayLabel);

        // Create a JTextField for input
        textField = new JTextField();
        textField.setBounds(5, 5, width - 10, height - 10);
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setBorder(null);
        textField.setVisible(false); // Initially hidden
        textField.setBackground(color);
        add(textField);

        // Focus listener to update label when user clicks away
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                saveText();
            }
        });

        // Key listener to save text on Enter key press
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveText();
                }
            }
        });

        // Click event to allow text editing
        displayLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                enterEditMode();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();

                if (isInResizeZone(e.getPoint())) {
                    isResizing = true;
                } 
                else if(isInDeleteZone(e.getPoint())){
                    delete();
                }
                else {
                    isResizing = false;
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick == null) return;

                if (isResizing) {
                    int newWidth = e.getX();
                    int newHeight = e.getY();
                    originalWidth = (int)(Math.max(newWidth, 50) / scale);
                    originalHeight = (int)(Math.max(newHeight, 50) / scale);
                    setSize(Math.max(newWidth, 50), Math.max(newHeight, 50));
                    updateTextStyle();
                } else {
                    int thisX = getX();
                    int thisY = getY();
                    int deltaX = e.getX() - initialClick.x;
                    int deltaY = e.getY() - initialClick.y;
                    setLocation(thisX + deltaX, thisY + deltaY);
                }
            }
        });
    }

    private void saveText() {
        String text = textField.getText().trim();
        if (!text.isEmpty()) {
            displayLabel.setText("<html><body style='text-align:center'>" + text.replace("\n", "<br>") + "</body></html>");
        }
        textField.setVisible(false);
        displayLabel.setVisible(true);
    }

    private void enterEditMode() {
        textField.setText(displayLabel.getText().replaceAll("<[^>]*>", "")); // Remove HTML formatting
        textField.setVisible(true);
        displayLabel.setVisible(false);
        textField.requestFocus();
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public void setScale(double newScale) {
        this.scale = newScale;
        updateTextStyle();
    }

    private void updateTextStyle() {
        int fontSize = Math.max(1, (int) Math.round(12 * scale));
        displayLabel.setFont(new Font("Arial", Font.PLAIN, fontSize));
        textField.setFont(new Font("Arial", Font.PLAIN, fontSize));
        displayLabel.setBounds(5, 5, getWidth() - 10, getHeight() - 10);
        textField.setBounds(5, 5, getWidth() - 10, getHeight() - 10);
    }

    private boolean isInResizeZone(Point p) {
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
        g.setColor(RESIZE_COLOR);
        g.fillRect(getWidth() - RESIZE_MARGIN, getHeight() - RESIZE_MARGIN, RESIZE_MARGIN , RESIZE_MARGIN);
        g.setColor(Color.RED);
        g.fillRect(getWidth() - DELETE_MARGIN, getHeight() - getHeight(), DELETE_MARGIN, DELETE_MARGIN);
    }

    public void repaintInside() {
        updateTextStyle();
        repaint();
    }

    //get a click in top right corner and delete all components inside and then delete the sticky note....
    public void delete(){
        removeAll(); // remove all child components first just to be safe :)
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
            parent.revalidate();
            parent.repaint();
        }
    }

}
