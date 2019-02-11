package View;

import Model.DLineModel;
import Model.DShapeModel;

import java.awt.*;
import java.util.ArrayList;

public class DLine extends DShape {
    private DLineModel lineModel = getModel();

    public DLine(DShapeModel model, Canvas canvas) {
        super(model, canvas);
    }

    @Override
    public DLineModel getModel(){ return (DLineModel) model; }

    @Override
    public void draw(Graphics g) {
        g.setColor(getColor());
        g.drawLine(lineModel.getP1().x, lineModel.getP1().y, lineModel.getP2().x, lineModel.getP2().y);
    }

    @Override
    public ArrayList<Point> getKnobPoints(){
        ArrayList<Point> knobPoints = new ArrayList<>();
        knobPoints.add(new Point(lineModel.getP1().x, lineModel.getP1().y));
        knobPoints.add(new Point(lineModel.getP2().x, lineModel.getP2().y));

        return knobPoints;
    }
}
