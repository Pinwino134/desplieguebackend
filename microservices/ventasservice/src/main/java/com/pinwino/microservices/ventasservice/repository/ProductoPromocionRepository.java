package com.pinwino.microservices.ventasservice.repository;

import com.pinwino.microservices.ventasservice.model.ProductoPromocion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoPromocionRepository extends JpaRepository<ProductoPromocion, Long> {
    List<ProductoPromocion> findByPromocionId(Long promocionId);
    List<ProductoPromocion> findByProductoId(Long productoId);
}
