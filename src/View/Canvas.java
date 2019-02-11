package View;

import Model.*;
import Main.Whiteboard;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

public class Canvas extends JPanel implements ModelListener{
    private Whiteboard whiteboard;

    //for use of clientMode
    private int id = 1;

    private LinkedList<DShape> shapes = new LinkedList<>();

    public LinkedList<DShape> getShapes(){return shapes;}

    // for clientMode
    public DShape getShapeFromModelID(int modelID){
        for(DShape shape: shapes){
            if(shape.getModel().getID() == modelID) {
                return shape;
            }
        }
        return null; // can't happen
    }

    public void createNewShapes() {
        this.shapes = new LinkedList<>();
    }

    public DShapeModel[] getShapeModels() {
        DShapeModel[] models = new DShapeModel[shapes.size()];
        for(int i = 0; i < models.length; i++){
            models[i] = shapes.get(i).getModel();
        }
        return models;
    }

    private DShape selectedShape;

    public void setSelectedShape(DShape shape){selectedShape = shape;}

    public DShape getSelectedShape(){return selectedShape;}

    private ArrayList<Rectangle> knobs = new ArrayList<>();

    private Point anchorPoint, movingPoint, lastPoint;
    private boolean isDraggable;

    public Canvas(Whiteboard whiteboard){
        initCanvas();
        this.whiteboard = whiteboard;
    }

    public void initCanvas(){
        new JPanel();
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400,400));
        setVisible(true);
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    MouseInputAdapter listener = new MouseInputAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            selectShape(e);
        }

        @Override
        public void mouseReleased(MouseEvent e){
            super.mouseReleased(e);
            if(!whiteboard.isClient()) {
                lastPoint = null;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e){
            super.mouseDragged(e);
            if(selectedShape != null && !whiteboard.isClient()) {
                int dx = e.getX() - lastPoint.x;
                int dy = e.getY() - lastPoint.y;
                lastPoint = e.getPoint();
                if (isDraggable) { // move the selected shape
                    selectedShape.move(dx, dy);
                } else { // resize the selected shape
                    movingPoint.x += dx;
                    movingPoint.y += dy;
                    selectedShape.resize(anchorPoint, movingPoint);
                }
            }
        }
    };

    private Point getAnchorPoint(){
        //movingPoint and selectedShape != null
        Point centerPoint = new Point((int)selectedShape.getBounds().getCenterX(), (int)selectedShape.getBounds().getCenterY());
        int x = (movingPoint.x > centerPoint.x ?
                movingPoint.x - selectedShape.getBounds().width : movingPoint.x + selectedShape.getBounds().width);
        int y = (movingPoint.y > centerPoint.y ?
                movingPoint.y - selectedShape.getBounds().height : movingPoint.y + selectedShape.getBounds().height);
        return new Point(x, y);
    }

    private void selectShape(MouseEvent e){
        //don't allow clients to edit canvas
        if(whiteboard.isClient())
            return;

        movingPoint = null;
        anchorPoint = null;
        lastPoint = e.getPoint();
        if(selectedShape != null){
            //determine if the mouse clicked is on any knob, knobs is filled
            for(Rectangle knob: knobs){
                if(knob.contains(e.getPoint())){
                    movingPoint = new Point((int)knob.getCenterX(), (int)knob.getCenterY());
                    anchorPoint = getAnchorPoint();
                    isDraggable = false;
                    // knobs can't be overlapped, so break to stop checking
                    break;
                }
            }
        }
        if(movingPoint == null) {
            if (shapes.size() > 0) {
                selectedShape = null;
                for (DShape shape : shapes) {
                    if (shape.getBounds().contains(e.getPoint())) {
                        selectedShape = shape;
                        isDraggable = true;
                        // dont need to break the loop because 2 more or shapes overlap,
                        // choose the topmost shape
                    }
                }
            }
        }
        updateScriptMenu();
        repaint();
    }

    public void addShape(DShapeModel model){
        DShape shape = null;
        if(model instanceof DRectModel){
            shape = new DRect(model, this);
        } else if (model instanceof DOvalModel){
            shape = new DOval(model, this);
        } else if (model instanceof DLineModel){
            shape = new DLine(model, this);
        } else if (model instanceof DTextModel){
            shape = new DText(model, this);
        }
        shapes.add(shape);
        model.addListener(this);
        model.setID(id);
        id++;

        if(whiteboard.isServer())
            whiteboard.send("add", shape.getModel());

        //don't set selectedShape for clientMode
        if(!whiteboard.isClient()) // can be either normal or server mode
            selectedShape = shape;

        //if user adds DText, JTextField will be default loaded
        updateScriptMenu();
        //update JTable on whiteboard
        whiteboard.getTableModel().addRow(model);
        setJTableSelection(model);

        repaint();
    }

    public void setJTableSelection(DShapeModel model){
        // selectedShape != null
        int index = whiteboard.getTableModel().getModels().indexOf(model);
        whiteboard.getDataTable().setRowSelectionInterval(index, index);
    }

    public void setColor(Color pickedColor){
        if(selectedShape != null){
            selectedShape.setColor(pickedColor);
        }
    }

    public void removeShape(){
        if(selectedShape != null){
            shapes.remove(selectedShape);
            selectedShape.getModel().removeListener(this);

            if(whiteboard.isServer())
                whiteboard.send("remove", selectedShape.getModel());

            whiteboard.getTableModel().removeRow(selectedShape.getModel());

            if(selectedShape.getModel() instanceof DTextModel) {
                selectedShape = null;
                updateScriptMenu();
            }
            repaint();
        }
    }

    public void moveTo(boolean isToFront){
        if(selectedShape != null)
        {
            String cmd;
            shapes.remove(selectedShape);
            if(isToFront) {
                shapes.addLast(selectedShape);
                cmd = "front";
            }else{
                shapes.addFirst(selectedShape);
                cmd = "back";
            }

            if(whiteboard.isServer())
                whiteboard.send(cmd, selectedShape.getModel());

            whiteboard.getTableModel().moveTo(selectedShape.getModel(), isToFront);
            repaint();
        }
    }

    public void removeKnobs(){
        if(!knobs.isEmpty())
           knobs.clear();
    }

    public void updateScriptMenu(){
        if(selectedShape != null){
            if (selectedShape.getModel() instanceof DTextModel) {
                //update script text field
                whiteboard.getScriptTxtField().setText(((DTextModel) selectedShape.getModel()).getText());
                whiteboard.getScriptComboBox().setSelectedItem(((DTextModel) selectedShape.getModel()).getFontName());
                whiteboard.getScriptTxtField().setEnabled(true);
                whiteboard.getScriptComboBox().setEnabled(true);
                return;
            }
        }
        //reset to default
        whiteboard.getScriptTxtField().setText("");
        whiteboard.getScriptTxtField().setEnabled(false);
        whiteboard.getScriptComboBox().setSelectedIndex(-1);
        whiteboard.getScriptComboBox().setEnabled(false);
    }

    public void updateScriptText(String newText){
        if(selectedShape != null && selectedShape.getModel() instanceof DTextModel) {
            //selectedShape != null
            ((DTextModel)selectedShape.getModel()).setText(newText);
        }
    }

    public void setFont(String fontName){
        // JComboBox is enabled, so selectedShape != null && selectedShape is DText
        ((DTextModel) selectedShape.getModel()).setFontName(fontName);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(whiteboard.isClient())
            selectedShape = null;
        for(DShape shape: shapes){
            shape.draw(g);
            if(selectedShape != null) {
                if (selectedShape.equals(shape))
                    knobs = shape.drawKnobs(g);
            }
        }
        if(selectedShape == null)
            removeKnobs();
    }

    @Override
    public void modelChanged(DShapeModel model) {
        if(whiteboard.isServer())
            whiteboard.send("change", model);
    }
}
