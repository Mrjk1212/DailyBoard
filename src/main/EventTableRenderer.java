import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class EventTableRenderer extends DefaultTableCellRenderer {
    private final Map<Point, Color> eventCells = new HashMap<>();

    public void addEventCell(int row, int col, Color color) {
        eventCells.put(new Point(row, col), color);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cell.setBackground(eventCells.getOrDefault(new Point(row, column), Color.GRAY)); // Default white if no event
        return cell;
    }
}