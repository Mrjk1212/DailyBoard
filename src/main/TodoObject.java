import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/* 
TODO
--------LIST NAME----------
--------Add Item-----------
----------Item-----------X-
----------Item-----------X-
----------Item-----------X-
----------Item-----------X-
---------------------------

list name = Jtext field over a JLabel for changing the name of the list
Add item = adds an item to the task list array and adds the item into a JList
Item = String data in the
X = Completed/Checkmark. aka removes item from list when clicked

*/

/**
 * TodoObject represents a resizable and draggable component that allows
 * users to input and check off items on a todo list.
 * resizing is done by clicking and dragging the bottom left corner of the note,
 * moving the list is done by clicking an edge and draggin the list to a new location,
 * and deleting the list is done by clicking the top right corner of the list.
 * @author Aaron Cherney
 * @version 1.0
 */
public class TodoObject extends JPanel {
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
    private int ARC_RADIUS = 10;

    public TodoObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        setLayout(new MigLayout("", "[][][]", "[][]"));
        setBackground(color);
        setOpaque(false);
        setBounds(xPos, yPos, width, height);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        

        // Create a JLabel to display text
        displayLabel = new JLabel("");
        displayLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        //displayLabel.setBounds(5, 5, width - 10, height - 10);
        //displayLabel.setHorizontalAlignment(JLabel.LEFT);
        //displayLabel.setVerticalAlignment(JLabel.TOP);
        displayLabel.setBackground(Color.RED);
        displayLabel.setVisible(true);
        add(displayLabel, "span 3, wrap");

        // Create a JTextField for input
        textField = new JTextField();
        //textField.setBounds(5, 5, width - 10, height - 100);
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setBorder(null);
        textField.setVisible(true); // Initially hidden
        textField.setBackground(Color.BLUE);
        add(textField, "span 3");

 

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

    
    public String getText() { 
        return displayLabel.getText();
    }

    public void setText(String text) { 
        displayLabel.setText(text);
        updateTextStyle();
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
        //displayLabel.setBounds(5, 5, getWidth() - 10, getHeight() - 10);
        //textField.setBounds(5, 5, getWidth() - 10, getHeight() - 10);
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
            ((CanvasPanel) parent).removeTodoObject(this); // Notify CanvasPanel
        }
    }

}
