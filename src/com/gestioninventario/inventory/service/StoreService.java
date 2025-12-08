package com.gestioninventario.inventory.service;

import com.gestioninventario.inventory.domain.Cliente;
import com.gestioninventario.inventory.domain.Location;
import com.gestioninventario.inventory.domain.Product;
import com.gestioninventario.inventory.domain.Store;
import com.gestioninventario.inventory.common.LocationGraph;
import java.util.List;

/**
 * Servicio para gestionar la lógica de negocio de la tienda.
 * Incluye gestión de inventario, clientes y optimización de rutas de entrega.
 */
public class StoreService {
    private Store store;
    private LocationGraph deliveryGraph;
    
    public StoreService(String storeName) {
        this.store = new Store(storeName);
        this.deliveryGraph = new LocationGraph();
    }
    
    public StoreService(String storeName, Location storeLocation) {
        this.store = new Store(storeName, storeLocation);
        this.deliveryGraph = new LocationGraph();
        
        // Agregar la ubicación de la tienda al grafo
        if (storeLocation != null) {
            deliveryGraph.addVertex(storeLocation);
        }
    }
    
    public Store getStore() {
        return store;
    }
    
    public LocationGraph getDeliveryGraph() {
        return deliveryGraph;
    }
    
    // ========== Métodos de gestión de inventario ==========
    
    /**
     * Agrega un producto al inventario de la tienda.
     */
    public void addProductToInventory(Product product) {
        store.addProductToInventory(product);
    }
    
    /**
     * Busca un producto en el inventario por nombre.
     */
    public Product searchProductByName(String name) {
        return store.searchProductInInventory(name);
    }
    
    /**
     * Busca productos que contengan el texto especificado en su nombre.
     */
    public List<Product> searchProductsByMatch(String text) {
        return store.searchProductsByMatch(text);
    }
    
    /**
     * Elimina un producto del inventario.
     */
    public boolean removeProduct(String name) {
        return store.removeProductFromInventory(name);
    }
    
    /**
     * Obtiene todos los productos del inventario.
     */
    public List<Product> getAllProducts() {
        return store.getAllProducts();
    }
    
    /**
     * Actualiza el stock de un producto.
     */
    public boolean updateStock(String productName, int newQuantity) {
        Product product = store.searchProductInInventory(productName);
        if (product != null) {
            product.setQuantity(newQuantity);
            store.addProductToInventory(product); // Actualizar en el árbol
            return true;
        }
        return false;
    }
    
    // ========== Métodos de gestión de clientes ==========
    
    /**
     * Agrega un cliente a la cola de atención.
     * Automáticamente agrega su ubicación al grafo de entregas si no existe.
     */
    public void addClientToQueue(Cliente client) {
        // Agregar ubicación del cliente al grafo si tiene una
        if (client.getLocation() != null) {
            deliveryGraph.addVertex(client.getLocation());
        }
        
        store.addClientToQueue(client);
    }
    
    /**
     * Agrega un producto al carrito de un cliente que está en la cola.
     * Verifica que haya stock disponible antes de agregarlo.
     */
    public boolean addProductToClientCart(String clientId, String productName, int quantity) {
        Cliente client = store.findClientInQueue(clientId);
        if (client == null) {
            return false;
        }
        
        Product product = store.searchProductInInventory(productName);
        if (product == null) {
            return false;
        }
        
        // Verificar stock disponible
        if (!store.verifyStock(productName, quantity)) {
            return false;
        }
        
        client.addToCart(product, quantity);
        return true;
    }
    
    /**
     * Elimina un producto del carrito de un cliente.
     */
    public boolean removeProductFromClientCart(String clientId, String productId) {
        Cliente client = store.findClientInQueue(clientId);
        if (client == null) {
            return false;
        }
        
        return client.removeFromCart(productId);
    }
    
    /**
     * Atiende al siguiente cliente de la cola.
     * Verifica que la ubicación del cliente esté conectada antes de procesar.
     * @return El cliente atendido o null si no hay clientes o no está conectado.
     */
    public Cliente attendNextClient() {
        Cliente nextClient = store.viewNextClient();
        
        if (nextClient == null) {
            return null;
        }
        
        // Verificar que el cliente tenga ubicación
        if (nextClient.getLocation() == null) {
            throw new IllegalStateException("El cliente no tiene ubicación asignada");
        }
        
        // Verificar que la ubicación del cliente esté conectada al grafo
        if (!deliveryGraph.isConnected(nextClient.getLocation())) {
            throw new IllegalStateException(
                "No se puede atender al cliente: su ubicación está desconectada del sistema de entregas");
        }
        
        // Verificar que la tienda tenga ubicación
        if (store.getLocation() == null) {
            throw new IllegalStateException("La tienda no tiene ubicación asignada");
        }
        
        return store.attendNextClient();
    }
    
    /**
     * Genera el texto de la factura para un cliente, incluyendo la ruta de entrega óptima.
     */
    public String generateInvoice(Cliente client) {
        if (client == null) {
            return "No hay cliente para facturar.";
        }
        
        StringBuilder invoice = new StringBuilder();
        invoice.append("═══════════════════════════════════════════════════\n");
        invoice.append("                    FACTURA\n");
        invoice.append("           ").append(store.getName()).append("\n");
        invoice.append("═══════════════════════════════════════════════════\n\n");
        
        invoice.append("Cliente: ").append(client.getName()).append(" ").append(client.getLastName()).append("\n");
        invoice.append("ID: ").append(client.getId()).append("\n");
        invoice.append("Email: ").append(client.getEmail()).append("\n");
        invoice.append("Tipo: ").append(client.getPriorityDescription()).append("\n");
        
        // Información de ubicación
        if (client.getLocation() != null) {
            invoice.append("Ubicación: ").append(client.getLocation().getName()).append("\n");
            invoice.append("Dirección: ").append(client.getLocation().getAddress()).append("\n");
        }
        
        invoice.append("───────────────────────────────────────────────────\n\n");
        
        invoice.append("PRODUCTOS:\n\n");
        
        if (client.getCart().isEmpty()) {
            invoice.append("  (Carrito vacío)\n");
        } else {
            for (Cliente.CartItem item : client.getCart()) {
                Product prod = item.getProduct();
                invoice.append(String.format("  %-30s x%2d  $%.2f\n", 
                    prod.getName(), 
                    item.getQuantity(), 
                    item.getSubtotal()));
            }
        }
        
        invoice.append("\n───────────────────────────────────────────────────\n");
        invoice.append(String.format("TOTAL: $%.2f\n", client.calculateTotal()));
        invoice.append("═══════════════════════════════════════════════════\n");
        
        // Agregar información de ruta de entrega
        if (store.getLocation() != null && client.getLocation() != null) {
            invoice.append("\n📦 INFORMACIÓN DE ENTREGA:\n\n");
            
            LocationGraph.PathResult pathResult = deliveryGraph.findShortestPath(
                store.getLocation(), 
                client.getLocation()
            );
            
            if (pathResult.isConnected()) {
                invoice.append("Distancia total: ").append(String.format("%.2f km", pathResult.getTotalDistance())).append("\n");
                invoice.append("\nRuta óptima (Dijkstra):\n");
                
                List<Location> path = pathResult.getPath();
                for (int i = 0; i < path.size(); i++) {
                    invoice.append("  ").append(i + 1).append(". ").append(path.get(i).getName());
                    
                    if (i < path.size() - 1) {
                        double segmentDistance = deliveryGraph.getEdgeWeight(path.get(i), path.get(i + 1));
                        invoice.append(String.format(" → (%.2f km)", segmentDistance));
                    }
                    
                    invoice.append("\n");
                }
            } else {
                invoice.append("⚠️ ADVERTENCIA: No hay ruta disponible\n");
            }
            
            invoice.append("═══════════════════════════════════════════════════\n");
        }
        
        return invoice.toString();
    }
    
    /**
     * Ver el siguiente cliente sin atenderlo.
     */
    public Cliente viewNextClient() {
        return store.viewNextClient();
    }
    
    /**
     * Obtiene todos los clientes en la cola.
     */
    public List<Cliente> getAllClients() {
        return store.getAllClients();
    }
    
    /**
     * Elimina un cliente de la cola.
     */
    public boolean removeClientFromQueue(String clientId) {
        return store.removeClientFromQueue(clientId);
    }
    
    /**
     * Busca un cliente en la cola por ID.
     */
    public Cliente findClientInQueue(String clientId) {
        return store.findClientInQueue(clientId);
    }
    
    // ========== Métodos de gestión del grafo de entregas ==========
    
    /**
     * Agrega una ubicación al grafo de entregas.
     */
    public void addLocationToGraph(Location location) {
        deliveryGraph.addVertex(location);
    }
    
    /**
     * Agrega una conexión (arista) entre dos ubicaciones con la distancia especificada.
     */
    public void addConnection(Location from, Location to, double distance) {
        deliveryGraph.addEdge(from, to, distance);
    }
    
    /**
     * Verifica si una ubicación está en el grafo.
     */
    public boolean locationExistsInGraph(Location location) {
        return deliveryGraph.containsVertex(location);
    }
    
    /**
     * Verifica si una ubicación está conectada al sistema de entregas.
     */
    public boolean isLocationConnected(Location location) {
        return deliveryGraph.isConnected(location);
    }
    
    /**
     * Obtiene todas las ubicaciones del grafo.
     */
    public java.util.Set<Location> getAllLocations() {
        return deliveryGraph.getAllLocations();
    }
    
    /**
     * Calcula el camino más corto entre dos ubicaciones.
     */
    public LocationGraph.PathResult findShortestPath(Location from, Location to) {
        return deliveryGraph.findShortestPath(from, to);
    }
    
    // ========== Métodos de información ==========
    
    /**
     * Obtiene estadísticas de la tienda.
     */
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("═══ ESTADÍSTICAS DE LA TIENDA ═══\n");
        stats.append("Nombre: ").append(store.getName()).append("\n");
        stats.append("Productos en inventario: ").append(store.getInventoryProductCount()).append("\n");
        stats.append("Clientes en cola: ").append(store.getQueueClientCount()).append("\n");
        stats.append("Ubicaciones en red: ").append(deliveryGraph.getVertexCount()).append("\n");
        stats.append("Conexiones (aristas): ").append(deliveryGraph.getEdgeCount()).append("\n");
        
        if (store.getLocation() != null) {
            stats.append("Ubicación tienda: ").append(store.getLocation().getName()).append("\n");
        }
        
        if (!store.isQueueEmpty()) {
            Cliente next = store.viewNextClient();
            stats.append("Próximo cliente: ").append(next.getName())
                 .append(" (").append(next.getPriorityDescription()).append(")\n");
        }
        
        return stats.toString();
    }
    
    /**
     * Calcula el valor total del inventario.
     */
    public double calculateTotalInventoryValue() {
        double total = 0.0;
        for (Product product : store.getAllProducts()) {
            total += product.getPrice() * product.getQuantity();
        }
        return total;
    }
}

