package com.pinwino.microservices.serviciosservice.service.impl;

import com.pinwino.microservices.serviciosservice.exception.BusinessException;
import com.pinwino.microservices.serviciosservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.serviciosservice.model.*;
import com.pinwino.microservices.serviciosservice.repository.SolicitudRepository;
import com.pinwino.microservices.serviciosservice.service.AuditoriaService;
import com.pinwino.microservices.serviciosservice.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final AuditoriaService auditoriaService;

    @Override
    @Transactional
    public Solicitud crear(Solicitud solicitud, String ipOrigen, String correlationId) {
        if (solicitud.getTipo() == null) {
            solicitud.setTipo(TipoSolicitud.SERVICIO);
        }
        if (solicitud.getPrioridad() == null) {
            solicitud.setPrioridad(PrioridadSolicitud.MEDIA);
        }
        if (solicitud.getTipo() == TipoSolicitud.ACCESO
                && (solicitud.getRolSolicitado() == null || solicitud.getRolSolicitado().isBlank())) {
            throw new BusinessException("En solicitudes de acceso debes especificar el rol o permiso solicitado");
        }

        LocalDateTime ahora = LocalDateTime.now();
        solicitud.setEstado(EstadoSolicitud.ABIERTA);
        solicitud.setCodigo(generarCodigo(solicitud.getTipo()));
        solicitud.setFechaLimiteSla(ahora.plusHours(solicitud.getPrioridad().getHorasSla()));
        solicitud.setSlaCumplido(null);

        Solicitud guardada = solicitudRepository.save(solicitud);

        auditoriaService.registrar(RegistroAuditoria.builder()
                .usuarioId(guardada.getUsuarioId())
                .usuarioEmail(guardada.getSolicitanteEmail())
                .usuarioRol(guardada.getSolicitanteRol() != null ? guardada.getSolicitanteRol() : "PUBLICO")
                .modulo("GESTION_SERVICIOS")
                .accion("CREAR_SOLICITUD_" + guardada.getTipo().name())
                .recurso("solicitud/" + guardada.getCodigo())
                .detalle("Asunto: " + guardada.getAsunto() + " | Prioridad: " + guardada.getPrioridad())
                .resultado("EXITO")
                .ipOrigen(ipOrigen)
                .correlationId(correlationId)
                .build());

        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> listar(TipoSolicitud tipo, EstadoSolicitud estado) {
        if (tipo != null && estado != null) {
            return solicitudRepository.findByTipoAndEstadoOrderByFechaCreacionDesc(tipo, estado);
        }
        if (tipo != null) {
            return solicitudRepository.findByTipoOrderByFechaCreacionDesc(tipo);
        }
        if (estado != null) {
            return solicitudRepository.findByEstadoOrderByFechaCreacionDesc(estado);
        }
        return solicitudRepository.findAllByOrderByFechaCreacionDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> listarPorUsuario(Long usuarioId) {
        return solicitudRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Solicitud buscarPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", id));
    }

    @Override
    @Transactional
    public Solicitud asignar(
            Long id,
            Long asignadoA,
            String asignadoNombre,
            Long actorId,
            String actorEmail,
            String actorRol,
            String correlationId) {

        Solicitud solicitud = buscarPorId(id);
        solicitud.setAsignadoA(asignadoA != null ? asignadoA : actorId);
        solicitud.setAsignadoNombre(
                (asignadoNombre != null && !asignadoNombre.isBlank()) ? asignadoNombre : actorEmail
        );
        if (solicitud.getEstado() == EstadoSolicitud.ABIERTA) {
            solicitud.setEstado(EstadoSolicitud.EN_PROCESO);
        }

        Solicitud actualizada = solicitudRepository.save(solicitud);

        auditoriaService.registrar(RegistroAuditoria.builder()
                .usuarioId(actorId)
                .usuarioEmail(actorEmail)
                .usuarioRol(actorRol)
                .modulo("GESTION_SERVICIOS")
                .accion("ASIGNAR_SOLICITUD")
                .recurso("solicitud/" + actualizada.getCodigo())
                .detalle("Asignada a: " + actualizada.getAsignadoNombre())
                .resultado("EXITO")
                .correlationId(correlationId)
                .build());

        return actualizada;
    }

    @Override
    @Transactional
    public Solicitud cambiarEstado(
            Long id,
            EstadoSolicitud nuevoEstado,
            String respuesta,
            Long actorId,
            String actorEmail,
            String actorRol,
            String correlationId) {

        Solicitud solicitud = buscarPorId(id);
        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.setEstado(nuevoEstado);

        if (respuesta != null && !respuesta.isBlank()) {
            solicitud.setRespuestaResolucion(respuesta.trim());
        }

        boolean esTerminal = nuevoEstado == EstadoSolicitud.RESUELTA
                || nuevoEstado == EstadoSolicitud.CERRADA
                || nuevoEstado == EstadoSolicitud.APROBADA
                || nuevoEstado == EstadoSolicitud.RECHAZADA;

        if (esTerminal && solicitud.getFechaResolucion() == null) {
            LocalDateTime ahora = LocalDateTime.now();
            solicitud.setFechaResolucion(ahora);
            solicitud.setSlaCumplido(!ahora.isAfter(solicitud.getFechaLimiteSla()));
        }

        Solicitud actualizada = solicitudRepository.save(solicitud);

        auditoriaService.registrar(RegistroAuditoria.builder()
                .usuarioId(actorId)
                .usuarioEmail(actorEmail)
                .usuarioRol(actorRol)
                .modulo("GESTION_SERVICIOS")
                .accion("CAMBIAR_ESTADO_" + nuevoEstado.name())
                .recurso("solicitud/" + actualizada.getCodigo())
                .detalle("Transición " + estadoAnterior + " -> " + nuevoEstado
                        + (respuesta != null && !respuesta.isBlank() ? " | Respuesta: " + respuesta : ""))
                .resultado("EXITO")
                .correlationId(correlationId)
                .build());

        return actualizada;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerResumenSla() {
        List<Solicitud> todas = solicitudRepository.findAll();
        LocalDateTime ahora = LocalDateTime.now();

        long total = todas.size();
        long abiertas = todas.stream().filter(s -> s.getEstado() == EstadoSolicitud.ABIERTA).count();
        long enProceso = todas.stream().filter(s -> s.getEstado() == EstadoSolicitud.EN_PROCESO).count();
        long resueltas = todas.stream().filter(s ->
                s.getEstado() == EstadoSolicitud.RESUELTA
                        || s.getEstado() == EstadoSolicitud.CERRADA
                        || s.getEstado() == EstadoSolicitud.APROBADA
                        || s.getEstado() == EstadoSolicitud.RECHAZADA
        ).count();

        long slaCumplidos = todas.stream().filter(s -> Boolean.TRUE.equals(s.getSlaCumplido())).count();
        long slaVencidos = todas.stream().filter(s ->
                Boolean.FALSE.equals(s.getSlaCumplido())
                        || (s.getFechaResolucion() == null && s.getFechaLimiteSla() != null && ahora.isAfter(s.getFechaLimiteSla()))
        ).count();

        double porcentajeSla = resueltas > 0
                ? Math.round((slaCumplidos * 10000.0) / resueltas) / 100.0
                : 100.0;

        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            porTipo.put(tipo.name(), todas.stream().filter(s -> s.getTipo() == tipo).count());
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("total", total);
        resumen.put("abiertas", abiertas);
        resumen.put("enProceso", enProceso);
        resumen.put("resueltas", resueltas);
        resumen.put("slaCumplidos", slaCumplidos);
        resumen.put("slaVencidos", slaVencidos);
        resumen.put("porcentajeCumplimientoSla", porcentajeSla);
        resumen.put("porTipo", porTipo);
        return resumen;
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long actorId, String actorEmail, String actorRol, String correlationId) {
        Solicitud solicitud = buscarPorId(id);
        solicitudRepository.delete(solicitud);

        auditoriaService.registrar(RegistroAuditoria.builder()
                .usuarioId(actorId)
                .usuarioEmail(actorEmail)
                .usuarioRol(actorRol)
                .modulo("GESTION_SERVICIOS")
                .accion("ELIMINAR_SOLICITUD")
                .recurso("solicitud/" + solicitud.getCodigo())
                .detalle("Solicitud eliminada por administrador")
                .resultado("EXITO")
                .correlationId(correlationId)
                .build());
    }

    private String generarCodigo(TipoSolicitud tipo) {
        String prefijo = switch (tipo) {
            case SERVICIO -> "SRV";
            case INFORMACION -> "INF";
            case ACCESO -> "ACC";
        };
        int sufijo = ThreadLocalRandom.current().nextInt(10000, 99999);
        return prefijo + "-" + Year.now().getValue() + "-" + sufijo;
    }
}
