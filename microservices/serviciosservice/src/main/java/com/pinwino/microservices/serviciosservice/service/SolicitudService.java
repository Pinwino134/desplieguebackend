package com.pinwino.microservices.serviciosservice.service;

import com.pinwino.microservices.serviciosservice.model.EstadoSolicitud;
import com.pinwino.microservices.serviciosservice.model.Solicitud;
import com.pinwino.microservices.serviciosservice.model.TipoSolicitud;

import java.util.List;
import java.util.Map;

public interface SolicitudService {

    Solicitud crear(Solicitud solicitud, String ipOrigen, String correlationId);

    List<Solicitud> listar(TipoSolicitud tipo, EstadoSolicitud estado);

    List<Solicitud> listarPorUsuario(Long usuarioId);

    Solicitud buscarPorId(Long id);

    Solicitud asignar(Long id, Long asignadoA, String asignadoNombre, Long actorId, String actorEmail, String actorRol, String correlationId);

    Solicitud cambiarEstado(Long id, EstadoSolicitud nuevoEstado, String respuesta, Long actorId, String actorEmail, String actorRol, String correlationId);

    Map<String, Object> obtenerResumenSla();

    void eliminar(Long id, Long actorId, String actorEmail, String actorRol, String correlationId);
}
