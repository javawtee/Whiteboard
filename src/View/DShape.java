package View;

import Model.DShapeModel;
import Model.ModelListener;

import java.awt.*;
import java.util.ArrayList;

public abstract class DShape implements ModelListener {
    protected DShapeModel model; //pointer
    protected Canvas canvas;

    protected final int knobSize = 9;
    protected final Color knobColor = Color.BLACK;
    //protected ArrayList<Point> knobs;


    public DShape(DShapeModel model, Canvas canvas){
        this.model = model;
        this.canvas = canvas;
        model.addListener(this);
    }

    public DShapeModel getModel(){ return this.model; }

    public void setColor(Color color){
        model.setColor(color);
    }

    public Color getColor(){ return model.getColor(); }

    public void setBounds(Rectangle newBounds){
        model.setBounds(newBounds);
    }

    public Rectangle getBounds(){ return model.getBounds(); }

    public ArrayList<Point> getKnobPoints(){
        ArrayList<Point> knobPoints = new ArrayList<>();
        knobPoints.add(new Point(model.getBounds().x, model.getBounds().y));
        knobPoints.add(new Point(model.getBounds().x + model.getBounds().width, model.getBounds().y));
        knobPoints.add(new Point(model.getBounds().x + model.getBounds().width, model.getBounds().y + model.getBounds().height));
        knobPoints.add(new Point(model.getBounds().x, model.getBounds().y + model.getBounds().height));

        return knobPoints;
    }

    public ArrayList<Rectangle> drawKnobs(Graphics g){
        ArrayList<Rectangle> knobs = new ArrayList<>();
        for(Point p: getKnobPoints()){
            Rectangle knob = new Rectangle(p.x - knobSize/2, p.y - knobSize/2, knobSize, knobSize);
            g.setColor(knobColor);
            ((Graphics2D)g).fill(knob);
            knobs.add(knob);
        }

        return knobs;
    }

    public void move(int dx, int dy){
        model.move(dx, dy);
    }

    public void resize(Point anchorPoint, Point movingPoint ){
        model.resize(anchorPoint, movingPoint);
    }

    public abstract void draw(Graphics g);

    @Override
    public void modelChanged(DShapeModel model) {
        canvas.repaint();
    }
}
