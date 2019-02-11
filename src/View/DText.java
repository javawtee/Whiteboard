package View;

import Model.DShapeModel;
import Model.DTextModel;

import java.awt.*;

public class DText extends DShape {
    private final static double INIT_FONT_SIZE = 1.0;

    private boolean modelChanged = true;
    private int lastHeight;
    private Font font;

    DTextModel textModel = (DTextModel) model;

    public DText(DShapeModel model, Canvas canvas) {
        super(model, canvas);
        lastHeight = (int) textModel.getBounds().getHeight();
        font = new Font(textModel.getFontName(), Font.PLAIN, (int)INIT_FONT_SIZE);
    }

    @Override
    public void draw(Graphics g) {
        Shape clip = g.getClip();
        g.setClip(clip.getBounds().createIntersection(getBounds()));

        Font font = computeFont(g);
        g.setFont(font);
        g.setColor(getColor());

        g.drawString(textModel.getText(), getBounds().x - 1 , (getBounds().y + getBounds().height *3/4 ));

        g.setClip(clip);
    }

    public Font computeFont(Graphics g) {
        double size;
        double lastSize;
        if(modelChanged) {
            size = INIT_FONT_SIZE;
            lastSize = size;
            while (true) {
                font = new Font(textModel.getFontName(), Font.PLAIN, (int) size);
                if (font.getLineMetrics(textModel.getText(),
                        ((Graphics2D) g).getFontRenderContext()).getHeight() > getModel().getBounds().getHeight())
                    break;
                lastSize = size;
                size = (size * 1.1) + 1;
            }
            font = new Font(textModel.getFontName(), Font.PLAIN, (int) lastSize);
            modelChanged = false;
        }
        return font; // can't be null

    }

    @Override
    public void modelChanged(DShapeModel model){
        if(model.getBounds().getHeight() != lastHeight
                || !((DTextModel)model).getFontName().equals(font.getFontName())) {
            lastHeight = (int) model.getBounds().getHeight();
            modelChanged = true;
        }
        super.modelChanged(model);
    }

}
