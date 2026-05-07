package com.universidad.productosservice.service;

import com.universidad.productosservice.domain.Producto;
import com.universidad.productosservice.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
    void crear_datosValidos_retornaProductoGuardado() {
        Producto guardado = new Producto(1L, "Laptop", 1500.0, 10);
        when(productoRepository.save(any(Producto.class))).thenReturn(guardado);

        Producto resultado = productoService.crear("Laptop", 1500.0, 10);

        assertNotNull(resultado.getId());
        assertEquals("Laptop", resultado.getNombre());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void listarTodos_productosExistentes_retornaListaCompleta() {
        List<Producto> productos = List.of(
                new Producto(1L, "Laptop", 1500.0, 10),
                new Producto(2L, "Mouse", 50.0, 100));
        when(productoRepository.findAll()).thenReturn(productos);

        List<Producto> resultado = productoService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Laptop", resultado.getFirst().getNombre());
        verify(productoRepository).findAll();
    }

    @Test
    void buscarPorId_existente_retornaProducto() {
        Producto producto = new Producto(1L, "Mouse", 50.0, 100);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoService.buscarPorId(1L);

        assertEquals("Mouse", resultado.getNombre());
        assertEquals(50.0, resultado.getPrecio());
    }

    @Test
    void buscarPorId_noExistente_lanzaRuntimeException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productoService.buscarPorId(99L));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void crear_nombreInvalido_lanzaIllegalArgumentException(String nombre) {
        assertThrows(IllegalArgumentException.class, () -> productoService.crear(nombre, 100.0, 5));
        verifyNoInteractions(productoRepository);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -100.0, -0.01})
    void crear_precioInvalido_lanzaIllegalArgumentException(double precio) {
        assertThrows(IllegalArgumentException.class, () -> productoService.crear("Producto", precio, 5));
        verifyNoInteractions(productoRepository);
    }

    @Test
    void crear_nombreConEspacios_guardaNombreNormalizado() {
        when(productoRepository.save(any())).thenAnswer(inv -> {
            Producto producto = inv.getArgument(0);
            producto.setId(1L);
            return producto;
        });

        productoService.crear("  Laptop Pro  ", 1500.0, 5);

        verify(productoRepository).save(productoCaptor.capture());
        Producto capturado = productoCaptor.getValue();

        assertEquals("Laptop Pro", capturado.getNombre());
        assertEquals(1500.0, capturado.getPrecio());
    }

    @Test
    void eliminar_productoExistente_llamaDeleteById() {
        Producto producto = new Producto(1L, "Teclado", 80.0, 20);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(productoRepository).deleteById(1L);

        productoService.eliminar(1L);

        verify(productoRepository, times(1)).deleteById(1L);
        verify(productoRepository, times(1)).findById(1L);
    }
}
