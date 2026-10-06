package com.pinwino.microservice.logisticaservice.repository;

import com.pinwino.microservice.logisticaservice.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByInventarioId(Long inventarioId);
}
