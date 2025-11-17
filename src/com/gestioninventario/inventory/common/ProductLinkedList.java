package com.gestioninventario.inventory.common;

import com.gestioninventario.inventory.domain.Product;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Lista enlazada simple específica para productos.
 * Usa el atributo 'next' de Product directamente, sin necesidad de un nodo wrapper.
 */
public class ProductLinkedList implements Iterable<Product> {
    private Product head;
    private int size = 0;

    public ProductLinkedList() {
        this.head = null;
    }

    public int size() { 
        return size; 
    }
    
    public boolean isEmpty() { 
        return head == null; 
    }

    /**
     * Agrega un producto al inicio de la lista.
     */
    public void addFirst(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        product.setNext(head);
        head = product;
        size++;
    }

    /**
     * Agrega un producto al final de la lista.
     */
    public void addLast(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        
        if (head == null) {
            head = product;
            product.setNext(null);
        } else {
            Product current = head;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(product);
            product.setNext(null);
        }
        size++;
    }

    /**
     * Remueve el primer producto que cumple con el predicado.
     * @return true si se removió un producto, false en caso contrario.
     */
    public boolean removeIf(Predicate<Product> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("El predicado no puede ser nulo");
        }
        
        Product current = head;
        Product previous = null;
        
        while (current != null) {
            if (predicate.test(current)) {
                if (previous == null) {
                    // Remover el head
                    head = current.getNext();
                } else {
                    previous.setNext(current.getNext());
                }
                current.setNext(null); // Limpiar referencia
                size--;
                return true;
            }
            previous = current;
            current = current.getNext();
        }
        
        return false;
    }

    /**
     * Busca el primer producto que cumple con el predicado.
     * @return El producto encontrado o null si no existe.
     */
    public Product findFirst(Predicate<Product> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("El predicado no puede ser nulo");
        }
        
        Product current = head;
        while (current != null) {
            if (predicate.test(current)) {
                return current;
            }
            current = current.getNext();
        }
        return null;
    }

    /**
     * Reemplaza el primer producto que cumple con el predicado.
     * @return true si se reemplazó, false si no se encontró.
     */
    public boolean replaceFirstIf(Predicate<Product> predicate, Product newProduct) {
        if (predicate == null || newProduct == null) {
            throw new IllegalArgumentException("El predicado y el producto no pueden ser nulos");
        }
        
        Product current = head;
        Product previous = null;
        
        while (current != null) {
            if (predicate.test(current)) {
                // Reemplazar el producto
                newProduct.setNext(current.getNext());
                if (previous == null) {
                    head = newProduct;
                } else {
                    previous.setNext(newProduct);
                }
                current.setNext(null); // Limpiar referencia del producto removido
                return true;
            }
            previous = current;
            current = current.getNext();
        }
        
        return false;
    }

    /**
     * Calcula el costo total de todos los productos en la lista.
     * El costo de cada producto es precio * cantidad.
     * @return El costo total.
     */
    public double calcularCostoTotal() {
        double total = 0.0;
        Product current = head;
        
        while (current != null) {
            total += current.getPrice() * current.getQuantity();
            current = current.getNext();
        }
        
        return total;
    }

    /**
     * Limpia la lista.
     */
    public void clear() {
        // Limpiar referencias para ayudar al garbage collector
        Product current = head;
        while (current != null) {
            Product next = current.getNext();
            current.setNext(null);
            current = next;
        }
        head = null;
        size = 0;
    }

    @Override
    public Iterator<Product> iterator() {
        return new Iterator<Product>() {
            private Product current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Product next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Product product = current;
                current = current.getNext();
                return product;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Product current = head;
        while (current != null) {
            sb.append(current.getName());
            if (current.getNext() != null) {
                sb.append(", ");
            }
            current = current.getNext();
        }
        sb.append("]");
        return sb.toString();
    }
}

