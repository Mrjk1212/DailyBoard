import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
public class CanvasPanel extends JPanel {
    private List<StickyNoteObject> stickyNotes = new ArrayList<>();
    private double scale = 1.0;
    private int offsetX = 0, offsetY = 0;
    private Point lastDrag = null;

    public CanvasPanel() {
        setPreferredSize(new Dimension(800, 600));
        setLayout(null);

        // Mouse wheel listener for zooming
        addMouseWheelListener(e -> {
            double zoomFactor = 1.1;
            if (e.getPreciseWheelRotation() < 0) {
                scale *= zoomFactor; // Zoom in
            } else {
                scale /= zoomFactor; // Zoom out
            }
            updateStickyNotePositions();
            repaint();
        });

        // Mouse listeners for panning
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDrag = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDrag != null) {
                    int dx = e.getX() - lastDrag.x;
                    int dy = e.getY() - lastDrag.y;
                    offsetX += dx;
                    offsetY += dy;
                    lastDrag = e.getPoint();
                    updateStickyNotePositions();
                    repaint();
                }
            }
        });
    }

    // Ensure sticky notes scale & move properly
    private void updateStickyNotePositions() {
        for (StickyNoteObject note : stickyNotes) {
            int currentX = note.getX();
            int currentY = note.getY();
    
            int scaledX = (int) ((currentX * scale) + offsetX);
            int scaledY = (int) ((currentY * scale) + offsetY);
            int newWidth = (int) (note.getOriginalWidth() * scale);
            int newHeight = (int) (note.getOriginalHeight() * scale);
    
            note.setBounds(scaledX, scaledY, newWidth, newHeight);
            note.updateOriginalPosition(); // Save new position so it doesn't reset
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Graphics2D g2d = (Graphics2D) g.create();
        AffineTransform transform = new AffineTransform();
        transform.translate(offsetX, offsetY);
        transform.scale(scale, scale);
        g2d.setTransform(transform);
        
        g2d.dispose();
        drawGrid(g2);
        
    }

    private void drawGrid(Graphics2D g2) {
        int gridSize = (int) (50 * scale);
        g2.setColor(new Color(220, 220, 220));

        for (int x = offsetX % gridSize; x < getWidth(); x += gridSize) {
            g2.drawLine(x, 0, x, getHeight());
        }
        for (int y = offsetY % gridSize; y < getHeight(); y += gridSize) {
            g2.drawLine(0, y, getWidth(), y);
        }
    }

    public void addStickyNote() {
        StickyNoteObject note = new StickyNoteObject(300, 200, 100, 100, Color.YELLOW);
        stickyNotes.add(note);
        add(note);
        updateStickyNotePositions();
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