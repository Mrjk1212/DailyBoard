import javax.swing.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import main.saves.BoardObjectState;

import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class CanvasPanel extends JPanel {
    private StickyNoteObject stickyNote;
    private CalendarObject calendar;
    private TodoObject todo;
    private WhiteBoardObject whiteBoard;
    
    private double scale = 1.0;
    private int offsetX = 0, offsetY = 0;
    private Point lastDrag = null;
    private boolean isPanning = false;

    private List<StickyNoteObject> stickyNoteObjectList = new ArrayList<>();
    private List<CalendarObject> calendarObjectList = new ArrayList<>();
    private List<TodoObject> todoObjectList = new ArrayList<>();
    private List<WhiteBoardObject> whiteBoardObjectList = new ArrayList<>();

    private static final double ZOOM_FACTOR = 0.1;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 3.0;

    public CanvasPanel() {
        setPreferredSize(new Dimension(800, 600));
        setLayout(null);

        loadBoardState();

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

            for (TodoObject td : todoObjectList) {
                // Calculate distance from mouse to note
                double dx = td.getX() - mousePoint.x;
                double dy = td.getY() - mousePoint.y;
                
                // Scale this distance by the change in scale
                double scaleChange = scale / oldScale;
                dx *= scaleChange;
                dy *= scaleChange;
                
                // Set new position relative to mouse point
                td.setLocation(
                    (int)(mousePoint.x + dx),
                    (int)(mousePoint.y + dy)
                );
                
                // Update size
                td.setBounds(
                    td.getX(),
                    td.getY(),
                    (int)(td.getOriginalWidth() * scale),
                    (int)(td.getOriginalHeight() * scale)
                );
                td.setScale(scale);
                td.repaintInside();
            }

            for (WhiteBoardObject wb : whiteBoardObjectList) {
                // Calculate distance from mouse to note
                double dx = wb.getX() - mousePoint.x;
                double dy = wb.getY() - mousePoint.y;
                
                // Scale this distance by the change in scale
                double scaleChange = scale / oldScale;
                dx *= scaleChange;
                dy *= scaleChange;
                
                // Set new position relative to mouse point
                wb.setLocation(
                    (int)(mousePoint.x + dx),
                    (int)(mousePoint.y + dy)
                );
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
                    for (TodoObject obj : todoObjectList){
                        obj.setLocation(obj.getX() + dx, obj.getY() + dy);
                    }
                    for(WhiteBoardObject obj : whiteBoardObjectList){
                        obj.setLocation(obj.getX() + dx,obj.getY() + dy);
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

    public void removeStickyNote(StickyNoteObject note) {
        stickyNoteObjectList.remove(note); // Remove from list
        remove(note); // Remove from UI
        revalidate();
        repaint();
    }


    public void addCalendar() {
        calendar = new CalendarObject(300, 300, 550, 400, Color.GRAY);
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

    public void removeCalendarObject(CalendarObject cal) {
        calendarObjectList.remove(cal); // Remove from list
        remove(cal); // Remove from UI
        revalidate();
        repaint();
    }

    public void addToDoList() {
        todo = new TodoObject(50, 50, 200, 400, new Color(245, 245, 245)); //Off White
        todoObjectList.add(todo);
        // Update size to account for zoom out/in
        todo.setBounds(
            todo.getX(),
            todo.getY(),
            (int)(todo.getOriginalWidth() * scale),
            (int)(todo.getOriginalHeight() * scale)
        );
        todo.setScale(scale);
        todo.repaintInside();
        add(todo);
        repaint();
    }

    public void removeTodoObject(TodoObject td) {
        todoObjectList.remove(td); // Remove from list
        remove(td); // Remove from UI
        revalidate();
        repaint();
    }

    public void addWhiteBoard() {
        WhiteBoardObject whiteBoard = new WhiteBoardObject(0, 0, 500, 500, Color.WHITE);
        whiteBoardObjectList.add(whiteBoard);
        
        add(whiteBoard);
        repaint();
    }

    public void removeWhiteBoardObject(WhiteBoardObject wb){
        whiteBoardObjectList.remove(wb);
        remove(wb);
        revalidate();
        repaint();
    }

    public void saveBoardState() {
        List<BoardObjectState> boardState = new ArrayList<>();

        for (StickyNoteObject note : stickyNoteObjectList) {
            boardState.add(new BoardObjectState(
                "StickyNote",
                note.getX(), note.getY(),
                note.getWidth(), note.getHeight(),
                note.getBackground(),  // Get color
                note.getText(),  // Get text content
                (List<String>) null, //Type casting to null so I don't get some random null pointer exception!
                note.getTitle()
            ));
        }

        for (CalendarObject cal : calendarObjectList) {
            boardState.add(new BoardObjectState(
                "Calendar",
                cal.getX(), cal.getY(),
                cal.getWidth(), cal.getHeight(),
                cal.getBackground(),  // Get color
                "",  // Calendars dont store text lol
                (List<String>) null,
                ""
            ));
        }

        for (TodoObject td : todoObjectList){
            boardState.add(new BoardObjectState(
                "Todo",
                td.getX(), td.getY(),
                td.getWidth(), td.getHeight(),
                td.getBackground(),
                td.getText(),
                td.getList(),
                ""
            ));
        }

        // Convert to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("boardState.json")) {
            gson.toJson(boardState, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadBoardState() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("boardState.json")) {
            BoardObjectState[] boardState = gson.fromJson(reader, BoardObjectState[].class);
            
            if (boardState != null) {
                for (BoardObjectState obj : boardState) {
                    if (obj.type.equals("StickyNote")) {
                        StickyNoteObject note = new StickyNoteObject(
                            obj.x, obj.y, obj.width, obj.height, obj.getColor()
                        );
                        note.setText(obj.text);
                        note.setTitle(obj.Title);
                        stickyNoteObjectList.add(note);
                        add(note);
                    } else if (obj.type.equals("Calendar")) {
                        CalendarObject cal = new CalendarObject(
                            obj.x, obj.y, obj.width, obj.height, obj.getColor()
                        );
                        calendarObjectList.add(cal);
                        add(cal);
                    } else if (obj.type.equals("Todo")) {
                        TodoObject td = new TodoObject(
                            obj.x, obj.y, obj.width, obj.height, obj.getColor()
                        );
                        for(String str: obj.todoListStrings){
                            td.addTaskToList(str);
                        }
                        td.setText(obj.text);
                        //Initialize the actual list here
                        todoObjectList.add(td);
                        add(td);
                    }
                }
                revalidate();
                repaint(); //repaint make sure everything loads in....
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        JButton addStickyNoteButton = new JButton("Sticky Note");
        addStickyNoteButton.addActionListener(e -> canvasPanel.addStickyNote());
        toolBar.add(addStickyNoteButton);

        JButton addCalendarButton = new JButton("Calendar");
        addCalendarButton.addActionListener(e -> canvasPanel.addCalendar());
        toolBar.add(addCalendarButton);

        JButton addToDoListButton = new JButton("To-Do List");
        addToDoListButton.addActionListener(e -> canvasPanel.addToDoList());
        toolBar.add(addToDoListButton);

        JButton addWhiteBoardButton = new JButton("Under Construction");
        addWhiteBoardButton.addActionListener(e -> canvasPanel.addWhiteBoard());
        toolBar.add(addWhiteBoardButton);

        // Save board state on exit
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                canvasPanel.saveBoardState();
            }
        });

        // Set up frame layout
        frame.getContentPane().add(toolBar, BorderLayout.NORTH);
        frame.getContentPane().add(canvasPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}