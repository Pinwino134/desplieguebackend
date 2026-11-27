package com.pinwino.microservices.productosservice.controller;

import com.pinwino.microservices.productosservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.productosservice.model.Artesano;
import com.pinwino.microservices.productosservice.model.EstadoRevisionLogro;
import com.pinwino.microservices.productosservice.model.LogroArtesano;
import com.pinwino.microservices.productosservice.repository.ArtesanoRepository;
import com.pinwino.microservices.productosservice.repository.LogroArtesanoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Perfil público/privado de artesanos y gestión de reconocimientos destacados
 * con flujo de revisión y aprobación del administrador antes de publicarse al cliente.
 */
@RestController
@RequestMapping("/api/v1/artesanos")
@RequiredArgsConstructor
public class ArtesanoController {

    private static final String ADMIN = "ADMINISTRADOR";

    private final ArtesanoRepository artesanoRepository;
    private final LogroArtesanoRepository logroRepository;

    // Listado para administración y gerencia (DNI solo visible para ADMINISTRADOR).
    @GetMapping
    public ResponseEntity<List<Artesano>> listar(
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipo) {

        boolean esAdmin = ADMIN.equalsIgnoreCase(tipo);

        List<Artesano> lista = artesanoRepository.findAll().stream()
                .map(a -> Artesano.builder()
                        .usuarioId(a.getUsuarioId())
                        .nombres(a.getNombres())
                        .apellidos(a.getApellidos())
                        .dni(esAdmin ? a.getDni() : null)
                        .especialidad(a.getEspecialidad())
                        .region(a.getRegion())
                        .telefono(a.getTelefono())
                        .descripcion(a.getDescripcion())
                        .fotoUrl(a.getFotoUrl())
                        .build())
                .toList();

        return ResponseEntity.ok(lista);
    }

    /**
     * Directorio público para clientes: expone perfiles sin DNI junto con
     * únicamente los logros/reconocimientos APROBADOS por el administrador.
     */
    @GetMapping("/publico")
    public ResponseEntity<List<Map<String, Object>>> listarPublico() {
        List<Map<String, Object>> resultado = artesanoRepository.findAll().stream()
                .map(this::construirPerfilPublico)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{usuarioId}/publico")
    public ResponseEntity<Map<String, Object>> obtenerPublico(@PathVariable Long usuarioId) {
        Artesano perfil = artesanoRepository.findById(usuarioId)
                .orElseGet(() -> Artesano.builder().usuarioId(usuarioId).nombres("Artesano #" + usuarioId).build());
        return ResponseEntity.ok(construirPerfilPublico(perfil));
    }

    // Listado global de logros para revisión del administrador.
    @GetMapping("/logros")
    public ResponseEntity<List<LogroArtesano>> listarTodosLosLogros(
            @RequestParam(required = false) EstadoRevisionLogro estado) {
        if (estado != null) {
            return ResponseEntity.ok(logroRepository.findByEstadoOrderByFechaCreacionDesc(estado));
        }
        return ResponseEntity.ok(logroRepository.findAllByOrderByFechaCreacionDesc());
    }

    // Revisión administrativa: aprobar (publica al cliente) o rechazar un logro.
    @PatchMapping("/logros/{logroId}/revisar")
    public ResponseEntity<LogroArtesano> revisarLogro(
            @PathVariable Long logroId,
            @RequestBody RevisionLogroRequest body) {

        LogroArtesano logro = logroRepository.findById(logroId)
                .orElseThrow(() -> new ResourceNotFoundException("LogroArtesano", logroId));

        logro.setEstado(body.aprobar() ? EstadoRevisionLogro.APROBADO : EstadoRevisionLogro.RECHAZADO);
        logro.setComentarioAdmin(body.comentarioAdmin() != null ? body.comentarioAdmin().trim() : null);
        logro.setFechaRevision(LocalDateTime.now());
        return ResponseEntity.ok(logroRepository.save(logro));
    }

    // Logros propios del artesano (muestra pendientes, aprobados y rechazados).
    @GetMapping("/{usuarioId}/logros")
    public ResponseEntity<?> listarLogrosDeArtesano(
            @PathVariable Long usuarioId,
            @RequestHeader(value = "X-Usuario-Id", required = false) String solicitanteId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipo) {

        if (!puedeAcceder(usuarioId, solicitanteId, tipo)) {
            return prohibido();
        }
        return ResponseEntity.ok(logroRepository.findByArtesanoIdOrderByFechaCreacionDesc(usuarioId));
    }

    // Artesano registra un logro/distinción (siempre nace en PENDIENTE_REVISION).
    @PostMapping("/{usuarioId}/logros")
    public ResponseEntity<?> registrarLogro(
            @PathVariable Long usuarioId,
            @Valid @RequestBody LogroArtesano logro,
            @RequestHeader(value = "X-Usuario-Id", required = false) String solicitanteId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipo) {

        if (!puedeAcceder(usuarioId, solicitanteId, tipo)) {
            return prohibido();
        }

        Artesano perfil = artesanoRepository.findById(usuarioId).orElse(null);
        String nombreCompleto = perfil != null
                ? ((perfil.getNombres() != null ? perfil.getNombres() : "") + " " + (perfil.getApellidos() != null ? perfil.getApellidos() : "")).trim()
                : logro.getArtesanoNombre();

        logro.setArtesanoId(usuarioId);
        if (nombreCompleto != null && !nombreCompleto.isBlank()) {
            logro.setArtesanoNombre(nombreCompleto);
        }
        logro.setEstado(EstadoRevisionLogro.PENDIENTE_REVISION);
        logro.setComentarioAdmin(null);
        logro.setFechaRevision(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(logroRepository.save(logro));
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<?> obtener(
            @PathVariable Long usuarioId,
            @RequestHeader(value = "X-Usuario-Id", required = false) String solicitanteId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipo) {

        if (!puedeAcceder(usuarioId, solicitanteId, tipo)) {
            return prohibido();
        }

        Artesano perfil = artesanoRepository.findById(usuarioId)
                .orElseGet(() -> Artesano.builder().usuarioId(usuarioId).build());

        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/{usuarioId}")
    public ResponseEntity<?> guardar(
            @PathVariable Long usuarioId,
            @Valid @RequestBody Artesano datos,
            @RequestHeader(value = "X-Usuario-Id", required = false) String solicitanteId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipo) {

        if (!puedeAcceder(usuarioId, solicitanteId, tipo)) {
            return prohibido();
        }

        Artesano perfil = artesanoRepository.findById(usuarioId)
                .orElseGet(() -> Artesano.builder().usuarioId(usuarioId).build());

        perfil.setNombres(datos.getNombres());
        perfil.setApellidos(datos.getApellidos());
        perfil.setDni(datos.getDni());
        perfil.setEspecialidad(datos.getEspecialidad());
        perfil.setRegion(datos.getRegion());
        perfil.setTelefono(datos.getTelefono());
        perfil.setDescripcion(datos.getDescripcion());
        perfil.setFotoUrl(datos.getFotoUrl());

        return ResponseEntity.ok(artesanoRepository.save(perfil));
    }

    private Map<String, Object> construirPerfilPublico(Artesano a) {
        List<LogroArtesano> logrosAprobados = logroRepository
                .findByArtesanoIdAndEstadoOrderByAnioDesc(a.getUsuarioId(), EstadoRevisionLogro.APROBADO);

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("usuarioId", a.getUsuarioId());
        dto.put("nombres", a.getNombres());
        dto.put("apellidos", a.getApellidos());
        dto.put("especialidad", a.getEspecialidad());
        dto.put("region", a.getRegion());
        dto.put("descripcion", a.getDescripcion());
        dto.put("fotoUrl", a.getFotoUrl());
        dto.put("destacado", !logrosAprobados.isEmpty());
        dto.put("logrosAprobados", logrosAprobados);
        return dto;
    }

    private boolean puedeAcceder(Long usuarioId, String solicitanteId, String tipo) {
        return ADMIN.equalsIgnoreCase(tipo)
                || String.valueOf(usuarioId).equals(solicitanteId);
    }

    private ResponseEntity<Map<String, Object>> prohibido() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", 403,
                "mensaje", "Solo puedes ver o editar tu propio perfil"));
    }

    public record RevisionLogroRequest(boolean aprobar, String comentarioAdmin) {}
}
