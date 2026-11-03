package com.pinwino.microservices.ventasservice.controller;

import com.pinwino.microservices.ventasservice.exception.BusinessException;
import com.pinwino.microservices.ventasservice.model.Solicitud;
import com.pinwino.microservices.ventasservice.model.SolicitudHistorial;
import com.pinwino.microservices.ventasservice.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Solicitudes de servicio.
 *
 * La identidad sale de las cabeceras X-Usuario-Id y X-Usuario-Tipo, que pone el
 * gateway después de validar el JWT (el cliente no puede falsificarlas).
 * El gateway además restringe por rol los listados globales y las acciones
 * de administración (ver apigateway.yml); el servicio vuelve a comprobarlo.
 */
@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    // ---------- catálogo e indicadores ----------

    @GetMapping("/tipos")
    public ResponseEntity<List<Map<String, Object>>> tipos() {
        return ResponseEntity.ok(solicitudService.catalogo());
    }

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> resumen() {
        return ResponseEntity.ok(solicitudService.resumen());
    }

    // ---------- consultas ----------

    @GetMapping("/mias")
    public ResponseEntity<List<Solicitud>> mias(
            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(solicitudService.listarMias(usuarioId));
    }

    // Listado global: administrador y gerencia (lo restringe el gateway)
    @GetMapping
    public ResponseEntity<List<Solicitud>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String prioridad) {
        return ResponseEntity.ok(solicitudService.listarTodas(estado, tipo, prioridad));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> obtener(
            @PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(solicitudService.obtener(id, usuarioId, tipoActor));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<SolicitudHistorial>> historial(
            @PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(solicitudService.historial(id, usuarioId, tipoActor));
    }

    // ---------- acciones del usuario ----------

    @PostMapping
    public ResponseEntity<Solicitud> crear(
            @RequestBody Map<String, Object> cuerpo,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {

        Solicitud creada = solicitudService.crear(
                usuarioId,
                tipoActor == null ? "DESCONOCIDO" : tipoActor,
                texto(cuerpo, "tipo"),
                texto(cuerpo, "asunto"),
                texto(cuerpo, "descripcion"),
                numero(cuerpo, "pedidoId"));

        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<Solicitud> comentar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> cuerpo,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(
                solicitudService.comentar(id, usuarioId, tipoActor, texto(cuerpo, "texto")));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Solicitud> cancelar(
            @PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(solicitudService.cancelar(id, usuarioId, tipoActor));
    }

    // ---------- acciones del administrador ----------

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Solicitud> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Object> cuerpo,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(solicitudService.cambiarEstado(
                id, usuarioId, tipoActor, texto(cuerpo, "estado"), texto(cuerpo, "comentario")));
    }

    @PatchMapping("/{id}/asignar")
    public ResponseEntity<Solicitud> asignar(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> cuerpo,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        Long asignadoA = cuerpo == null ? null : numero(cuerpo, "asignadoA");
        return ResponseEntity.ok(solicitudService.asignar(id, usuarioId, tipoActor, asignadoA));
    }

    @PatchMapping("/{id}/prioridad")
    public ResponseEntity<Solicitud> prioridad(
            @PathVariable Long id,
            @RequestBody Map<String, Object> cuerpo,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(solicitudService.cambiarPrioridad(
                id, usuarioId, tipoActor, texto(cuerpo, "prioridad")));
    }

    // ---------- lectura de cuerpos JSON ----------

    private String texto(Map<String, Object> cuerpo, String clave) {
        Object valor = cuerpo.get(clave);
        return valor == null ? null : valor.toString();
    }

    private Long numero(Map<String, Object> cuerpo, String clave) {
        Object valor = cuerpo.get(clave);
        if (valor == null || valor.toString().isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(valor.toString().trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("El campo " + clave + " debe ser un número");
        }
    }
}
