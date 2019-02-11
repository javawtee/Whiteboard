package Model;

import java.awt.*;

public class DTextModel extends DShapeModel {
    protected String text;
    protected String fontName;

    public DTextModel(){
        super(10, 10, 50, 20, Color.GRAY);
        text = "Hello";
        fontName = "Dialog";
    }

    @Override
    public void mimic(DShapeModel other) {
        DTextModel mimic = (DTextModel) other;
        setText(mimic.getText());
        setFontName(mimic.getFontName());
        super.mimic(other);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        notifyListeners();
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
        notifyListeners();
    }

    @Override
    public void resize(Point anchorPoint, Point movingPoint){
        int xLimit = Math.abs(movingPoint.x - anchorPoint.x);
        int yLimit = Math.abs(movingPoint.y - anchorPoint.y);
        if(xLimit >= 0 && yLimit >= 0) {
            super.resize(anchorPoint, movingPoint);
        }
    }
}
