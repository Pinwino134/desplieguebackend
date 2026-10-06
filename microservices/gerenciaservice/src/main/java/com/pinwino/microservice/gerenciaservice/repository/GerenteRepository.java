package com.pinwino.microservice.gerenciaservice.repository;

import com.pinwino.microservice.gerenciaservice.model.Gerente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GerenteRepository extends JpaRepository<Gerente, Long> {
}
