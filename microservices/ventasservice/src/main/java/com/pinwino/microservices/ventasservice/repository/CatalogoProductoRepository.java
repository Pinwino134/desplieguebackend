package com.pinwino.microservices.ventasservice.repository;

import com.pinwino.microservices.ventasservice.model.CatalogoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CatalogoProductoRepository extends JpaRepository<CatalogoProducto, Long> {
    List<CatalogoProducto> findByCategoria(String categoria);
}
