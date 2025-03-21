import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

/* 
TODO
-The Title can be smaller font.
-Adjust Color of text to automatically set all text to be the correct either white or black for contrast.
-SAVE THE TITLE for use on reload.


--------BUGS----------
-The Title JTextField Background does not change when changing the notes color.

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
    private JTextField titleField;
    private JTextArea textField;
    private JLabel displayLabel;
    private JButton settingsButton;
    private Point initialClick;
    private boolean isResizing;
    private static final int RESIZE_MARGIN = 10;
    private static final int DELETE_MARGIN = 10;
    private static final Color RESIZE_COLOR = Color.GRAY;
    private int originalWidth;
    private int originalHeight;
    private double scale = 1.0;
    private int ARC_RADIUS = 10;

    public StickyNoteObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        setBackground(color);
        setOpaque(false);
        setBounds(xPos, yPos, width, height);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setLayout(new MigLayout("", "[grow, fill][][]", ""));

        final JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem(new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e){
                delete();
            }
        }));
        popup.add(new JMenuItem(new AbstractAction("Color") {
            public void actionPerformed(ActionEvent e){
                Color choosedColor = JColorChooser.showDialog(textField.getParent(), "Choose JPanel Background Color", color);
                    setBackground(choosedColor);
                    textField.setBackground(choosedColor);
                    titleField.setBackground(choosedColor);
            }
        }));

        // Create a JTextField for input
        titleField = new JTextField();
        titleField.setFont(new Font("Arial", Font.PLAIN, 14));
        titleField.setBorder(null);
        titleField.setVisible(true);
        titleField.setBackground(getBackground());
        titleField.setForeground(Color.BLACK);
        titleField.setHorizontalAlignment(JTextField.LEFT);
                
        add(titleField, "span 2");

        // Add an ActionListener to disable editing when Enter is pressed
        titleField.addActionListener(e -> {
            titleField.setEditable(false); // Disable editing
            titleField.setRequestFocusEnabled(false);
        });
        titleField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                titleField.setEditable(true); // Enable editing again
                titleField.setRequestFocusEnabled(true);
            }
        });


        final JButton settingsButton = new JButton();
        settingsButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e){
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        // Make the button transparent
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setOpaque(false);
        settingsButton.setText("...");
        add(settingsButton, "span 1, wrap");

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setBackground(getBackground().brighter());
        sep.setForeground(getBackground().darker());
        add(sep, "span");

        // Create a JLabel to display text
        displayLabel = new JLabel("", SwingConstants.CENTER);
        displayLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        displayLabel.setBounds(5, 5, width - 10, height - 10);
        displayLabel.setHorizontalAlignment(JLabel.LEFT);
        displayLabel.setVerticalAlignment(JLabel.TOP);
        add(displayLabel, "span");

        // Create a JTextField for input
        textField = new JTextArea();
        textField.setBounds(5, 5, width - 10, height - 10);
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setBorder(null);
        textField.setVisible(true);
        textField.setBackground(getBackground());
        textField.setForeground(Color.BLACK);
        textField.setLineWrap(true);
        textField.setWrapStyleWord(true);
        textField.setOpaque(false);
        add(textField,"span");

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
        else{
            displayLabel.setText("");
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
        textField.setText(displayLabel.getText().replaceAll("<br>", "\n").replaceAll("<[^>]*>", "")); // Remove HTML formatting
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
            ((CanvasPanel) parent).removeStickyNote(this); // Notify CanvasPanel
        }
    }

}
