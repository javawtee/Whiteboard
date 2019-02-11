package Model;

import java.awt.*;

public class DLineModel extends DShapeModel {
    protected Point p1, p2;

    public DLineModel(){
        super();
        p1 = new Point(getBounds().x, getBounds().y);
        p2 = new Point(getBounds().x + getBounds().width, getBounds().y + getBounds().height);
    }

    @Override
    public void mimic(DShapeModel other){
        DLineModel mimic = (DLineModel) other;
        setP1(mimic.getP1());
        setP2(mimic.getP2());
        super.mimic(other);
    }

    public Point getP1() {
        return p1;
    }

    public void setP1(Point p1) {
        this.p1 = p1;
    }

    public Point getP2() {
        return p2;
    }

    public void setP2(Point p2) {
        this.p2 = p2;
    }

    @Override
    public void move(int dx, int dy){
        p1.x += dx;
        p1.y += dy;
        p2.x += dx;
        p2.y += dy;
        super.move(dx, dy);
    }

    @Override
    public void resize(Point anchorPoint, Point movingPoint){
        p1 = (p1.equals(anchorPoint) ? anchorPoint : movingPoint);
        p2 = (p2.equals(anchorPoint) ? anchorPoint : movingPoint);
        super.resize(anchorPoint, movingPoint);
        fixLineBounds(p1, p2);
    }

    public void fixLineBounds(Point p1, Point p2){
        if(p1.x > p2.x && p2.y > p1.y || p2.x > p1.x && p1.y > p2.y) {
            int x = (p1.x > p2.x ? p2.x : p1.x);
            int y = (p2.y > p1.y ? p1.y : p2.y);
            int width = getBounds().width;
            int height = getBounds().height;
            setBounds(new Rectangle(x, y, width, height));
        }
    }

}
