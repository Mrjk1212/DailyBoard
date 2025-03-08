import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class CanvasPanel extends JPanel {
    private StickyNoteObject stickyNote;
    private CalendarObject calendar;
    
    private double scale = 1.0;
    private int offsetX = 0, offsetY = 0;
    private Point lastDrag = null;
    private boolean isPanning = false;

    private List<StickyNoteObject> stickyNoteObjectList = new ArrayList<>();
    private List<CalendarObject> calendarObjectList = new ArrayList<>();

    private static final double ZOOM_FACTOR = 0.1;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 3.0;

    public CanvasPanel() {
        setPreferredSize(new Dimension(800, 600));
        setLayout(null);

        addMouseWheelListener(e -> {
            Point mousePoint = e.getPoint();
            double oldScale = scale;
            
            if (e.getWheelRotation() < 0) {
                scale = Math.min(scale * (1 + ZOOM_FACTOR), MAX_SCALE);
            } else {
                scale = Math.max(scale * (1 - ZOOM_FACTOR), MIN_SCALE);
            }

            for (StickyNoteObject note : stickyNoteObjectList) {
                // Calculate distance from mouse to note
                double dx = note.getX() - mousePoint.x;
                double dy = note.getY() - mousePoint.y;
                
                // Scale this distance by the change in scale
                double scaleChange = scale / oldScale;
                dx *= scaleChange;
                dy *= scaleChange;
                
                // Set new position relative to mouse point
                note.setLocation(
                    (int)(mousePoint.x + dx),
                    (int)(mousePoint.y + dy)
                );
                
                // Update size
                note.setBounds(
                    note.getX(),
                    note.getY(),
                    (int)(note.getOriginalWidth() * scale),
                    (int)(note.getOriginalHeight() * scale)
                );
                note.setScale(scale);
                note.repaintInside();
            }
            for (CalendarObject cal : calendarObjectList) {
                // Calculate distance from mouse to note
                double dx = cal.getX() - mousePoint.x;
                double dy = cal.getY() - mousePoint.y;
                
                // Scale this distance by the change in scale
                double scaleChange = scale / oldScale;
                dx *= scaleChange;
                dy *= scaleChange;
                
                // Set new position relative to mouse point
                cal.setLocation(
                    (int)(mousePoint.x + dx),
                    (int)(mousePoint.y + dy)
                );
                
                // Update size
                cal.setBounds(
                    cal.getX(),
                    cal.getY(),
                    (int)(cal.getOriginalWidth() * scale),
                    (int)(cal.getOriginalHeight() * scale)
                );
                cal.setScale(scale);
                cal.repaintInside();
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    isPanning = true;
                    lastDrag = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    isPanning = false;
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPanning && lastDrag != null) {
                    int dx = e.getX() - lastDrag.x;
                    int dy = e.getY() - lastDrag.y;
                    offsetX += dx;
                    offsetY += dy;
                    lastDrag = e.getPoint();

                    for (StickyNoteObject obj : stickyNoteObjectList) {
                        obj.setLocation(obj.getX() + dx, obj.getY() + dy);
                    }
                    for (CalendarObject obj : calendarObjectList) {
                        obj.setLocation(obj.getX() + dx, obj.getY() + dy);
                    }
                    repaint();
                }
            }
        });

        
    }

    private void drawGrid(Graphics2D g2) {
        int baseGridSize = 50;
        int scaledGridSize = (int)(baseGridSize * scale);
        g2.setColor(new Color(220, 220, 220));

        int startX = offsetX % scaledGridSize;
        int startY = offsetY % scaledGridSize;

        for (int x = startX; x < getWidth(); x += scaledGridSize) {
            g2.drawLine(x, 0, x, getHeight());
        }

        for (int y = startY; y < getHeight(); y += scaledGridSize) {
            g2.drawLine(0, y, getWidth(), y);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        drawGrid(g2);
    }

    public void addStickyNote() {
        stickyNote = new StickyNoteObject(300, 200, 100, 100, Color.YELLOW);
        stickyNoteObjectList.add(stickyNote);
        // Update size to account for zoom out/in
        stickyNote.setBounds(
            stickyNote.getX(),
            stickyNote.getY(),
            (int)(stickyNote.getOriginalWidth() * scale),
            (int)(stickyNote.getOriginalHeight() * scale)
        );
        stickyNote.setScale(scale);
        stickyNote.repaintInside();
        add(stickyNote);
        repaint();
    }


    public void addCalendar() {
        calendar = new CalendarObject(300, 500, 200, 150, Color.LIGHT_GRAY);
        calendarObjectList.add(calendar);
        // Update size to account for zoom out/in
        calendar.setBounds(
            calendar.getX(),
            calendar.getY(),
            (int)(calendar.getOriginalWidth() * scale),
            (int)(calendar.getOriginalHeight() * scale)
        );
        calendar.setScale(scale);
        calendar.repaintInside();
        add(calendar);
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