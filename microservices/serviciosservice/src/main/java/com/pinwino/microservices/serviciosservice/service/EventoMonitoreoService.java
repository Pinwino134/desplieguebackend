package com.pinwino.microservices.serviciosservice.service;

import com.pinwino.microservices.serviciosservice.model.EstadoEvento;
import com.pinwino.microservices.serviciosservice.model.EventoMonitoreo;
import com.pinwino.microservices.serviciosservice.model.Solicitud;
import com.pinwino.microservices.serviciosservice.model.TipoEvento;

import java.util.List;
import java.util.Map;

public interface EventoMonitoreoService {

    EventoMonitoreo registrar(EventoMonitoreo evento, String correlationId);

    List<EventoMonitoreo> listar(TipoEvento tipo, EstadoEvento estado);

    EventoMonitoreo actualizarEstado(Long id, EstadoEvento nuevoEstado, String accionRespuesta, Long actorId, String actorEmail, String actorRol, String correlationId);

    Solicitud escalarAIncidente(Long eventoId, Long actorId, String actorEmail, String actorRol, String ipOrigen, String correlationId);

    Map<String, Object> obtenerResumenEventos();
}
