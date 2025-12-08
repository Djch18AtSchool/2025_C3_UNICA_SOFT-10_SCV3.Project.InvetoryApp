package com.gestioninventario.inventory.common;

import com.gestioninventario.inventory.domain.Cliente;
import java.util.ArrayList;
import java.util.List;

/**
 * Cola de prioridad para clientes.
 * Los clientes con mayor prioridad (3 > 2 > 1) son atendidos primero.
 * En caso de empate de prioridad, se atiende al que llegó primero (FIFO).
 */
public class ClientPriorityQueue {
    
    /**
     * Nodo que envuelve un Cliente con información de su orden de llegada.
     */
    private static class PriorityNode {
        private Cliente client;
        private int arrivalOrder; // Para manejar empates
        
        public PriorityNode(Cliente client, int arrivalOrder) {
            this.client = client;
            this.arrivalOrder = arrivalOrder;
        }
    }
    
    private Node<PriorityNode> front;
    private Node<PriorityNode> rear;
    private int size;
    private int orderCounter; // Para asignar orden de llegada
    
    public ClientPriorityQueue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
        this.orderCounter = 0;
    }
    
    /**
     * Encola un cliente según su prioridad.
     * Los clientes con mayor prioridad van más adelante en la cola.
     * Si hay empate de prioridad, se respeta el orden de llegada (FIFO).
     */
    public void enqueue(Cliente client) {
        if (client == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        
        PriorityNode newNode = new PriorityNode(client, orderCounter++);
        Node<PriorityNode> node = new Node<>(newNode);
        
        // Si la cola está vacía
        if (isEmpty()) {
            front = node;
            rear = node;
            size++;
            return;
        }
        
        // Buscar la posición correcta según prioridad y orden de llegada
        Node<PriorityNode> current = front;
        Node<PriorityNode> previous = null;
        
        while (current != null) {
            PriorityNode currentNode = current.getValue();
            
            // Si la prioridad del nuevo cliente es mayor, debe ir antes
            if (client.getPriority() > currentNode.client.getPriority()) {
                break;
            }
            
            // Si tienen la misma prioridad, verificar orden de llegada
            // (el nuevo cliente va después del actual si tienen la misma prioridad)
            if (client.getPriority() == currentNode.client.getPriority()) {
                // Continuar hasta encontrar un cliente con menor prioridad
                previous = current;
                current = current.getNext();
                continue;
            }
            
            // Si la prioridad del nuevo cliente es menor, continuar buscando
            previous = current;
            current = current.getNext();
        }
        
        // Insertar el nodo en la posición encontrada
        if (previous == null) {
            // Insertar al frente
            node.setNext(front);
            front = node;
        } else {
            // Insertar en medio o al final
            node.setNext(current);
            previous.setNext(node);
            
            // Si se insertó al final, actualizar rear
            if (current == null) {
                rear = node;
            }
        }
        
        size++;
    }
    
    /**
     * Desencola y retorna el cliente con mayor prioridad.
     * En caso de empate, retorna el que llegó primero.
     * @return El cliente desencolado o null si la cola está vacía.
     */
    public Cliente dequeue() {
        if (isEmpty()) {
            return null;
        }
        
        Cliente client = front.getValue().client;
        front = front.getNext();
        
        if (front == null) {
            rear = null;
        }
        
        size--;
        return client;
    }
    
    /**
     * Retorna el cliente que está al frente sin desencolarlo.
     * @return El cliente al frente o null si la cola está vacía.
     */
    public Cliente peek() {
        if (isEmpty()) {
            return null;
        }
        return front.getValue().client;
    }
    
    /**
     * Verifica si la cola está vacía.
     */
    public boolean isEmpty() {
        return front == null;
    }
    
    /**
     * Retorna el número de clientes en la cola.
     */
    public int size() {
        return size;
    }
    
    /**
     * Retorna todos los clientes en la cola (en orden de atención) sin modificar la cola.
     */
    public List<Cliente> getAll() {
        List<Cliente> list = new ArrayList<>();
        Node<PriorityNode> current = front;
        
        while (current != null) {
            list.add(current.getValue().client);
            current = current.getNext();
        }
        
        return list;
    }
    
    /**
     * Limpia la cola eliminando todos los clientes.
     */
    public void clear() {
        front = null;
        rear = null;
        size = 0;
        orderCounter = 0;
    }
    
    /**
     * Busca y elimina un cliente por ID.
     * @return true si se eliminó, false si no se encontró.
     */
    public boolean removeById(String clientId) {
        if (isEmpty() || clientId == null) {
            return false;
        }
        
        Node<PriorityNode> current = front;
        Node<PriorityNode> previous = null;
        
        while (current != null) {
            if (current.getValue().client.getId().equals(clientId)) {
                // Cliente encontrado, eliminar
                if (previous == null) {
                    // Es el frente
                    front = current.getNext();
                    if (front == null) {
                        rear = null;
                    }
                } else {
                    previous.setNext(current.getNext());
                    if (current.getNext() == null) {
                        rear = previous;
                    }
                }
                size--;
                return true;
            }
            
            previous = current;
            current = current.getNext();
        }
        
        return false;
    }
    
    /**
     * Busca un cliente por ID sin modificar la cola.
     * @return El cliente si se encuentra, null en caso contrario.
     */
    public Cliente findById(String clientId) {
        if (isEmpty() || clientId == null) {
            return null;
        }
        
        Node<PriorityNode> current = front;
        
        while (current != null) {
            if (current.getValue().client.getId().equals(clientId)) {
                return current.getValue().client;
            }
            current = current.getNext();
        }
        
        return null;
    }
}

