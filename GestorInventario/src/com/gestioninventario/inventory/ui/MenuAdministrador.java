package com.gestioninventario.inventory.ui;

import com.gestioninventario.inventory.domain.Product;
import com.gestioninventario.inventory.repository.ProductRepository;
import com.gestioninventario.inventory.service.ProductService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class MenuAdministrador extends JFrame {
    private final ProductService productService;
    private final ProductRepository productRepository;

    // Form fields
    private JTextField txtId;
    private JComboBox<String> cmbCategory;
    private JTextField txtBrand;
    private JTextField txtName;
    private JTextField txtPrice;
    private JTextField txtQuantity;
    private JTextArea txtDescription;
    private DefaultListModel<String> imagesListModel;

    // Table
    private JTable table;
    private ProductTableModel tableModel;

    // Selected image files staging (before saving)
    private List<Path> stagedImagePaths = new ArrayList<>();

    public MenuAdministrador() {
        // init repo & service (in memory)
        this.productRepository = new ProductRepository();
        this.productService = new ProductService(productRepository);

        setTitle("Gestor de Componentes - Inventario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        initUI();
        refreshTable();
    }

    private void initUI() {
        JTabbedPane tabbed = new JTabbedPane();

        // Form panel
        JPanel pnlForm = new JPanel(new BorderLayout());
        pnlForm.setBorder(new EmptyBorder(12,12,12,12));

        JPanel pnlInputs = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font lblFont = new Font("Arial", Font.BOLD, 14);

        // ID
        gbc.gridx = 0; gbc.gridy = 0;
        pnlInputs.add(label("ID Producto:", lblFont), gbc);
        gbc.gridx = 1;
        txtId = new JTextField(20);
        pnlInputs.add(txtId, gbc);

        // Categoria
        gbc.gridx = 0; gbc.gridy++;
        pnlInputs.add(label("Categoría:", lblFont), gbc);
        gbc.gridx = 1;
        String[] categories = {"---","Procesador","Tarjeta gráfica","Tarjeta madre","Memoria RAM","Almacenamiento","Fuente de poder","Enfriamiento","Gabinete","Otros"};
        cmbCategory = new JComboBox<>(categories);
        pnlInputs.add(cmbCategory, gbc);

        // Marca
        gbc.gridx = 0; gbc.gridy++;
        pnlInputs.add(label("Marca:", lblFont), gbc);
        gbc.gridx = 1;
        txtBrand = new JTextField();
        pnlInputs.add(txtBrand, gbc);

        // Nombre
        gbc.gridx = 0; gbc.gridy++;
        pnlInputs.add(label("Nombre:", lblFont), gbc);
        gbc.gridx = 1;
        txtName = new JTextField();
        pnlInputs.add(txtName, gbc);

        // Precio
        gbc.gridx = 0; gbc.gridy++;
        pnlInputs.add(label("Precio:", lblFont), gbc);
        gbc.gridx = 1;
        txtPrice = new JTextField();
        pnlInputs.add(txtPrice, gbc);

        // Cantidad
        gbc.gridx = 0; gbc.gridy++;
        pnlInputs.add(label("Cantidad:", lblFont), gbc);
        gbc.gridx = 1;
        txtQuantity = new JTextField("1");
        pnlInputs.add(txtQuantity, gbc);

        // Descripcion
        gbc.gridx = 0; gbc.gridy++;
        pnlInputs.add(label("Descripción:", lblFont), gbc);
        gbc.gridx = 1;
        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        pnlInputs.add(descScroll, gbc);

        // Imagenes list + botones
        gbc.gridx = 0; gbc.gridy++;
        pnlInputs.add(label("Imágenes (primera = miniatura):", lblFont), gbc);
        gbc.gridx = 1;
        JPanel imgPanel = new JPanel(new BorderLayout(4,4));
        imagesListModel = new DefaultListModel<>();
        JList<String> imagesList = new JList<>(imagesListModel);
        imagesList.setVisibleRowCount(4);
        JScrollPane imgScroll = new JScrollPane(imagesList);
        imgPanel.add(imgScroll, BorderLayout.CENTER);
        JPanel imgBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddImages = new JButton("Agregar imágenes...");
        JButton btnRemoveImage = new JButton("Eliminar imagen seleccionada");
        imgBtns.add(btnAddImages);
        imgBtns.add(btnRemoveImage);
        imgPanel.add(imgBtns, BorderLayout.SOUTH);
        pnlInputs.add(imgPanel, gbc);

        btnAddImages.addActionListener(e -> onAddImages());
        btnRemoveImage.addActionListener(e -> {
            int idx = imagesList.getSelectedIndex();
            if (idx >= 0) {
                imagesListModel.remove(idx);
                if (idx < stagedImagePaths.size()) stagedImagePaths.remove(idx);
            }
        });

        // Botones guardar/limpiar
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel btns = new JPanel();
        JButton btnSave = new JButton("Guardar/Actualizar");
        JButton btnClear = new JButton("Limpiar formulario");
        btns.add(btnSave);
        btns.add(btnClear);
        pnlInputs.add(btns, gbc);

        btnSave.addActionListener(e -> onSaveProduct());
        btnClear.addActionListener(e -> clearForm());

        pnlForm.add(pnlInputs, BorderLayout.NORTH);
        tabbed.add("Gestionar producto", pnlForm);

        // Tabla panel
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBorder(new EmptyBorder(12,12,12,12));
        tableModel = new ProductTableModel();
        table = new JTable(tableModel);

        // Renderer para miniaturas (columna 6)
        table.setRowHeight(64);
        table.getColumnModel().getColumn(6).setCellRenderer(new ImagePathCellRenderer());
        JScrollPane tableScroll = new JScrollPane(table);
        pnlTable.add(tableScroll, BorderLayout.CENTER);

        JPanel tableButtons = new JPanel();
        JButton btnEdit = new JButton("Editar seleccionado");
        JButton btnDelete = new JButton("Eliminar seleccionado");
        JButton btnRefresh = new JButton("Refrescar tabla");
        tableButtons.add(btnEdit);
        tableButtons.add(btnDelete);
        tableButtons.add(btnRefresh);
        pnlTable.add(tableButtons, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> refreshTable());
        btnDelete.addActionListener(e -> onDeleteSelected());
        btnEdit.addActionListener(e -> onEditSelected());

        tabbed.add("Ver productos", pnlTable);

        add(tabbed);
    }

    private JLabel label(String text, Font font) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        return l;
    }

    private void onAddImages() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Imagenes", ImageIO.getReaderFileSuffixes()));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            for (File f : files) {
                stagedImagePaths.add(f.toPath());
                imagesListModel.addElement(f.getName());
            }
        }
    }

    private void onSaveProduct() {
        // Validaciones
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El ID no puede estar vacío.");
            return;
        }
        String category = (String) cmbCategory.getSelectedItem();
        if (category == null || category.equals("---")) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría válida.");
            return;
        }
        String brand = txtBrand.getText().trim();
        if (brand.isEmpty()) { JOptionPane.showMessageDialog(this, "La marca no puede estar vacía."); return; }
        String name = txtName.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío."); return; }
        double price;
        try { price = Double.parseDouble(txtPrice.getText().trim()); if (price <= 0) { JOptionPane.showMessageDialog(this,"Precio debe ser > 0"); return;} }
        catch (Exception ex) { JOptionPane.showMessageDialog(this,"Precio inválido"); return; }
        int qty;
        try { qty = Integer.parseInt(txtQuantity.getText().trim()); if (qty < 0) { JOptionPane.showMessageDialog(this,"Cantidad inválida"); return;} }
        catch (Exception ex) { JOptionPane.showMessageDialog(this,"Cantidad inválida"); return; }
        String desc = txtDescription.getText().trim();
        if (desc.isEmpty()) desc = "";

        // Build product
        Product p = new Product(id, category, brand, name, price, qty, desc);

        // Guardar imágenes: stagedImagePaths -> copy -> get rutas
        if (!stagedImagePaths.isEmpty()) {
            List<Path> toStore = new ArrayList<>(stagedImagePaths);
            List<String> stored = productService.storeImageFiles(toStore);
            p.setImagePaths(stored);
        } else {
            // si existe producto anterior y no seleccionaste nuevas imágenes, preservarlas
            Product existing = productService.getById(id);
            if (existing != null) {
                p.setImagePaths(existing.getImagePaths());
            }
        }

        productService.saveProduct(p);
        JOptionPane.showMessageDialog(this,"Producto guardado exitosamente.");
        clearForm();
        refreshTable();
    }

    private void clearForm() {
        txtId.setText("");
        cmbCategory.setSelectedIndex(0);
        txtBrand.setText("");
        txtName.setText("");
        txtPrice.setText("");
        txtQuantity.setText("1");
        txtDescription.setText("");
        imagesListModel.clear();
        stagedImagePaths.clear();
    }

    private void refreshTable() {
        List<Product> all = productService.getAll();
        tableModel.setData(all);
    }

    private void onDeleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una fila."); return; }
        Product p = tableModel.getProductAt(table.convertRowIndexToModel(row));
        int confirm = JOptionPane.showConfirmDialog(this, "Eliminar producto '" + p.getName() + "'?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = productService.deleteById(p.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Producto eliminado.");
                refreshTable();
            } else JOptionPane.showMessageDialog(this, "No se pudo eliminar.");
        }
    }

    private void onEditSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una fila para editar."); return; }
        Product p = tableModel.getProductAt(table.convertRowIndexToModel(row));
        // Cargar datos en formulario
        txtId.setText(p.getId());
        cmbCategory.setSelectedItem(p.getCategory());
        txtBrand.setText(p.getBrand());
        txtName.setText(p.getName());
        txtPrice.setText(String.valueOf(p.getPrice()));
        txtQuantity.setText(String.valueOf(p.getQuantity()));
        txtDescription.setText(p.getDescription());
        imagesListModel.clear();
        stagedImagePaths.clear();
        // mostrar rutas actuales en lista (no duplicar archivos; si el usuario agrega nuevas imágenes, se almacenan y reemplazarán las anteriores)
        for (String path : p.getImagePaths()) {
            File f = new File(path);
            imagesListModel.addElement(f.getName());
        }
        ((JTabbedPane)getContentPane().getComponent(0)).setSelectedIndex(0);
    }

    // Renderer para mostrar miniatura a partir de ruta (String)
    private static class ImagePathCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(CENTER);
            lbl.setIcon(null);
            if (value != null) {
                try {
                    String path = value.toString();
                    File f = new File(path);
                    if (f.exists()) {
                        BufferedImage img = ImageIO.read(f);
                        // scale maintaining ratio to 64x64
                        Image scaled = img.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                        lbl.setIcon(new ImageIcon(scaled));
                    } else {
                        lbl.setText("No image");
                    }
                } catch (Exception ex) {
                    lbl.setText("Err");
                }
            } else {
                lbl.setText("—");
            }
            return lbl;
        }
    }
}
