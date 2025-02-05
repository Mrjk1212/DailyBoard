import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CanvasPanel extends JPanel {
    private MoveableObject calendarBlock;
    private MoveableObject stickyNote;
    private MoveableObject selectedObject = null;


    private double scale = 1.0; // Zoom level
    private int offsetX = 0, offsetY = 0; // Panning offsets
    private Point lastDrag = null;
    private boolean isPanning = false;
    private boolean isResizing = false;

    // Add new objects to the canvas
    private java.util.List<MoveableObject> objects = new java.util.ArrayList<>();

    public CanvasPanel() {
        setPreferredSize(new Dimension(800, 600));
        setLayout(null);

        calendarBlock = new CalendarObject(100, 100, 200, 150, Color.LIGHT_GRAY);
        stickyNote = new StickyNoteObject(300, 200, 100, 100, Color.YELLOW);

        objects.add(calendarBlock);
        objects.add(stickyNote);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Point scaledPoint = getScaledPoint(e.getPoint());
        
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Right click starts panning
                    isPanning = true;
                } else {
                    // Left click selects objects or starts resizing
                    for (MoveableObject obj : objects) {
                        if (obj instanceof StickyNoteObject && ((StickyNoteObject) obj).isResizing(scaledPoint)) {
                            // Start resizing if clicked on the resize corner
                            isResizing = true;
                            selectedObject = obj;
                            break;
                        } else if (obj.contains(scaledPoint)) {
                            // Select the object for moving
                            selectedObject = obj;
                            repaint();
                            break;
                        }
                    }
                }
                lastDrag = e.getPoint();
            }
        
            public void mouseReleased(MouseEvent e) {
                if (selectedObject != null) {
                    selectedObject.selected(getGraphics(), false);
                }
                lastDrag = null;
                isResizing = false;
                isPanning = false;
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isPanning) {
                    // Panning the canvas
                    int dx = e.getX() - lastDrag.x;
                    int dy = e.getY() - lastDrag.y;
                    offsetX += dx;
                    offsetY += dy;
                    lastDrag = e.getPoint();
                    repaint();
                } else if (selectedObject != null) {
                    // If resizing, adjust the size
                    int dx = e.getX() - lastDrag.x;
                    int dy = e.getY() - lastDrag.y;
                    if (isResizing) {
                        ((StickyNoteObject) selectedObject).move(dx, dy, true); // Resize
                    } else {
                        selectedObject.move(dx, dy, false); // Move
                    }
                    lastDrag = e.getPoint();
                    repaint();
                }
            }
        });

        // Mouse wheel listener for zooming (centered on cursor)
        addMouseWheelListener(e -> {
            double zoomFactor = e.getPreciseWheelRotation() > 0 ? 0.9 : 1.1;
            Point mousePoint = e.getPoint();
            zoomAtPoint(mousePoint, zoomFactor);
            repaint();
        });
    }

    // Converts screen coordinates to scaled coordinates
    private Point getScaledPoint(Point p) {
        int x = (int) ((p.x - offsetX) / scale);
        int y = (int) ((p.y - offsetY) / scale);
        return new Point(x, y);
    }

    // Zoom centered on a specific point
    private void zoomAtPoint(Point cursor, double zoomFactor) {
        double newScale = scale * zoomFactor;

        // Prevent zooming too far out
        if (newScale < 0.1) return;
        
        // Cursor position in world coordinates before zooming
        double worldX = (cursor.x - offsetX) / scale;
        double worldY = (cursor.y - offsetY) / scale;

        // Apply new scale
        scale = newScale;

        // Adjust offset so cursor remains at the same world position
        offsetX = (int) (cursor.x - worldX * scale);
        offsetY = (int) (cursor.y - worldY * scale);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Apply zoom and panning transformations
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        // Draw the scaling grid (always centered)
        drawGrid(g2);

        // Draw moveable objects
        for (MoveableObject obj : objects) {
            obj.draw(g2);
        }
    }

    // Draws an infinite scaling grid, always centered on the canvas
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

    // Method to add different objects
    public void addStickyNote() {
        objects.add(new StickyNoteObject(100, 100, 100, 100, Color.YELLOW));
        repaint();
    }

    public void addCalendar() {
        objects.add(new CalendarObject(300, 100, 200, 150, Color.LIGHT_GRAY));
        repaint();
    }

    public void addToDoList() {
        objects.add(new ToDoListObject(500, 100, 200, 100, Color.CYAN));
        repaint();
    }

    public void addParagraph() {
        objects.add(new ParagraphObject(700, 100, 300, 100, Color.PINK));
        repaint();
    }

    public void deleteSelectedObject() {
        if (selectedObject != null) {
            objects.remove(selectedObject);
            selectedObject = null;
            repaint();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Canvas with Toolbar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CanvasPanel canvasPanel = new CanvasPanel();

        // Create the toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Add buttons for the toolbar
        JButton addStickyNoteButton = new JButton("Add Sticky Note");
        addStickyNoteButton.addActionListener(e -> canvasPanel.addStickyNote());
        toolBar.add(addStickyNoteButton);

        JButton addCalendarButton = new JButton("Add Calendar");
        addCalendarButton.addActionListener(e -> canvasPanel.addCalendar());
        toolBar.add(addCalendarButton);

        JButton addToDoListButton = new JButton("Add To-Do List");
        addToDoListButton.addActionListener(e -> canvasPanel.addToDoList());
        toolBar.add(addToDoListButton);

        JButton addParagraphButton = new JButton("Add Paragraph");
        addParagraphButton.addActionListener(e -> canvasPanel.addParagraph());
        toolBar.add(addParagraphButton);

        // Add the delete button
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> canvasPanel.deleteSelectedObject());
        toolBar.add(deleteButton);

        // Set up frame layout
        frame.getContentPane().add(toolBar, BorderLayout.NORTH);
        frame.getContentPane().add(canvasPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}
