package com.gestioninventario.inventory.ui;

import com.gestioninventario.inventory.domain.Product;
import com.gestioninventario.inventory.domain.Cliente;
import com.gestioninventario.inventory.repository.ProductRepository;
import com.gestioninventario.inventory.service.ProductService;
import com.gestioninventario.inventory.service.TiendaService;

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
    private final TiendaService tiendaService;

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

    public MenuAdministrador() {
        // init repo & service (in memory)
        this.productRepository = new ProductRepository();
        this.productService = new ProductService(productRepository);
        this.tiendaService = new TiendaService("TechStore");

        setTitle("Gestor de Componentes - Inventario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
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
        double costoTotal = productService.getAll().calcularCostoTotal();
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
    private JList<String> listaInventarioTienda;
    private DefaultListModel<String> modeloInventarioTienda;
    
    private JPanel createInventarioTiendaPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel titulo = new JLabel("Inventario de la Tienda (Árbol Binario de Búsqueda)");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titulo, BorderLayout.NORTH);
        
        // Lista de productos en el inventario
        modeloInventarioTienda = new DefaultListModel<>();
        listaInventarioTienda = new JList<>(modeloInventarioTienda);
        listaInventarioTienda.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(listaInventarioTienda);
        panel.add(scroll, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregarDesdeRepo = new JButton("Agregar producto desde repositorio");
        JButton btnBuscar = new JButton("Buscar producto");
        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnEliminar = new JButton("Eliminar del inventario");
        
        btnPanel.add(btnAgregarDesdeRepo);
        btnPanel.add(btnBuscar);
        btnPanel.add(btnRefrescar);
        btnPanel.add(btnEliminar);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // Listeners
        btnAgregarDesdeRepo.addActionListener(e -> onAgregarProductoAlInventario());
        btnBuscar.addActionListener(e -> onBuscarEnInventario());
        btnRefrescar.addActionListener(e -> refreshInventarioTienda());
        btnEliminar.addActionListener(e -> onEliminarDeInventario());
        
        refreshInventarioTienda();
        return panel;
    }
    
    private void refreshInventarioTienda() {
        modeloInventarioTienda.clear();
        List<Product> productos = tiendaService.obtenerTodosLosProductos();
        
        if (productos.isEmpty()) {
            modeloInventarioTienda.addElement("(Inventario vacío)");
        } else {
            for (Product p : productos) {
                modeloInventarioTienda.addElement(String.format("%-30s | Stock: %3d | $%.2f", 
                    p.getName(), p.getQuantity(), p.getPrice()));
            }
        }
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
                tiendaService.agregarProductoAlInventario(producto);
                JOptionPane.showMessageDialog(this, "Producto agregado al inventario de la tienda.");
                refreshInventarioTienda();
            }
        }
    }
    
    private void onBuscarEnInventario() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del producto:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            Product producto = tiendaService.buscarProductoPorNombre(nombre.trim());
            if (producto != null) {
                JOptionPane.showMessageDialog(this, 
                    "Producto encontrado:\n" + producto.toString());
            } else {
                // Buscar por coincidencia
                List<Product> coincidencias = tiendaService.buscarProductosPorCoincidencia(nombre.trim());
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
            if (tiendaService.eliminarProducto(nombre.trim())) {
                JOptionPane.showMessageDialog(this, "Producto eliminado del inventario.");
                refreshInventarioTienda();
            } else {
                JOptionPane.showMessageDialog(this, "Producto no encontrado.");
            }
        }
    }
    
    // ========== PANEL: GESTIÓN DE CLIENTES ==========
    private JList<String> listaClientes;
    private DefaultListModel<String> modeloClientes;
    
    private JPanel createGestionClientesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel titulo = new JLabel("Cola de Clientes (Cola de Prioridad)");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titulo, BorderLayout.NORTH);
        
        // Lista de clientes
        modeloClientes = new DefaultListModel<>();
        listaClientes = new JList<>(modeloClientes);
        listaClientes.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(listaClientes);
        panel.add(scroll, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregar = new JButton("Agregar cliente");
        JButton btnVerCarrito = new JButton("Ver carrito");
        JButton btnAgregarProducto = new JButton("Agregar producto al carrito");
        JButton btnRefrescar = new JButton("Refrescar");
        
        btnPanel.add(btnAgregar);
        btnPanel.add(btnVerCarrito);
        btnPanel.add(btnAgregarProducto);
        btnPanel.add(btnRefrescar);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // Listeners
        btnAgregar.addActionListener(e -> onAgregarCliente());
        btnVerCarrito.addActionListener(e -> onVerCarritoCliente());
        btnAgregarProducto.addActionListener(e -> onAgregarProductoAlCarrito());
        btnRefrescar.addActionListener(e -> refreshColaClientes());
        
        refreshColaClientes();
        return panel;
    }
    
    private void refreshColaClientes() {
        modeloClientes.clear();
        List<Cliente> clientes = tiendaService.obtenerTodosLosClientes();
        
        if (clientes.isEmpty()) {
            modeloClientes.addElement("(No hay clientes en cola)");
        } else {
            for (int i = 0; i < clientes.size(); i++) {
                Cliente c = clientes.get(i);
                modeloClientes.addElement(String.format("%d. %-20s | Prioridad: %d (%s) | Items: %d", 
                    i + 1, c.getNombre() + " " + c.getApellido(), 
                    c.getPrioridad(), c.getPrioridadDescripcion(),
                    contarItemsCarrito(c)));
            }
        }
    }
    
    private int contarItemsCarrito(Cliente cliente) {
        int count = 0;
        for (@SuppressWarnings("unused") Cliente.ItemCarrito item : cliente.getCarrito()) {
            count++;
        }
        return count;
    }
    
    private void onAgregarCliente() {
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        
        JTextField txtId = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtApellido = new JTextField();
        JTextField txtEmail = new JTextField();
        String[] prioridades = {"1 - Básico", "2 - Afiliado", "3 - Premium"};
        JComboBox<String> cmbPrioridad = new JComboBox<>(prioridades);
        
        formPanel.add(new JLabel("ID:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(txtNombre);
        formPanel.add(new JLabel("Apellido:"));
        formPanel.add(txtApellido);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("Prioridad:"));
        formPanel.add(cmbPrioridad);
        
        int result = JOptionPane.showConfirmDialog(this, formPanel, 
            "Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = txtId.getText().trim();
                String nombre = txtNombre.getText().trim();
                String apellido = txtApellido.getText().trim();
                String email = txtEmail.getText().trim();
                int prioridad = cmbPrioridad.getSelectedIndex() + 1;
                
                if (id.isEmpty() || nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "ID y Nombre son obligatorios.");
                    return;
                }
                
                Cliente cliente = new Cliente(id, nombre, apellido, email, prioridad);
                tiendaService.agregarClienteACola(cliente);
                JOptionPane.showMessageDialog(this, "Cliente agregado a la cola.");
                refreshColaClientes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
    
    private void onVerCarritoCliente() {
        List<Cliente> clientes = tiendaService.obtenerTodosLosClientes();
        if (clientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay clientes en cola.");
            return;
        }
        
        String[] ids = clientes.stream()
            .map(c -> c.getId() + " - " + c.getNombre())
            .toArray(String[]::new);
        
        String seleccion = (String) JOptionPane.showInputDialog(this,
            "Seleccione un cliente:", "Ver Carrito",
            JOptionPane.PLAIN_MESSAGE, null, ids, ids[0]);
        
        if (seleccion != null) {
            String clienteId = seleccion.split(" - ")[0];
            Cliente cliente = tiendaService.buscarClienteEnCola(clienteId);
            
            if (cliente != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Cliente: ").append(cliente.getNombre()).append("\n");
                sb.append("Carrito:\n\n");
                
                if (cliente.getCarrito().isEmpty()) {
                    sb.append("(Carrito vacío)");
                } else {
                    for (Cliente.ItemCarrito item : cliente.getCarrito()) {
                        sb.append(item.toString()).append("\n");
                    }
                    sb.append("\nTOTAL: $").append(String.format("%.2f", cliente.calcularTotal()));
                }
                
                JOptionPane.showMessageDialog(this, sb.toString());
            }
        }
    }
    
    private void onAgregarProductoAlCarrito() {
        List<Cliente> clientes = tiendaService.obtenerTodosLosClientes();
        if (clientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay clientes en cola.");
            return;
        }
        
        List<Product> productos = tiendaService.obtenerTodosLosProductos();
        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos en el inventario.");
            return;
        }
        
        // Seleccionar cliente
        String[] clienteIds = clientes.stream()
            .map(c -> c.getId() + " - " + c.getNombre())
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
            
            if (tiendaService.agregarProductoAlCarritoCliente(clienteId, productoNombre, cantidad)) {
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
    
    private JPanel createAtencionClientesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel titulo = new JLabel("Atención de Clientes y Facturación");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titulo, BorderLayout.NORTH);
        
        // Área de texto para mostrar factura
        txtAreaFactura = new JTextArea(20, 60);
        txtAreaFactura.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtAreaFactura.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtAreaFactura);
        panel.add(scroll, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAtender = new JButton("Atender siguiente cliente");
        JButton btnVerSiguiente = new JButton("Ver siguiente sin atender");
        JButton btnEstadisticas = new JButton("Ver estadísticas");
        
        btnPanel.add(btnAtender);
        btnPanel.add(btnVerSiguiente);
        btnPanel.add(btnEstadisticas);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // Listeners
        btnAtender.addActionListener(e -> onAtenderCliente());
        btnVerSiguiente.addActionListener(e -> onVerSiguienteCliente());
        btnEstadisticas.addActionListener(e -> onVerEstadisticas());
        
        return panel;
    }
    
    private void onAtenderCliente() {
        Cliente cliente = tiendaService.atenderSiguienteCliente();
        
        if (cliente == null) {
            txtAreaFactura.setText("No hay clientes en la cola para atender.");
            return;
        }
        
        String factura = tiendaService.generarFactura(cliente);
        txtAreaFactura.setText(factura);
        
        refreshColaClientes();
        refreshInventarioTienda();
        
        JOptionPane.showMessageDialog(this, "Cliente atendido exitosamente.");
    }
    
    private void onVerSiguienteCliente() {
        Cliente cliente = tiendaService.verSiguienteCliente();
        
        if (cliente == null) {
            txtAreaFactura.setText("No hay clientes en la cola.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("              SIGUIENTE CLIENTE\n");
        sb.append("═══════════════════════════════════════════════════\n\n");
        sb.append("Cliente: ").append(cliente.getNombre()).append(" ").append(cliente.getApellido()).append("\n");
        sb.append("ID: ").append(cliente.getId()).append("\n");
        sb.append("Email: ").append(cliente.getEmail()).append("\n");
        sb.append("Prioridad: ").append(cliente.getPrioridad()).append(" (").append(cliente.getPrioridadDescripcion()).append(")\n");
        sb.append("───────────────────────────────────────────────────\n\n");
        sb.append("CARRITO:\n\n");
        
        if (cliente.getCarrito().isEmpty()) {
            sb.append("  (Carrito vacío)\n");
        } else {
            for (Cliente.ItemCarrito item : cliente.getCarrito()) {
                sb.append("  ").append(item.toString()).append("\n");
            }
            sb.append("\nTOTAL ESTIMADO: $").append(String.format("%.2f", cliente.calcularTotal())).append("\n");
        }
        
        txtAreaFactura.setText(sb.toString());
    }
    
    private void onVerEstadisticas() {
        String stats = tiendaService.obtenerEstadisticas();
        double valorInventario = tiendaService.calcularValorTotalInventario();
        
        stats += "Valor total del inventario: $" + String.format("%.2f", valorInventario) + "\n";
        
        txtAreaFactura.setText(stats);
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
