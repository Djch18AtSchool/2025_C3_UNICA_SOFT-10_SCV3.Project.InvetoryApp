package com.gestioninventario.inventory.domain;

import com.gestioninventario.inventory.common.ArbolProductos;
import com.gestioninventario.inventory.common.ColaClientes;
import java.util.List;

/**
 * Representa una tienda con inventario de productos y cola de clientes.
 */
public class Tienda {
    private String nombre;
    private ArbolProductos inventario;
    private ColaClientes colaClientes;
    
    public Tienda(String nombre) {
        this.nombre = nombre;
        this.inventario = new ArbolProductos();
        this.colaClientes = new ColaClientes();
    }
    
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public ArbolProductos getInventario() {
        return inventario;
    }
    
    public ColaClientes getColaClientes() {
        return colaClientes;
    }
    
    // Métodos de gestión de inventario
    
    /**
     * Agrega un producto al inventario de la tienda.
     */
    public void agregarProductoAlInventario(Product producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        inventario.insertar(producto);
    }
    
    /**
     * Busca un producto en el inventario por nombre.
     */
    public Product buscarProductoEnInventario(String nombre) {
        return inventario.buscar(nombre);
    }
    
    /**
     * Busca productos en el inventario que contengan el texto especificado.
     */
    public List<Product> buscarProductosPorCoincidencia(String texto) {
        return inventario.buscarPorCoincidencia(texto);
    }
    
    /**
     * Elimina un producto del inventario por nombre.
     */
    public boolean eliminarProductoDelInventario(String nombre) {
        return inventario.eliminar(nombre);
    }
    
    /**
     * Obtiene todos los productos del inventario ordenados alfabéticamente.
     */
    public List<Product> obtenerTodosLosProductos() {
        return inventario.obtenerTodos();
    }
    
    /**
     * Verifica si hay suficiente stock de un producto.
     */
    public boolean verificarStock(String nombreProducto, int cantidadRequerida) {
        Product producto = inventario.buscar(nombreProducto);
        return producto != null && producto.getQuantity() >= cantidadRequerida;
    }
    
    /**
     * Reduce el stock de un producto.
     * @return true si se pudo reducir, false si no hay suficiente stock.
     */
    public boolean reducirStock(String nombreProducto, int cantidad) {
        Product producto = inventario.buscar(nombreProducto);
        if (producto != null && producto.getQuantity() >= cantidad) {
            producto.setQuantity(producto.getQuantity() - cantidad);
            // Actualizar en el árbol
            inventario.insertar(producto);
            return true;
        }
        return false;
    }
    
    /**
     * Aumenta el stock de un producto.
     */
    public boolean aumentarStock(String nombreProducto, int cantidad) {
        Product producto = inventario.buscar(nombreProducto);
        if (producto != null) {
            producto.setQuantity(producto.getQuantity() + cantidad);
            // Actualizar en el árbol
            inventario.insertar(producto);
            return true;
        }
        return false;
    }
    
    // Métodos de gestión de clientes
    
    /**
     * Agrega un cliente a la cola de atención.
     */
    public void agregarClienteACola(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        colaClientes.encolar(cliente);
    }
    
    /**
     * Atiende al siguiente cliente (el de mayor prioridad).
     * Procesa su compra reduciendo el stock de los productos en su carrito.
     * @return El cliente atendido o null si no hay clientes en cola.
     */
    public Cliente atenderSiguienteCliente() {
        Cliente cliente = colaClientes.desencolar();
        if (cliente != null) {
            procesarCompra(cliente);
        }
        return cliente;
    }
    
    /**
     * Procesa la compra de un cliente, reduciendo el stock del inventario.
     */
    private void procesarCompra(Cliente cliente) {
        for (Cliente.ItemCarrito item : cliente.getCarrito()) {
            Product producto = item.getProducto();
            int cantidad = item.getCantidad();
            
            // Verificar y reducir stock
            Product productoInventario = inventario.buscar(producto.getName());
            if (productoInventario != null) {
                if (productoInventario.getQuantity() >= cantidad) {
                    reducirStock(producto.getName(), cantidad);
                } else {
                    // Stock insuficiente - ajustar cantidad al disponible
                    int cantidadDisponible = productoInventario.getQuantity();
                    if (cantidadDisponible > 0) {
                        reducirStock(producto.getName(), cantidadDisponible);
                        item.setCantidad(cantidadDisponible);
                    }
                }
            }
        }
    }
    
    /**
     * Ver el siguiente cliente en la cola sin atenderlo.
     */
    public Cliente verSiguienteCliente() {
        return colaClientes.verFrente();
    }
    
    /**
     * Obtiene todos los clientes en la cola.
     */
    public List<Cliente> obtenerTodosLosClientes() {
        return colaClientes.obtenerTodos();
    }
    
    /**
     * Elimina un cliente de la cola por su ID.
     */
    public boolean eliminarClienteDeCola(String clienteId) {
        return colaClientes.eliminarPorId(clienteId);
    }
    
    /**
     * Busca un cliente en la cola por su ID.
     */
    public Cliente buscarClienteEnCola(String clienteId) {
        return colaClientes.buscarPorId(clienteId);
    }
    
    // Métodos auxiliares
    
    /**
     * Retorna el número de productos en el inventario.
     */
    public int cantidadProductosEnInventario() {
        return inventario.size();
    }
    
    /**
     * Retorna el número de clientes en cola.
     */
    public int cantidadClientesEnCola() {
        return colaClientes.size();
    }
    
    /**
     * Verifica si el inventario está vacío.
     */
    public boolean inventarioVacio() {
        return inventario.isEmpty();
    }
    
    /**
     * Verifica si la cola de clientes está vacía.
     */
    public boolean colaVacia() {
        return colaClientes.isEmpty();
    }
    
    @Override
    public String toString() {
        return "Tienda{nombre='" + nombre + "', productos=" + cantidadProductosEnInventario() +
               ", clientesEnCola=" + cantidadClientesEnCola() + "}";
    }
}

