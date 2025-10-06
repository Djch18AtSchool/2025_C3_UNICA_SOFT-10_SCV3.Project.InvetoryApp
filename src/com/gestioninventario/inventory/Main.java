package com.gestioninventario.inventory;

import javax.swing.SwingUtilities;
import com.gestioninventario.inventory.ui.MenuAdministrador;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MenuAdministrador().setVisible(true);
        });
    }
}

