import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import java.util.HashMap;
import java.util.Map;

import main.customComponents.*;

/* 
TODO
--------LIST NAME----------
--------Add Item-----------
----------Item-----------X-
----------Item-----------X-
----------Item-----------X-
----------Item-----------X-
---------------------------

list name = Jtext field for changing the name of the list
Add item = adds an item to the task list array and adds the item into the JPanel as a JLabel(span 2) + JButton("span 1,wrap")
Item = String data in the
X = Completed/Checkmark. aka removes item from list when clicked


------BUGS--------
Can still see cursor even after pressing enter after naming a task and the header of the list.
Need to preserve order of list on save and load.
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
    private JButton settingsButton;
    private JSeparator titleSep;
    private JButton addTaskButton;
    private Point initialClick;
    private boolean isResizing;
    private static final int RESIZE_MARGIN = 10;
    private static final Color RESIZE_COLOR = Color.GRAY;
    private int originalWidth;
    private int originalHeight;
    private double scale = 1.0;
    private int ARC_RADIUS = 10;
    private boolean isSelected = false;

    private Map<JTextField, CircleButton> todoDict = new HashMap<>();
    private List<JSeparator> sepList = new ArrayList<>();

    public TodoObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        setLayout(new MigLayout("", "[grow, fill][][]", ""));
        setBackground(color);
        setOpaque(false);
        setBounds(xPos, yPos, width, height);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // Create a JTextField for input
        textField = new JTextField("Todo List");
        textField.setFont(new Font("Arial", Font.BOLD, 16));
        textField.setBorder(null);
        textField.setVisible(true);
        textField.setBackground(getBackground());
        textField.setForeground(getContrastColor(getBackground()));
        textField.setHorizontalAlignment(JTextField.LEFT);
        textField.setBounds(5,5, width - 50, 30);
        add(textField, "span 7");

        // Add an ActionListener to disable editing when Enter is pressed
        textField.addActionListener(e -> {
            textField.setEditable(false); // Disable editing
            textField.setRequestFocusEnabled(false);
        });
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textField.setEditable(true); // Enable editing again
                textField.setRequestFocusEnabled(true);
            }
        });

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
                        
                        textField.setBackground(getBackground());
                        textField.setForeground(getContrastColor(getBackground()));
                        //Set everything to have higher contrast with new selected color.
                        // Adjust text color for readability
                        titleSep.setBackground(getBackground());
                        titleSep.setForeground(getBackground().darker());

                        for(JTextField task : todoDict.keySet()){
                            task.setBackground(getBackground());
                            task.setForeground(getContrastColor(getBackground()));
                        }
                        for(CircleButton cButton : todoDict.values()){
                            cButton.setBackground(getBackground());
                            cButton.setForeground(getContrastColor(getBackground()));
                        }
                        for(JSeparator sep : sepList){
                            sep.setBackground(getBackground());
                            sep.setForeground(getBackground().darker());
                        }
                        addTaskButton.setBackground(getBackground());
                        addTaskButton.setForeground(getContrastColor(getBackground()));
                        settingsButton.setForeground(getContrastColor(choosedColor));
                    }
            }
        }));

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
        settingsButton.setHorizontalAlignment(JButton.RIGHT);
        settingsButton.setForeground(getContrastColor(getBackground()));
        settingsButton.setBounds(width - 40,5, 30, 30);
        add(settingsButton, "right, gapbefore push, wrap");

        titleSep = new JSeparator(SwingConstants.HORIZONTAL);
        titleSep.setBackground(getBackground());
        titleSep.setForeground(getBackground().darker());
        titleSep.setBounds(5, 30, width, 1);
        add(titleSep, "span");

        // Create a JButton
        addTaskButton = new JButton("Add Task +");
        addTaskButton.setFont(new Font("Arial", Font.BOLD, 12));
        addTaskButton.setOpaque(false);
        addTaskButton.setBackground(getBackground());
        addTaskButton.setForeground(getContrastColor(getBackground()));
        addTaskButton.setBorder(null);
        addTaskButton.setFocusPainted(false);
        
        addTaskButton.setHorizontalAlignment(JButton.LEFT);
        addTaskButton.addActionListener(e -> addTaskToList("Example Text"));
        addTaskButton.setBounds(5,45, width, 15);
        add(addTaskButton, "span 3, wrap");
 

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
                    Point current = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), getParent());
                    int dx = current.x - initialClick.x;
                    int dy = current.y - initialClick.y;

                    Container parent = getParent();
                    if (parent instanceof CanvasPanel) {
                        if (getSelected() && ((CanvasPanel)parent).getSelectedObjects().size() > 1) {
                            // Group dragging
                            ((CanvasPanel)parent).moveGroup(dx, dy);
                        } else {
                            // Single object drag
                            setLocation(getX() + dx, getY() + dy);
                        }
                    }

                    // Only update initialClick once per frame, for smooth delta
                    initialClick = current;
                }
            }
        });
        updateTextStyle();
        revalidate();
        repaint();
    }

    public void addTaskToList(String text){
        

        int fontSize = Math.max(1, (int) Math.round(12 * scale));

        JTextField newTask = new JTextField();
        newTask.setText(text);
        newTask.setFont(new Font("Arial", Font.BOLD, fontSize));
        newTask.setBorder(null);
        newTask.setVisible(true);
        newTask.setBackground(getContrastColor(getBackground()));
        newTask.setForeground(getContrastColor(getBackground()));
        newTask.setOpaque(false);
        newTask.setHorizontalAlignment(JTextField.LEFT);

        // Add an ActionListener to disable editing when Enter is pressed
        newTask.addActionListener(e -> {
            newTask.setEditable(false); // Disable editing
            newTask.setRequestFocusEnabled(false);
        });
        newTask.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                newTask.setEditable(true); // Enable editing again
                newTask.setRequestFocusEnabled(true);
            }
        });
        
        
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setBackground(getBackground());
        sep.setForeground(getBackground().darker());
        sepList.add(sep);
        add(sep, "span");

        CircleButton newTaskCompleteButton = new CircleButton(15);
        newTaskCompleteButton.setFocusPainted(false);
        newTaskCompleteButton.setBackground(getBackground());
        newTaskCompleteButton.setForeground(getBackground().brighter());
        newTaskCompleteButton.addActionListener(e -> removeTaskFromList(newTask, newTaskCompleteButton, sep));
        
        todoDict.put(newTask, newTaskCompleteButton);

        add(newTask, "span 7");
        add(newTaskCompleteButton, "gapleft push, wrap");
        
        revalidate();
        repaintInside();
        repaint();
    }

    private void removeTaskFromList(JTextField tf, CircleButton button, JSeparator sep){
        todoDict.remove(tf, button);

        remove(tf);
        remove(button);
        remove(sep);

        revalidate();
        repaint();

    }

    public Color getContrastColor(Color newColor){
        double brightness = (0.299 * newColor.getRed()) + (0.587 * newColor.getGreen()) + (0.114 * newColor.getBlue());
        Color contrastColor = (brightness > 128) ? Color.BLACK : Color.WHITE;
        return contrastColor;
    }

    private void saveText() {
        String text = textField.getText().trim();
        if (!text.isEmpty()) {
            textField.setText("<html><body style='text-align:center'>" + text.replace("\n", "<br>") + "</body></html>");
        }

    }

    public List<String> getList(){
        List<String> saveList = new ArrayList<String>();
        for(JTextField tf: todoDict.keySet()){
            saveList.add(tf.getText());
        }
        return saveList;
    }
    
    public String getText() { 
        return textField.getText();
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

    
    public void setSelected(boolean selected){
        this.isSelected = selected;
    }
    public boolean getSelected(){
        return this.isSelected;
    }

    public void setScale(double newScale) {
        this.scale = newScale;
        updateTextStyle();
    }

    
    private void updateTextStyle() {
        int fontSize = Math.max(1, (int) Math.round(12 * scale));
        int titleSize = Math.max(1, (int) Math.round(16 * scale));
        addTaskButton.setFont(new Font("Arial", Font.BOLD, fontSize));
        textField.setFont(new Font("Arial", Font.BOLD, titleSize));
        
        for(JTextField tf: todoDict.keySet()){
            tf.setFont(new Font("Arial", Font.BOLD, fontSize));
        }
        for(CircleButton button: todoDict.values()){
            button.setFont(new Font("Arial", Font.BOLD, fontSize));
            button.setSize(new Dimension(fontSize, fontSize));
        }

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

    // Needs to scale text in tasks
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
