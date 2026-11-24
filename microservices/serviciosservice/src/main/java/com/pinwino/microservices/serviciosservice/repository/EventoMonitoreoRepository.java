package com.pinwino.microservices.serviciosservice.repository;

import com.pinwino.microservices.serviciosservice.model.EstadoEvento;
import com.pinwino.microservices.serviciosservice.model.EventoMonitoreo;
import com.pinwino.microservices.serviciosservice.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoMonitoreoRepository extends JpaRepository<EventoMonitoreo, Long> {

    List<EventoMonitoreo> findAllByOrderByFechaDeteccionDesc();

    List<EventoMonitoreo> findByTipoOrderByFechaDeteccionDesc(TipoEvento tipo);

    List<EventoMonitoreo> findByEstadoOrderByFechaDeteccionDesc(EstadoEvento estado);

    List<EventoMonitoreo> findByTipoAndEstadoOrderByFechaDeteccionDesc(TipoEvento tipo, EstadoEvento estado);
}
