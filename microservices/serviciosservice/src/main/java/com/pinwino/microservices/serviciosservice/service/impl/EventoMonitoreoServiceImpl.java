package com.pinwino.microservices.serviciosservice.service.impl;

import com.pinwino.microservices.serviciosservice.exception.BusinessException;
import com.pinwino.microservices.serviciosservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.serviciosservice.model.*;
import com.pinwino.microservices.serviciosservice.repository.EventoMonitoreoRepository;
import com.pinwino.microservices.serviciosservice.service.AuditoriaService;
import com.pinwino.microservices.serviciosservice.service.EventoMonitoreoService;
import com.pinwino.microservices.serviciosservice.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EventoMonitoreoServiceImpl implements EventoMonitoreoService {

    private final EventoMonitoreoRepository eventoRepository;
    private final SolicitudService solicitudService;
    private final AuditoriaService auditoriaService;

    @Override
    @Transactional
    public EventoMonitoreo registrar(EventoMonitoreo evento, String correlationId) {
        if (evento.getTipo() == null) {
            evento.setTipo(TipoEvento.INFORMATIVO);
        }
        evento.setEstado(EstadoEvento.DETECTADO);
        evento.setCodigo(generarCodigo(evento.getTipo()));

        if (evento.getImpactoEstimado() == null) {
            evento.setImpactoEstimado(switch (evento.getTipo()) {
                case EXCEPCION -> NivelImpacto.ALTO;
                case ADVERTENCIA -> NivelImpacto.MEDIO;
                case INFORMATIVO -> NivelImpacto.BAJO;
            });
        }
        if (evento.getUrgenciaEstimada() == null) {
            evento.setUrgenciaEstimada(switch (evento.getTipo()) {
                case EXCEPCION -> NivelUrgencia.ALTA;
                case ADVERTENCIA -> NivelUrgencia.MEDIA;
                case INFORMATIVO -> NivelUrgencia.BAJA;
            });
        }

        EventoMonitoreo guardado = eventoRepository.save(evento);

        auditoriaService.registrar(RegistroAuditoria.builder()
                .usuarioRol("SISTEMA_MONITOREO")
                .modulo("GESTION_EVENTOS")
                .accion("EVENTO_" + guardado.getTipo().name())
                .recurso("evento/" + guardado.getCodigo())
                .detalle("Servicio: " + guardado.getServicioOrigen() + " | " + guardado.getTitulo())
                .resultado(guardado.getTipo() == TipoEvento.EXCEPCION ? "ALERTA" : "EXITO")
                .correlationId(correlationId)
                .build());

        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoMonitoreo> listar(TipoEvento tipo, EstadoEvento estado) {
        if (tipo != null && estado != null) {
            return eventoRepository.findByTipoAndEstadoOrderByFechaDeteccionDesc(tipo, estado);
        }
        if (tipo != null) {
            return eventoRepository.findByTipoOrderByFechaDeteccionDesc(tipo);
        }
        if (estado != null) {
            return eventoRepository.findByEstadoOrderByFechaDeteccionDesc(estado);
        }
        return eventoRepository.findAllByOrderByFechaDeteccionDesc();
    }

    @Override
    @Transactional
    public EventoMonitoreo actualizarEstado(
            Long id,
            EstadoEvento nuevoEstado,
            String accionRespuesta,
            Long actorId,
            String actorEmail,
            String actorRol,
            String correlationId) {

        EventoMonitoreo evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventoMonitoreo", id));

        evento.setEstado(nuevoEstado);
        if (accionRespuesta != null && !accionRespuesta.isBlank()) {
            evento.setAccionRespuesta(accionRespuesta.trim());
        }
        if (nuevoEstado == EstadoEvento.ATENDIDO || nuevoEstado == EstadoEvento.DESCARTADO) {
            evento.setFechaAtencion(LocalDateTime.now());
        }

        EventoMonitoreo actualizado = eventoRepository.save(evento);

        auditoriaService.registrar(RegistroAuditoria.builder()
                .usuarioId(actorId)
                .usuarioEmail(actorEmail)
                .usuarioRol(actorRol)
                .modulo("GESTION_EVENTOS")
                .accion("ACTUALIZAR_EVENTO_" + nuevoEstado.name())
                .recurso("evento/" + actualizado.getCodigo())
                .detalle(accionRespuesta != null ? accionRespuesta : "Estado actualizado a " + nuevoEstado)
                .resultado("EXITO")
                .correlationId(correlationId)
                .build());

        return actualizado;
    }

    @Override
    @Transactional
    public Solicitud escalarAIncidente(
            Long eventoId,
            Long actorId,
            String actorEmail,
            String actorRol,
            String ipOrigen,
            String correlationId) {

        EventoMonitoreo evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("EventoMonitoreo", eventoId));

        if (evento.getIncidenteGeneradoId() != null) {
            throw new BusinessException("El evento " + evento.getCodigo()
                    + " ya fue escalado al incidente " + evento.getIncidenteGeneradoCodigo());
        }

        NivelImpacto impacto = evento.getImpactoEstimado() != null ? evento.getImpactoEstimado() : NivelImpacto.MEDIO;
        NivelUrgencia urgencia = evento.getUrgenciaEstimada() != null ? evento.getUrgenciaEstimada() : NivelUrgencia.ALTA;

        Solicitud incidente = Solicitud.builder()
                .tipo(TipoSolicitud.INCIDENTE)
                .impacto(impacto)
                .urgencia(urgencia)
                .categoria("INCIDENTE_OPERATIVO")
                .servicioAfectado(evento.getServicioOrigen())
                .eventoOrigenId(evento.getId())
                .asunto("[" + evento.getServicioOrigen() + "] " + evento.getTitulo())
                .descripcion("Incidente generado automáticamente desde evento de monitoreo " + evento.getCodigo()
                        + " (Fuente: " + evento.getFuenteMonitoreo()
                        + ", Métrica: " + (evento.getMetrica() != null ? evento.getMetrica() : "N/A")
                        + ", Valor: " + (evento.getValorDetectado() != null ? evento.getValorDetectado() : "N/A")
                        + ", Umbral: " + (evento.getUmbral() != null ? evento.getUmbral() : "N/A") + "). "
                        + (evento.getDescripcion() != null ? evento.getDescripcion() : ""))
                .usuarioId(actorId)
                .solicitanteNombre("Centro de Monitoreo Arte Andino")
                .solicitanteEmail(actorEmail != null ? actorEmail : "monitoreo@arteandino.pe")
                .solicitanteRol(actorRol != null ? actorRol : "SISTEMA_MONITOREO")
                .build();

        Solicitud creado = solicitudService.crear(incidente, ipOrigen, correlationId);

        evento.setEstado(EstadoEvento.ESCALADO_A_INCIDENTE);
        evento.setIncidenteGeneradoId(creado.getId());
        evento.setIncidenteGeneradoCodigo(creado.getCodigo());
        evento.setAccionRespuesta("Escalado formalmente al incidente " + creado.getCodigo()
                + " (" + creado.getPrioridad().getCodigoNivel() + " - " + creado.getNivelEscalamiento() + ")");
        evento.setFechaAtencion(LocalDateTime.now());
        eventoRepository.save(evento);

        return creado;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerResumenEventos() {
        List<EventoMonitoreo> todos = eventoRepository.findAll();

        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (TipoEvento t : TipoEvento.values()) {
            porTipo.put(t.name(), todos.stream().filter(e -> e.getTipo() == t).count());
        }

        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoEvento st : EstadoEvento.values()) {
            porEstado.put(st.name(), todos.stream().filter(e -> e.getEstado() == st).count());
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("total", todos.size());
        resumen.put("activos", todos.stream().filter(e ->
                e.getEstado() == EstadoEvento.DETECTADO || e.getEstado() == EstadoEvento.EN_REVISION).count());
        resumen.put("excepciones", porTipo.getOrDefault("EXCEPCION", 0L));
        resumen.put("advertencias", porTipo.getOrDefault("ADVERTENCIA", 0L));
        resumen.put("informativos", porTipo.getOrDefault("INFORMATIVO", 0L));
        resumen.put("escaladosAIncidente", porEstado.getOrDefault("ESCALADO_A_INCIDENTE", 0L));
        resumen.put("porTipo", porTipo);
        resumen.put("porEstado", porEstado);
        return resumen;
    }

    private String generarCodigo(TipoEvento tipo) {
        String prefijo = switch (tipo) {
            case INFORMATIVO -> "EVI";
            case ADVERTENCIA -> "EVA";
            case EXCEPCION -> "EVX";
        };
        int sufijo = ThreadLocalRandom.current().nextInt(10000, 99999);
        return prefijo + "-" + Year.now().getValue() + "-" + sufijo;
    }
}
