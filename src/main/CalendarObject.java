import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
/* 
TODO
-Go Back And Forward Weeks.
-Automatically "Center" The Calendar On The Current Time, and Color the first column's associated row as blue.
-
*/

/**
 * CalendarObject represents a resizable and draggable component that allows
 * users to input a google account.
 * resizing is done by clicking and dragging the bottom left corner of the component,
 * moving the component is done by clicking an edge and draggin the component to a new location,
 * and deleting the component is done by clicking the top right corner of the component.
 * @author Aaron Cherney
 * @version 1.0
 */




public class CalendarObject extends JPanel {
    private Point initialClick;
    private boolean isResizing;
    private static final int RESIZE_MARGIN = 10;
    private static final int DELETE_MARGIN = 10;
    private static final Color RESIZE_COLOR = Color.LIGHT_GRAY;
    private int originalWidth;
    private int originalHeight;
    private double scale = 1.0;
    private int ARC_RADIUS = 10;

    private JTable eventTable;
    private JScrollPane scrollPane;
    private DefaultTableModel tableModel;


    /**
    * Application name.
    */
    private static final String APPLICATION_NAME = "Google Calendar API";
    /**
    * Global instance of the JSON factory.
    */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
    * Directory to store authorization tokens for this application.
    */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
    * Global instance of the scopes required by this quickstart.
    * If modifying these scopes, delete your previously saved tokens/ folder.
    */
    private static final List<String> SCOPES =
        Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    /**
    * Creates an authorized Credential object.
    *
    * @param HTTP_TRANSPORT The network HTTP Transport.
    * @return An authorized Credential object.
    * @throws IOException If the credentials.json file cannot be found.
    */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
        throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendarTest.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
        throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    public List<Event> listWeeksEvents() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service =
            new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    
        DateTime now = new DateTime(System.currentTimeMillis() - (System.currentTimeMillis() % (24L * 60 * 60 * 1000))); // Midnight today
        DateTime oneWeekLater = new DateTime(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000);
        Events events = service.events().list("primary")
            .setMaxResults(50)
            .setTimeMin(now)
            .setTimeMax(oneWeekLater)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();
        List<Event> eventList = events.getItems();
        if (eventList.isEmpty()) {
            System.out.println("No upcoming events found.");
            return Collections.emptyList();
        } else {
            for (Event event : eventList) {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                start = event.getStart().getDate();
            }
            System.out.printf("%s (%s)\n", event.getSummary(), start);

            }
            return eventList;
        }
    }

    // Helper method to format time in AM/PM format
    private static String formatTime(int hour) {
        int displayHour = (hour % 12 == 0) ? 12 : hour % 12;
        String period = (hour < 12) ? "AM" : "PM";
        return displayHour + " " + period;
    }

    private int getRowIndexForTime(Date eventDate) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(eventDate);
        int eventHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
    
        // The first row (index 0) is the header row, so shift the index by +1
        return eventHour + 1;
    }

    public void populateTable(List<Event> events) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE"); // "Sunday"
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd"); // "5/11"
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a"); // "12:30 PM"
    
        if (events == null || events.isEmpty()) {
            System.out.println("No events to populate.");
            return;
        }
    
        // Clear existing table data
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
    
        // Generate next 7 days as columns
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        String[] columns = new String[8]; // 1 extra for "Time" column
        columns[0] = "Time";
    
        for (int i = 0; i < 7; i++) {
            String dayName = dayFormat.format(calendar.getTime());
            String date = dateFormat.format(calendar.getTime());
            columns[i + 1] = dayName + " - " + date;
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
    
        tableModel.setColumnIdentifiers(columns);
        //tableModel.addRow(columns); // Add header row if I decide to not use a JScrollPane
    
        // Special row for all-day events
        Object[] allDayRow = new Object[8];
        allDayRow[0] = "All Day"; // Label for the first column
        for (int i = 1; i < 8; i++) {
            allDayRow[i] = ""; // Initialize empty slots for events
        }
        tableModel.addRow(allDayRow);
    
        // Populate the "Time" column from 12 AM to 11 PM
        int totalHours = 24;
        for (int hour = 0; hour < totalHours; hour++) {
            String timeLabel = formatTime(hour);
            tableModel.addRow(new Object[]{timeLabel, "", "", "", "", "", "", ""});
        }
    
        // Initialize the cell renderer
        EventTableRenderer renderer = new EventTableRenderer();
        eventTable.setDefaultRenderer(Object.class, renderer);
    
        Random random = new Random();
        for (Event event : events) {
            DateTime startDateTime = event.getStart().getDateTime();
            DateTime endDateTime = event.getEnd().getDateTime();
            boolean isAllDay = false;
    
            if (startDateTime == null) { // All-day event detected
                isAllDay = true;
                startDateTime = event.getStart().getDate();
                if (startDateTime != null) {
                    startDateTime = new DateTime(startDateTime.getValue() + 24L * 60 * 60 * 1000); // Add 1 day
                }
                endDateTime = event.getEnd() != null ? event.getEnd().getDate() : startDateTime;
            }
    
            if (startDateTime == null) continue;
    
            Date eventStart = new Date(startDateTime.getValue());
            Date eventEnd = endDateTime != null ? new Date(endDateTime.getValue()) : eventStart;
    
            String eventDay = dayFormat.format(eventStart);
            int dayIndex = -1;
            for (int i = 1; i <= 7; i++) {
                if (columns[i].startsWith(eventDay)) {
                    dayIndex = i;
                    break;
                }
            }
            if (dayIndex == -1) continue;
    
            int startRow = isAllDay ? 0 : getRowIndexForTime(eventStart); // Row 1 for all-day events
            int endRow = isAllDay ? 0 : getRowIndexForTime(eventEnd);
    
            if (startRow == -1 || endRow == -1) continue;
    
            Color eventColor = new Color(100 + (int)(Math.random() * 156), 100 + (int)(Math.random() * 156), 100 + (int)(Math.random() * 156));
            for (int row = startRow; row <= endRow; row++) {
                renderer.addEventCell(row, dayIndex, eventColor);
            }
    
            Object existingValue = tableModel.getValueAt(startRow, dayIndex);
            String newValue = (existingValue == null || existingValue.toString().isEmpty()) ? event.getSummary() : existingValue + ", " + event.getSummary();
            tableModel.setValueAt(newValue, startRow, dayIndex);
        }
    
        tableModel.fireTableDataChanged();
    }

    
    public CalendarObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        setBackground(color);
        setBounds(xPos, yPos, width, height);
        setOpaque(false);
        
        setLayout(null);

        // Table setup
        String[] columnNames = {"Day", "Time", "Event"};
        tableModel = new DefaultTableModel(columnNames, 0);
        eventTable = new JTable(tableModel);
        eventTable.setEnabled(false);
        eventTable.setBackground(new Color(229, 228, 226));
        eventTable.setForeground(Color.WHITE);
        eventTable.setShowGrid(true);
        eventTable.setGridColor(Color.DARK_GRAY);
        eventTable.setIntercellSpacing(new Dimension(0, 0));
        eventTable.getTableHeader().setReorderingAllowed(false);
        eventTable.getTableHeader().setBackground(Color.WHITE);
        eventTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.BLACK));
        eventTable.getTableHeader().setForeground(Color.BLACK);
        eventTable.setBounds(0,0,width,height);
        
        
        // ScrollPane setup to show only 12 rows at a time
        eventTable.setRowHeight(30);
        int visibleRows = 12;
        int tableHeight = visibleRows * eventTable.getRowHeight();
        eventTable.setPreferredScrollableViewportSize(new Dimension(width, tableHeight));

        scrollPane = new JScrollPane(eventTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); // Hide scrollbar width

        // Enable scrolling with the mouse wheel
        scrollPane.addMouseWheelListener(e -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getValue() + e.getWheelRotation() * verticalBar.getUnitIncrement() * 40);
        });
        scrollPane.setEnabled(false);
        
        scrollPane.setBounds(0, 0, width, tableHeight);
        add(scrollPane, BorderLayout.CENTER);
        
        // Populate the table
        try {
            List<Event> eventList = listWeeksEvents();
            populateTable(eventList);
            tableModel.fireTableDataChanged();
            revalidate();
            repaint();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

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
        int fontSize = Math.max(1, (int) Math.round(12));
        eventTable.setFont(new Font("Arial", Font.PLAIN, fontSize));
        eventTable.setBounds(0, 0, getWidth() - 10, getHeight() - 10);
        scrollPane.setFont(new Font("Arial", Font.PLAIN, fontSize));
        scrollPane.setBounds(0, 0, getWidth() - 10, getHeight() - 10);
        repaint();
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
        g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, ARC_RADIUS, ARC_RADIUS);

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

    public void repaintInside() {
        updateTextStyle();
        repaint();
    }

    //get a click in top right corner and delete all components inside and then delete the sticky note....
    public void delete(){
        removeAll(); // remove all child components first just to be safe :)
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
            parent.revalidate();
            parent.repaint();
        }
    }

    


}
