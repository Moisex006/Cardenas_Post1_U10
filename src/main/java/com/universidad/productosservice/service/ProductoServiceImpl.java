package com.universidad.productosservice.service;

import com.universidad.productosservice.domain.Producto;
import com.universidad.productosservice.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository repo; // Code Smell: nombre generico e inyeccion por campo

    @Override
    public Producto procesarProducto(String n, Double p, Integer s,
                                     String cat, boolean activo, String proveedor) {
        Producto producto = new Producto();
        if (n == null || n.equals("")) { // Code Smell: usar isBlank()
            throw new IllegalArgumentException("nombre requerido");
        }
        if (p == null) {
            throw new IllegalArgumentException("precio requerido");
        } else if (p <= 0) {
            throw new IllegalArgumentException("precio invalido");
        } else if (p > 999999) {
            throw new IllegalArgumentException("precio excesivo");
        }
        if (s == null || s < 0) {
            throw new IllegalArgumentException("stock invalido");
        }
        if (activo) {
            if (cat != null && cat.equalsIgnoreCase("premium")) {
                producto.setPrecio(p * 0.95);
            } else if (cat != null && cat.equalsIgnoreCase("liquidacion")) {
                producto.setPrecio(p * 0.80);
            } else {
                producto.setPrecio(p);
            }
        } else {
            producto.setPrecio(p);
        }
        producto.setNombre(n);
        producto.setStock(s);
        // TODO: implementar logica de categoria y proveedor
        return repo.save(producto);
    }

    @Override
    public List<Producto> listar() {
        return repo.findAll();
    }

    @Override
    public Producto buscar(Long id) {
        Optional<Producto> producto = repo.findById(id);
        return producto.get(); // Bug: accede al Optional sin verificar si existe
    }
}
