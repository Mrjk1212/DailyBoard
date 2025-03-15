package main.saves;

import java.awt.Color;
import java.util.List;

import javax.swing.JTextField;

public class BoardObjectState {
    public String type; // "StickyNote" or "Calendar"
    public int x, y, width, height;
    public int colorR, colorG, colorB; // Store color as RGB
    public String text; // For sticky notes
    public List<String> todoListStrings;

    // Constructor
    public BoardObjectState(String type, int x, int y, int width, int height, Color color, String text, List<String> todoList) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.colorR = color.getRed();
        this.colorG = color.getGreen();
        this.colorB = color.getBlue();
        this.text = text;
        this.todoListStrings = todoList;
    }

    // Convert stored color back to Color object
    public Color getColor() {
        return new Color(colorR, colorG, colorB);
    }
}
