package com.pinwino.microservices.authservice.controller;

import com.pinwino.microservices.authservice.model.TipoActor;
import com.pinwino.microservices.authservice.model.Usuario;
import com.pinwino.microservices.authservice.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor

public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> listar(@RequestParam(required = false) TipoActor tipoActor) {
        if (tipoActor != null) {
            return ResponseEntity.ok(usuarioService.listarPorTipo(tipoActor));
        }
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // =========================================================
    // MI PERFIL (cualquier usuario autenticado)
    // El gateway envía X-Usuario-Id ya validado, así que cada quien solo
    // ve y edita su propia cuenta. No se puede cambiar rol, estado ni correo.
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<?> miPerfil(
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId) {

        Long id = idSolicitante(usuarioId);
        if (id == null) {
            return sinSesion();
        }

        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/me")
    public ResponseEntity<?> actualizarMiPerfil(
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestBody Map<String, String> cuerpo) {

        Long id = idSolicitante(usuarioId);
        if (id == null) {
            return sinSesion();
        }

        String nombres = cuerpo.getOrDefault("nombres", "").trim();
        String apellidos = cuerpo.getOrDefault("apellidos", "").trim();

        if (nombres.isEmpty() || apellidos.isEmpty()
                || nombres.length() > 100 || apellidos.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "mensaje", "Nombres y apellidos son obligatorios (máximo 100 caracteres)"));
        }

        Usuario actual = usuarioService.buscarPorId(id);

        Usuario datos = new Usuario();
        datos.setNombres(nombres);
        datos.setApellidos(apellidos);
        datos.setEstado(actual.getEstado());       // sin cambios
        datos.setTipoActor(actual.getTipoActor()); // sin cambios

        return ResponseEntity.ok(usuarioService.actualizar(id, datos));
    }

    private Long idSolicitante(String header) {
        try {
            return header == null ? null : Long.valueOf(header);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ResponseEntity<Map<String, Object>> sinSesion() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", 401,
                "mensaje", "Sesión no válida"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> buscarPorEmail(@PathVariable String email) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @Valid @RequestBody Usuario usuario,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoSolicitante) {

        // El registro es público: sin sesión de administrador solo se pueden
        // crear cuentas de cliente o artesano (si no, cualquiera podría
        // registrarse como ADMINISTRADOR o GERENCIA).
        boolean esAdmin = "ADMINISTRADOR".equalsIgnoreCase(tipoSolicitante);
        boolean tipoPublico = usuario.getTipoActor() == TipoActor.CLIENTE
                || usuario.getTipoActor() == TipoActor.ARTESANO;

        if (!esAdmin && !tipoPublico) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of(
                    "status", 403,
                    "mensaje", "Solo un administrador puede crear este tipo de cuenta"));
        }

        Usuario creado = usuarioService.crear(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
