package com.pinwino.microservice.gerenciaservice.repository;

import com.pinwino.microservice.gerenciaservice.model.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KPIRepository extends JpaRepository<KPI, Long> {
    List<KPI> findByPeriodo(String periodo);
}
