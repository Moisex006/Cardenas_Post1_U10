package com.universidad.productosservice.service;

import com.universidad.productosservice.domain.Producto;

import java.util.List;

public interface ProductoService {
    Producto procesarProducto(String n, Double p, Integer s, String cat, boolean activo, String proveedor);
    List<Producto> listar();
    Producto buscar(Long id);
}
