import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.swing.*;
import main.customComponents.*;

import net.miginfocom.swing.MigLayout;
import java.util.Date;
import java.util.Calendar;


/* 
TODO
-
--------BUGS BELOW----------
-

*/

/**
 * Goal represents a resizable and draggable component that allows
 * users to input and display text with an end date. The text can be edited by clicking inside the note,
 * resizing is done by clicking and dragging the bottom left corner of the note,
 * moving the note is done by clicking an edge and draggin the goal to a new location,
 * and deleting the note is done by clicking the top right corner of the goal.
 * @author Aaron Cherney
 * @version 1.0
 */
public class GoalObject extends JPanel {
    private JTextField titleField;
    private JTextArea textField;
    private JButton settingsButton;
    private RoundedButton dateButton;
    private JLabel daysRemainingLabel;
    private JSpinner dateJSpinner;
    private JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
    private Point initialClick;
    private boolean isResizing;
    private static final int RESIZE_MARGIN = 10;
    private static final Color RESIZE_COLOR = Color.GRAY;
    private int originalWidth;
    private int originalHeight;
    private double scale = 1.0;
    private int ARC_RADIUS = 10;
    private Date selectedDate;

    public GoalObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        setBackground(color);
        setOpaque(false);
        setBounds(xPos, yPos, width, height);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setLayout(new MigLayout("", "[grow, fill][][][]", ""));

        // Date picker
        dateButton = new RoundedButton(100, 30, "Select Date");
        dateButton.addActionListener(e -> showDatePicker());
        dateButton.setBackground(getBackground());
        dateButton.setForeground(getContrastColor(getBackground()));
        add(dateButton, "span 1, gapright push");

        // Label to display days remaining
        daysRemainingLabel = new JLabel("- Days");
        daysRemainingLabel.setBackground(getBackground());
        daysRemainingLabel.setForeground(getContrastColor(getBackground()));
        daysRemainingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(daysRemainingLabel, "gapleft push, span 1, wrap");

        final JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem(new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e){
                delete();
            }
        }));
        popup.add(new JMenuItem(new AbstractAction("Color") {
            public void actionPerformed(ActionEvent e){
                Color choosedColor = JColorChooser.showDialog(textField.getParent(), "Choose JPanel Background Color", color);
                    
                    if(choosedColor != null){
                        setBackground(choosedColor);
                        sep.setBackground(choosedColor.darker());
                        sep.setForeground(choosedColor);
                        textField.setBackground(choosedColor);
                        titleField.setBackground(choosedColor);
                        daysRemainingLabel.setBackground(choosedColor);
                        dateButton.setBackground(choosedColor);
                        //Set everything to have higher contrast with new selected color.
                        // Adjust text color for readability
                        dateButton.setForeground(getContrastColor(choosedColor));
                        textField.setForeground(getContrastColor(choosedColor));
                        titleField.setForeground(getContrastColor(choosedColor));
                        settingsButton.setForeground(getContrastColor(choosedColor));
                        daysRemainingLabel.setForeground(getContrastColor(choosedColor));
                    }
            }
        }));

        // Create a JTextField for input
        titleField = new JTextField();
        titleField.setFont(new Font("Arial", Font.BOLD, 14));
        titleField.setBorder(null);
        titleField.setVisible(true);
        titleField.setBackground(getBackground());
        titleField.setForeground(getContrastColor(getBackground()));
        titleField.setText("Example Goal");
        titleField.setHorizontalAlignment(JTextField.LEFT);
                
        add(titleField, "span 2");

        // Focus listener to update label when user clicks away
        titleField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                saveText();
            }
        });

        // Key listener to save text on Enter key press
        titleField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveText();
                }
            }
        });


        settingsButton = new JButton();
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

        
        add(sep, "span");
        sep.setBackground(getBackground().brighter());
        sep.setForeground(getBackground().darker());

        // Create a JTextField for input
        textField = new JTextArea();
        textField.setBounds(5, 5, width - 10, height - 10);
        textField.setFont(new Font("Arial", Font.BOLD, 12));
        textField.setBorder(null);
        textField.setEditable(false);
        textField.setVisible(true);
        textField.setBackground(getBackground());
        textField.setForeground(getContrastColor(getBackground()));
        textField.setLineWrap(true);
        textField.setWrapStyleWord(true);
        textField.setOpaque(false);
        add(textField,"span 3");

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

    } // End Constructor


    // Function to show date picker
    private void showDatePicker() {
        // Use JSpinner with Date Model
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);

        int option = JOptionPane.showConfirmDialog(this, spinner, "Select Date", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            selectedDate = (Date) spinner.getValue();

            updateDaysRemaining(selectedDate);
        }
    }

    // Function to update label with days remaining
    private void updateDaysRemaining(Date localSelectedDate) {

        LocalDate today = LocalDate.now();
        LocalDate targetDate = new java.sql.Date(localSelectedDate.getTime()).toLocalDate();
        long daysLeft = ChronoUnit.DAYS.between(today, targetDate);

        daysRemainingLabel.setText(daysLeft + "\n" + " Days");
        textField.setText("Due: " + targetDate);
    }

    public Color getContrastColor(Color newColor){
        double brightness = (0.299 * newColor.getRed()) + (0.587 * newColor.getGreen()) + (0.114 * newColor.getBlue());
        Color contrastColor = (brightness > 128) ? Color.BLACK : Color.WHITE;
        return contrastColor;
    }

    public void setDate(Date savedDate){
        selectedDate = savedDate;
        updateDaysRemaining(selectedDate);
    }

    public Date getDate(){
        return selectedDate;
    }

    private void saveText() {
        String text = textField.getText().trim();
        if (!text.isEmpty()) {
            textField.setText(text);
        }
        String titleText = titleField.getText().trim();
        if (!titleText.isEmpty()) {
            titleField.setText(titleText);
        }
    }

    public String getText() { 
        return textField.getText();
    }

    public String getTitle() { 
        return titleField.getText();
    }

    public void setTitle(String newTitle){
        titleField.setText(newTitle);
    }

    public void setText(String text) { 
        textField.setText(text);
        updateTextStyle();
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(int newOriginalHeight){
        this.originalHeight = newOriginalHeight;
    }

    public void setOriginalWidth(int newOriginalWidth){
        this.originalWidth = newOriginalWidth;
    }

    public void setScale(double newScale) {
        this.scale = newScale;
        updateTextStyle();
    }

    private void updateTextStyle() {
        int fontSize = Math.max(1, (int) Math.round(12 * scale));
        int biggerFontSize = Math.max(1, (int) Math.round(14 * scale));
        textField.setFont(new Font("Arial", Font.BOLD, fontSize));
        textField.setBounds(5, 5, getWidth() - 10, getHeight() - 10);
        titleField.setFont(new Font("Arial", Font.BOLD, biggerFontSize));
        dateButton.setFont(new Font("Arial", Font.BOLD, fontSize));
        daysRemainingLabel.setFont(new Font("Arial", Font.BOLD, biggerFontSize));
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
            ((CanvasPanel) parent).removeGoal(this); // Notify CanvasPanel
        }
    }

}
