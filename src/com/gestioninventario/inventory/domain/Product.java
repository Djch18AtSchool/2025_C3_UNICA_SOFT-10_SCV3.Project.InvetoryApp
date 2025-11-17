package com.gestioninventario.inventory.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Product {
    private String id;
    private String category;
    private String brand;
    private String name;
    private List<String> imagePaths;
    private double price;
    private int quantity;
    private String description;
    private Product next; // Enlace al siguiente producto en la lista enlazada

    public Product(String id, String category, String brand, String name,
                   double price, int quantity, String description) {
        this.id = id;
        this.category = category;
        this.brand = brand;
        this.name = name;
        this.imagePaths = new ArrayList<>();
        this.price = price;
        this.quantity = quantity;
        this.description = description;
    }

    // Getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Product getNext() { return next; }
    public void setNext(Product next) { this.next = next; }

    @Override
    public String toString() {
        return "Product{id='" + id + "', name='" + name + "', brand='" + brand + "', category='" + category +
                "', price=" + price + ", qty=" + quantity + ", images=" + imagePaths + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product prod = (Product) o;
        return Objects.equals(id, prod.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
