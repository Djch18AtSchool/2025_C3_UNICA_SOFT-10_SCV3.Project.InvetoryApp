package com.gestioninventario.inventory.domain;

import com.gestioninventario.inventory.common.SinglyLinkedList;

/**
 * Representa un Cliente con prioridad (1-3) y un carrito de compras.
 * Prioridad: 1 = Básico, 2 = Afiliado, 3 = Premium
 */
public class Cliente {
    private String id;
    private String name;
    private String lastName;
    private String email;
    private int priority; // 1 = Básico, 2 = Afiliado, 3 = Premium
    private SinglyLinkedList<CartItem> cart; // Lista de productos en el carrito
    private Location location; // Ubicación del cliente para entrega

    public Cliente(String id, String name, String lastName, String email, int priority) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        setPriority(priority);
        this.cart = new SinglyLinkedList<>();
        this.location = null;
    }

    public Cliente(String id, String name, String lastName, String email, int priority, Location location) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        setPriority(priority);
        this.cart = new SinglyLinkedList<>();
        this.location = location;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (priority < 1 || priority > 3) {
            throw new IllegalArgumentException("La prioridad debe estar entre 1 y 3");
        }
        this.priority = priority;
    }

    public SinglyLinkedList<CartItem> getCart() {
        return cart;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Agrega un producto al carrito con la cantidad especificada.
     */
    public void addToCart(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        // Buscar si el producto ya está en el carrito
        CartItem existing = cart.findFirst(item ->
                item.getProduct().getId().equals(product.getId()));

        if (existing != null) {
            // Incrementar cantidad
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            // Agregar nuevo item
            cart.addLast(new CartItem(product, quantity));
        }
    }

    /**
     * Elimina un producto del carrito.
     */
    public boolean removeFromCart(String productId) {
        return cart.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    /**
     * Calcula el total del carrito.
     */
    public double calculateTotal() {
        double total = 0.0;
        for (CartItem item : cart) {
            total += item.getSubtotal();
        }
        return total;
    }

    /**
     * Limpia el carrito.
     */
    public void clearCart() {
        this.cart = new SinglyLinkedList<>();
    }

    public String getPriorityDescription() {
        switch (priority) {
            case 1: return "Básico";
            case 2: return "Afiliado";
            case 3: return "Premium";
            default: return "Desconocido";
        }
    }

    @Override
    public String toString() {
        return "Cliente{id='" + id + "', nombre='" + name + " " + lastName +
                "', email='" + email + "', prioridad=" + priority +
                " (" + getPriorityDescription() + ")}";
    }

    /**
     * Clase interna para representar un item en el carrito.
     */
    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getSubtotal() {
            return product.getPrice() * quantity;
        }

        @Override
        public String toString() {
            return product.getName() + " x" + quantity + " = $" + getSubtotal();
        }
    }
}

