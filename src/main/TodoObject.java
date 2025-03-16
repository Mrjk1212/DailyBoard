import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import java.util.HashMap;
import java.util.Map;

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
    private JButton addTaskButton;
    private Point initialClick;
    private boolean isResizing;
    private static final int RESIZE_MARGIN = 10;
    private static final int DELETE_MARGIN = 10;
    private static final Color RESIZE_COLOR = Color.GRAY;
    private int originalWidth;
    private int originalHeight;
    private double scale = 1.0;
    private int ARC_RADIUS = 10;

    private Map<JTextField, JButton> todoDict = new HashMap<>();

    public TodoObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        setLayout(new MigLayout("", "[grow, fill][][]", ""));//
        setBackground(color);
        setOpaque(false);
        setBounds(xPos, yPos, width, height);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // Create a JTextField for input
        textField = new JTextField();
        //textField.setBounds(5, 5, width - 10, height - 100);
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setBorder(null);
        textField.setVisible(true);
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.BLACK);
        textField.setHorizontalAlignment(JTextField.CENTER);
                
        add(textField, "span 3, wrap");

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

        // Create a JButton
        addTaskButton = new JButton("+  Add Task");
        addTaskButton.setFont(new Font("Arial", Font.PLAIN, 12));
        addTaskButton.setOpaque(false);
        addTaskButton.setBackground(Color.BLACK);
        addTaskButton.setForeground(Color.WHITE);
        addTaskButton.setBorder(null);
        addTaskButton.setFocusPainted(false);
        
        addTaskButton.setHorizontalAlignment(JButton.LEFT);
        addTaskButton.addActionListener(e -> addTaskToList(""));
        add(addTaskButton, "span 3, wrap");
 

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

    public void addTaskToList(String text){
        
        System.out.println("Adding Task");

        int fontSize = Math.max(1, (int) Math.round(12 * scale));

        JTextField newTask = new JTextField();
        newTask.setText(text);
        newTask.setFont(new Font("Arial", Font.PLAIN, fontSize));
        newTask.setBorder(null);
        newTask.setVisible(true);
        newTask.setBackground(Color.WHITE);
        newTask.setForeground(Color.BLACK);
        newTask.setOpaque(false);
        newTask.setHorizontalAlignment(JTextField.CENTER);

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
        sep.setForeground(Color.LIGHT_GRAY);
        add(sep, "span");

        JButton newTaskCompleteButton = new JButton();
        newTaskCompleteButton.setSize(new Dimension(fontSize, fontSize));
        newTaskCompleteButton.setFont(new Font("Arial", Font.PLAIN, fontSize));
        newTaskCompleteButton.setMargin(new Insets(2, 2, 2, 2)); // Smaller padding
        newTaskCompleteButton.setFocusPainted(false);
        newTaskCompleteButton.addActionListener(e -> removeTaskFromList(newTask, newTaskCompleteButton, sep));

        todoDict.put(newTask, newTaskCompleteButton);
        System.out.println("Added Task: " + newTask.getText());
        System.out.println("Current Dict Size: " + todoDict.size());
        add(newTask, "span 7");
        add(newTaskCompleteButton, "width ::10, height ::10, wrap");
        
        revalidate();
        repaintInside(); // Needs to scale text in tasks
        repaint();
    }

    private void removeTaskFromList(JTextField tf, JButton button, JSeparator sep){
        todoDict.remove(tf, button);

        remove(tf);
        remove(button);
        remove(sep);

        revalidate();
        repaint();

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
            System.out.println("Task: " + tf.getText());
        }
        System.out.println("TodoDict Size: " + todoDict.size());
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

    public void setScale(double newScale) {
        this.scale = newScale;
        updateTextStyle();
    }

    // Needs to scale text in tasks
    private void updateTextStyle() {
        int fontSize = Math.max(1, (int) Math.round(12 * scale));
        addTaskButton.setFont(new Font("Arial", Font.PLAIN, fontSize));
        textField.setFont(new Font("Arial", Font.PLAIN, fontSize));
        
        for(JTextField tf: todoDict.keySet()){
            tf.setFont(new Font("Arial", Font.PLAIN, fontSize));
        }
        for(JButton button: todoDict.values()){
            button.setFont(new Font("Arial", Font.PLAIN, fontSize));
            button.setSize(new Dimension(fontSize, fontSize));
        }

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
