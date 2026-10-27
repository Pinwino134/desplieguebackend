package com.pinwino.microservices.productosservice.controller;

import com.pinwino.microservices.productosservice.model.Artesano;
import com.pinwino.microservices.productosservice.repository.ArtesanoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Perfil de los artesanos.
 *
 * El gateway ya validó el token y envía X-Usuario-Id / X-Usuario-Tipo
 * (siempre sobrescritos, el cliente no puede falsificarlos). Con eso se
 * comprueba que un artesano solo vea y edite SU perfil.
 */
@RestController
@RequestMapping("/api/v1/artesanos")
@RequiredArgsConstructor
public class ArtesanoController {

    private static final String ADMIN = "ADMINISTRADOR";

    private final ArtesanoRepository artesanoRepository;

    // Listado para administración y gerencia (el gateway restringe el rol).
    // El DNI solo lo ve el administrador.
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

    // Si el artesano todavía no completó su perfil, devuelve uno vacío.
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

    // Crea o actualiza el perfil.
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

    private boolean puedeAcceder(Long usuarioId, String solicitanteId, String tipo) {
        return ADMIN.equalsIgnoreCase(tipo)
                || String.valueOf(usuarioId).equals(solicitanteId);
    }

    private ResponseEntity<Map<String, Object>> prohibido() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", 403,
                "mensaje", "Solo puedes ver o editar tu propio perfil"));
    }
}
