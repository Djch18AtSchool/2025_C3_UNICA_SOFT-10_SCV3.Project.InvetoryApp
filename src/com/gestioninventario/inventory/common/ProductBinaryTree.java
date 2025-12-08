package com.gestioninventario.inventory.common;

import com.gestioninventario.inventory.domain.Product;
import java.util.ArrayList;
import java.util.List;

/**
 * Árbol binario de búsqueda de productos.
 * La clave para la búsqueda es el nombre del producto.
 */
public class ProductBinaryTree {
    
    /**
     * Nodo del árbol que contiene un Producto.
     */
    private static class TreeNode {
        private Product product;
        private TreeNode left;
        private TreeNode right;
        
        public TreeNode(Product product) {
            this.product = product;
            this.left = null;
            this.right = null;
        }
    }
    
    private TreeNode root;
    private int size;
    
    public ProductBinaryTree() {
        this.root = null;
        this.size = 0;
    }
    
    /**
     * Inserta un producto en el árbol usando el nombre como clave.
     * Si ya existe un producto con el mismo nombre, lo reemplaza.
     */
    public void insert(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        root = insertRecursive(root, product);
    }
    
    private TreeNode insertRecursive(TreeNode node, Product product) {
        if (node == null) {
            size++;
            return new TreeNode(product);
        }
        
        int comparison = product.getName().compareToIgnoreCase(node.product.getName());
        
        if (comparison < 0) {
            node.left = insertRecursive(node.left, product);
        } else if (comparison > 0) {
            node.right = insertRecursive(node.right, product);
        } else {
            // El producto ya existe, lo reemplazamos
            node.product = product;
        }
        
        return node;
    }
    
    /**
     * Busca un producto por nombre (clave).
     * @return El producto si se encuentra, null si no existe.
     */
    public Product search(String name) {
        if (name == null) return null;
        return searchRecursive(root, name);
    }
    
    private Product searchRecursive(TreeNode node, String name) {
        if (node == null) {
            return null;
        }
        
        int comparison = name.compareToIgnoreCase(node.product.getName());
        
        if (comparison < 0) {
            return searchRecursive(node.left, name);
        } else if (comparison > 0) {
            return searchRecursive(node.right, name);
        } else {
            return node.product;
        }
    }
    
    /**
     * Busca productos cuyo nombre contenga el texto especificado.
     * @return Lista de productos que coinciden.
     */
    public List<Product> searchByMatch(String text) {
        List<Product> results = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return results;
        }
        searchByMatchRecursive(root, text.toLowerCase(), results);
        return results;
    }
    
    private void searchByMatchRecursive(TreeNode node, String text, List<Product> results) {
        if (node == null) return;
        
        searchByMatchRecursive(node.left, text, results);
        
        if (node.product.getName().toLowerCase().contains(text)) {
            results.add(node.product);
        }
        
        searchByMatchRecursive(node.right, text, results);
    }
    
    /**
     * Elimina un producto del árbol por nombre.
     * @return true si se eliminó, false si no se encontró.
     */
    public boolean delete(String name) {
        if (name == null) return false;
        int sizeBefore = size;
        root = deleteRecursive(root, name);
        return size < sizeBefore;
    }
    
    private TreeNode deleteRecursive(TreeNode node, String name) {
        if (node == null) {
            return null;
        }
        
        int comparison = name.compareToIgnoreCase(node.product.getName());
        
        if (comparison < 0) {
            node.left = deleteRecursive(node.left, name);
        } else if (comparison > 0) {
            node.right = deleteRecursive(node.right, name);
        } else {
            // Nodo encontrado, proceder a eliminarlo
            size--;
            
            // Caso 1: Nodo sin hijos
            if (node.left == null && node.right == null) {
                return null;
            }
            
            // Caso 2: Nodo con un solo hijo
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            
            // Caso 3: Nodo con dos hijos
            // Encontrar el sucesor inorden (el menor del subárbol derecho)
            TreeNode successor = findMinimum(node.right);
            node.product = successor.product;
            node.right = deleteRecursive(node.right, successor.product.getName());
            size++; // Compensar porque deleteRecursive lo decrementará
        }
        
        return node;
    }
    
    private TreeNode findMinimum(TreeNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }
    
    /**
     * Retorna todos los productos del árbol en orden alfabético (inorden).
     */
    public List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        inorderTraversal(root, list);
        return list;
    }
    
    private void inorderTraversal(TreeNode node, List<Product> list) {
        if (node == null) return;
        
        inorderTraversal(node.left, list);
        list.add(node.product);
        inorderTraversal(node.right, list);
    }
    
    /**
     * Verifica si el árbol está vacío.
     */
    public boolean isEmpty() {
        return root == null;
    }
    
    /**
     * Retorna el número de productos en el árbol.
     */
    public int size() {
        return size;
    }
    
    /**
     * Limpia el árbol eliminando todos los productos.
     */
    public void clear() {
        root = null;
        size = 0;
    }
    
    /**
     * Verifica si existe un producto con el nombre especificado.
     */
    public boolean contains(String name) {
        return search(name) != null;
    }
}

