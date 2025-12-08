package com.gestioninventario.inventory.domain;

import java.util.Objects;

/**
 * Representa una ubicación geográfica en el sistema de entrega.
 * Cada ubicación puede ser de una tienda o de un cliente.
 */
public class Location {
    private String id;
    private String name;
    private String address;
    
    public Location(String id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(id, location.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Location{id='" + id + "', name='" + name + "', address='" + address + "'}";
    }
}

