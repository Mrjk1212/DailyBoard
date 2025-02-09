import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CanvasPanel extends JPanel {
    private StickyNoteObject stickyNote;


    private double scale = 1.0; // Zoom level
    private int offsetX = 0, offsetY = 0; // Panning offsets
    private Point lastDrag = null;
    private boolean isPanning = false;
    private boolean isResizing = false;

    // Add new objects to the canvas
    private java.util.List<StickyNoteObject> StickyNoteObjectList = new java.util.ArrayList<>();

    public CanvasPanel() {
        setPreferredSize(new Dimension(800, 600));
        setLayout(null);

        repaint();
    }

    // Draws a grid
    private void drawGrid(Graphics2D g2) {
        int baseGridSize = 50; // Default grid spacing
        int gridSpacing = (int) (baseGridSize * scale);

        // Adjust spacing dynamically so the grid stays visible at extreme zoom levels
        while (gridSpacing < 5) {
            baseGridSize *= 2;
            gridSpacing = (int) (baseGridSize * scale);
        }

        g2.setColor(new Color(220, 220, 220)); // Light gray grid

        // Determine bounds for grid rendering
        int width = getWidth() * 20;
        int height = getHeight() * 20;

        // Calculate the number of grid lines to draw based on panel size and grid spacing
        int centerX = width;
        int centerY = height;

        // Draw vertical grid lines
        for (int x = centerX % baseGridSize; x < width; x += baseGridSize) {
            g2.drawLine(x, 0, x, height);
        }
        for (int x = centerX % baseGridSize; x > 0; x -= baseGridSize) {
            g2.drawLine(x, 0, x, height);
        }

        // Draw horizontal grid lines
        for (int y = centerY % baseGridSize; y < height; y += baseGridSize) {
            g2.drawLine(0, y, width, y);
        }
        for (int y = centerY % baseGridSize; y > 0; y -= baseGridSize) {
            g2.drawLine(0, y, width, y);
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid((Graphics2D) g);
    }

    // Method to add different objects
    public void addStickyNote() {
        stickyNote = new StickyNoteObject(300, 200, 100, 100, Color.YELLOW);
        StickyNoteObjectList.add(stickyNote);
        add(stickyNote);
        repaint();
    }

    public void addCalendar() {
        //objects.add(new CalendarObject(300, 100, 200, 150, Color.LIGHT_GRAY));
        repaint();
    }

    public void addToDoList() {
        //objects.add(new ToDoListObject(500, 100, 200, 100, Color.CYAN));
        repaint();
    }

    public void addParagraph() {
        //objects.add(new ParagraphObject(700, 100, 300, 100, Color.PINK));
        repaint();
    }

    /* 
    public void deleteSelectedObject() {
        if (selectedObject != null) {
            if(selectedObject instanceof StickyNoteObject){
                ((StickyNoteObject) selectedObject).destroy();
            }
            
            objects.remove(selectedObject);
            selectedObject = null;
            repaint();
        }
    }
        */

    public static void main(String[] args) {
        JFrame frame = new JFrame("Canvas with Toolbar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CanvasPanel canvasPanel = new CanvasPanel();

        // Create the toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Add buttons for the toolbar
        JButton addStickyNoteButton = new JButton("Sticky Note");
        addStickyNoteButton.addActionListener(e -> canvasPanel.addStickyNote());
        toolBar.add(addStickyNoteButton);

        JButton addCalendarButton = new JButton("Calendar");
        addCalendarButton.addActionListener(e -> canvasPanel.addCalendar());
        toolBar.add(addCalendarButton);

        JButton addToDoListButton = new JButton("To-Do List");
        addToDoListButton.addActionListener(e -> canvasPanel.addToDoList());
        toolBar.add(addToDoListButton);

        JButton addParagraphButton = new JButton("Paragraph");
        addParagraphButton.addActionListener(e -> canvasPanel.addParagraph());
        toolBar.add(addParagraphButton);

        /* 
        // Add the delete button
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> canvasPanel.deleteSelectedObject());
        toolBar.add(deleteButton);
*/
        // Set up frame layout
        frame.getContentPane().add(toolBar, BorderLayout.NORTH);
        frame.getContentPane().add(canvasPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}