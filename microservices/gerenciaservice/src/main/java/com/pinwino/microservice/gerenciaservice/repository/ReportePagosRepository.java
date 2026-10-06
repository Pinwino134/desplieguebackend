package com.pinwino.microservice.gerenciaservice.repository;

import com.pinwino.microservice.gerenciaservice.model.ReportePagos;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReportePagosRepository extends JpaRepository<ReportePagos, Long> {
    Optional<ReportePagos> findByPeriodo(String periodo);
}
