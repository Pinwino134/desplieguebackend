package com.pinwino.microservice.gerenciaservice.repository;

import com.pinwino.microservice.gerenciaservice.model.ReporteVentas;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReporteVentasRepository extends JpaRepository<ReporteVentas, Long> {
    List<ReporteVentas> findAllByOrderByPeriodoDesc();
    Optional<ReporteVentas> findByPeriodo(String periodo);
}
