import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

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
-Everything

*/

/**
 * CalendarObject represents a resizable and draggable component that allows
 * users to input a google calendar link.
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
    
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
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
            return null;
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

    public void populateTable(List<Event> events) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE"); // Day of the week
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a"); // 12-hour format
    
        if (events == null || events.isEmpty()) {
            System.out.println("No events to populate.");
            return;
        }
    
        for (Event event : events) {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                start = event.getStart().getDate();
            }
    
            String day = dayFormat.format(new Date(start.getValue()));
            String time = (start.getTimeZoneShift() == 0) ? "All Day" : timeFormat.format(new Date(start.getValue()));
            String summary = event.getSummary();
    
            System.out.println("Adding to table: " + day + " | " + time + " | " + summary);
    
            tableModel.addRow(new Object[]{day, time, summary});
        }
        tableModel.fireTableDataChanged();
    }

    public CalendarObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        setBackground(color);
        setBounds(xPos, yPos, width, height);
        setOpaque(false);
        //setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setLayout(null);



        // Table setup
        String[] columnNames = {"Day", "Time", "Event"};
        tableModel = new DefaultTableModel(columnNames, 0);
        eventTable = new JTable(tableModel);
        eventTable.setEnabled(false);
        eventTable.setBackground(Color.GRAY);
        eventTable.setForeground(Color.WHITE);
        eventTable.setBounds(0,0,width,height);
        
        
        add(eventTable);

        // Populate the table
        try {
            List<Event> eventList = listWeeksEvents();
            populateTable(eventList);
            // Ensure the table updates after adding events
            tableModel.fireTableDataChanged();
            revalidate();
            repaint();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        // Focus listener to update label when user clicks away
        /* Maybe implement listeners later if i want to change events from this application
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                saveText();
            }
        });

        // Key listener to save text on Enter key press
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveText();
                }
            }
        });
        

        // Click event to allow text editing
        displayLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                enterEditMode();
            }
        });
        */

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

    /*
    private void saveText() {
        String text = textField.getText().trim();
        if (!text.isEmpty()) {
            displayLabel.setText("<html><body style='text-align:center'>" + text.replace("\n", "<br>") + "</body></html>");
        }
        textField.setVisible(false);
        displayLabel.setVisible(true);
    }

    private void enterEditMode() {
        textField.setText(displayLabel.getText().replaceAll("<[^>]*>", "")); // Remove HTML formatting
        textField.setVisible(true);
        displayLabel.setVisible(false);
        textField.requestFocus();
    }
    */
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
        int fontSize = Math.max(1, (int) Math.round(12 * scale));
        eventTable.setFont(new Font("Arial", Font.PLAIN, fontSize));
        eventTable.setBounds(0, 0, getWidth() - 10, getHeight() - 10);
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
