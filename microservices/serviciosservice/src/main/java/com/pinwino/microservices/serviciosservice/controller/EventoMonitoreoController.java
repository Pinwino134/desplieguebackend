package com.pinwino.microservices.serviciosservice.controller;

import com.pinwino.microservices.serviciosservice.model.*;
import com.pinwino.microservices.serviciosservice.service.EventoMonitoreoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class EventoMonitoreoController {

    private final EventoMonitoreoService eventoService;

    @GetMapping
    public ResponseEntity<List<EventoMonitoreo>> listar(
            @RequestParam(required = false) TipoEvento tipo,
            @RequestParam(required = false) EstadoEvento estado) {
        return ResponseEntity.ok(eventoService.listar(tipo, estado));
    }

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> resumen() {
        return ResponseEntity.ok(eventoService.obtenerResumenEventos());
    }

    @PostMapping
    public ResponseEntity<EventoMonitoreo> registrar(
            @Valid @RequestBody EventoMonitoreo evento,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.registrar(evento, correlationId));
    }

    /**
     * Webhook compatible con Prometheus Alertmanager para ingestión automática de alertas.
     */
    @PostMapping("/webhook-alertmanager")
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<EventoMonitoreo>> recibirWebhookAlertmanager(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {

        List<EventoMonitoreo> creados = new ArrayList<>();
        Object alertsObj = payload.get("alerts");
        if (alertsObj instanceof List<?> alerts) {
            for (Object item : alerts) {
                if (item instanceof Map<?, ?> rawAlert) {
                    Map<String, Object> alertMap = (Map<String, Object>) rawAlert;
                    Map<String, Object> labels = alertMap.get("labels") instanceof Map<?, ?> l
                            ? (Map<String, Object>) l : Map.of();
                    Map<String, Object> annotations = alertMap.get("annotations") instanceof Map<?, ?> a
                            ? (Map<String, Object>) a : Map.of();

                    String severity = String.valueOf(labels.getOrDefault("severity", "warning")).toLowerCase();
                    TipoEvento tipo = switch (severity) {
                        case "critical", "error" -> TipoEvento.EXCEPCION;
                        case "warning" -> TipoEvento.ADVERTENCIA;
                        default -> TipoEvento.INFORMATIVO;
                    };

                    EventoMonitoreo ev = EventoMonitoreo.builder()
                            .tipo(tipo)
                            .fuenteMonitoreo("PROMETHEUS_ALERTMANAGER")
                            .servicioOrigen(String.valueOf(labels.getOrDefault("application", "apigateway")))
                            .titulo(String.valueOf(annotations.getOrDefault("summary", labels.getOrDefault("alertname", "Alerta de Monitoreo"))))
                            .descripcion(String.valueOf(annotations.getOrDefault("description", "Alerta recibida desde Prometheus")))
                            .metrica(String.valueOf(labels.getOrDefault("alertname", "custom_alert")))
                            .valorDetectado(String.valueOf(alertMap.getOrDefault("status", "firing")))
                            .umbral(severity.toUpperCase())
                            .build();
                    creados.add(eventoService.registrar(ev, correlationId));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(creados);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EventoMonitoreo> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoEventoRequest body,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Email", required = false) String usuarioEmail,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String usuarioRol,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {

        return ResponseEntity.ok(eventoService.actualizarEstado(
                id, body.estado(), body.accionRespuesta(),
                parseLong(usuarioId), usuarioEmail, usuarioRol, correlationId
        ));
    }

    @PostMapping("/{id}/escalar-incidente")
    public ResponseEntity<Solicitud> escalarAIncidente(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Email", required = false) String usuarioEmail,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String usuarioRol,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            HttpServletRequest request) {

        Solicitud incidente = eventoService.escalarAIncidente(
                id, parseLong(usuarioId), usuarioEmail, usuarioRol, request.getRemoteAddr(), correlationId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(incidente);
    }

    private Long parseLong(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Long.valueOf(valor);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record EstadoEventoRequest(
            @NotNull(message = "El estado es obligatorio") EstadoEvento estado,
            String accionRespuesta
    ) {}
}
