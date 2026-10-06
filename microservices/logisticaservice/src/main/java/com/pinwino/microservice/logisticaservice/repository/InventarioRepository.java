package com.pinwino.microservice.logisticaservice.repository;

import com.pinwino.microservice.logisticaservice.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    List<Inventario> findByAlmacenId(Long almacenId);
    Optional<Inventario> findByAlmacenIdAndProductoId(Long almacenId, Long productoId);
    List<Inventario> findByProductoId(Long productoId);
}
