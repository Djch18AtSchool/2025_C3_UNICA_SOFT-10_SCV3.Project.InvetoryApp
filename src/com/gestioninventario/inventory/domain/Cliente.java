package com.gestioninventario.inventory.domain;

import com.gestioninventario.inventory.common.SinglyLinkedList;

/**
 * Representa un Cliente con prioridad (1-3) y un carrito de compras.
 * Prioridad: 1 = Básico, 2 = Afiliado, 3 = Premium
 */
public class Cliente {
    private String id;
    private String nombre;
    private String apellido;
    private String email;
    private int prioridad; // 1 = Básico, 2 = Afiliado, 3 = Premium
    private SinglyLinkedList<ItemCarrito> carrito; // Lista de productos en el carrito
    
    public Cliente(String id, String nombre, String apellido, String email, int prioridad) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        setPrioridad(prioridad);
        this.carrito = new SinglyLinkedList<>();
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public int getPrioridad() {
        return prioridad;
    }
    
    public void setPrioridad(int prioridad) {
        if (prioridad < 1 || prioridad > 3) {
            throw new IllegalArgumentException("La prioridad debe estar entre 1 y 3");
        }
        this.prioridad = prioridad;
    }
    
    public SinglyLinkedList<ItemCarrito> getCarrito() {
        return carrito;
    }
    
    /**
     * Agrega un producto al carrito con la cantidad especificada.
     */
    public void agregarAlCarrito(Product producto, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        
        // Buscar si el producto ya está en el carrito
        ItemCarrito existente = carrito.findFirst(item -> 
            item.getProducto().getId().equals(producto.getId()));
        
        if (existente != null) {
            // Incrementar cantidad
            existente.setCantidad(existente.getCantidad() + cantidad);
        } else {
            // Agregar nuevo item
            carrito.addLast(new ItemCarrito(producto, cantidad));
        }
    }
    
    /**
     * Elimina un producto del carrito.
     */
    public boolean eliminarDelCarrito(String productoId) {
        return carrito.removeIf(item -> item.getProducto().getId().equals(productoId));
    }
    
    /**
     * Calcula el total del carrito.
     */
    public double calcularTotal() {
        double total = 0.0;
        for (ItemCarrito item : carrito) {
            total += item.getSubtotal();
        }
        return total;
    }
    
    /**
     * Limpia el carrito.
     */
    public void vaciarCarrito() {
        this.carrito = new SinglyLinkedList<>();
    }
    
    public String getPrioridadDescripcion() {
        switch (prioridad) {
            case 1: return "Básico";
            case 2: return "Afiliado";
            case 3: return "Premium";
            default: return "Desconocido";
        }
    }
    
    @Override
    public String toString() {
        return "Cliente{id='" + id + "', nombre='" + nombre + " " + apellido + 
               "', email='" + email + "', prioridad=" + prioridad + 
               " (" + getPrioridadDescripcion() + ")}";
    }
    
    /**
     * Clase interna para representar un item en el carrito.
     */
    public static class ItemCarrito {
        private Product producto;
        private int cantidad;
        
        public ItemCarrito(Product producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }
        
        public Product getProducto() {
            return producto;
        }
        
        public void setProducto(Product producto) {
            this.producto = producto;
        }
        
        public int getCantidad() {
            return cantidad;
        }
        
        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }
        
        public double getSubtotal() {
            return producto.getPrice() * cantidad;
        }
        
        @Override
        public String toString() {
            return producto.getName() + " x" + cantidad + " = $" + getSubtotal();
        }
    }
}

