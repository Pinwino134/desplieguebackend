package com.pinwino.microservices.productosservice.repository;

import com.pinwino.microservices.productosservice.model.ProductoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoMaterialRepository extends JpaRepository<ProductoMaterial, Long> {
    List<ProductoMaterial> findByProductoId(Long productoId);
}
