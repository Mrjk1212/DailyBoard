import javax.swing.*;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import main.saves.BoardObjectState;
import main.saves.BoardScaleState;
import main.customComponents.*;

import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


//TODO
/*
- Select box to move multiple objects


- Fix Scaling so that each thing scales and adjusts size based off of DOUBLE, instead of INT (never gonna work btw, forget this)
^^ Not sure this can be possble to just "fix" will need to change how scaling works.
-
 */

public class CanvasPanel extends JPanel {
    private StickyNoteObject stickyNote;
    private CalendarObject calendar;
    private TodoObject todo;
    private WhiteBoardObject whiteBoard;
    private GoalObject goal;
    
    private double scale = 1.0;
    private int offsetX = 0, offsetY = 0;
    private Point lastDrag = null;
    private boolean isPanning = false;

    private Rectangle selectionBox = null;
    private boolean isSelecting = false;
    private final Set<JComponent> selectedObjects = new HashSet<>();
    private JComponent draggingSelectedObject = null;

    private boolean darkMode = false;
    private Color darkBackgroundColor = new Color(128,128,128);
    private Color darkGridLineColor = new Color(144,144,144);

    private Color lightBackgroundColor = new Color(245, 245, 245);
    private Color lightGridLineColor = new Color(220, 220, 220);

    private List<StickyNoteObject> stickyNoteObjectList = new ArrayList<>();
    private List<CalendarObject> calendarObjectList = new ArrayList<>();
    private List<TodoObject> todoObjectList = new ArrayList<>();
    private List<WhiteBoardObject> whiteBoardObjectList = new ArrayList<>();
    private List<GoalObject> goalObjectList = new ArrayList<>();

    private static final double ZOOM_FACTOR = 0.1;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 3.0;

    public CanvasPanel() {
        setPreferredSize(new Dimension(800, 600));
        setLayout(null);

        loadBoardState();

        addMouseWheelListener(e -> {
            //Point mousePoint = e.getPoint();
            Dimension viewSize = getSize();
            Point mousePoint = new Point(viewSize.width / 2, viewSize.height / 2);
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
                wb.setScale(scale);
                wb.repaintInside();
            }

            for (GoalObject Goal : goalObjectList) {
                // Calculate distance from mouse to note
                double dx = Goal.getX() - mousePoint.x;
                double dy = Goal.getY() - mousePoint.y;
                
                // Scale this distance by the change in scale
                double scaleChange = scale / oldScale;
                dx *= scaleChange;
                dy *= scaleChange;
                
                // Set new position relative to mouse point
                Goal.setLocation(
                    (int)(mousePoint.x + dx),
                    (int)(mousePoint.y + dy)
                );
                
                // Update size
                Goal.setBounds(
                    Goal.getX(),
                    Goal.getY(),
                    (int)(Goal.getOriginalWidth() * scale),
                    (int)(Goal.getOriginalHeight() * scale)
                );
                Goal.setScale(scale);
                Goal.repaintInside();
            }

            repaint();
        });

        MouseAdapter mouseHandler = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                isPanning = true;
                lastDrag = e.getPoint();
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            } else if (SwingUtilities.isLeftMouseButton(e)) {
                isSelecting = true;
                selectionBox = new Rectangle(e.getPoint());
                for(Object obj : selectedObjects){
                    if(obj instanceof StickyNoteObject){
                        ((StickyNoteObject) obj).setSelected(false);;
                    }
                }
                selectedObjects.clear();
                draggingSelectedObject = getObjectAt(e.getPoint());
                //handle clicking directly on a note (no drag-box)
                if (draggingSelectedObject != null && draggingSelectedObject instanceof StickyNoteObject) {
                    ((StickyNoteObject) draggingSelectedObject).setSelected(true);
                    selectedObjects.add(draggingSelectedObject);
                }
                lastDrag = e.getPoint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                isPanning = false;
                setCursor(Cursor.getDefaultCursor());
            } else if (SwingUtilities.isLeftMouseButton(e)) {
                if (isSelecting && selectionBox != null) {
                    selectObjectsWithin(selectionBox);
                    selectionBox = null;
                    repaint();
                }
                draggingSelectedObject = null;
                isSelecting = false;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (isPanning && lastDrag != null) {
                int dx = e.getX() - lastDrag.x;
                int dy = e.getY() - lastDrag.y;
                offsetX += dx;
                offsetY += dy;
                lastDrag = e.getPoint();

                for (JComponent obj : getAllObjects()) {
                    obj.setLocation(obj.getX() + dx, obj.getY() + dy);
                }
                repaint();
            }
            
            else if (isSelecting && selectionBox != null) {
                selectionBox.setSize(
                    e.getX() - selectionBox.x,
                    e.getY() - selectionBox.y
                );
                repaint();
            }
            
        }
    };

    addMouseListener(mouseHandler);
    addMouseMotionListener(mouseHandler);
    
}

    public void moveGroup(int dx, int dy){
        
        for (JComponent obj : getAllObjects() ) {
            if(obj instanceof StickyNoteObject){
                if(((StickyNoteObject) obj).getSelected()){
                    obj.setLocation(obj.getX() + dx, obj.getY() + dy);
                }
            }
            if(obj instanceof TodoObject){
                if(((TodoObject) obj).getSelected()){
                    obj.setLocation(obj.getX() + dx, obj.getY() + dy);
                }
            }
        }
    }

    private JComponent getObjectAt(Point point) {
        for (JComponent obj : getAllObjects()) {
            if (obj.getBounds().contains(point)) {
                return obj;
            }
        }
        return null;
    }

    private List<JComponent> getAllObjects() {
        List<JComponent> all = new ArrayList<>();
        all.addAll(stickyNoteObjectList);
        all.addAll(calendarObjectList);
        all.addAll(todoObjectList);
        all.addAll(whiteBoardObjectList);
        all.addAll(goalObjectList);
        return all;
    }

    private void selectObjectsWithin(Rectangle rect) {
    for (JComponent obj : getAllObjects()) {
        Rectangle objBounds = obj.getBounds();
        if (rect.contains(objBounds)) {
            selectedObjects.add(obj);

            if(obj instanceof StickyNoteObject){
                ((StickyNoteObject) obj).setSelected(true);
            }
            if(obj instanceof TodoObject){
                ((TodoObject) obj).setSelected(true);
            }
        }
        }
    }

    public Set<JComponent> getSelectedObjects(){
        return selectedObjects;
    }

    //This is for the button switch
    private void themeMode(){

        if(darkMode){
            this.setBackground(lightBackgroundColor);
            darkMode = false;
        }
        else{
            this.setBackground(darkBackgroundColor);
            darkMode = true;
        }
        repaint();
    }

    //this is for loading
    private void darkMode(boolean yes){

    if(!yes){
        this.setBackground(lightBackgroundColor);
        
        darkMode = false;
    }
    else{
        this.setBackground(darkBackgroundColor);

        darkMode = true;
    }
    repaint();
}

    private void drawGrid(Graphics2D g2) {
        int baseGridSize = 50;
        int scaledGridSize = (int)(baseGridSize * scale);

        if(darkMode){
            g2.setColor(darkGridLineColor);
        }
        else{
            g2.setColor(lightGridLineColor);
        }

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

        // Draw position information in the corner
        g2.setColor(new Color(0, 0, 0, 180)); // Semi-transparent black background
        g2.fillRect(10, getHeight() - 60, 200, 50);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(String.format("Position: X=%d, Y=%d", offsetX, offsetY), 15, getHeight() - 40);
        g2.drawString(String.format("Scale: %.2f", scale), 15, getHeight() - 20);

        if (selectionBox != null && isSelecting) {
        g2.setColor(Color.BLUE);
        g2.setStroke(new BasicStroke(1));
        g2.draw(selectionBox);
        g2.setColor(new Color(0, 0, 255, 50));
        g2.fill(selectionBox);
    }
    }

    public void addStickyNote() {
        stickyNote = new StickyNoteObject(300, 200, 200, 120, Color.YELLOW);
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
        this.setComponentZOrder(stickyNote, 2);
        repaint();
        
    }

    public void removeStickyNote(StickyNoteObject note) {
        stickyNoteObjectList.remove(note); // Remove from list
        remove(note); // Remove from UI
        revalidate();
        repaint();
    }


    public void addCalendar() {
        int scaledX = (int) (300 * scale);
        int scaledY = (int) (300 * scale);
        int scaledWidth = (int) (550 * scale);
        int scaledHeight = (int) (400 * scale);
        calendar = new CalendarObject(scaledX, scaledY, scaledWidth, scaledHeight, new Color(245,245,245), "");//Color = offwhite
        calendarObjectList.add(calendar);
        // Update size to account for zoom out/in
        calendar.setBounds(
            calendar.getX(),
            calendar.getY(),
            (int)(calendar.getOriginalWidth() * scale),
            (int)(calendar.getOriginalHeight() * scale)
        );
        add(calendar);
        this.setComponentZOrder(calendar, 2);
        calendar.setScale(scale);
        calendar.repaintInside();
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
        this.setComponentZOrder(todo, 2);
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
        // Update size to account for zoom out/in
        whiteBoard.setBounds(
            whiteBoard.getX(),
            whiteBoard.getY(),
            (int)(whiteBoard.getOriginalWidth() * scale),
            (int)(whiteBoard.getOriginalHeight() * scale)
        );
        whiteBoard.setScale(scale);
        whiteBoard.repaintInside();
        add(whiteBoard);
        this.setComponentZOrder(whiteBoard, 2);
        repaint();
    }

    public void removeWhiteBoardObject(WhiteBoardObject wb){
        whiteBoardObjectList.remove(wb);
        remove(wb);
        revalidate();
        repaint();
    }

    public void addGoal(){
        GoalObject Goal = new GoalObject(0, 0, 300, 100, new Color(245,245,245));//OFF WHITE
        goalObjectList.add(Goal);
        // Update size to account for zoom out/in
        Goal.setBounds(
            Goal.getX(),
            Goal.getY(),
            (int)(Goal.getOriginalWidth() * scale),
            (int)(Goal.getOriginalHeight() * scale)
        );
        Goal.setScale(scale);
        Goal.repaintInside();
        add(Goal);
        this.setComponentZOrder(Goal, 2);
        repaint();

    }

    public void removeGoal(GoalObject Goal){
        goalObjectList.remove(Goal); // Remove from list
        remove(Goal); // Remove from UI
        revalidate();
        repaint();
    }

    public void saveBoardState() {
        List<BoardObjectState> boardState = new ArrayList<>();

        for (StickyNoteObject note : stickyNoteObjectList) {
            BoardObjectState bs = new BoardObjectState(
                "StickyNote",
                note.getX(), note.getY(),
                note.getWidth(), note.getHeight(),
                note.getOriginalWidth(), note.getOriginalHeight(),
                note.getBackground());
                bs.setText(note.getText());
                bs.setTitle(note.getTitle());

                boardState.add(bs);
        }

        for (CalendarObject cal : calendarObjectList) {
            BoardObjectState bs = new BoardObjectState(
                "Calendar",
                cal.getX(), cal.getY(),
                cal.getWidth(), cal.getHeight(),
                cal.getOriginalWidth(), cal.getOriginalHeight(),
                cal.getBackground());
                bs.setText(cal.getICalFileLocation()); // Calendars dont store text lol, but we will multiPurpose use this to store the ICalFile Location if applicable

                boardState.add(bs);
        }

        for (TodoObject td : todoObjectList){
            BoardObjectState bs = new BoardObjectState(
                "Todo",
                td.getX(), td.getY(),
                td.getWidth(), td.getHeight(),
                td.getOriginalWidth(), td.getOriginalHeight(),
                td.getBackground());
                bs.setText(td.getText()); // Actually TITLE.... just made this inconsistent because lazy? fix later
                bs.setTodoList(td.getList());

                boardState.add(bs);
        }

        for (GoalObject Goal : goalObjectList){
            BoardObjectState bs = new BoardObjectState(
                "Goal",
                Goal.getX(), Goal.getY(),
                Goal.getWidth(), Goal.getHeight(),
                Goal.getOriginalWidth(), Goal.getOriginalHeight(),
                Goal.getBackground());
                bs.setGoalDate(Goal.getDate());
                bs.setText(Goal.getText());
                bs.setTitle(Goal.getTitle());

                boardState.add(bs);
        }

        for (WhiteBoardObject wb : whiteBoardObjectList){
            wb.saveImage();
            BoardObjectState bs = new BoardObjectState(
            "WhiteBoard",
            wb.getX(),
            wb.getY(),
            wb.getWidth(),
            wb.getHeight(),
            wb.getOriginalWidth(),
            wb.getOriginalHeight(),
            wb.getBackground());
            bs.setText(wb.getImageLocation());// Actually ImageLocation String.... just made this inconsistent because lazy? fix later
            boardState.add(bs);
        }
        

        // Convert to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("boardState.json")) {
            gson.toJson(boardState, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BoardScaleState boardScaleState = new BoardScaleState(this.scale, this.offsetX, this.offsetY, this.darkMode);

        try (FileWriter writer = new FileWriter("boardScaleState.json")) {
            gson.toJson(boardScaleState, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void loadBoardState() {
        Gson gson = new Gson();


        try (FileReader reader = new FileReader("boardScaleState.json")){

            BoardScaleState boardScaleState = gson.fromJson(reader, BoardScaleState.class);
            if(boardScaleState != null){

                this.scale = boardScaleState.Scale;
                this.offsetX = boardScaleState.OffsetX;
                this.offsetY = boardScaleState.OffsetY;
                this.darkMode = boardScaleState.darkMode;
                darkMode(darkMode);
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
        
        try (FileReader reader = new FileReader("boardState.json")) {
            BoardObjectState[] boardState = gson.fromJson(reader, BoardObjectState[].class);
            
            if (boardState != null) {
                for (BoardObjectState obj : boardState) {
                    if (obj.type.equals("StickyNote")) {
                        StickyNoteObject note = new StickyNoteObject(
                            obj.x, obj.y, obj.width, obj.height, obj.getColor()
                        );
                        note.setScale(this.scale);
                        note.setOriginalHeight(obj.OriginalHeight);
                        note.setOriginalWidth(obj.OriginalWidth);
                        note.setText(obj.text);
                        note.setTitle(obj.Title);
                        stickyNoteObjectList.add(note);
                        add(note);
                    } else if (obj.type.equals("Calendar")) {
                        CalendarObject cal = new CalendarObject(
                        obj.x, obj.y, obj.width, obj.height, obj.getColor(), obj.text); // Hacky gross badd way of storing file locations=
                        cal.setScale(this.scale);
                        cal.setOriginalHeight(obj.OriginalHeight);
                        cal.setOriginalWidth(obj.OriginalWidth);
                        calendarObjectList.add(cal);
                        add(cal);
                    } else if (obj.type.equals("Todo")) {
                        TodoObject td = new TodoObject(
                            obj.x, obj.y, obj.width, obj.height, obj.getColor()
                        );
                        td.setScale(this.scale);
                        td.setOriginalWidth(obj.OriginalWidth);
                        td.setOriginalHeight(obj.OriginalHeight);
                        for(String str: obj.todoListStrings){
                            td.addTaskToList(str);
                        }
                        td.setText(obj.text);
                        //Initialize the actual list here
                        todoObjectList.add(td);
                        add(td);
                    }else if (obj.type.equals("Goal")) {
                        GoalObject Goal = new GoalObject(
                            obj.x, obj.y, obj.width, obj.height, obj.getColor()
                        );
                        Goal.setScale(this.scale);
                        Goal.setOriginalHeight(obj.OriginalHeight);
                        Goal.setOriginalWidth(obj.OriginalWidth);
                        Goal.setTitle(obj.Title);
                        Goal.setText(obj.text);
                        Goal.setDate(obj.GoalDate);
                        //Initialize the actual list here
                        goalObjectList.add(Goal);
                        add(Goal);
                    }else if (obj.type.equals("WhiteBoard")){
                        WhiteBoardObject wb = new WhiteBoardObject(obj.x, obj.y, obj.width, obj.height, obj.getColor());
                        wb.setScale(this.scale);
                        wb.setOriginalHeight(obj.OriginalHeight);
                        wb.setOriginalWidth(obj.OriginalWidth);
                        wb.loadImage(obj.text);
                        whiteBoardObjectList.add(wb);
                        add(wb);
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
        JFrame frame = new JFrame("Daily Board");
        ImageIcon logoIcon = new ImageIcon("src\\main\\resources\\logo.PNG");
        frame.setIconImage(logoIcon.getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CanvasPanel canvasPanel = new CanvasPanel();

        // Create the toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Add buttons for the toolbar
        RoundedButton addStickyNoteButton = new RoundedButton(100, 30, "Sticky Note");
        addStickyNoteButton.addActionListener(e -> canvasPanel.addStickyNote());
        toolBar.add(addStickyNoteButton);

        RoundedButton addCalendarButton = new RoundedButton(100, 30, "Calendar");
        addCalendarButton.addActionListener(e -> canvasPanel.addCalendar());
        toolBar.add(addCalendarButton);

        RoundedButton addToDoListButton = new RoundedButton(100, 30, "To-Do List");
        addToDoListButton.addActionListener(e -> canvasPanel.addToDoList());
        toolBar.add(addToDoListButton);

        RoundedButton addWhiteBoardButton = new RoundedButton(100, 30, "White Board");
        addWhiteBoardButton.addActionListener(e -> canvasPanel.addWhiteBoard());
        toolBar.add(addWhiteBoardButton);

        RoundedButton addGoalButton = new RoundedButton(100, 30, "Goal");
        addGoalButton.addActionListener(e -> canvasPanel.addGoal());
        toolBar.add(addGoalButton);

        RoundedButton DarkModeButton = new RoundedButton(100, 30, "Dark/Light");
        DarkModeButton.addActionListener(e -> canvasPanel.themeMode());
        toolBar.add(DarkModeButton);

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