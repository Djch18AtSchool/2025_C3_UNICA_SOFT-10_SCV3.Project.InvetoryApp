package com.gestioninventario.inventory.service;

import com.gestioninventario.inventory.domain.Product;
import com.gestioninventario.inventory.repository.ProductRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lógica de aplicación / reglas (servicio).
 * También incluye util básico para guardar imágenes en disco.
 */
public class ProductService {
    private final ProductRepository repository;
    private final Path imagesFolder;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
        this.imagesFolder = Paths.get("images");
        ensureImagesFolder();
    }

    private void ensureImagesFolder() {
        try {
            if (!Files.exists(imagesFolder)) Files.createDirectories(imagesFolder);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear carpeta de imágenes", e);
        }
    }

    public void saveProduct(Product product) {
        repository.save(product);
    }

    public List<Product> getAll() {
        return repository.findAll();
    }

    public Product getById(String id) {
        return repository.findById(id);
    }

    public boolean deleteById(String id) {
        Product p = repository.findById(id);
        if (p != null) {
            // opcional: borrar imágenes físicas
            for (String path : p.getImagePaths()) {
                try {
                    Files.deleteIfExists(Paths.get(path));
                } catch (IOException ignored) {}
            }
            return repository.deleteById(id);
        }
        return false;
    }

    /**
     * Copia archivos a la carpeta images con nombre único y devuelve las rutas resultantes.
     */
    public List<String> storeImageFiles(List<Path> sourcePaths) {
        List<String> stored = new ArrayList<>();
        for (Path src : sourcePaths) {
            if (Files.exists(src)) {
                String extension = "";
                String fileName = src.getFileName().toString();
                int i = fileName.lastIndexOf('.');
                if (i > 0) extension = fileName.substring(i);
                String uniqueName = UUID.randomUUID().toString() + extension;
                Path dest = imagesFolder.resolve(uniqueName);
                try {
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    stored.add(dest.toString());
                } catch (IOException e) {
                    System.err.println("Error guardando imagen: " + e.getMessage());
                }
            }
        }
        return stored;
    }
}
