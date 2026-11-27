package com.pinwino.microservice.gerenciaservice.controller;

import com.pinwino.microservice.gerenciaservice.dto.CambioRequest;
import com.pinwino.microservice.gerenciaservice.model.Cambio;
import com.pinwino.microservice.gerenciaservice.model.CambioHistorial;
import com.pinwino.microservice.gerenciaservice.service.CambioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Gestión de cambios. El gateway deja pasar solo a ADMINISTRADOR y GERENCIA;
 * la identidad viene de X-Usuario-Id / X-Usuario-Tipo (puestas por el gateway) y
 * el servicio valida qué puede hacer cada rol.
 */
@RestController
@RequestMapping("/api/v1/cambios")
@RequiredArgsConstructor
public class CambioController {

    private final CambioService cambioService;

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> resumen() {
        return ResponseEntity.ok(cambioService.resumen());
    }

    @GetMapping
    public ResponseEntity<List<Cambio>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String riesgo) {
        return ResponseEntity.ok(cambioService.listar(estado, tipo, riesgo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cambio> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(cambioService.obtener(id));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<CambioHistorial>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(cambioService.historial(id));
    }

    @PostMapping
    public ResponseEntity<Cambio> crear(
            @RequestBody CambioRequest req,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cambioService.crear(usuarioId, tipoActor, req));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<Cambio> comentar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> cuerpo,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(cambioService.comentar(id, usuarioId, tipoActor, texto(cuerpo, "texto")));
    }

    @PostMapping("/{id}/evaluar")
    public ResponseEntity<Cambio> evaluar(
            @PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(cambioService.evaluar(id, usuarioId, tipoActor));
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<Cambio> decidir(
            @PathVariable Long id,
            @RequestBody Map<String, Object> cuerpo,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        boolean aprobado = Boolean.parseBoolean(String.valueOf(cuerpo.get("aprobado")));
        return ResponseEntity.ok(
                cambioService.decidir(id, usuarioId, tipoActor, aprobado, texto(cuerpo, "motivo")));
    }

    @PostMapping("/{id}/implementar")
    public ResponseEntity<Cambio> implementar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> cuerpo,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(cambioService.implementar(
                id, usuarioId, tipoActor, texto(cuerpo, "resultado"), texto(cuerpo, "notas")));
    }

    @PostMapping("/{id}/verificar")
    public ResponseEntity<Cambio> verificar(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> cuerpo,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(cambioService.verificar(
                id, usuarioId, tipoActor, cuerpo == null ? null : texto(cuerpo, "notas")));
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<Cambio> cerrar(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> cuerpo,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(cambioService.cerrar(
                id, usuarioId, tipoActor, cuerpo == null ? null : texto(cuerpo, "notas")));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Cambio> cancelar(
            @PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {
        return ResponseEntity.ok(cambioService.cancelar(id, usuarioId, tipoActor));
    }

    private String texto(Map<String, Object> cuerpo, String clave) {
        Object valor = cuerpo.get(clave);
        return valor == null ? null : valor.toString();
    }
}
