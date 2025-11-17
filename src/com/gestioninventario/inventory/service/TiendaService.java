package com.gestioninventario.inventory.service;

import com.gestioninventario.inventory.domain.Cliente;
import com.gestioninventario.inventory.domain.Product;
import com.gestioninventario.inventory.domain.Tienda;
import java.util.List;

/**
 * Servicio para gestionar la lógica de negocio de la tienda.
 */
public class TiendaService {
    private Tienda tienda;
    
    public TiendaService(String nombreTienda) {
        this.tienda = new Tienda(nombreTienda);
    }
    
    public Tienda getTienda() {
        return tienda;
    }
    
    // Métodos de gestión de inventario
    
    /**
     * Agrega un producto al inventario de la tienda.
     */
    public void agregarProductoAlInventario(Product producto) {
        tienda.agregarProductoAlInventario(producto);
    }
    
    /**
     * Busca un producto en el inventario por nombre.
     */
    public Product buscarProductoPorNombre(String nombre) {
        return tienda.buscarProductoEnInventario(nombre);
    }
    
    /**
     * Busca productos que contengan el texto especificado en su nombre.
     */
    public List<Product> buscarProductosPorCoincidencia(String texto) {
        return tienda.buscarProductosPorCoincidencia(texto);
    }
    
    /**
     * Elimina un producto del inventario.
     */
    public boolean eliminarProducto(String nombre) {
        return tienda.eliminarProductoDelInventario(nombre);
    }
    
    /**
     * Obtiene todos los productos del inventario.
     */
    public List<Product> obtenerTodosLosProductos() {
        return tienda.obtenerTodosLosProductos();
    }
    
    /**
     * Actualiza el stock de un producto.
     */
    public boolean actualizarStock(String nombreProducto, int nuevaCantidad) {
        Product producto = tienda.buscarProductoEnInventario(nombreProducto);
        if (producto != null) {
            producto.setQuantity(nuevaCantidad);
            tienda.agregarProductoAlInventario(producto); // Actualizar en el árbol
            return true;
        }
        return false;
    }
    
    // Métodos de gestión de clientes
    
    /**
     * Agrega un cliente a la cola de atención.
     */
    public void agregarClienteACola(Cliente cliente) {
        tienda.agregarClienteACola(cliente);
    }
    
    /**
     * Agrega un producto al carrito de un cliente que está en la cola.
     * Verifica que haya stock disponible antes de agregarlo.
     */
    public boolean agregarProductoAlCarritoCliente(String clienteId, String nombreProducto, int cantidad) {
        Cliente cliente = tienda.buscarClienteEnCola(clienteId);
        if (cliente == null) {
            return false;
        }
        
        Product producto = tienda.buscarProductoEnInventario(nombreProducto);
        if (producto == null) {
            return false;
        }
        
        // Verificar stock disponible
        if (!tienda.verificarStock(nombreProducto, cantidad)) {
            return false;
        }
        
        cliente.agregarAlCarrito(producto, cantidad);
        return true;
    }
    
    /**
     * Elimina un producto del carrito de un cliente.
     */
    public boolean eliminarProductoDelCarritoCliente(String clienteId, String productoId) {
        Cliente cliente = tienda.buscarClienteEnCola(clienteId);
        if (cliente == null) {
            return false;
        }
        
        return cliente.eliminarDelCarrito(productoId);
    }
    
    /**
     * Atiende al siguiente cliente de la cola.
     * Procesa su compra y genera una factura.
     * @return El cliente atendido o null si no hay clientes.
     */
    public Cliente atenderSiguienteCliente() {
        return tienda.atenderSiguienteCliente();
    }
    
    /**
     * Genera el texto de la factura para un cliente.
     */
    public String generarFactura(Cliente cliente) {
        if (cliente == null) {
            return "No hay cliente para facturar.";
        }
        
        StringBuilder factura = new StringBuilder();
        factura.append("═══════════════════════════════════════════════════\n");
        factura.append("                    FACTURA\n");
        factura.append("           ").append(tienda.getNombre()).append("\n");
        factura.append("═══════════════════════════════════════════════════\n\n");
        
        factura.append("Cliente: ").append(cliente.getNombre()).append(" ").append(cliente.getApellido()).append("\n");
        factura.append("ID: ").append(cliente.getId()).append("\n");
        factura.append("Email: ").append(cliente.getEmail()).append("\n");
        factura.append("Tipo: ").append(cliente.getPrioridadDescripcion()).append("\n");
        factura.append("───────────────────────────────────────────────────\n\n");
        
        factura.append("PRODUCTOS:\n\n");
        
        if (cliente.getCarrito().isEmpty()) {
            factura.append("  (Carrito vacío)\n");
        } else {
            for (Cliente.ItemCarrito item : cliente.getCarrito()) {
                Product prod = item.getProducto();
                factura.append(String.format("  %-30s x%2d  $%.2f\n", 
                    prod.getName(), 
                    item.getCantidad(), 
                    item.getSubtotal()));
            }
        }
        
        factura.append("\n───────────────────────────────────────────────────\n");
        factura.append(String.format("TOTAL: $%.2f\n", cliente.calcularTotal()));
        factura.append("═══════════════════════════════════════════════════\n");
        
        return factura.toString();
    }
    
    /**
     * Ver el siguiente cliente sin atenderlo.
     */
    public Cliente verSiguienteCliente() {
        return tienda.verSiguienteCliente();
    }
    
    /**
     * Obtiene todos los clientes en la cola.
     */
    public List<Cliente> obtenerTodosLosClientes() {
        return tienda.obtenerTodosLosClientes();
    }
    
    /**
     * Elimina un cliente de la cola.
     */
    public boolean eliminarClienteDeCola(String clienteId) {
        return tienda.eliminarClienteDeCola(clienteId);
    }
    
    /**
     * Busca un cliente en la cola por ID.
     */
    public Cliente buscarClienteEnCola(String clienteId) {
        return tienda.buscarClienteEnCola(clienteId);
    }
    
    // Métodos de información
    
    /**
     * Obtiene estadísticas de la tienda.
     */
    public String obtenerEstadisticas() {
        StringBuilder stats = new StringBuilder();
        stats.append("═══ ESTADÍSTICAS DE LA TIENDA ═══\n");
        stats.append("Nombre: ").append(tienda.getNombre()).append("\n");
        stats.append("Productos en inventario: ").append(tienda.cantidadProductosEnInventario()).append("\n");
        stats.append("Clientes en cola: ").append(tienda.cantidadClientesEnCola()).append("\n");
        
        if (!tienda.colaVacia()) {
            Cliente siguiente = tienda.verSiguienteCliente();
            stats.append("Próximo cliente: ").append(siguiente.getNombre())
                 .append(" (").append(siguiente.getPrioridadDescripcion()).append(")\n");
        }
        
        return stats.toString();
    }
    
    /**
     * Calcula el valor total del inventario.
     */
    public double calcularValorTotalInventario() {
        double total = 0.0;
        for (Product producto : tienda.obtenerTodosLosProductos()) {
            total += producto.getPrice() * producto.getQuantity();
        }
        return total;
    }
}

