package com.gestioninventario.inventory.common;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Lista enlazada simple genérica.
 * Provee operaciones básicas: addFirst, addLast, removeIf, findFirst.
 */
public class SinglyLinkedList<T> implements Iterable<T> {
    private Node<T> head;
    private int size = 0;

    public SinglyLinkedList() {
        this.head = null;
    }

    public int size() { return size; }
    public boolean isEmpty() { return head == null; }

    public void addFirst(T value) {
        Node<T> node = new Node<>(value);
        node.setNext(head);
        head = node;
        size++;
    }

    public void addLast(T value) {
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = node;
        } else {
            Node<T> cur = head;
            while (cur.getNext() != null) cur = cur.getNext();
            cur.setNext(node);
        }
        size++;
    }

    /**
     * Remove first element matching predicate.
     * @return true si se removió
     */
    public boolean removeIf(Predicate<T> predicate) {
        Node<T> cur = head;
        Node<T> prev = null;
        while (cur != null) {
            if (predicate.test(cur.getValue())) {
                if (prev == null) { // remove head
                    head = cur.getNext();
                } else {
                    prev.setNext(cur.getNext());
                }
                size--;
                return true;
            }
            prev = cur;
            cur = cur.getNext();
        }
        return false;
    }

    /**
     * Busca el primer elemento que cumple predicate.
     */
    public T findFirst(Predicate<T> predicate) {
        Node<T> cur = head;
        while (cur != null) {
            if (predicate.test(cur.getValue())) return cur.getValue();
            cur = cur.getNext();
        }
        return null;
    }

    /**
     * Reemplaza el primer elemento que cumple predicate con newValue.
     * @return true si se reemplazó.
     */
    public boolean replaceFirstIf(Predicate<T> predicate, T newValue) {
        Node<T> cur = head;
        while (cur != null) {
            if (predicate.test(cur.getValue())) {
                cur.setValue(newValue);
                return true;
            }
            cur = cur.getNext();
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() { return current != null; }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T value = current.getValue();
                current = current.getNext();
                return value;
            }
        };
    }
}
