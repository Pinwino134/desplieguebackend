package com.pinwino.microservices.serviciosservice.controller;

import com.pinwino.microservices.serviciosservice.model.RegistroAuditoria;
import com.pinwino.microservices.serviciosservice.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<RegistroAuditoria>> listar(
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(auditoriaService.listar(modulo, usuarioId));
    }

    @PostMapping
    public ResponseEntity<RegistroAuditoria> registrar(
            @Valid @RequestBody RegistroAuditoria registro,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Email", required = false) String usuarioEmail,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String usuarioRol,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            HttpServletRequest request) {

        if (usuarioId != null && !usuarioId.isBlank()) {
            try {
                registro.setUsuarioId(Long.valueOf(usuarioId));
            } catch (NumberFormatException ignored) {
                // ignorar header no numérico
            }
        }
        if (usuarioEmail != null && !usuarioEmail.isBlank()) {
            registro.setUsuarioEmail(usuarioEmail);
        }
        if (usuarioRol != null && !usuarioRol.isBlank()) {
            registro.setUsuarioRol(usuarioRol);
        }
        registro.setCorrelationId(correlationId);
        registro.setIpOrigen(extraerIp(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(auditoriaService.registrar(registro));
    }

    private String extraerIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
