package com.pinwino.microservices.ventasservice.repository;

import com.pinwino.microservices.ventasservice.model.SolicitudHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudHistorialRepository extends JpaRepository<SolicitudHistorial, Long> {
    List<SolicitudHistorial> findBySolicitudIdOrderByFechaAscIdAsc(Long solicitudId);
}
