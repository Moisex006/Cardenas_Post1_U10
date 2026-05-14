package com.universidad.productosservice.service;

import com.universidad.productosservice.domain.Producto;
import com.universidad.productosservice.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @Captor
    private ArgumentCaptor<Producto> productoCaptor;

    @Test
    void procesarProducto_datosValidos_retornaProductoGuardado() {
        Producto guardado = producto(1L, "Laptop", 1500.0, 10);
        when(productoRepository.save(any(Producto.class))).thenReturn(guardado);

        Producto resultado = productoService.procesarProducto("Laptop", 1500.0, 10, "general", true, "ACME");

        assertNotNull(resultado.getId());
        assertEquals("Laptop", resultado.getNombre());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void listar_productosExistentes_retornaListaCompleta() {
        List<Producto> productos = List.of(
                producto(1L, "Laptop", 1500.0, 10),
                producto(2L, "Mouse", 50.0, 100));
        when(productoRepository.findAll()).thenReturn(productos);

        List<Producto> resultado = productoService.listar();

        assertEquals(2, resultado.size());
        assertEquals("Laptop", resultado.getFirst().getNombre());
        verify(productoRepository).findAll();
    }

    @Test
    void buscar_existente_retornaProducto() {
        Producto producto = producto(1L, "Mouse", 50.0, 100);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoService.buscar(1L);

        assertEquals("Mouse", resultado.getNombre());
        assertEquals(50.0, resultado.getPrecio());
    }

    @Test
    void buscar_noExistente_lanzaNoSuchElementException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> productoService.buscar(99L));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t", "\n"})
    void procesarProducto_nombreEnBlanco_seGuardaPorDefectoIntencional(String nombre) {
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Producto resultado = productoService.procesarProducto(nombre, 100.0, 5, "general", true, "ACME");

        assertEquals(nombre, resultado.getNombre());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "SIN_NOMBRE_NULO"})
    void procesarProducto_nombreNuloOVacio_lanzaIllegalArgumentException(String valor) {
        String nombre = "SIN_NOMBRE_NULO".equals(valor) ? null : valor;

        assertThrows(IllegalArgumentException.class,
                () -> productoService.procesarProducto(nombre, 100.0, 5, "general", true, "ACME"));
        verifyNoInteractions(productoRepository);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -100.0, -0.01})
    void procesarProducto_precioInvalido_lanzaIllegalArgumentException(double precio) {
        assertThrows(IllegalArgumentException.class,
                () -> productoService.procesarProducto("Producto", precio, 5, "general", true, "ACME"));
        verifyNoInteractions(productoRepository);
    }

    @Test
    void procesarProducto_categoriaPremium_aplicaDescuento() {
        when(productoRepository.save(any())).thenAnswer(inv -> {
            Producto producto = inv.getArgument(0);
            producto.setId(1L);
            return producto;
        });

        productoService.procesarProducto("Laptop Pro", 1000.0, 5, "premium", true, "ACME");

        verify(productoRepository).save(productoCaptor.capture());
        Producto capturado = productoCaptor.getValue();

        assertEquals("Laptop Pro", capturado.getNombre());
        assertEquals(950.0, capturado.getPrecio());
    }

    @Test
    void getEstado_stockBajo_retornaBajo() {
        Producto producto = producto(1L, "Teclado", 80.0, 3);

        assertEquals("BAJO", producto.getEstado());
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
