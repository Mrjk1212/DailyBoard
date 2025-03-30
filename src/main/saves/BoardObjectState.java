package main.saves;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;

import javax.swing.JTextField;

public class BoardObjectState {
    public String type; // "StickyNote" or "Calendar"
    public int x, y, width, height;
    public int colorR, colorG, colorB; // Store color as RGB
    public String text; // For sticky notes // Store file location for whiteboards too !
    public List<String> todoListStrings;
    public String Title;
    public Date GoalDate;
    public int OriginalWidth;
    public int OriginalHeight;

    // Constructor
    public BoardObjectState(String type, int x, int y, int width, int height, int orignalWidth, int originalHeight, Color color, String text, List<String> todoList, String title, Date goalDate) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.OriginalWidth = orignalWidth;
        this.OriginalHeight = originalHeight;
        this.colorR = color.getRed();
        this.colorG = color.getGreen();
        this.colorB = color.getBlue();
        this.text = text;
        this.todoListStrings = todoList;
        this.Title = title;
        this.GoalDate = goalDate;
    }

    // Convert stored color back to Color object
    public Color getColor() {
        return new Color(colorR, colorG, colorB);
    }
}
