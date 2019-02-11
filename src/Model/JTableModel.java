package Model;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;

public class JTableModel extends AbstractTableModel implements ModelListener {

    private LinkedList<DShapeModel> models;

    public LinkedList<DShapeModel> getModels(){ return this.models; }

    private final String[] columnHeaders = {"x", "y", "width", "height"};

    public JTableModel(){
        super();
        models = new LinkedList<>();
    }

    @Override
    public String getColumnName(int colIndex){
        return columnHeaders[colIndex];
    }

    public void addRow(DShapeModel model){
        models.addLast(model);
        model.addListener(this);
        //update Table
        fireTableDataChanged();
    }

    public void removeRow(DShapeModel model){
        models.remove(model);
        model.removeListener(this);
        //update Table
        fireTableDataChanged();
    }

    public void moveTo(DShapeModel model, boolean isToFront){
        models.remove(model);
        if(isToFront)
            models.addLast(model);
        else
            models.addFirst(model);
        fireTableDataChanged();
    }

    @Override
    public void modelChanged(DShapeModel model) {
        int index = models.indexOf(model);
        fireTableRowsUpdated(index, index);
    }

    @Override
    public int getRowCount() {
        return models.size();
    }

    @Override
    public int getColumnCount() {
        return columnHeaders.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex){
            case 0: //x
                return models.get(rowIndex).getBounds().x;
            case 1: //y
                return models.get(rowIndex).getBounds().y;
            case 2: //width
                return models.get(rowIndex).getBounds().width;
            case 3: //height
                return models.get(rowIndex).getBounds().height;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
