// ProductTableModel.java
package com.gestioninventario.inventory.ui;

import com.gestioninventario.inventory.domain.Product;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel para mostrar productos en JTable.
 * Columnas: ID, Categoria, Marca, Nombre, Precio, Cant, Miniatura, Descripcion
 */
public class ProductTableModel extends AbstractTableModel {
    private final String[] columns = {"ID","Categoría","Marca","Nombre","Precio","Cant","Miniatura","Descripción"};
    private List<Product> data = new ArrayList<>();

    public void setData(List<Product> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public Product getProductAt(int row) {
        if (row < 0 || row >= data.size()) return null;
        return data.get(row);
    }

    @Override
    public int getRowCount() { return data.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product p = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return p.getId();
            case 1: return p.getCategory();
            case 2: return p.getBrand();
            case 3: return p.getName();
            case 4: return p.getPrice();
            case 5: return p.getQuantity();
            case 6:
                return p.getImagePaths().isEmpty() ? null : p.getImagePaths().getFirst();
            case 7: return p.getDescription();
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 4) return Double.class;
        if (columnIndex == 5) return Integer.class;
        return String.class;
    }
}
