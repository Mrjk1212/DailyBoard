import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
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
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
/* 
TODO
- Add support for ICalendar file import
- Add support for Outlook calendar?
- 

===========BUGS===================
- Recurring All Day Events For ICal Files Don't Load!


*/

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.miginfocom.swing.MigLayout;

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
    private static final Color RESIZE_COLOR = Color.LIGHT_GRAY;
    private int originalWidth;
    private int originalHeight;
    private double scale = 1.0;
    private int ARC_RADIUS = 10;
    private JButton forwardWeekButton;
    private JButton backwardWeekButton;
    private JButton settingsButton;
    private JLabel calRange;

    private JTable eventTable;
    private JScrollPane scrollPane;
    private DefaultTableModel tableModel;

    private String calType;
    private String ICalFileLocation;

    private LocalDateTime currentDayStart;
    private LocalDateTime threeDaysLater;
    private String timeZone = "America/Chicago";

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
            .setApprovalPrompt("force") // Ensures new refresh token is granted
            .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        // Reuse existing credential instead of requesting a new one
        Credential credential = flow.loadCredential("user");
        if (credential != null && credential.getRefreshToken() != null) {
            System.out.println("Using stored refresh token.");
            credential.refreshToken(); // Manually refresh
        } else {
            //not using stored token
            credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }

        return credential;
    }

    public DateTime toGoogleDateTime(LocalDateTime startTime, String zoneId) {
        ZonedDateTime zdtSource = startTime.atZone(ZoneId.of(zoneId));
        Date date = Date.from(zdtSource.toInstant());
        return new DateTime(date, TimeZone.getTimeZone(ZoneId.of(zoneId)));
    }


    


    public List<Event> listWeeksEvents(LocalDateTime now, LocalDateTime threeDaysLater) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service =
            new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        //Convert Localdatetime into google API DateTime.
        DateTime googleNow = toGoogleDateTime(now, timeZone);
        DateTime googleThreeDaysLater = toGoogleDateTime(threeDaysLater, timeZone);
    
        Events events = service.events().list("primary")
            .setMaxResults(50)
            .setTimeMin(googleNow)  // Start from 3 days ago
            .setTimeMax(googleThreeDaysLater)  // End at 11:59:59 PM of 3 days from today
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();
    
        List<Event> eventList = events.getItems();
        if (eventList.isEmpty()) {
            System.out.println("No events found in this range.");
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
        int eventMinute = calendar.get(java.util.Calendar.MINUTE);
    
        // The first row (index 0) is the header row and the second row is All Day Events, so shift the index by +2
        return (eventHour * 2) + (eventMinute >= 30 ? 2 : 1);
    }

    public void populateTable(List<Event> events, LocalDateTime startDay) {
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

    //////////////////////////////////IMPORTANT SPOT///////////////////////////////////////////////////////////
        // Generate next 4 days as columns
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        String[] columns = new String[5]; // 1 extra for "Time" column
        columns[0] = "Time";
    
        for (int i = 0; i < 4; i++) {
            //String dayName = dayFormat.format(startDay.getdat());
            //String date = dateFormat.format(startDay.getDayOfMonth());
            columns[i + 1] = startDay.getDayOfWeek().getDisplayName(TextStyle.SHORT, getLocale()) + " - " + Integer.toString(startDay.getDayOfMonth());
            startDay = startDay.plusDays(1).withHour(23).withMinute(59).withSecond(59); //increase by one day?
            //calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
    
        tableModel.setColumnIdentifiers(columns);
    //////////////////////////////////IMPORTANT SPOT///////////////////////////////////////////////////////////
    
        // Special row for all-day events
        Object[] allDayRow = new Object[5];
        allDayRow[0] = "All Day"; // Label for the first column
        for (int i = 1; i < 5; i++) {
            allDayRow[i] = ""; // Initialize empty slots for events
        }
        tableModel.addRow(allDayRow);
    
        // Populate the "Time" column with 30-minute increments
        int totalHours = 24;
        for (int hour = 0; hour < totalHours; hour++) {
            String timeLabel = formatTime(hour);
            tableModel.addRow(new Object[]{timeLabel, "", "", "", "", "", "", ""}); // Full hour
            tableModel.addRow(new Object[]{"", "", "", "", "", "", "", ""}); // Half-hour
        }
    
        // Initialize the cell renderer
        EventTableRenderer renderer = new EventTableRenderer();
        eventTable.setDefaultRenderer(Object.class, renderer);
    
        // Set the current time label to be colored on the side bar
        DateTime nowDateTime = new DateTime(System.currentTimeMillis());
        Date now = new Date(nowDateTime.getValue());
        renderer.addEventCell(getRowIndexForTime(now), 0, new Color(190, 218, 240));
    
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
            for (int i = 1; i <= 4; i++) {
                if (columns[i].startsWith(eventDay)) {
                    dayIndex = i;
                    break;
                }
            }
            if (dayIndex == -1) continue;
    
            int startRow = isAllDay ? 0 : getRowIndexForTime(eventStart); // Row 1 for all-day events
            int endRow = isAllDay ? 0 : getRowIndexForTime(eventEnd);
    
            if (startRow == -1 || endRow == -1) continue;
    
            Color eventColor = new Color(190, 218, 240); // Close to light blue
            for (int row = startRow; row <= endRow; row++) {
                renderer.addEventCell(row, dayIndex, eventColor);
            }
    
            Object existingValue = tableModel.getValueAt(startRow, dayIndex);
            String newValue = (existingValue == null || existingValue.toString().isEmpty()) ? event.getSummary() : existingValue + ", " + event.getSummary();
            tableModel.setValueAt(newValue, startRow, dayIndex);
        }
        
        tableModel.fireTableDataChanged();
    }


    public List<Event> loadEventsFromICal(String filename, LocalDateTime startDate, LocalDateTime endDate) {
        List<Event> googleEvents = new ArrayList<>();

        try {
            InputStream ip = new FileInputStream(filename);
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar calendar = builder.build(ip);

            for (Object obj : calendar.getComponents(Component.VEVENT)) {
                net.fortuna.ical4j.model.Component icalEvent = (net.fortuna.ical4j.model.Component) obj;

                // Extract event properties
                String summary = icalEvent.getProperty(Property.SUMMARY).getValue();
                Property dtstartProp = icalEvent.getProperty(Property.DTSTART);
                Property dtendProp = icalEvent.getProperty(Property.DTEND);

                // Detect if DTSTART uses VALUE=DATE (all-day event)
                System.out.println("DTSTART: " + dtstartProp);
                boolean isAllDay = dtstartProp.toString().contains("VALUE=DATE");
                System.out.println("Is all day: " + isAllDay);

                // Extract DTSTART and DTEND values
                String start = dtstartProp.getValue();
                String end = (dtendProp != null) ? dtendProp.getValue() : start; // Default to same day if DTEND is missing

                DateTimeFormatter dateTimeFormatterWithTZ = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX"); // For times with 'Z' or offset
                DateTimeFormatter dateTimeFormatterNoTZ = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"); // For times without timezone
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd"); // For all-day events

                LocalDateTime startDateTime, endDateTime;
                ZoneId zoneId = ZoneId.of(timeZone); // Use America/Chicago time zone

                if (isAllDay) {
                        // For all-day events, use LocalDate to avoid time component
                        startDateTime = LocalDate.parse(start, dateFormatter).atStartOfDay(zoneId).toLocalDateTime(); 
                        endDateTime = LocalDate.parse(end, dateFormatter).atStartOfDay(zoneId).toLocalDateTime(); // End is the next day
                        
                        startDateTime = startDateTime.withHour(1).withMinute(00).withSecond(00);
                        endDateTime = endDateTime.withHour(1).withMinute(00).withSecond(00);
                        System.out.println("This is the event start: " + startDateTime);
                        System.out.println("This is the event end: " + endDateTime);
                } else {
                    // Handle timestamps with or without time zone
                    if (start.endsWith("Z") || start.matches(".*[+-]\\d{4}$")) {
                        // Has time zone
                        ZonedDateTime startZoned = ZonedDateTime.parse(start, dateTimeFormatterWithTZ);
                        ZonedDateTime endZoned = ZonedDateTime.parse(end, dateTimeFormatterWithTZ);
                        
                        // Convert to America/Chicago time zone
                        startDateTime = startZoned.withZoneSameInstant(zoneId).toLocalDateTime();
                        endDateTime = endZoned.withZoneSameInstant(zoneId).toLocalDateTime();
                    } else {
                        // No time zone, parse as LocalDateTime and assume America/Chicago time zone
                        startDateTime = LocalDateTime.parse(start, dateTimeFormatterNoTZ).atZone(zoneId).toLocalDateTime();
                        endDateTime = LocalDateTime.parse(end, dateTimeFormatterNoTZ).atZone(zoneId).toLocalDateTime();
                    }
                }

                // // Convert to Google API Event

                // Filter based on input date range
                if (!startDateTime.isBefore(startDate) && !endDateTime.isAfter(endDate)) {
                    // Convert to Google API Event
                    Event googleEvent = new Event().setSummary(summary);

                    if (isAllDay) {
                        // Use `setDate` instead of `setDateTime` for all-day events
                        googleEvent.setStart(new EventDateTime().setDate(new DateTime(startDateTime.toLocalDate().toString()))); // Only date, no time
                        googleEvent.setEnd(new EventDateTime().setDate(new DateTime(endDateTime.toLocalDate().toString()))); // Only date, no time
                        System.out.println("Found all day event: " + googleEvent.getSummary());
                    } else {
                        // Convert LocalDateTime to Google Calendar DateTime in America/Chicago timezone
                        DateTime googleStart = new DateTime(startDateTime.atZone(zoneId).toInstant().toEpochMilli());
                        DateTime googleEnd = new DateTime(endDateTime.atZone(zoneId).toInstant().toEpochMilli());

                        googleEvent.setStart(new EventDateTime().setDateTime(googleStart));
                        googleEvent.setEnd(new EventDateTime().setDateTime(googleEnd));
                    }

                    googleEvents.add(googleEvent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return googleEvents;
    }


    public void populateTableFromICal(List<Event> events){

        System.out.println("Made it into populateTableFromICal!");

    }

    //prompt to ask the user to use either google cal or import a Ical file.
    //select ical -> loadEventsFromICal("filename", LocalDateTime startDate, LocalDateTime endDate)
    //               inside return a list of all events inside the startDate and endDate
    //
    //              populateTableFromICal(List<Strings>)
    //              every 3 strings is a event!
    //              convert the DTStart to LocalDateTime
    //              converrt the DTEND to LocalDateTime
    //              populate the same way as a normal google cal api.

    

    
    public CalendarObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        setBackground(color);
        setOpaque(false);
        setBounds(xPos, yPos, width, height);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        calType = "";
        setLayout(new MigLayout("", "[grow, fill][grow, fill][grow, fill][grow, fill]", ""));


        JDialog dialog = new JDialog();
        dialog.setSize(200, 150);
        dialog.setLayout(new FlowLayout());
        dialog.add(new JLabel("Choose Your Calendar Method"));

        JButton googleCalOptionButton = new JButton("Google Calendar");
        JButton icalImportButton = new JButton("I-Calendar File");
        googleCalOptionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calType = "GoogleCalendar";
                dialog.dispose();
            }
        });
        icalImportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calType = "ICalFile";

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select an iCalendar (.ics) File");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                
                // Optional: Set a filter to only show .ics files
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("iCalendar Files (.ics)", "ics"));

                int userSelection = fileChooser.showOpenDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    ICalFileLocation = selectedFile.getAbsolutePath(); // Set global variable
                }

                dialog.dispose();
            }
        });
        dialog.add(googleCalOptionButton);
        dialog.add(icalImportButton);
        dialog.setVisible(true);
        



        final JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem(new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e){
                delete();
            }
        }));
        
    
        // Midnight at the start of today
        currentDayStart = LocalDateTime.now().withHour(00).withMinute(00).withSecond(00);
        
    
        // 11:59:59 PM of the third day from today
        threeDaysLater = currentDayStart.plusDays(3).withHour(23).withMinute(59).withSecond(59);

        forwardWeekButton = new JButton();
        backwardWeekButton = new JButton();
        // Make the button transparent
        forwardWeekButton.setContentAreaFilled(false);
        forwardWeekButton.setBorderPainted(false);
        forwardWeekButton.setFocusPainted(false);
        forwardWeekButton.setOpaque(false);
        forwardWeekButton.setText(">");
        // Make the button transparent
        backwardWeekButton.setContentAreaFilled(false);
        backwardWeekButton.setBorderPainted(false);
        backwardWeekButton.setFocusPainted(false);
        backwardWeekButton.setOpaque(false);
        backwardWeekButton.setText("<");

        forwardWeekButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e){
                //call function to go forward 4 days.
                // Populate the table
                currentDayStart = currentDayStart.plusDays(4).withHour(00).withMinute(00).withSecond(00);// now equal to 3 days from now.
                threeDaysLater = currentDayStart.plusDays(3).withHour(23).withMinute(59).withSecond(59);// now equal to current time(e.g. current time in increments of 3 days) + 3 days.

                try {

                    if(calType == "GoogleCalendar"){
                        List<Event> eventList = listWeeksEvents(currentDayStart, threeDaysLater);
                        populateTable(eventList, currentDayStart);
                    }

                    else if(calType == "ICalFile"){
                        List<Event> eventList = loadEventsFromICal(ICalFileLocation,currentDayStart, threeDaysLater);
                        populateTable(eventList, currentDayStart);
                    }

                    tableModel.fireTableDataChanged();
                    eventTable.getColumnModel().getColumn(0).setHeaderValue(""); //Display Month in the first row first col -> 0,0
                    calRange.setText(currentDayStart.getMonth().getDisplayName(TextStyle.FULL, getLocale()) + " " 
                    + Integer.toString(currentDayStart.getDayOfMonth()) + "-" 
                    + threeDaysLater.getMonth().getDisplayName(TextStyle.FULL, getLocale()) 
                    + " " + Integer.toString(threeDaysLater.getDayOfMonth()));


                    revalidate();
                    repaint();
                } catch (IOException | GeneralSecurityException errorGoingForward) {
                    errorGoingForward.printStackTrace();
                }

            }
        });
        backwardWeekButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e){
                //call function to go backwards 4 days.
                // Populate the table
                currentDayStart = currentDayStart.minusDays(4).withHour(00).withMinute(00).withSecond(00);// now equal to 3 days from now.
                threeDaysLater = currentDayStart.plusDays(3).withHour(23).withMinute(59).withSecond(59);// now equal to current time(e.g. current time in increments of 3 days) + 3 days.

                try {

                    if (calType == "GoogleCalendar"){
                        List<Event> eventList = listWeeksEvents(currentDayStart, threeDaysLater);
                        populateTable(eventList, currentDayStart);
                    }
                    else if(calType == "ICalFile"){
                        List<Event> eventList = loadEventsFromICal(ICalFileLocation,currentDayStart, threeDaysLater);
                        populateTable(eventList, currentDayStart);
                    }
                    tableModel.fireTableDataChanged();
                    eventTable.getColumnModel().getColumn(0).setHeaderValue(""); //Display Month in the first row first col -> 0,0
                    calRange.setText(currentDayStart.getMonth().getDisplayName(TextStyle.FULL, getLocale()) + " " 
                    + Integer.toString(currentDayStart.getDayOfMonth()) + "-" 
                    + threeDaysLater.getMonth().getDisplayName(TextStyle.FULL, getLocale()) 
                    + " " + Integer.toString(threeDaysLater.getDayOfMonth()));
                    
                    revalidate();
                    repaint();
                } catch (IOException | GeneralSecurityException errorGoingBackward) {
                    errorGoingBackward.printStackTrace();
                }
            }
        });

        calRange = new JLabel();
        calRange.setOpaque(false);
        calRange.setBackground(color);
        calRange.setForeground(Color.BLACK);
        calRange.setVisible(true);
        calRange.setFont(new Font("Arial", Font.BOLD, 12));

        calRange.setText(currentDayStart.getMonth().getDisplayName(TextStyle.FULL, getLocale()) + " " 
        + Integer.toString(currentDayStart.getDayOfMonth()) + "-" 
        + threeDaysLater.getMonth().getDisplayName(TextStyle.FULL, getLocale()) 
        + " " + Integer.toString(threeDaysLater.getDayOfMonth()));

        
        add(calRange, "growx, span 1,h 30!");
        add(backwardWeekButton, "gapleft push, span 1, h 30!");
        add(forwardWeekButton, "gapright push, span 1, h 30!");
        
        calRange.setPreferredSize(new Dimension(30, 30));
        backwardWeekButton.setPreferredSize(new Dimension(30, 30));
        forwardWeekButton.setPreferredSize(new Dimension(30, 30));

        //settings button here ("gap left push, span 1, wrap")
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
        settingsButton.setPreferredSize(new Dimension(30, 30));
        settingsButton.setText("...");
        add(settingsButton, "span 1, wrap, h 30!");

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setBackground(Color.GRAY);
        sep.setForeground(Color.LIGHT_GRAY);
        add(sep, "grow, span, wrap");

        // Table setup
        String[] columnNames = {"Day", "Time", "Event"};
        tableModel = new DefaultTableModel(columnNames, 0);
        eventTable = new JTable(tableModel);
        eventTable.setEnabled(false);
        eventTable.setBackground(Color.RED);
        eventTable.setShowGrid(false);
        eventTable.setBorder(BorderFactory.createEmptyBorder());
        eventTable.setIntercellSpacing(new Dimension(0, 0));
        eventTable.getTableHeader().setReorderingAllowed(false);
        eventTable.getTableHeader().setBackground(new Color(250, 249, 248));
        eventTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        eventTable.getTableHeader().setForeground(Color.BLACK);
        eventTable.getTableHeader().setFont(new Font("Aptos", Font.PLAIN, 14));        

        // ScrollPane setup to show only 12 rows at a time
        eventTable.setRowHeight(30);
        int visibleRows = 12;
        int tableHeight = visibleRows * eventTable.getRowHeight();
        eventTable.setPreferredScrollableViewportSize(new Dimension(width, tableHeight));

        scrollPane = new JScrollPane(eventTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); // Hide scrollbar width
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Enable scrolling with the mouse wheel
        scrollPane.addMouseWheelListener(e -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getValue() + e.getWheelRotation() * verticalBar.getUnitIncrement() * 40);
        });
        scrollPane.setEnabled(false);

        add(scrollPane, "span");
        
        // Populate the table
        try {

            if (calType == "GoogleCalendar"){
                List<Event> eventList = listWeeksEvents(currentDayStart, threeDaysLater);
                populateTable(eventList, currentDayStart);
            }
            else if (calType == "ICalFile"){
                List<Event> eventList = loadEventsFromICal(ICalFileLocation,currentDayStart, threeDaysLater);
                populateTable(eventList, currentDayStart);
            }
            
            tableModel.fireTableDataChanged();
            eventTable.getColumnModel().getColumn(0).setHeaderValue(""); //Display Month in the first row first col -> 0,0
            calRange.setText(currentDayStart.getMonth().getDisplayName(TextStyle.FULL, getLocale()) + " " 
            + Integer.toString(currentDayStart.getDayOfMonth()) + "-" 
            + threeDaysLater.getMonth().getDisplayName(TextStyle.FULL, getLocale()) 
            + " " + Integer.toString(threeDaysLater.getDayOfMonth()));

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

    public void setOriginalHeight(int newOriginalHeight){
        this.originalHeight = newOriginalHeight;
    }

    public void setOriginalWidth(int newOriginalWidth){
        this.originalWidth = newOriginalWidth;
    }

    public void setScale(double newScale) {
        this.scale = newScale;
    }
    
    private void updateTextStyle() {
        settingsButton.setBounds(500, 5, 50, 30);
        forwardWeekButton.setBounds(400, 5, 50, 30);
        backwardWeekButton.setBounds(300, 5, 50, 30);
        calRange.setBounds(5, 5, 200, 30);
        scrollPane.setBounds(5, 50, getWidth() - 10, getHeight() - 10);
        repaint();
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
            ((CanvasPanel) parent).removeCalendarObject(this);// Notify CanvasPanel
        }
    }


}
