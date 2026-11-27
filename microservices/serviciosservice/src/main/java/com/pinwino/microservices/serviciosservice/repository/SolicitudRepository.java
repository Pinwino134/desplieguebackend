package com.pinwino.microservices.serviciosservice.repository;

import com.pinwino.microservices.serviciosservice.model.EstadoSolicitud;
import com.pinwino.microservices.serviciosservice.model.Solicitud;
import com.pinwino.microservices.serviciosservice.model.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findAllByOrderByFechaCreacionDesc();

    List<Solicitud> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Solicitud> findByTipoOrderByFechaCreacionDesc(TipoSolicitud tipo);

    List<Solicitud> findByEstadoOrderByFechaCreacionDesc(EstadoSolicitud estado);

    List<Solicitud> findByTipoAndEstadoOrderByFechaCreacionDesc(TipoSolicitud tipo, EstadoSolicitud estado);
}
