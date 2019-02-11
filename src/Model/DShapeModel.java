package Model;

import java.awt.*;
import java.util.ArrayList;

public class DShapeModel {
    protected Color color;
    protected Rectangle bounds;
    protected ArrayList<ModelListener> listeners = new ArrayList<>();

    private int id;

    public DShapeModel(){
        this(10, 10, 20, 20, Color.GRAY); // default settings
    }

    public DShapeModel(int x, int y, int width, int height, Color color){
        bounds = new Rectangle(x, y, width, height);
        this.color = color;
    }

    public void mimic(DShapeModel other) {
        setID(other.getID());
        setBounds(other.getBounds());
        setColor(other.getColor());
        notifyListeners();
    }

    public void setID(int id){this.id = id;}

    public int getID(){return id;}

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        notifyListeners();
    }

    public void setBounds(Rectangle newBounds){
        bounds = new Rectangle(newBounds);
        notifyListeners();
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void move(int dx, int dy){
        bounds.x += dx;
        bounds.y += dy;
        notifyListeners();
    }

    public void resize(Point anchorPoint, Point movingPoint){
        int x = (anchorPoint.x > movingPoint.x ? movingPoint.x : anchorPoint.x );
        int y = (anchorPoint.y > movingPoint.y ? movingPoint.y : anchorPoint.y);
        int width = Math.abs(movingPoint.x - anchorPoint.x);
        int height = Math.abs(movingPoint.y - anchorPoint.y);
        setBounds(new Rectangle(x, y, width, height)); // notifyListeners implicitly
    }

    public void addListener(ModelListener listener){
        listeners.add(listener);
    }

    public void removeListener(ModelListener listener){
        listeners.remove(listener);
    }

    public void notifyListeners(){
        for(ModelListener listener: listeners){
            listener.modelChanged(this);
        }
    }
}
