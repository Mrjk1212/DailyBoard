package main.saves;

public class BoardScaleState {
    
    public double Scale;
    public int OffsetX;
    public int OffsetY;
    public boolean darkMode;

    public BoardScaleState(double scale, int offsetX, int offsetY, boolean darkMode){
        this.Scale = scale;
        this.OffsetX = offsetX;
        this.OffsetY = offsetY;
        this.darkMode = darkMode;
    }




}
