package com.gestioninventario.inventory.ui;

import com.gestioninventario.inventory.domain.Cliente;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel para mostrar clientes en la cola en JTable.
 * Columnas: Posición, Cliente, Email, Ubicación, Prioridad, Items en Carrito
 */
public class ClienteTableModel extends AbstractTableModel {
    private final String[] columns = {"Pos.", "Nombre Completo", "Email", "Ubicación", "Prioridad", "Tipo", "Items"};
    private final List<Cliente> data = new ArrayList<>();

    public void setData(List<Cliente> clientes) {
        data.clear();
        data.addAll(clientes);
        fireTableDataChanged();
    }

    public Cliente getClienteAt(int row) {
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
        Cliente c = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return rowIndex + 1;
            case 1: return c.getName() + " " + c.getLastName();
            case 2: return c.getEmail();
            case 3: return c.getLocation() != null ? c.getLocation().getName() : "Sin ubicación";
            case 4: return c.getPriority();
            case 5: return c.getPriorityDescription();
            case 6: return contarItems(c);
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0 || columnIndex == 4 || columnIndex == 6) return Integer.class;
        return String.class;
    }

    private int contarItems(Cliente cliente) {
        int count = 0;
        for (@SuppressWarnings("unused") Cliente.CartItem item : cliente.getCart()) {
            count++;
        }
        return count;
    }
}

