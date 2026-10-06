package com.pinwino.microservice.gerenciaservice.repository;

import com.pinwino.microservice.gerenciaservice.model.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {
    List<Presupuesto> findByPeriodo(String periodo);
    List<Presupuesto> findByArea(String area);
}
