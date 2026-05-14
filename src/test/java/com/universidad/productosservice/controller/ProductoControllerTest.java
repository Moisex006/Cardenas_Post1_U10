package com.universidad.productosservice.controller;

import com.universidad.productosservice.domain.Producto;
import com.universidad.productosservice.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Test
    void listarProductos_retorna200ConLista() throws Exception {
        List<Producto> lista = List.of(
                producto(1L, "Laptop", 1500.0, 10),
                producto(2L, "Mouse", 50.0, 100));
        when(productoService.listar()).thenReturn(lista);

        mockMvc.perform(get("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Laptop"));
    }

    @Test
    void crearProducto_datosValidos_retorna201() throws Exception {
        Producto creado = producto(1L, "Tablet", 800.0, 5);
        when(productoService.procesarProducto(anyString(), anyDouble(), anyInt(), isNull(), anyBoolean(), isNull()))
                .thenReturn(creado);

        String json = """
                {"nombre":"Tablet","precio":800.0,"stock":5}""";

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Tablet"));
    }

    @Test
    void buscarProducto_noExistente_retorna404() throws Exception {
        when(productoService.buscar(99L)).thenReturn(null);

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    private Producto producto(Long id, String nombre, Double precio, Integer stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setStock(stock);
        return producto;
    }
}
