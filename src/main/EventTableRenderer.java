import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

//Dashed lines for half hour's only
//lighter border colors on first column and header row
//header row same background as default cell

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
            
            // Adjust text color for readability
            double brightness = (0.299 * bgColor.getRed()) + (0.587 * bgColor.getGreen()) + (0.114 * bgColor.getBlue());
            Color textColor = (brightness > 128) ? Color.BLACK : Color.WHITE;
            //Event cells
            cell.setForeground(textColor);
            cell.setFont(new Font("Aptos", Font.PLAIN, 12));
            cell.setHorizontalAlignment(SwingConstants.CENTER);
            cell.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(39, 139, 215)));
        } else {
            if(column == 0){ //First column cell
                cell.setBackground(Color.WHITE);
                cell.setForeground(Color.BLACK);
                cell.setFont(new Font("Aptos", Font.PLAIN, 12));
                if(row % 2 == 0){
                    Border matteBorder = BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY); //left, right
                    Border empty = BorderFactory.createEmptyBorder(0, -1, -1, -1);
                    Border dashed = BorderFactory.createDashedBorder(Color.GRAY, 1, 1);
                    Border compound = new CompoundBorder(empty, dashed);
                    Border doubleCompound = new CompoundBorder(compound, matteBorder);
                    cell.setBorder(doubleCompound);
                }else{
                    

                    cell.setBorder(BorderFactory.createMatteBorder(1, 1, 0, -1, Color.LIGHT_GRAY)); // Top, left, right
                    
                    
                }
                
            }else{//Default cell
                cell.setBackground(new Color(250, 249, 248));
                if(row % 2 == 0){
                    Border matteBorder = BorderFactory.createMatteBorder(0, 1, 0, 1, Color.LIGHT_GRAY); //left, right
                    Border empty = BorderFactory.createEmptyBorder(0, -1, -1, -1);
                    Border dashed = BorderFactory.createDashedBorder(Color.GRAY, 1, 1);
                    Border compound = new CompoundBorder(empty, dashed);
                    Border doubleCompound = new CompoundBorder(compound, matteBorder);
                    cell.setBorder(doubleCompound);
                }else{
                    cell.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.LIGHT_GRAY)); // Top, left, right
                }
            }
            
        }
        
        return cell;
    }
}