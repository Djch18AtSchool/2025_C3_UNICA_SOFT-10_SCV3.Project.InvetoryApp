package com.gestioninventario.inventory.common;

import com.gestioninventario.inventory.domain.Product;
import java.util.ArrayList;
import java.util.List;

/**
 * Árbol binario de búsqueda de productos.
 * La clave para la búsqueda es el nombre del producto.
 */
public class ArbolProductos {
    
    /**
     * Nodo del árbol que contiene un Producto.
     */
    private static class TreeNode {
        private Product producto;
        private TreeNode left;
        private TreeNode right;
        
        public TreeNode(Product producto) {
            this.producto = producto;
            this.left = null;
            this.right = null;
        }
    }
    
    private TreeNode root;
    private int size;
    
    public ArbolProductos() {
        this.root = null;
        this.size = 0;
    }
    
    /**
     * Inserta un producto en el árbol usando el nombre como clave.
     * Si ya existe un producto con el mismo nombre, lo reemplaza.
     */
    public void insertar(Product producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        root = insertarRecursivo(root, producto);
    }
    
    private TreeNode insertarRecursivo(TreeNode nodo, Product producto) {
        if (nodo == null) {
            size++;
            return new TreeNode(producto);
        }
        
        int comparacion = producto.getName().compareToIgnoreCase(nodo.producto.getName());
        
        if (comparacion < 0) {
            nodo.left = insertarRecursivo(nodo.left, producto);
        } else if (comparacion > 0) {
            nodo.right = insertarRecursivo(nodo.right, producto);
        } else {
            // El producto ya existe, lo reemplazamos
            nodo.producto = producto;
        }
        
        return nodo;
    }
    
    /**
     * Busca un producto por nombre (clave).
     * @return El producto si se encuentra, null si no existe.
     */
    public Product buscar(String nombre) {
        if (nombre == null) return null;
        return buscarRecursivo(root, nombre);
    }
    
    private Product buscarRecursivo(TreeNode nodo, String nombre) {
        if (nodo == null) {
            return null;
        }
        
        int comparacion = nombre.compareToIgnoreCase(nodo.producto.getName());
        
        if (comparacion < 0) {
            return buscarRecursivo(nodo.left, nombre);
        } else if (comparacion > 0) {
            return buscarRecursivo(nodo.right, nombre);
        } else {
            return nodo.producto;
        }
    }
    
    /**
     * Busca productos cuyo nombre contenga el texto especificado.
     * @return Lista de productos que coinciden.
     */
    public List<Product> buscarPorCoincidencia(String texto) {
        List<Product> resultados = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return resultados;
        }
        buscarPorCoincidenciaRecursivo(root, texto.toLowerCase(), resultados);
        return resultados;
    }
    
    private void buscarPorCoincidenciaRecursivo(TreeNode nodo, String texto, List<Product> resultados) {
        if (nodo == null) return;
        
        buscarPorCoincidenciaRecursivo(nodo.left, texto, resultados);
        
        if (nodo.producto.getName().toLowerCase().contains(texto)) {
            resultados.add(nodo.producto);
        }
        
        buscarPorCoincidenciaRecursivo(nodo.right, texto, resultados);
    }
    
    /**
     * Elimina un producto del árbol por nombre.
     * @return true si se eliminó, false si no se encontró.
     */
    public boolean eliminar(String nombre) {
        if (nombre == null) return false;
        int sizeAntes = size;
        root = eliminarRecursivo(root, nombre);
        return size < sizeAntes;
    }
    
    private TreeNode eliminarRecursivo(TreeNode nodo, String nombre) {
        if (nodo == null) {
            return null;
        }
        
        int comparacion = nombre.compareToIgnoreCase(nodo.producto.getName());
        
        if (comparacion < 0) {
            nodo.left = eliminarRecursivo(nodo.left, nombre);
        } else if (comparacion > 0) {
            nodo.right = eliminarRecursivo(nodo.right, nombre);
        } else {
            // Nodo encontrado, proceder a eliminarlo
            size--;
            
            // Caso 1: Nodo sin hijos
            if (nodo.left == null && nodo.right == null) {
                return null;
            }
            
            // Caso 2: Nodo con un solo hijo
            if (nodo.left == null) {
                return nodo.right;
            }
            if (nodo.right == null) {
                return nodo.left;
            }
            
            // Caso 3: Nodo con dos hijos
            // Encontrar el sucesor inorden (el menor del subárbol derecho)
            TreeNode sucesor = encontrarMinimo(nodo.right);
            nodo.producto = sucesor.producto;
            nodo.right = eliminarRecursivo(nodo.right, sucesor.producto.getName());
            size++; // Compensar porque eliminarRecursivo lo decrementará
        }
        
        return nodo;
    }
    
    private TreeNode encontrarMinimo(TreeNode nodo) {
        while (nodo.left != null) {
            nodo = nodo.left;
        }
        return nodo;
    }
    
    /**
     * Retorna todos los productos del árbol en orden alfabético (inorden).
     */
    public List<Product> obtenerTodos() {
        List<Product> lista = new ArrayList<>();
        recorridoInorden(root, lista);
        return lista;
    }
    
    private void recorridoInorden(TreeNode nodo, List<Product> lista) {
        if (nodo == null) return;
        
        recorridoInorden(nodo.left, lista);
        lista.add(nodo.producto);
        recorridoInorden(nodo.right, lista);
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
    public boolean contiene(String nombre) {
        return buscar(nombre) != null;
    }
}

