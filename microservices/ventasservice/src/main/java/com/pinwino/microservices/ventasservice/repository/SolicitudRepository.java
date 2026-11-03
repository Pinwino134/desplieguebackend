package com.pinwino.microservices.ventasservice.repository;

import com.pinwino.microservices.ventasservice.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findBySolicitanteIdOrderByFechaCreacionDesc(Long solicitanteId);
    List<Solicitud> findAllByOrderByFechaCreacionDesc();
}
