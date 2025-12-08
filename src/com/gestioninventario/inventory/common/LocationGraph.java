package com.gestioninventario.inventory.common;

import com.gestioninventario.inventory.domain.Location;
import java.util.*;

/**
 * Grafo ponderado no dirigido para representar ubicaciones y sus conexiones.
 * Utiliza el algoritmo de Dijkstra para encontrar caminos más cortos.
 */
public class LocationGraph {
    
    /**
     * Representa una arista con peso (distancia) entre dos ubicaciones.
     */
    private static class Edge {
        private Location destination;
        private double weight;
        
        public Edge(Location destination, double weight) {
            this.destination = destination;
            this.weight = weight;
        }
        
        public Location getDestination() {
            return destination;
        }
        
        public double getWeight() {
            return weight;
        }
    }
    
    /**
     * Nodo para el algoritmo de Dijkstra.
     */
    private static class DijkstraNode implements Comparable<DijkstraNode> {
        private Location location;
        private double distance;
        
        public DijkstraNode(Location location, double distance) {
            this.location = location;
            this.distance = distance;
        }
        
        @Override
        public int compareTo(DijkstraNode other) {
            return Double.compare(this.distance, other.distance);
        }
    }
    
    /**
     * Representa el resultado de una búsqueda de camino.
     */
    public static class PathResult {
        private List<Location> path;
        private double totalDistance;
        
        public PathResult(List<Location> path, double totalDistance) {
            this.path = path;
            this.totalDistance = totalDistance;
        }
        
        public List<Location> getPath() {
            return path;
        }
        
        public double getTotalDistance() {
            return totalDistance;
        }
        
        public boolean isConnected() {
            return path != null && !path.isEmpty();
        }
    }
    
    // Mapa de adyacencia: Location -> Lista de aristas
    private Map<Location, List<Edge>> adjacencyList;
    
    public LocationGraph() {
        this.adjacencyList = new HashMap<>();
    }
    
    /**
     * Agrega un vértice (ubicación) al grafo.
     */
    public void addVertex(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("La ubicación no puede ser nula");
        }
        adjacencyList.putIfAbsent(location, new ArrayList<>());
    }
    
    /**
     * Agrega una arista no dirigida entre dos ubicaciones con el peso especificado.
     * Si alguna ubicación no existe en el grafo, la agrega automáticamente.
     */
    public void addEdge(Location from, Location to, double weight) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Las ubicaciones no pueden ser nulas");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("El peso no puede ser negativo");
        }
        
        // Asegurar que ambos vértices existen
        addVertex(from);
        addVertex(to);
        
        // Agregar arista en ambas direcciones (grafo no dirigido)
        adjacencyList.get(from).add(new Edge(to, weight));
        adjacencyList.get(to).add(new Edge(from, weight));
    }
    
    /**
     * Verifica si una ubicación existe en el grafo.
     */
    public boolean containsVertex(Location location) {
        return adjacencyList.containsKey(location);
    }
    
    /**
     * Verifica si existe una arista entre dos ubicaciones.
     */
    public boolean containsEdge(Location from, Location to) {
        if (!containsVertex(from) || !containsVertex(to)) {
            return false;
        }
        
        List<Edge> edges = adjacencyList.get(from);
        for (Edge edge : edges) {
            if (edge.getDestination().equals(to)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtiene el peso de una arista entre dos ubicaciones.
     * @return El peso o -1 si no existe la arista.
     */
    public double getEdgeWeight(Location from, Location to) {
        if (!containsVertex(from) || !containsVertex(to)) {
            return -1;
        }
        
        List<Edge> edges = adjacencyList.get(from);
        for (Edge edge : edges) {
            if (edge.getDestination().equals(to)) {
                return edge.getWeight();
            }
        }
        return -1;
    }
    
    /**
     * Encuentra el camino más corto entre dos ubicaciones usando el algoritmo de Dijkstra.
     * @return PathResult con el camino y la distancia total, o camino vacío si no están conectadas.
     */
    public PathResult findShortestPath(Location start, Location end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Las ubicaciones no pueden ser nulas");
        }
        
        if (!containsVertex(start) || !containsVertex(end)) {
            return new PathResult(new ArrayList<>(), Double.POSITIVE_INFINITY);
        }
        
        if (start.equals(end)) {
            List<Location> path = new ArrayList<>();
            path.add(start);
            return new PathResult(path, 0.0);
        }
        
        // Inicializar estructuras para Dijkstra
        Map<Location, Double> distances = new HashMap<>();
        Map<Location, Location> previous = new HashMap<>();
        PriorityQueue<DijkstraNode> pq = new PriorityQueue<>();
        Set<Location> visited = new HashSet<>();
        
        // Inicializar distancias
        for (Location location : adjacencyList.keySet()) {
            distances.put(location, Double.POSITIVE_INFINITY);
        }
        distances.put(start, 0.0);
        
        pq.offer(new DijkstraNode(start, 0.0));
        
        // Algoritmo de Dijkstra
        while (!pq.isEmpty()) {
            DijkstraNode current = pq.poll();
            Location currentLocation = current.location;
            
            if (visited.contains(currentLocation)) {
                continue;
            }
            
            visited.add(currentLocation);
            
            // Si llegamos al destino, podemos terminar
            if (currentLocation.equals(end)) {
                break;
            }
            
            // Explorar vecinos
            List<Edge> edges = adjacencyList.get(currentLocation);
            for (Edge edge : edges) {
                Location neighbor = edge.getDestination();
                
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                double newDistance = distances.get(currentLocation) + edge.getWeight();
                
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, currentLocation);
                    pq.offer(new DijkstraNode(neighbor, newDistance));
                }
            }
        }
        
        // Reconstruir el camino
        List<Location> path = new ArrayList<>();
        double totalDistance = distances.get(end);
        
        if (totalDistance == Double.POSITIVE_INFINITY) {
            // No hay camino
            return new PathResult(new ArrayList<>(), Double.POSITIVE_INFINITY);
        }
        
        Location current = end;
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }
        
        return new PathResult(path, totalDistance);
    }
    
    /**
     * Verifica si una ubicación está conectada al resto del grafo.
     * Una ubicación está conectada si existe al menos un camino desde ella hacia cualquier otra ubicación.
     */
    public boolean isConnected(Location location) {
        if (!containsVertex(location)) {
            return false;
        }
        
        if (adjacencyList.size() <= 1) {
            return true;
        }
        
        // Realizar BFS para verificar conectividad
        Set<Location> visited = new HashSet<>();
        Queue<Location> queue = new LinkedList<>();
        
        queue.offer(location);
        visited.add(location);
        
        while (!queue.isEmpty()) {
            Location current = queue.poll();
            List<Edge> edges = adjacencyList.get(current);
            
            for (Edge edge : edges) {
                Location neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
        
        return visited.size() == adjacencyList.size();
    }
    
    /**
     * Obtiene todas las ubicaciones del grafo.
     */
    public Set<Location> getAllLocations() {
        return new HashSet<>(adjacencyList.keySet());
    }
    
    /**
     * Obtiene los vecinos de una ubicación con sus pesos.
     */
    public Map<Location, Double> getNeighbors(Location location) {
        Map<Location, Double> neighbors = new HashMap<>();
        
        if (containsVertex(location)) {
            List<Edge> edges = adjacencyList.get(location);
            for (Edge edge : edges) {
                neighbors.put(edge.getDestination(), edge.getWeight());
            }
        }
        
        return neighbors;
    }
    
    /**
     * Retorna el número de vértices en el grafo.
     */
    public int getVertexCount() {
        return adjacencyList.size();
    }
    
    /**
     * Retorna el número total de aristas en el grafo.
     */
    public int getEdgeCount() {
        int count = 0;
        for (List<Edge> edges : adjacencyList.values()) {
            count += edges.size();
        }
        return count / 2; // Dividir por 2 porque el grafo no es dirigido
    }
    
    /**
     * Limpia el grafo eliminando todos los vértices y aristas.
     */
    public void clear() {
        adjacencyList.clear();
    }
    
    /**
     * Elimina un vértice y todas sus aristas asociadas.
     */
    public boolean removeVertex(Location location) {
        if (!containsVertex(location)) {
            return false;
        }
        
        // Eliminar todas las aristas que apuntan a esta ubicación
        for (List<Edge> edges : adjacencyList.values()) {
            edges.removeIf(edge -> edge.getDestination().equals(location));
        }
        
        // Eliminar el vértice
        adjacencyList.remove(location);
        return true;
    }
    
    /**
     * Elimina una arista entre dos ubicaciones.
     */
    public boolean removeEdge(Location from, Location to) {
        if (!containsVertex(from) || !containsVertex(to)) {
            return false;
        }
        
        boolean removed1 = adjacencyList.get(from).removeIf(edge -> edge.getDestination().equals(to));
        boolean removed2 = adjacencyList.get(to).removeIf(edge -> edge.getDestination().equals(from));
        
        return removed1 && removed2;
    }
}

