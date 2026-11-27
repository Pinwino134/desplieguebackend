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

        // Priorización formal ITIL: si vienen Impacto y Urgencia, aplicamos la Matriz Impacto × Urgencia.
        if (solicitud.getImpacto() != null && solicitud.getUrgencia() != null) {
            solicitud.setPrioridad(PrioridadSolicitud.calcularPorMatriz(solicitud.getImpacto(), solicitud.getUrgencia()));
        } else if (solicitud.getPrioridad() == null) {
            solicitud.setPrioridad(PrioridadSolicitud.MEDIA);
        }

        if (solicitud.getImpacto() == null) {
            solicitud.setImpacto(switch (solicitud.getPrioridad()) {
                case CRITICA, ALTA -> NivelImpacto.ALTO;
                case MEDIA -> NivelImpacto.MEDIO;
                case BAJA -> NivelImpacto.BAJO;
            });
        }
        if (solicitud.getUrgencia() == null) {
            solicitud.setUrgencia(switch (solicitud.getPrioridad()) {
                case CRITICA, ALTA -> NivelUrgencia.ALTA;
                case MEDIA -> NivelUrgencia.MEDIA;
                case BAJA -> NivelUrgencia.BAJA;
            });
        }

        if (solicitud.getTipo() == TipoSolicitud.ACCESO
                && (solicitud.getRolSolicitado() == null || solicitud.getRolSolicitado().isBlank())) {
            throw new BusinessException("En solicitudes de acceso debes especificar el rol o permiso solicitado");
        }

        // Nivel inicial de escalamiento: P1 entra directo a N2_ADMINISTRADOR, el resto inicia en N1_MESA_SERVICIO.
        if (solicitud.getNivelEscalamiento() == null) {
            solicitud.setNivelEscalamiento(
                    solicitud.getPrioridad() == PrioridadSolicitud.CRITICA
                            ? NivelEscalamiento.N2_ADMINISTRADOR
                            : NivelEscalamiento.N1_MESA_SERVICIO
            );
        }

        LocalDateTime ahora = LocalDateTime.now();
        solicitud.setEstado(EstadoSolicitud.ABIERTA);
        solicitud.setCodigo(generarCodigo(solicitud.getTipo()));
        solicitud.setFechaLimiteRespuestaSla(ahora.plusMinutes(solicitud.getPrioridad().getMinutosRespuestaSla()));
        solicitud.setFechaLimiteSla(ahora.plusHours(solicitud.getPrioridad().getHorasSla()));
        solicitud.setSlaRespuestaCumplido(null);
        solicitud.setSlaCumplido(null);

        Solicitud guardada = solicitudRepository.save(solicitud);

        auditoriaService.registrar(RegistroAuditoria.builder()
                .usuarioId(guardada.getUsuarioId())
                .usuarioEmail(guardada.getSolicitanteEmail())
                .usuarioRol(guardada.getSolicitanteRol() != null ? guardada.getSolicitanteRol() : "PUBLICO")
                .modulo(guardada.getTipo() == TipoSolicitud.INCIDENTE ? "GESTION_INCIDENTES" : "GESTION_SERVICIOS")
                .accion("CREAR_" + guardada.getTipo().name())
                .recurso("solicitud/" + guardada.getCodigo())
                .detalle("Asunto: " + guardada.getAsunto()
                        + " | Prioridad: " + guardada.getPrioridad().getCodigoNivel() + " (" + guardada.getPrioridad() + ")"
                        + " | Escalamiento: " + guardada.getNivelEscalamiento())
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
        registrarPrimeraRespuestaSiFalta(solicitud);

        Solicitud actualizada = solicitudRepository.save(solicitud);

        auditoriaService.registrar(RegistroAuditoria.builder()
                .usuarioId(actorId)
                .usuarioEmail(actorEmail)
                .usuarioRol(actorRol)
                .modulo(actualizada.getTipo() == TipoSolicitud.INCIDENTE ? "GESTION_INCIDENTES" : "GESTION_SERVICIOS")
                .accion("ASIGNAR_TICKET")
                .recurso("solicitud/" + actualizada.getCodigo())
                .detalle("Asignado a: " + actualizada.getAsignadoNombre() + " | Nivel: " + actualizada.getNivelEscalamiento())
                .resultado("EXITO")
                .correlationId(correlationId)
                .build());

        return actualizada;
    }

    @Override
    @Transactional
    public Solicitud escalar(
            Long id,
            NivelEscalamiento nuevoNivel,
            String motivo,
            Long actorId,
            String actorEmail,
            String actorRol,
            String correlationId) {

        Solicitud solicitud = buscarPorId(id);
        NivelEscalamiento nivelAnterior = solicitud.getNivelEscalamiento();
        solicitud.setNivelEscalamiento(nuevoNivel);
        if (solicitud.getEstado() == EstadoSolicitud.ABIERTA) {
            solicitud.setEstado(EstadoSolicitud.EN_PROCESO);
        }
        registrarPrimeraRespuestaSiFalta(solicitud);

        Solicitud actualizada = solicitudRepository.save(solicitud);

        auditoriaService.registrar(RegistroAuditoria.builder()
                .usuarioId(actorId)
                .usuarioEmail(actorEmail)
                .usuarioRol(actorRol)
                .modulo("GESTION_INCIDENTES")
                .accion("ESCALAR_" + nuevoNivel.name())
                .recurso("solicitud/" + actualizada.getCodigo())
                .detalle("Escalamiento " + nivelAnterior + " -> " + nuevoNivel
                        + (motivo != null && !motivo.isBlank() ? " | Motivo: " + motivo.trim() : ""))
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
            String solucionTemporal,
            String causaRaiz,
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
        if (solucionTemporal != null && !solucionTemporal.isBlank()) {
            solicitud.setSolucionTemporal(solucionTemporal.trim());
        }
        if (causaRaiz != null && !causaRaiz.isBlank()) {
            solicitud.setCausaRaiz(causaRaiz.trim());
        }

        registrarPrimeraRespuestaSiFalta(solicitud);

        boolean esTerminal = nuevoEstado == EstadoSolicitud.RESUELTA
                || nuevoEstado == EstadoSolicitud.CERRADA
                || nuevoEstado == EstadoSolicitud.APROBADA
                || nuevoEstado == EstadoSolicitud.RECHAZADA;

        if (esTerminal && solicitud.getFechaResolucion() == null) {
            LocalDateTime ahora = LocalDateTime.now();
            solicitud.setFechaResolucion(ahora);
            solicitud.setSlaCumplido(solicitud.getFechaLimiteSla() == null || !ahora.isAfter(solicitud.getFechaLimiteSla()));
        }

        Solicitud actualizada = solicitudRepository.save(solicitud);

        auditoriaService.registrar(RegistroAuditoria.builder()
                .usuarioId(actorId)
                .usuarioEmail(actorEmail)
                .usuarioRol(actorRol)
                .modulo(actualizada.getTipo() == TipoSolicitud.INCIDENTE ? "GESTION_INCIDENTES" : "GESTION_SERVICIOS")
                .accion("CAMBIAR_ESTADO_" + nuevoEstado.name())
                .recurso("solicitud/" + actualizada.getCodigo())
                .detalle("Transición " + estadoAnterior + " -> " + nuevoEstado
                        + (respuesta != null && !respuesta.isBlank() ? " | Respuesta: " + respuesta.trim() : ""))
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

        long slaRespuestaCumplidos = todas.stream().filter(s -> Boolean.TRUE.equals(s.getSlaRespuestaCumplido())).count();
        long conPrimeraRespuesta = todas.stream().filter(s -> s.getFechaPrimeraRespuesta() != null).count();

        double porcentajeSla = resueltas > 0
                ? Math.round((slaCumplidos * 10000.0) / resueltas) / 100.0
                : 100.0;
        double porcentajeSlaRespuesta = conPrimeraRespuesta > 0
                ? Math.round((slaRespuestaCumplidos * 10000.0) / conPrimeraRespuesta) / 100.0
                : 100.0;

        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            porTipo.put(tipo.name(), todas.stream().filter(s -> s.getTipo() == tipo).count());
        }

        Map<String, Long> porPrioridad = new LinkedHashMap<>();
        for (PrioridadSolicitud prio : PrioridadSolicitud.values()) {
            porPrioridad.put(prio.name(), todas.stream().filter(s -> s.getPrioridad() == prio).count());
        }

        Map<String, Long> porNivelEscalamiento = new LinkedHashMap<>();
        for (NivelEscalamiento nivel : NivelEscalamiento.values()) {
            porNivelEscalamiento.put(nivel.name(), todas.stream().filter(s -> s.getNivelEscalamiento() == nivel).count());
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("total", total);
        resumen.put("abiertas", abiertas);
        resumen.put("enProceso", enProceso);
        resumen.put("resueltas", resueltas);
        resumen.put("slaCumplidos", slaCumplidos);
        resumen.put("slaVencidos", slaVencidos);
        resumen.put("porcentajeCumplimientoSla", porcentajeSla);
        resumen.put("porcentajeCumplimientoSlaRespuesta", porcentajeSlaRespuesta);
        resumen.put("porTipo", porTipo);
        resumen.put("porPrioridad", porPrioridad);
        resumen.put("porNivelEscalamiento", porNivelEscalamiento);
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

    private void registrarPrimeraRespuestaSiFalta(Solicitud solicitud) {
        if (solicitud.getFechaPrimeraRespuesta() == null) {
            LocalDateTime ahora = LocalDateTime.now();
            solicitud.setFechaPrimeraRespuesta(ahora);
            solicitud.setSlaRespuestaCumplido(
                    solicitud.getFechaLimiteRespuestaSla() == null || !ahora.isAfter(solicitud.getFechaLimiteRespuestaSla())
            );
        }
    }

    private String generarCodigo(TipoSolicitud tipo) {
        String prefijo = switch (tipo) {
            case SERVICIO -> "SRV";
            case INFORMACION -> "INF";
            case ACCESO -> "ACC";
            case INCIDENTE -> "INC";
        };
        int sufijo = ThreadLocalRandom.current().nextInt(10000, 99999);
        return prefijo + "-" + Year.now().getValue() + "-" + sufijo;
    }
}
