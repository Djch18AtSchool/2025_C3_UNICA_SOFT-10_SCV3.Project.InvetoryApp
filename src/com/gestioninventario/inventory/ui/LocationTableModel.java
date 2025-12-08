package com.gestioninventario.inventory.ui;

import com.gestioninventario.inventory.domain.Location;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel para mostrar ubicaciones en JTable.
 * Columnas: ID, Nombre, Dirección
 */
public class LocationTableModel extends AbstractTableModel {
    private final String[] columns = {"ID", "Nombre", "Dirección"};
    private final List<Location> data = new ArrayList<>();

    public void setData(List<Location> locations) {
        data.clear();
        data.addAll(locations);
        fireTableDataChanged();
    }

    public Location getLocationAt(int row) {
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
        Location loc = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return loc.getId();
            case 1: return loc.getName();
            case 2: return loc.getAddress();
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }
}

