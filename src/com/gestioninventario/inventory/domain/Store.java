package com.gestioninventario.inventory.domain;

import com.gestioninventario.inventory.common.ProductBinaryTree;
import com.gestioninventario.inventory.common.ClientPriorityQueue;
import java.util.List;

/**
 * Representa una tienda con inventario de productos y cola de clientes.
 */
public class Store {
    private String name;
    private ProductBinaryTree inventory;
    private ClientPriorityQueue clientQueue;
    private Location location; // Ubicación de la tienda
    
    public Store(String name) {
        this.name = name;
        this.inventory = new ProductBinaryTree();
        this.clientQueue = new ClientPriorityQueue();
        this.location = null;
    }
    
    public Store(String name, Location location) {
        this.name = name;
        this.inventory = new ProductBinaryTree();
        this.clientQueue = new ClientPriorityQueue();
        this.location = location;
    }
    
    // Getters y Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public ProductBinaryTree getInventory() {
        return inventory;
    }
    
    public ClientPriorityQueue getClientQueue() {
        return clientQueue;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    // Métodos de gestión de inventario
    
    /**
     * Agrega un producto al inventario de la tienda.
     */
    public void addProductToInventory(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        inventory.insert(product);
    }
    
    /**
     * Busca un producto en el inventario por nombre.
     */
    public Product searchProductInInventory(String name) {
        return inventory.search(name);
    }
    
    /**
     * Busca productos en el inventario que contengan el texto especificado.
     */
    public List<Product> searchProductsByMatch(String text) {
        return inventory.searchByMatch(text);
    }
    
    /**
     * Elimina un producto del inventario por nombre.
     */
    public boolean removeProductFromInventory(String name) {
        return inventory.delete(name);
    }
    
    /**
     * Obtiene todos los productos del inventario ordenados alfabéticamente.
     */
    public List<Product> getAllProducts() {
        return inventory.getAll();
    }
    
    /**
     * Verifica si hay suficiente stock de un producto.
     */
    public boolean verifyStock(String productName, int requiredQuantity) {
        Product product = inventory.search(productName);
        return product != null && product.getQuantity() >= requiredQuantity;
    }
    
    /**
     * Reduce el stock de un producto.
     * @return true si se pudo reducir, false si no hay suficiente stock.
     */
    public boolean reduceStock(String productName, int quantity) {
        Product product = inventory.search(productName);
        if (product != null && product.getQuantity() >= quantity) {
            product.setQuantity(product.getQuantity() - quantity);
            // Actualizar en el árbol
            inventory.insert(product);
            return true;
        }
        return false;
    }
    
    /**
     * Aumenta el stock de un producto.
     */
    public boolean increaseStock(String productName, int quantity) {
        Product product = inventory.search(productName);
        if (product != null) {
            product.setQuantity(product.getQuantity() + quantity);
            // Actualizar en el árbol
            inventory.insert(product);
            return true;
        }
        return false;
    }
    
    // Métodos de gestión de clientes
    
    /**
     * Agrega un cliente a la cola de atención.
     */
    public void addClientToQueue(Cliente client) {
        if (client == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        clientQueue.enqueue(client);
    }
    
    /**
     * Atiende al siguiente cliente (el de mayor prioridad).
     * Procesa su compra reduciendo el stock de los productos en su carrito.
     * @return El cliente atendido o null si no hay clientes en cola.
     */
    public Cliente attendNextClient() {
        Cliente client = clientQueue.dequeue();
        if (client != null) {
            processPurchase(client);
        }
        return client;
    }
    
    /**
     * Procesa la compra de un cliente, reduciendo el stock del inventario.
     */
    private void processPurchase(Cliente client) {
        for (Cliente.CartItem item : client.getCart()) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            
            // Verificar y reducir stock
            Product inventoryProduct = inventory.search(product.getName());
            if (inventoryProduct != null) {
                if (inventoryProduct.getQuantity() >= quantity) {
                    reduceStock(product.getName(), quantity);
                } else {
                    // Stock insuficiente - ajustar cantidad al disponible
                    int availableQuantity = inventoryProduct.getQuantity();
                    if (availableQuantity > 0) {
                        reduceStock(product.getName(), availableQuantity);
                        item.setQuantity(availableQuantity);
                    }
                }
            }
        }
    }
    
    /**
     * Ver el siguiente cliente en la cola sin atenderlo.
     */
    public Cliente viewNextClient() {
        return clientQueue.peek();
    }
    
    /**
     * Obtiene todos los clientes en la cola.
     */
    public List<Cliente> getAllClients() {
        return clientQueue.getAll();
    }
    
    /**
     * Elimina un cliente de la cola por su ID.
     */
    public boolean removeClientFromQueue(String clientId) {
        return clientQueue.removeById(clientId);
    }
    
    /**
     * Busca un cliente en la cola por su ID.
     */
    public Cliente findClientInQueue(String clientId) {
        return clientQueue.findById(clientId);
    }
    
    // Métodos auxiliares
    
    /**
     * Retorna el número de productos en el inventario.
     */
    public int getInventoryProductCount() {
        return inventory.size();
    }
    
    /**
     * Retorna el número de clientes en cola.
     */
    public int getQueueClientCount() {
        return clientQueue.size();
    }
    
    /**
     * Verifica si el inventario está vacío.
     */
    public boolean isInventoryEmpty() {
        return inventory.isEmpty();
    }
    
    /**
     * Verifica si la cola de clientes está vacía.
     */
    public boolean isQueueEmpty() {
        return clientQueue.isEmpty();
    }
    
    @Override
    public String toString() {
        return "Store{name='" + name + "', products=" + getInventoryProductCount() +
               ", clientsInQueue=" + getQueueClientCount() + "}";
    }
}

