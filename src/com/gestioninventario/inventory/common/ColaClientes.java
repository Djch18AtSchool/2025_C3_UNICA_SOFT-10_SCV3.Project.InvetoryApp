package com.gestioninventario.inventory.common;

import com.gestioninventario.inventory.domain.Cliente;
import java.util.ArrayList;
import java.util.List;

/**
 * Cola de prioridad para clientes.
 * Los clientes con mayor prioridad (3 > 2 > 1) son atendidos primero.
 * En caso de empate de prioridad, se atiende al que llegó primero (FIFO).
 */
public class ColaClientes {
    
    /**
     * Nodo que envuelve un Cliente con información de su orden de llegada.
     */
    private static class PriorityNode {
        private Cliente cliente;
        private int ordenLlegada; // Para manejar empates
        
        public PriorityNode(Cliente cliente, int ordenLlegada) {
            this.cliente = cliente;
            this.ordenLlegada = ordenLlegada;
        }
    }
    
    private Node<PriorityNode> frente;
    private Node<PriorityNode> fin;
    private int size;
    private int contadorOrden; // Para asignar orden de llegada
    
    public ColaClientes() {
        this.frente = null;
        this.fin = null;
        this.size = 0;
        this.contadorOrden = 0;
    }
    
    /**
     * Encola un cliente según su prioridad.
     * Los clientes con mayor prioridad van más adelante en la cola.
     * Si hay empate de prioridad, se respeta el orden de llegada (FIFO).
     */
    public void encolar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        
        PriorityNode nuevoNodo = new PriorityNode(cliente, contadorOrden++);
        Node<PriorityNode> nodo = new Node<>(nuevoNodo);
        
        // Si la cola está vacía
        if (isEmpty()) {
            frente = nodo;
            fin = nodo;
            size++;
            return;
        }
        
        // Buscar la posición correcta según prioridad y orden de llegada
        Node<PriorityNode> actual = frente;
        Node<PriorityNode> anterior = null;
        
        while (actual != null) {
            PriorityNode actualNode = actual.getValue();
            
            // Si la prioridad del nuevo cliente es mayor, debe ir antes
            if (cliente.getPrioridad() > actualNode.cliente.getPrioridad()) {
                break;
            }
            
            // Si tienen la misma prioridad, verificar orden de llegada
            // (el nuevo cliente va después del actual si tienen la misma prioridad)
            if (cliente.getPrioridad() == actualNode.cliente.getPrioridad()) {
                // Continuar hasta encontrar un cliente con menor prioridad
                anterior = actual;
                actual = actual.getNext();
                continue;
            }
            
            // Si la prioridad del nuevo cliente es menor, continuar buscando
            anterior = actual;
            actual = actual.getNext();
        }
        
        // Insertar el nodo en la posición encontrada
        if (anterior == null) {
            // Insertar al frente
            nodo.setNext(frente);
            frente = nodo;
        } else {
            // Insertar en medio o al final
            nodo.setNext(actual);
            anterior.setNext(nodo);
            
            // Si se insertó al final, actualizar fin
            if (actual == null) {
                fin = nodo;
            }
        }
        
        size++;
    }
    
    /**
     * Desencola y retorna el cliente con mayor prioridad.
     * En caso de empate, retorna el que llegó primero.
     * @return El cliente desencolado o null si la cola está vacía.
     */
    public Cliente desencolar() {
        if (isEmpty()) {
            return null;
        }
        
        Cliente cliente = frente.getValue().cliente;
        frente = frente.getNext();
        
        if (frente == null) {
            fin = null;
        }
        
        size--;
        return cliente;
    }
    
    /**
     * Retorna el cliente que está al frente sin desencolarlo.
     * @return El cliente al frente o null si la cola está vacía.
     */
    public Cliente verFrente() {
        if (isEmpty()) {
            return null;
        }
        return frente.getValue().cliente;
    }
    
    /**
     * Verifica si la cola está vacía.
     */
    public boolean isEmpty() {
        return frente == null;
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
    public List<Cliente> obtenerTodos() {
        List<Cliente> lista = new ArrayList<>();
        Node<PriorityNode> actual = frente;
        
        while (actual != null) {
            lista.add(actual.getValue().cliente);
            actual = actual.getNext();
        }
        
        return lista;
    }
    
    /**
     * Limpia la cola eliminando todos los clientes.
     */
    public void clear() {
        frente = null;
        fin = null;
        size = 0;
        contadorOrden = 0;
    }
    
    /**
     * Busca y elimina un cliente por ID.
     * @return true si se eliminó, false si no se encontró.
     */
    public boolean eliminarPorId(String clienteId) {
        if (isEmpty() || clienteId == null) {
            return false;
        }
        
        Node<PriorityNode> actual = frente;
        Node<PriorityNode> anterior = null;
        
        while (actual != null) {
            if (actual.getValue().cliente.getId().equals(clienteId)) {
                // Cliente encontrado, eliminar
                if (anterior == null) {
                    // Es el frente
                    frente = actual.getNext();
                    if (frente == null) {
                        fin = null;
                    }
                } else {
                    anterior.setNext(actual.getNext());
                    if (actual.getNext() == null) {
                        fin = anterior;
                    }
                }
                size--;
                return true;
            }
            
            anterior = actual;
            actual = actual.getNext();
        }
        
        return false;
    }
    
    /**
     * Busca un cliente por ID sin modificar la cola.
     * @return El cliente si se encuentra, null en caso contrario.
     */
    public Cliente buscarPorId(String clienteId) {
        if (isEmpty() || clienteId == null) {
            return null;
        }
        
        Node<PriorityNode> actual = frente;
        
        while (actual != null) {
            if (actual.getValue().cliente.getId().equals(clienteId)) {
                return actual.getValue().cliente;
            }
            actual = actual.getNext();
        }
        
        return null;
    }
}

