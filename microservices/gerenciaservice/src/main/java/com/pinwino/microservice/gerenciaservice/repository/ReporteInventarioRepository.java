package com.pinwino.microservice.gerenciaservice.repository;

import com.pinwino.microservice.gerenciaservice.model.ReporteInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReporteInventarioRepository extends JpaRepository<ReporteInventario, Long> {
    List<ReporteInventario> findByPeriodo(String periodo);
    List<ReporteInventario> findByAlmacenId(Long almacenId);
}
