package com.gestioninventario.inventory.ui;

import com.gestioninventario.inventory.domain.Product;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel para mostrar productos del inventario de la tienda en JTable.
 * Columnas: Nombre, Categoría, Marca, Precio, Stock Disponible
 */
public class InventarioTiendaTableModel extends AbstractTableModel {
    private final String[] columns = {"Nombre", "Categoría", "Marca", "Precio", "Stock Disponible"};
    private final List<Product> data = new ArrayList<>();

    public void setData(List<Product> productos) {
        data.clear();
        data.addAll(productos);
        fireTableDataChanged();
    }

    public Product getProductAt(int row) {
        if (row < 0 || row >= data.size()) return null;
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product p = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return p.getName();
            case 1: return p.getCategory();
            case 2: return p.getBrand();
            case 3: return p.getPrice();
            case 4: return p.getQuantity();
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 3) return Double.class;
        if (columnIndex == 4) return Integer.class;
        return String.class;
    }
}

