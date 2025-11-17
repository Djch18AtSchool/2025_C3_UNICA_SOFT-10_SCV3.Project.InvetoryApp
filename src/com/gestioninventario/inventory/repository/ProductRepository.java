package com.gestioninventario.inventory.repository;

import com.gestioninventario.inventory.domain.Product;
import com.gestioninventario.inventory.common.ProductLinkedList;

public class ProductRepository {
    private final ProductLinkedList products = new ProductLinkedList();

    public void save(Product product) {
        Product existing = products.findFirst(p -> p.getId().equals(product.getId()));
        if (existing != null) {
            products.replaceFirstIf(p -> p.getId().equals(product.getId()), product);
        } else {
            products.addLast(product);
        }
    }

    public ProductLinkedList findAll() {
        return products;
    }

    public Product findById(String id) {
        return products.findFirst(p -> p.getId().equals(id));
    }

    public boolean deleteById(String id) {
        return products.removeIf(p -> p.getId().equals(id));
    }
}
