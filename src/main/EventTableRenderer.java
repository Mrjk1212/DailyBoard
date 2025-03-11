import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class EventTableRenderer extends DefaultTableCellRenderer {
    private final Map<Point, Color> eventCells = new HashMap<>();

    public void addEventCell(int row, int col, Color color) {
        eventCells.put(new Point(row, col), color);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Point key = new Point(row, column);
        if (eventCells.containsKey(key)) {
            Color bgColor = eventCells.get(key);
            cell.setBackground(bgColor);

            // Calculate brightness to determine text color
            double brightness = (0.299 * bgColor.getRed()) + (0.587 * bgColor.getGreen()) + (0.114 * bgColor.getBlue());
            Color textColor = (brightness > 128) ? Color.BLACK : Color.WHITE;
            
            cell.setForeground(textColor); // Set text color
            cell.setFont(new Font("Arial", Font.BOLD, 12)); // Make it bold for better readability
        } else {
            cell.setBackground(Color.GRAY);
            cell.setForeground(Color.WHITE);
        }

        cell.setHorizontalAlignment(SwingConstants.CENTER);
        return cell;
    }
}