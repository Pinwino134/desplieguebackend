package com.pinwino.microservice.gerenciaservice.repository;

import com.pinwino.microservice.gerenciaservice.model.DecisionEstrategica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DecisionEstrategicaRepository extends JpaRepository<DecisionEstrategica, Long> {
    List<DecisionEstrategica> findByGerenteId(Long gerenteId);
}
