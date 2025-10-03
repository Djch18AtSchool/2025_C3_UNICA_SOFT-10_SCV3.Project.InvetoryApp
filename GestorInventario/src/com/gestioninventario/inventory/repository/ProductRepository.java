package com.gestioninventario.inventory.repository;

import com.gestioninventario.inventory.domain.Product;
import com.gestioninventario.inventory.common.SinglyLinkedList;

import java.util.List;

public class ProductRepository {
    private final SinglyLinkedList<Product> products = new SinglyLinkedList<>();

    public void save(Product product) {
        // si ya existe (por id) lo reemplaza; si no, lo agrega al final
        Product existing = products.findFirst(p -> p.getId().equals(product.getId()));
        if (existing != null) {
            products.replaceFirstIf(p -> p.getId().equals(product.getId()), product);
        } else {
            products.addLast(product);
        }
    }

    public List<Product> findAll() {
        return products.toList();
    }

    public Product findById(String id) {
        return products.findFirst(p -> p.getId().equals(id));
    }

    public boolean deleteById(String id) {
        return products.removeIf(p -> p.getId().equals(id));
    }
}
