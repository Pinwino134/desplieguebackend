package com.pinwino.microservice.logisticaservice.repository;

import com.pinwino.microservice.logisticaservice.model.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportistaRepository extends JpaRepository<Transportista, Long> {
}
