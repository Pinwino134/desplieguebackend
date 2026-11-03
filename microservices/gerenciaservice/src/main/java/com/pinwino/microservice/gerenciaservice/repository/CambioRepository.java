package com.pinwino.microservice.gerenciaservice.repository;

import com.pinwino.microservice.gerenciaservice.model.Cambio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CambioRepository extends JpaRepository<Cambio, Long> {
    List<Cambio> findAllByOrderByFechaCreacionDesc();
}
