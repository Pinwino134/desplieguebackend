package com.pinwino.microservices.productosservice.repository;

import com.pinwino.microservices.productosservice.model.EstadoProducto;
import com.pinwino.microservices.productosservice.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByArtesanoId(Long artesanoId);
    List<Producto> findByCategoriaId(Long categoriaId);
    List<Producto> findByEstado(EstadoProducto estado);
}
