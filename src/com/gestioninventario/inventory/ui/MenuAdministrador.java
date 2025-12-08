package com.gestioninventario.inventory.ui;

import com.gestioninventario.inventory.domain.Product;
import com.gestioninventario.inventory.domain.Cliente;
import com.gestioninventario.inventory.domain.Location;
import com.gestioninventario.inventory.repository.ProductRepository;
import com.gestioninventario.inventory.service.ProductService;
import com.gestioninventario.inventory.service.StoreService;
import com.gestioninventario.inventory.common.LocationGraph;

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
    private final StoreService storeService;

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
    private final List<Path> stagedImagePaths = new ArrayList<>();

    // Referencias a combos de rutas para actualización dinámica
    private JComboBox<String> cmbOriginRoutes;
    private JComboBox<String> cmbDestinationRoutes;

    public MenuAdministrador() {
        // Inicializar repositorio y servicios
        this.productRepository = new ProductRepository();
        this.productService = new ProductService(productRepository);

        // Crear ubicación de la tienda
        Location storeLocation = new Location("LOC-STORE", "TechStore HQ", "Av. Principal 123");
        this.storeService = new StoreService("TechStore", storeLocation);

        setTitle("Sistema de Gestión de Inventarios y Entregas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
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
        JButton btnCostoTotal = new JButton("Calcular costo total");
        tableButtons.add(btnEdit);
        tableButtons.add(btnDelete);
        tableButtons.add(btnRefresh);
        tableButtons.add(btnCostoTotal);
        pnlTable.add(tableButtons, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> refreshTable());
        btnDelete.addActionListener(e -> onDeleteSelected());
        btnEdit.addActionListener(e -> onEditSelected());
        btnCostoTotal.addActionListener(e -> onCalcularCostoTotal());

        tabbed.add("Ver productos", pnlTable);

        // Nueva pestaña: Inventario de Tienda
        tabbed.add("Inventario Tienda", createInventarioTiendaPanel());

        // Nueva pestaña: Gestión de Clientes
        tabbed.add("Gestión Clientes", createGestionClientesPanel());

        // Nueva pestaña: Atención de Clientes
        tabbed.add("Atención Clientes", createAtencionClientesPanel());

        // Nuevas pestañas: Sistema de Entregas
        tabbed.add("📍 Ubicaciones", createLocationManagementPanel());
        tabbed.add("🚚 Rutas de Entrega", createDeliveryRoutesPanel());

        // Listener para actualizar combos de rutas cuando se cambia a esa pestaña
        tabbed.addChangeListener(e -> {
            if (tabbed.getSelectedIndex() == 6) { // Índice de pestaña "Rutas de Entrega"
                updateRoutesCombos();
            }
        });

        add(tabbed);
    }

    /**
     * Actualiza los combos de origen y destino en la pestaña de rutas.
     */
    private void updateRoutesCombos() {
        if (cmbOriginRoutes != null && cmbDestinationRoutes != null) {
            cmbOriginRoutes.removeAllItems();
            cmbDestinationRoutes.removeAllItems();
            for (Location loc : storeService.getAllLocations()) {
                String item = loc.getName() + " (" + loc.getId() + ")";
                cmbOriginRoutes.addItem(item);
                cmbDestinationRoutes.addItem(item);
            }
        }
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
        tableModel.setData(productService.getAll());
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

    private void onCalcularCostoTotal() {
        double costoTotal = productService.getAll().calculateTotalCost();
        int totalProductos = productService.getAll().size();

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("═══════════════════════════════════════════════════\n");
        mensaje.append("           COSTO TOTAL DE PRODUCTOS\n");
        mensaje.append("═══════════════════════════════════════════════════\n\n");
        mensaje.append("Total de productos: ").append(totalProductos).append("\n");
        mensaje.append("Costo total (Precio × Cantidad): $").append(String.format("%.2f", costoTotal)).append("\n");
        mensaje.append("\n═══════════════════════════════════════════════════\n");

        JOptionPane.showMessageDialog(this, mensaje.toString(),
                "Costo Total", JOptionPane.INFORMATION_MESSAGE);
    }

    // ========== PANEL: INVENTARIO DE TIENDA ==========
    private JTable tablaInventarioTienda;
    private InventarioTiendaTableModel modeloInventarioTienda;
    private JLabel lblTotalInventario;

    private JPanel createInventarioTiendaPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panel superior con título e información
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JLabel titulo = new JLabel("📦 Inventario de la Tienda");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titulo, BorderLayout.WEST);

        lblTotalInventario = new JLabel("Total: 0 productos");
        lblTotalInventario.setFont(new Font("Arial", Font.PLAIN, 13));
        lblTotalInventario.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(lblTotalInventario, BorderLayout.EAST);

        JLabel subtitulo = new JLabel("Estructura: Árbol Binario de Búsqueda (ordenado alfabéticamente por nombre)");
        subtitulo.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitulo.setForeground(Color.GRAY);
        topPanel.add(subtitulo, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);

        // Panel central con tabla y panel de información
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Tabla de productos en el inventario
        modeloInventarioTienda = new InventarioTiendaTableModel();
        tablaInventarioTienda = new JTable(modeloInventarioTienda);
        tablaInventarioTienda.setRowHeight(28);
        tablaInventarioTienda.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaInventarioTienda.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaInventarioTienda.setFont(new Font("Arial", Font.PLAIN, 12));

        // Ajustar anchos de columnas
        tablaInventarioTienda.getColumnModel().getColumn(0).setPreferredWidth(250); // Nombre
        tablaInventarioTienda.getColumnModel().getColumn(1).setPreferredWidth(150); // Categoría
        tablaInventarioTienda.getColumnModel().getColumn(2).setPreferredWidth(120); // Marca
        tablaInventarioTienda.getColumnModel().getColumn(3).setPreferredWidth(100); // Precio
        tablaInventarioTienda.getColumnModel().getColumn(4).setPreferredWidth(120); // Stock

        JScrollPane scroll = new JScrollPane(tablaInventarioTienda);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Productos en Inventario"));
        centerPanel.add(scroll, BorderLayout.CENTER);

        // Panel de información lateral
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Información"));
        infoPanel.setPreferredSize(new Dimension(220, 0));

        JTextArea txtInfo = new JTextArea();
        txtInfo.setEditable(false);
        txtInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        txtInfo.setBackground(new Color(250, 250, 250));
        txtInfo.setText("El árbol binario de búsqueda\npermite búsquedas rápidas O(log n)\n" +
                "y mantiene los productos\nordenados alfabéticamente.\n\n" +
                "Haga doble clic en una fila\npara ver detalles del producto.");
        txtInfo.setLineWrap(true);
        txtInfo.setWrapStyleWord(true);
        infoPanel.add(new JScrollPane(txtInfo));

        centerPanel.add(infoPanel, BorderLayout.EAST);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Panel de botones con mejor diseño
        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnAgregarDesdeRepo = createStyledButton("➕ Agregar desde repositorio", new Color(76, 175, 80));
        JButton btnBuscar = createStyledButton("🔍 Buscar producto", new Color(33, 150, 243));
        JButton btnEliminar = createStyledButton("🗑️ Eliminar seleccionado", new Color(244, 67, 54));
        JButton btnRefrescar = createStyledButton("🔄 Refrescar", new Color(158, 158, 158));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        btnPanel.add(btnAgregarDesdeRepo, gbc);
        gbc.gridx = 1;
        btnPanel.add(btnBuscar, gbc);
        gbc.gridx = 2;
        btnPanel.add(btnEliminar, gbc);
        gbc.gridx = 3;
        btnPanel.add(btnRefrescar, gbc);

        panel.add(btnPanel, BorderLayout.SOUTH);

        // Listeners
        btnAgregarDesdeRepo.addActionListener(e -> onAgregarProductoAlInventario());
        btnBuscar.addActionListener(e -> onBuscarEnInventario());
        btnRefrescar.addActionListener(e -> refreshInventarioTienda());
        btnEliminar.addActionListener(e -> onEliminarDeInventarioSeleccionado());

        // Doble clic para ver detalles
        tablaInventarioTienda.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onVerDetalleProductoInventario();
                }
            }
        });

        refreshInventarioTienda();
        return panel;
    }

    private void refreshInventarioTienda() {
        List<Product> productos = storeService.getAllProducts();
        modeloInventarioTienda.setData(productos);

        lblTotalInventario.setText(String.format("Total: %d producto%s",
                productos.size(), productos.size() == 1 ? "" : "s"));
    }

    private void onEliminarDeInventarioSeleccionado() {
        int row = tablaInventarioTienda.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un producto de la tabla.");
            return;
        }

        Product producto = modeloInventarioTienda.getProductAt(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar '" + producto.getName() + "' del inventario?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (storeService.removeProduct(producto.getName())) {
                JOptionPane.showMessageDialog(this, "✅ Producto eliminado del inventario.");
                refreshInventarioTienda();
            } else {
                JOptionPane.showMessageDialog(this, "❌ No se pudo eliminar el producto.");
            }
        }
    }

    private void onVerDetalleProductoInventario() {
        int row = tablaInventarioTienda.getSelectedRow();
        if (row < 0) return;

        Product producto = modeloInventarioTienda.getProductAt(row);
        JOptionPane.showMessageDialog(this,
                "Producto: " + producto.getName() + "\n" +
                        "Categoría: " + producto.getCategory() + "\n" +
                        "Marca: " + producto.getBrand() + "\n" +
                        "Precio: $" + String.format("%.2f", producto.getPrice()) + "\n" +
                        "Stock: " + producto.getQuantity() + "\n" +
                        "Descripción: " + producto.getDescription(),
                "Detalles del Producto",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private void onAgregarProductoAlInventario() {
        // Mostrar lista de productos del repositorio
        List<Product> productos = new ArrayList<>();
        for (Product p : productService.getAll()) {
            productos.add(p);
        }

        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos en el repositorio.");
            return;
        }

        String[] nombres = productos.stream().map(Product::getName).toArray(String[]::new);
        String seleccion = (String) JOptionPane.showInputDialog(this,
                "Seleccione un producto para agregar al inventario:",
                "Agregar Producto", JOptionPane.PLAIN_MESSAGE, null, nombres, nombres[0]);

        if (seleccion != null) {
            Product producto = productos.stream()
                    .filter(p -> p.getName().equals(seleccion))
                    .findFirst()
                    .orElse(null);

            if (producto != null) {
                storeService.addProductToInventory(producto);
                JOptionPane.showMessageDialog(this, "Producto agregado al inventario de la tienda.");
                refreshInventarioTienda();
            }
        }
    }

    private void onBuscarEnInventario() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del producto:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            Product producto = storeService.searchProductByName(nombre.trim());
            if (producto != null) {
                JOptionPane.showMessageDialog(this,
                        "Producto encontrado:\n" + producto.toString());
            } else {
                // Buscar por coincidencia
                List<Product> coincidencias = storeService.searchProductsByMatch(nombre.trim());
                if (!coincidencias.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Productos encontrados:\n\n");
                    for (Product p : coincidencias) {
                        sb.append(p.getName()).append(" (Stock: ").append(p.getQuantity()).append(")\n");
                    }
                    JOptionPane.showMessageDialog(this, sb.toString());
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontraron productos.");
                }
            }
        }
    }

    private void onEliminarDeInventario() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del producto a eliminar:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            if (storeService.removeProduct(nombre.trim())) {
                JOptionPane.showMessageDialog(this, "✅ Producto eliminado del inventario.");
                refreshInventarioTienda();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Producto no encontrado.");
            }
        }
    }

    // ========== PANEL: GESTIÓN DE CLIENTES ==========
    private JTable tablaClientes;
    private ClienteTableModel modeloClientes;
    private JLabel lblTotalClientes;

    private JPanel createGestionClientesPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panel superior con título e información
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JLabel titulo = new JLabel("👥 Gestión de Clientes");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titulo, BorderLayout.WEST);

        lblTotalClientes = new JLabel("En cola: 0 clientes");
        lblTotalClientes.setFont(new Font("Arial", Font.PLAIN, 13));
        lblTotalClientes.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(lblTotalClientes, BorderLayout.EAST);

        JLabel subtitulo = new JLabel("Estructura: Cola de Prioridad (Premium → Afiliado → Básico, luego FIFO)");
        subtitulo.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitulo.setForeground(Color.GRAY);
        topPanel.add(subtitulo, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);

        // Panel central con tabla y panel de información
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Tabla de clientes
        modeloClientes = new ClienteTableModel();
        tablaClientes = new JTable(modeloClientes);
        tablaClientes.setRowHeight(28);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaClientes.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaClientes.setFont(new Font("Arial", Font.PLAIN, 12));

        // Ajustar anchos de columnas
        tablaClientes.getColumnModel().getColumn(0).setPreferredWidth(50);  // Pos
        tablaClientes.getColumnModel().getColumn(1).setPreferredWidth(200); // Nombre
        tablaClientes.getColumnModel().getColumn(2).setPreferredWidth(180); // Email
        tablaClientes.getColumnModel().getColumn(3).setPreferredWidth(80);  // Prioridad
        tablaClientes.getColumnModel().getColumn(4).setPreferredWidth(100); // Tipo
        tablaClientes.getColumnModel().getColumn(5).setPreferredWidth(80);  // Items

        // Renderer personalizado para prioridad
        tablaClientes.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Integer) {
                    int prioridad = (Integer) value;
                    switch (prioridad) {
                        case 3:
                            c.setForeground(new Color(218, 165, 32)); // Dorado
                            break;
                        case 2:
                            c.setForeground(new Color(192, 192, 192)); // Plateado
                            break;
                        case 1:
                            c.setForeground(new Color(205, 127, 50)); // Bronce
                            break;
                    }
                }
                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tablaClientes);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Cola de Clientes (orden de atención)"));
        centerPanel.add(scroll, BorderLayout.CENTER);

        // Panel de información lateral
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Leyenda de Prioridades"));
        infoPanel.setPreferredSize(new Dimension(220, 0));

        infoPanel.add(createPriorityLabel("⭐⭐⭐ Premium (3)", new Color(218, 165, 32)));
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(createPriorityLabel("⭐⭐ Afiliado (2)", new Color(192, 192, 192)));
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(createPriorityLabel("⭐ Básico (1)", new Color(205, 127, 50)));
        infoPanel.add(Box.createVerticalStrut(15));

        JTextArea txtInfo = new JTextArea();
        txtInfo.setEditable(false);
        txtInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        txtInfo.setBackground(new Color(250, 250, 250));
        txtInfo.setText("Los clientes con mayor\nprioridad son atendidos\nprimero. En caso de empate,\n" +
                "se atiende por orden de\nllegada (FIFO).\n\n" +
                "Doble clic para ver\ndetalles del cliente.");
        txtInfo.setLineWrap(true);
        txtInfo.setWrapStyleWord(true);
        infoPanel.add(new JScrollPane(txtInfo));

        centerPanel.add(infoPanel, BorderLayout.EAST);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Panel de botones con mejor diseño
        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnAgregar = createStyledButton("➕ Nuevo cliente", new Color(76, 175, 80));
        JButton btnVerCarrito = createStyledButton("🛒 Ver carrito", new Color(33, 150, 243));
        JButton btnAgregarProducto = createStyledButton("📦 Agregar al carrito", new Color(255, 152, 0));
        JButton btnRefrescar = createStyledButton("🔄 Refrescar", new Color(158, 158, 158));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        btnPanel.add(btnAgregar, gbc);
        gbc.gridx = 1;
        btnPanel.add(btnVerCarrito, gbc);
        gbc.gridx = 2;
        btnPanel.add(btnAgregarProducto, gbc);
        gbc.gridx = 3;
        btnPanel.add(btnRefrescar, gbc);

        panel.add(btnPanel, BorderLayout.SOUTH);

        // Listeners
        btnAgregar.addActionListener(e -> onAgregarCliente());
        btnVerCarrito.addActionListener(e -> onVerCarritoClienteSeleccionado());
        btnAgregarProducto.addActionListener(e -> onAgregarProductoAlCarrito());
        btnRefrescar.addActionListener(e -> refreshColaClientes());

        // Doble clic para ver detalles
        tablaClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onVerCarritoClienteSeleccionado();
                }
            }
        });

        refreshColaClientes();
        return panel;
    }

    private JPanel createPriorityLabel(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(color);
        panel.add(label);
        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efecto hover
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private void refreshColaClientes() {
        List<Cliente> clientes = storeService.getAllClients();
        modeloClientes.setData(clientes);

        lblTotalClientes.setText(String.format("En cola: %d cliente%s",
                clientes.size(), clientes.size() == 1 ? "" : "s"));
    }

    private void onVerCarritoClienteSeleccionado() {
        int row = tablaClientes.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un cliente de la tabla.");
            return;
        }

        Cliente cliente = modeloClientes.getClienteAt(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Cliente: ").append(cliente.getName()).append(" ").append(cliente.getLastName()).append("\n");
        sb.append("Email: ").append(cliente.getEmail()).append("\n");
        sb.append("Prioridad: ").append(cliente.getPriorityDescription()).append("\n\n");
        sb.append("🛒 Carrito de Compras:\n\n");

        if (cliente.getCart().isEmpty()) {
            sb.append("  (Carrito vacío)");
        } else {
            for (Cliente.CartItem item : cliente.getCart()) {
                sb.append("  • ").append(item.toString()).append("\n");
            }
            sb.append("\n💰 TOTAL: $").append(String.format("%.2f", cliente.calculateTotal()));
        }

        JOptionPane.showMessageDialog(this, sb.toString(),
                "Carrito de " + cliente.getName(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private int contarItemsCarrito(Cliente cliente) {
        int count = 0;
        for (@SuppressWarnings("unused") Cliente.CartItem item : cliente.getCart()) {
            count++;
        }
        return count;
    }

    private void onAgregarCliente() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 12);

        JTextField txtId = new JTextField(20);
        JTextField txtNombre = new JTextField(20);
        JTextField txtApellido = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        String[] prioridades = {"1 - Básico ⭐", "2 - Afiliado ⭐⭐", "3 - Premium ⭐⭐⭐"};
        JComboBox<String> cmbPrioridad = new JComboBox<>(prioridades);
        cmbPrioridad.setSelectedIndex(0);

        // Combo de ubicaciones
        JComboBox<String> cmbUbicacion = new JComboBox<>();
        cmbUbicacion.addItem("-- Sin ubicación --");
        for (Location loc : storeService.getAllLocations()) {
            cmbUbicacion.addItem(loc.getName() + " (" + loc.getId() + ")");
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblId = new JLabel("ID Cliente:");
        lblId.setFont(labelFont);
        formPanel.add(lblId, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setFont(labelFont);
        formPanel.add(lblNombre, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblApellido = new JLabel("Apellido:");
        lblApellido.setFont(labelFont);
        formPanel.add(lblApellido, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtApellido, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        formPanel.add(lblEmail, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblPrioridad = new JLabel("Tipo de Cliente:");
        lblPrioridad.setFont(labelFont);
        formPanel.add(lblPrioridad, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(cmbPrioridad, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblUbicacion = new JLabel("Ubicación:");
        lblUbicacion.setFont(labelFont);
        formPanel.add(lblUbicacion, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(cmbUbicacion, gbc);

        int result = JOptionPane.showConfirmDialog(this, formPanel,
                "➕ Agregar Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = txtId.getText().trim();
                String nombre = txtNombre.getText().trim();
                String apellido = txtApellido.getText().trim();
                String email = txtEmail.getText().trim();
                int prioridad = cmbPrioridad.getSelectedIndex() + 1;

                // Obtener ubicación seleccionada
                Location location = null;
                if (cmbUbicacion.getSelectedIndex() > 0) {
                    String selected = (String) cmbUbicacion.getSelectedItem();
                    String locId = selected.substring(selected.lastIndexOf("(") + 1, selected.lastIndexOf(")"));
                    for (Location loc : storeService.getAllLocations()) {
                        if (loc.getId().equals(locId)) {
                            location = loc;
                            break;
                        }
                    }
                }

                if (id.isEmpty() || nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "❌ ID y Nombre son campos obligatorios.",
                            "Error de Validación",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (location == null) {
                    JOptionPane.showMessageDialog(this,
                            "❌ Debe seleccionar una ubicación para el cliente.\n" +
                                    "Si no hay ubicaciones, agregue una en la pestaña '📍 Ubicaciones'.",
                            "Error de Validación",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Cliente cliente = new Cliente(id, nombre, apellido, email, prioridad, location);
                storeService.addClientToQueue(cliente);
                JOptionPane.showMessageDialog(this,
                        "✅ Cliente agregado exitosamente a la cola.\n\n" +
                                "Cliente: " + nombre + " " + apellido + "\n" +
                                "Prioridad: " + cliente.getPriorityDescription() + "\n" +
                                "Ubicación: " + location.getName(),
                        "Cliente Agregado",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshColaClientes();
                actualizarSiguienteCliente();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAgregarProductoAlCarrito() {
        List<Cliente> clientes = storeService.getAllClients();
        if (clientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay clientes en cola.");
            return;
        }

        List<Product> productos = storeService.getAllProducts();
        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos en el inventario.");
            return;
        }

        // Seleccionar cliente
        String[] clienteIds = clientes.stream()
                .map(c -> c.getId() + " - " + c.getName())
                .toArray(String[]::new);

        String selCliente = (String) JOptionPane.showInputDialog(this,
                "Seleccione un cliente:", "Agregar Producto",
                JOptionPane.PLAIN_MESSAGE, null, clienteIds, clienteIds[0]);

        if (selCliente == null) return;

        String clienteId = selCliente.split(" - ")[0];

        // Seleccionar producto
        String[] productosNombres = productos.stream()
                .map(p -> p.getName() + " (Stock: " + p.getQuantity() + ")")
                .toArray(String[]::new);

        String selProducto = (String) JOptionPane.showInputDialog(this,
                "Seleccione un producto:", "Agregar Producto",
                JOptionPane.PLAIN_MESSAGE, null, productosNombres, productosNombres[0]);

        if (selProducto == null) return;

        String productoNombre = selProducto.split(" \\(Stock:")[0];

        // Cantidad
        String cantidadStr = JOptionPane.showInputDialog(this, "Cantidad:");
        if (cantidadStr == null) return;

        try {
            int cantidad = Integer.parseInt(cantidadStr.trim());
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "Cantidad debe ser mayor a 0.");
                return;
            }

            if (storeService.addProductToClientCart(clienteId, productoNombre, cantidad)) {
                JOptionPane.showMessageDialog(this, "Producto agregado al carrito.");
                refreshColaClientes();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo agregar. Verifique stock disponible.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida.");
        }
    }

    // ========== PANEL: ATENCIÓN DE CLIENTES ==========
    private JTextArea txtAreaFactura;
    private JLabel lblSiguienteCliente;

    private JPanel createAtencionClientesPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panel superior con título e información
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JLabel titulo = new JLabel("💰 Atención y Facturación");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titulo, BorderLayout.WEST);

        lblSiguienteCliente = new JLabel("Siguiente: --");
        lblSiguienteCliente.setFont(new Font("Arial", Font.BOLD, 13));
        lblSiguienteCliente.setHorizontalAlignment(SwingConstants.RIGHT);
        lblSiguienteCliente.setForeground(new Color(76, 175, 80));
        topPanel.add(lblSiguienteCliente, BorderLayout.EAST);

        JLabel subtitulo = new JLabel("Procesar compras y generar facturas automáticamente");
        subtitulo.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitulo.setForeground(Color.GRAY);
        topPanel.add(subtitulo, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);

        // Panel central con área de factura y panel de información
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Área de texto para mostrar factura
        txtAreaFactura = new JTextArea(22, 65);
        txtAreaFactura.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtAreaFactura.setEditable(false);
        txtAreaFactura.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        txtAreaFactura.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(txtAreaFactura);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Vista de Factura / Información"));
        centerPanel.add(scroll, BorderLayout.CENTER);

        // Panel de información lateral
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Acciones Rápidas"));
        infoPanel.setPreferredSize(new Dimension(200, 0));

        JButton btnAtenderQuick = new JButton("⚡ Atender Ahora");
        btnAtenderQuick.setFont(new Font("Arial", Font.BOLD, 13));
        btnAtenderQuick.setBackground(new Color(76, 175, 80));
        btnAtenderQuick.setForeground(Color.WHITE);
        btnAtenderQuick.setFocusPainted(false);
        btnAtenderQuick.setMaximumSize(new Dimension(180, 40));
        btnAtenderQuick.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAtenderQuick.addActionListener(e -> onAtenderCliente());

        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(btnAtenderQuick);
        infoPanel.add(Box.createVerticalStrut(20));

        JTextArea txtAyuda = new JTextArea();
        txtAyuda.setEditable(false);
        txtAyuda.setFont(new Font("Arial", Font.PLAIN, 11));
        txtAyuda.setBackground(new Color(250, 250, 250));
        txtAyuda.setText("💡 Instrucciones:\n\n" +
                "1. Ver siguiente: Muestra\n   el próximo cliente sin\n   procesarlo.\n\n" +
                "2. Atender: Procesa la\n   compra del cliente,\n   actualiza inventario y\n   genera factura.\n\n" +
                "3. Estadísticas: Muestra\n   resumen de la tienda.");
        txtAyuda.setLineWrap(true);
        txtAyuda.setWrapStyleWord(true);
        JScrollPane ayudaScroll = new JScrollPane(txtAyuda);
        ayudaScroll.setPreferredSize(new Dimension(180, 200));
        infoPanel.add(ayudaScroll);

        centerPanel.add(infoPanel, BorderLayout.EAST);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Panel de botones con mejor diseño
        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnAtender = createStyledButton("✅ Atender siguiente cliente", new Color(76, 175, 80));
        JButton btnVerSiguiente = createStyledButton("👁️ Ver siguiente", new Color(33, 150, 243));
        JButton btnEstadisticas = createStyledButton("📊 Estadísticas", new Color(156, 39, 176));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        btnPanel.add(btnAtender, gbc);
        gbc.gridx = 1;
        btnPanel.add(btnVerSiguiente, gbc);
        gbc.gridx = 2;
        btnPanel.add(btnEstadisticas, gbc);

        panel.add(btnPanel, BorderLayout.SOUTH);

        // Listeners
        btnAtender.addActionListener(e -> onAtenderCliente());
        btnVerSiguiente.addActionListener(e -> onVerSiguienteCliente());
        btnEstadisticas.addActionListener(e -> onVerEstadisticas());

        // Mensaje inicial
        txtAreaFactura.setText("\n\n\n" +
                "          ╔═══════════════════════════════════════════════╗\n" +
                "          ║                                               ║\n" +
                "          ║    Bienvenido al Sistema de Facturación      ║\n" +
                "          ║                                               ║\n" +
                "          ║    Use los botones para:                     ║\n" +
                "          ║    • Atender clientes en cola                ║\n" +
                "          ║    • Ver información del siguiente           ║\n" +
                "          ║    • Consultar estadísticas                  ║\n" +
                "          ║                                               ║\n" +
                "          ╚═══════════════════════════════════════════════╝\n");

        actualizarSiguienteCliente();
        return panel;
    }

    private void actualizarSiguienteCliente() {
        Cliente siguiente = storeService.viewNextClient();
        if (siguiente != null) {
            lblSiguienteCliente.setText(String.format("Siguiente: %s (%s)",
                    siguiente.getName(), siguiente.getPriorityDescription()));
        } else {
            lblSiguienteCliente.setText("Siguiente: No hay clientes");
        }
    }

    private void onAtenderCliente() {
        try {
            Cliente cliente = storeService.attendNextClient();

            if (cliente == null) {
                txtAreaFactura.setText("\n\n\n" +
                        "          ╔═══════════════════════════════════════════════╗\n" +
                        "          ║                                               ║\n" +
                        "          ║          ⚠️  NO HAY CLIENTES EN COLA          ║\n" +
                        "          ║                                               ║\n" +
                        "          ║    Agregue clientes desde la pestaña         ║\n" +
                        "          ║    'Gestión Clientes'                        ║\n" +
                        "          ║                                               ║\n" +
                        "          ╚═══════════════════════════════════════════════╝\n");
                return;
            }

            String factura = storeService.generateInvoice(cliente);
            txtAreaFactura.setText(factura);

            refreshColaClientes();
            refreshInventarioTienda();
            actualizarSiguienteCliente();

            JOptionPane.showMessageDialog(this,
                    "✅ Cliente atendido exitosamente.\n\n" +
                            "Cliente: " + cliente.getName() + " " + cliente.getLastName() + "\n" +
                            "Total: $" + String.format("%.2f", cliente.calculateTotal()),
                    "Atención Completada",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException ex) {
            // Error de ubicación desconectada o sin ubicación
            txtAreaFactura.setText("\n\n\n" +
                    "          ╔═══════════════════════════════════════════════╗\n" +
                    "          ║                                               ║\n" +
                    "          ║          ❌ ERROR DE CONECTIVIDAD             ║\n" +
                    "          ║                                               ║\n" +
                    "          ║    " + ex.getMessage() + "\n" +
                    "          ║                                               ║\n" +
                    "          ║    Solución:                                  ║\n" +
                    "          ║    1. Verifique las ubicaciones              ║\n" +
                    "          ║    2. Agregue conexiones necesarias          ║\n" +
                    "          ║    3. Use pestaña '📍 Ubicaciones'           ║\n" +
                    "          ║                                               ║\n" +
                    "          ╚═══════════════════════════════════════════════╝\n");

            JOptionPane.showMessageDialog(this,
                    "❌ " + ex.getMessage() + "\n\n" +
                            "Por favor, verifique las ubicaciones y conexiones\n" +
                            "en la pestaña '📍 Ubicaciones'.",
                    "Error de Conectividad",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onVerSiguienteCliente() {
        Cliente cliente = storeService.viewNextClient();

        if (cliente == null) {
            txtAreaFactura.setText("No hay clientes en la cola.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("              SIGUIENTE CLIENTE\n");
        sb.append("═══════════════════════════════════════════════════\n\n");
        sb.append("Cliente: ").append(cliente.getName()).append(" ").append(cliente.getLastName()).append("\n");
        sb.append("ID: ").append(cliente.getId()).append("\n");
        sb.append("Email: ").append(cliente.getEmail()).append("\n");
        sb.append("Prioridad: ").append(cliente.getPriority()).append(" (").append(cliente.getPriorityDescription()).append(")\n");
        sb.append("───────────────────────────────────────────────────\n\n");
        sb.append("CARRITO:\n\n");

        if (cliente.getCart().isEmpty()) {
            sb.append("  (Carrito vacío)\n");
        } else {
            for (Cliente.CartItem item : cliente.getCart()) {
                sb.append("  ").append(item.toString()).append("\n");
            }
            sb.append("\nTOTAL ESTIMADO: $").append(String.format("%.2f", cliente.calculateTotal())).append("\n");
        }

        txtAreaFactura.setText(sb.toString());
    }

    private void onVerEstadisticas() {
        String stats = storeService.getStatistics();
        double valorInventario = storeService.calculateTotalInventoryValue();

        stats += "Valor total del inventario: $" + String.format("%.2f", valorInventario) + "\n";

        txtAreaFactura.setText(stats);
    }

    // ========== PANEL: GESTIÓN DE UBICACIONES ==========

    private JPanel createLocationManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("📍 Gestión de Ubicaciones");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Panel izquierdo: Agregar ubicación
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Agregar Nueva Ubicación"));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtLocId = new JTextField(20);
        JTextField txtLocName = new JTextField(20);
        JTextField txtLocAddress = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtLocId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtLocName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtLocAddress, gbc);

        JButton btnAddLocation = new JButton("➕ Agregar Ubicación");
        btnAddLocation.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(btnAddLocation, gbc);

        leftPanel.add(formPanel, BorderLayout.NORTH);

        // Panel derecho: Agregar conexión
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Agregar Conexión (Arista)"));

        JPanel connPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cmbFrom = new JComboBox<>();
        JComboBox<String> cmbTo = new JComboBox<>();
        JTextField txtDistance = new JTextField(10);

        gbc.gridx = 0; gbc.gridy = 0;
        connPanel.add(new JLabel("Desde:"), gbc);
        gbc.gridx = 1;
        connPanel.add(cmbFrom, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        connPanel.add(new JLabel("Hasta:"), gbc);
        gbc.gridx = 1;
        connPanel.add(cmbTo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        connPanel.add(new JLabel("Distancia (km):"), gbc);
        gbc.gridx = 1;
        connPanel.add(txtDistance, gbc);

        JButton btnAddConnection = new JButton("🔗 Agregar Conexión");
        btnAddConnection.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        connPanel.add(btnAddConnection, gbc);

        rightPanel.add(connPanel, BorderLayout.NORTH);

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Tabla de ubicaciones
        LocationTableModel locationsTableModel = new LocationTableModel();
        JTable locationsTable = new JTable(locationsTableModel);
        locationsTable.setRowHeight(28);
        locationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        locationsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        locationsTable.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane(locationsTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Ubicaciones Registradas"));
        scroll.setPreferredSize(new Dimension(0, 200));
        panel.add(scroll, BorderLayout.SOUTH);

        // Listeners
        btnAddLocation.addActionListener(e -> {
            String id = txtLocId.getText().trim();
            String name = txtLocName.getText().trim();
            String address = txtLocAddress.getText().trim();

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID y Nombre son obligatorios");
                return;
            }

            Location location = new Location(id, name, address);
            storeService.addLocationToGraph(location);

            JOptionPane.showMessageDialog(this, "✅ Ubicación agregada exitosamente");
            txtLocId.setText("");
            txtLocName.setText("");
            txtLocAddress.setText("");

            // Actualizar combos y tabla
            cmbFrom.addItem(name + " (" + id + ")");
            cmbTo.addItem(name + " (" + id + ")");
            locationsTableModel.setData(new ArrayList<>(storeService.getAllLocations()));

            // Actualizar también los combos de rutas si existen
            updateRoutesCombos();
        });

        btnAddConnection.addActionListener(e -> {
            if (cmbFrom.getSelectedItem() == null || cmbTo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Seleccione ambas ubicaciones");
                return;
            }

            try {
                double distance = Double.parseDouble(txtDistance.getText().trim());
                if (distance <= 0) {
                    JOptionPane.showMessageDialog(this, "La distancia debe ser mayor a 0");
                    return;
                }

                // Extraer IDs de las selecciones
                String fromStr = (String) cmbFrom.getSelectedItem();
                String toStr = (String) cmbTo.getSelectedItem();
                String fromId = fromStr.substring(fromStr.lastIndexOf("(") + 1, fromStr.lastIndexOf(")"));
                String toId = toStr.substring(toStr.lastIndexOf("(") + 1, toStr.lastIndexOf(")"));

                // Buscar ubicaciones
                Location from = null, to = null;
                for (Location loc : storeService.getAllLocations()) {
                    if (loc.getId().equals(fromId)) from = loc;
                    if (loc.getId().equals(toId)) to = loc;
                }

                if (from != null && to != null) {
                    storeService.addConnection(from, to, distance);
                    JOptionPane.showMessageDialog(this, "✅ Conexión agregada: " + distance + " km");
                    txtDistance.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: Ubicaciones no encontradas");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Distancia inválida");
            }
        });

        // Inicializar con ubicación de la tienda si existe
        if (storeService.getStore().getLocation() != null) {
            Location storeLoc = storeService.getStore().getLocation();
            cmbFrom.addItem(storeLoc.getName() + " (" + storeLoc.getId() + ")");
            cmbTo.addItem(storeLoc.getName() + " (" + storeLoc.getId() + ")");
            locationsTableModel.setData(new ArrayList<>(storeService.getAllLocations()));
        }

        return panel;
    }

    // ========== PANEL: RUTAS DE ENTREGA ==========

    private JPanel createDeliveryRoutesPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("🚚 Optimización de Rutas de Entrega");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titulo, BorderLayout.NORTH);

        // Panel superior: Selector de ruta
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Calcular Ruta Óptima (Algoritmo de Dijkstra)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbOriginRoutes = new JComboBox<>();
        cmbDestinationRoutes = new JComboBox<>();

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Origen:"), gbc);
        gbc.gridx = 1;
        topPanel.add(cmbOriginRoutes, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(new JLabel("Destino:"), gbc);
        gbc.gridx = 1;
        topPanel.add(cmbDestinationRoutes, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnCalculate = new JButton("🔍 Calcular Ruta Más Corta");
        btnCalculate.setFont(new Font("Arial", Font.BOLD, 13));
        btnCalculate.setBackground(new Color(33, 150, 243));
        btnCalculate.setForeground(Color.WHITE);

        JButton btnRefresh = new JButton("🔄 Actualizar Ubicaciones");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefresh.setBackground(new Color(158, 158, 158));
        btnRefresh.setForeground(Color.WHITE);

        btnPanel.add(btnCalculate);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        topPanel.add(btnPanel, gbc);

        panel.add(topPanel, BorderLayout.NORTH);

        // Panel central: Resultado
        JTextArea txtResult = new JTextArea(20, 60);
        txtResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResult.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtResult);
        scroll.setBorder(BorderFactory.createTitledBorder("Resultado de la Búsqueda"));
        panel.add(scroll, BorderLayout.CENTER);

        // Inicializar combos con ubicaciones
        updateRoutesCombos();

        // Listener para refrescar combos
        btnRefresh.addActionListener(e -> {
            updateRoutesCombos();
            JOptionPane.showMessageDialog(this, "✅ Ubicaciones actualizadas",
                    "Actualización", JOptionPane.INFORMATION_MESSAGE);
        });

        // Listener para calcular ruta
        btnCalculate.addActionListener(e -> {
            if (cmbOriginRoutes.getSelectedItem() == null || cmbDestinationRoutes.getSelectedItem() == null) {
                txtResult.setText("⚠️ Por favor seleccione origen y destino");
                return;
            }

            String originStr = (String) cmbOriginRoutes.getSelectedItem();
            String destStr = (String) cmbDestinationRoutes.getSelectedItem();
            String originId = originStr.substring(originStr.lastIndexOf("(") + 1, originStr.lastIndexOf(")"));
            String destId = destStr.substring(destStr.lastIndexOf("(") + 1, destStr.lastIndexOf(")"));

            Location origin = null, dest = null;
            for (Location loc : storeService.getAllLocations()) {
                if (loc.getId().equals(originId)) origin = loc;
                if (loc.getId().equals(destId)) dest = loc;
            }

            if (origin == null || dest == null) {
                txtResult.setText("❌ Error: Ubicaciones no encontradas");
                return;
            }

            LocationGraph.PathResult result = storeService.findShortestPath(origin, dest);

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════\n");
            sb.append("        RUTA ÓPTIMA (ALGORITMO DE DIJKSTRA)\n");
            sb.append("═══════════════════════════════════════════════════\n\n");
            sb.append("Origen: ").append(origin.getName()).append("\n");
            sb.append("Destino: ").append(dest.getName()).append("\n\n");

            if (result.isConnected()) {
                sb.append("✅ RUTA ENCONTRADA\n\n");
                sb.append("Distancia Total: ").append(String.format("%.2f km", result.getTotalDistance())).append("\n\n");
                sb.append("Camino:\n");

                List<Location> path = result.getPath();
                for (int i = 0; i < path.size(); i++) {
                    sb.append("  ").append(i + 1).append(". ").append(path.get(i).getName());
                    sb.append(" (").append(path.get(i).getAddress()).append(")");

                    if (i < path.size() - 1) {
                        double segDist = storeService.getDeliveryGraph().getEdgeWeight(path.get(i), path.get(i + 1));
                        sb.append("\n     ↓ ").append(String.format("%.2f km", segDist));
                    }
                    sb.append("\n");
                }
            } else {
                sb.append("❌ NO HAY RUTA DISPONIBLE\n\n");
                sb.append("Las ubicaciones están desconectadas.\n");
                sb.append("Agregue conexiones en la pestaña '📍 Ubicaciones'.\n");
            }

            sb.append("\n═══════════════════════════════════════════════════\n");
            txtResult.setText(sb.toString());
        });

        return panel;
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
