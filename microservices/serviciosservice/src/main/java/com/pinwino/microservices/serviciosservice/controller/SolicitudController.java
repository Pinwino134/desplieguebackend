package com.pinwino.microservices.serviciosservice.controller;

import com.pinwino.microservices.serviciosservice.model.*;
import com.pinwino.microservices.serviciosservice.service.SolicitudService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    /**
     * Endpoint público para formulario de contacto / consultas iniciales.
     */
    @PostMapping("/publica")
    public ResponseEntity<Solicitud> crearPublica(
            @Valid @RequestBody Solicitud solicitud,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Email", required = false) String usuarioEmail,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String usuarioRol,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            HttpServletRequest request) {

        if (solicitud.getTipo() == TipoSolicitud.ACCESO) {
            solicitud.setTipo(TipoSolicitud.INFORMACION);
        }
        if (solicitud.getPrioridad() == null && solicitud.getImpacto() == null) {
            solicitud.setPrioridad(PrioridadSolicitud.MEDIA);
        }
        Long id = parseLong(usuarioId);
        if (id != null) {
            solicitud.setUsuarioId(id);
            solicitud.setSolicitanteRol(usuarioRol);
            if (solicitud.getSolicitanteEmail() == null || solicitud.getSolicitanteEmail().isBlank()) {
                solicitud.setSolicitanteEmail(usuarioEmail);
            }
        } else {
            solicitud.setSolicitanteRol("PUBLICO");
        }

        Solicitud creada = solicitudService.crear(solicitud, extraerIp(request), correlationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Crea una solicitud o incidente autenticado (SERVICIO, INFORMACION, ACCESO, INCIDENTE).
     */
    @PostMapping
    public ResponseEntity<Solicitud> crear(
            @Valid @RequestBody Solicitud solicitud,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Email", required = false) String usuarioEmail,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String usuarioRol,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            HttpServletRequest request) {

        solicitud.setUsuarioId(parseLong(usuarioId));
        solicitud.setSolicitanteEmail(usuarioEmail != null ? usuarioEmail : solicitud.getSolicitanteEmail());
        solicitud.setSolicitanteRol(usuarioRol != null ? usuarioRol : "CLIENTE");

        Solicitud creada = solicitudService.crear(solicitud, extraerIp(request), correlationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/mis-solicitudes")
    public ResponseEntity<?> misSolicitudes(
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId) {

        Long id = parseLong(usuarioId);
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "mensaje", "Sesión no válida"));
        }
        return ResponseEntity.ok(solicitudService.listarPorUsuario(id));
    }

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> resumenSla() {
        return ResponseEntity.ok(solicitudService.obtenerResumenSla());
    }

    @GetMapping
    public ResponseEntity<List<Solicitud>> listar(
            @RequestParam(required = false) TipoSolicitud tipo,
            @RequestParam(required = false) EstadoSolicitud estado) {
        return ResponseEntity.ok(solicitudService.listar(tipo, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String usuarioRol) {

        Solicitud solicitud = solicitudService.buscarPorId(id);
        boolean esGestor = "ADMINISTRADOR".equalsIgnoreCase(usuarioRol) || "GERENCIA".equalsIgnoreCase(usuarioRol);
        Long actorId = parseLong(usuarioId);

        if (!esGestor && (actorId == null || !actorId.equals(solicitud.getUsuarioId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", 403, "mensaje", "No tienes permiso para ver esta solicitud"));
        }
        return ResponseEntity.ok(solicitud);
    }

    @PatchMapping("/{id}/asignar")
    public ResponseEntity<Solicitud> asignar(
            @PathVariable Long id,
            @RequestBody(required = false) AsignacionRequest body,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Email", required = false) String usuarioEmail,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String usuarioRol,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {

        Long asignadoA = body != null ? body.asignadoA() : null;
        String asignadoNombre = body != null ? body.asignadoNombre() : null;

        return ResponseEntity.ok(solicitudService.asignar(
                id, asignadoA, asignadoNombre,
                parseLong(usuarioId), usuarioEmail, usuarioRol, correlationId
        ));
    }

    @PatchMapping("/{id}/escalar")
    public ResponseEntity<Solicitud> escalar(
            @PathVariable Long id,
            @Valid @RequestBody EscalamientoRequest body,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Email", required = false) String usuarioEmail,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String usuarioRol,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {

        return ResponseEntity.ok(solicitudService.escalar(
                id, body.nivel(), body.motivo(),
                parseLong(usuarioId), usuarioEmail, usuarioRol, correlationId
        ));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Solicitud> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoRequest body,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Email", required = false) String usuarioEmail,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String usuarioRol,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {

        return ResponseEntity.ok(solicitudService.cambiarEstado(
                id, body.estado(), body.respuesta(), body.solucionTemporal(), body.causaRaiz(),
                parseLong(usuarioId), usuarioEmail, usuarioRol, correlationId
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Email", required = false) String usuarioEmail,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String usuarioRol,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {

        solicitudService.eliminar(id, parseLong(usuarioId), usuarioEmail, usuarioRol, correlationId);
        return ResponseEntity.noContent().build();
    }

    private Long parseLong(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Long.valueOf(valor);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String extraerIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public record AsignacionRequest(Long asignadoA, String asignadoNombre) {}

    public record EscalamientoRequest(
            @NotNull(message = "El nivel de escalamiento es obligatorio") NivelEscalamiento nivel,
            String motivo
    ) {}

    public record CambioEstadoRequest(
            @NotNull(message = "El estado es obligatorio") EstadoSolicitud estado,
            String respuesta,
            String solucionTemporal,
            String causaRaiz
    ) {}
}
