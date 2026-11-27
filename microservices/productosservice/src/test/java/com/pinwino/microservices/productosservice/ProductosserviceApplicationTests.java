package com.pinwino.microservices.productosservice;

import com.pinwino.microservices.productosservice.exception.BusinessException;
import com.pinwino.microservices.productosservice.model.Categoria;
import com.pinwino.microservices.productosservice.model.EstadoCategoria;
import com.pinwino.microservices.productosservice.repository.CategoriaRepository;
import com.pinwino.microservices.productosservice.service.impl.CategoriaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductosserviceApplicationTests {

    @Mock
    private CategoriaRepository categoriaRepository;

    private CategoriaServiceImpl categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaServiceImpl(categoriaRepository);
    }

    @Test
    void crearCategoriaActivaCorrectamente() {
        Categoria nueva = new Categoria();
        nueva.setNombre("Textilería Andina");
        nueva.setDescripcion("Tejidos hechos a mano en alpaca");

        when(categoriaRepository.existsByNombre("Textilería Andina")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria guardada = categoriaService.crear(nueva);

        assertEquals(EstadoCategoria.ACTIVA, guardada.getEstado());
        assertEquals("Textilería Andina", guardada.getNombre());
    }

    @Test
    void crearCategoriaDuplicadaLanzaBusinessException() {
        Categoria duplicada = new Categoria();
        duplicada.setNombre("Cerámica");

        when(categoriaRepository.existsByNombre("Cerámica")).thenReturn(true);

        assertThrows(BusinessException.class, () -> categoriaService.crear(duplicada));
    }

    @Test
    void desactivarCategoriaCambiaEstadoAInactiva() {
        Categoria cat = new Categoria();
        cat.setId(1L);
        cat.setNombre("Joyería");
        cat.setEstado(EstadoCategoria.ACTIVA);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria desactivada = categoriaService.desactivar(1L);

        assertEquals(EstadoCategoria.INACTIVA, desactivada.getEstado());
    }
}
