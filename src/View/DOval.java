package View;

import Model.DShapeModel;

import java.awt.*;

public class DOval extends DShape {

    public DOval(DShapeModel model, Canvas canvas){
        super(model, canvas);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(getColor());
        g.fillOval(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
    }
}
