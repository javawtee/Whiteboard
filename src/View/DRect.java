package View;

import Model.DShapeModel;

import java.awt.*;

public class DRect extends DShape {

    public DRect(DShapeModel model, Canvas canvas) {
        super(model, canvas);
    }

    @Override
    public void draw(Graphics g){
        g.setColor(getColor());
        g.fillRect(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
    }
}
