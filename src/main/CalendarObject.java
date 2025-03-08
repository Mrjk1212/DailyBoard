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
    private JTextField textField;
    private JLabel displayLabel;
    private Point initialClick;
    private boolean isResizing;
    private static final int RESIZE_MARGIN = 10;
    private static final int DELETE_MARGIN = 10;
    private static final Color RESIZE_COLOR = Color.GRAY;
    private int originalWidth;
    private int originalHeight;
    private double scale = 1.0;


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

    public void listWeeksEvents() throws IOException, GeneralSecurityException {
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
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                start = event.getStart().getDate();
            }
            System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }
    }



    public CalendarObject(int xPos, int yPos, int width, int height, Color color) {
        originalWidth = width;
        originalHeight = height;
        setBackground(color);
        setBounds(xPos, yPos, width, height);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setLayout(null);

        //list events in terminal when added
        try {
            listWeeksEvents();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        // Create a JLabel to display text
        displayLabel = new JLabel("", SwingConstants.CENTER);
        displayLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        displayLabel.setBounds(5, 5, width - 10, height - 10);
        displayLabel.setHorizontalAlignment(JLabel.LEFT);
        displayLabel.setVerticalAlignment(JLabel.TOP);
        add(displayLabel);

        // Create a JTextField for input
        textField = new JTextField();
        textField.setBounds(5, 5, width - 10, height - 10);
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setBorder(null);
        textField.setVisible(false); // Initially hidden
        textField.setBackground(color);
        add(textField);

        // Focus listener to update label when user clicks away
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
        displayLabel.setFont(new Font("Arial", Font.PLAIN, fontSize));
        textField.setFont(new Font("Arial", Font.PLAIN, fontSize));
        displayLabel.setBounds(5, 5, getWidth() - 10, getHeight() - 10);
        textField.setBounds(5, 5, getWidth() - 10, getHeight() - 10);
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
        g.setColor(RESIZE_COLOR);
        g.fillRect(getWidth() - RESIZE_MARGIN, getHeight() - RESIZE_MARGIN, RESIZE_MARGIN , RESIZE_MARGIN);
        g.setColor(Color.RED);
        g.fillRect(getWidth() - DELETE_MARGIN, getHeight() - getHeight(), DELETE_MARGIN, DELETE_MARGIN);
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
