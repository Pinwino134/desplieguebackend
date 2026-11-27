package com.pinwino.microservice.gerenciaservice.repository;

import com.pinwino.microservice.gerenciaservice.model.CambioHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CambioHistorialRepository extends JpaRepository<CambioHistorial, Long> {
    List<CambioHistorial> findByCambioIdOrderByFechaAscIdAsc(Long cambioId);
}
