package com.pinwino.microservices.authservice.controller;

import com.pinwino.microservices.authservice.dto.LoginResponse;
import com.pinwino.microservices.authservice.model.Usuario;
import com.pinwino.microservices.authservice.security.JwtService;
import com.pinwino.microservices.authservice.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {

        // autenticar() ya valida credenciales, intentos fallidos y
        // bloqueo de cuenta; si llega a devolver el usuario es porque
        // el login es válido.
        Usuario usuario = usuarioService.autenticar(
                request.email(),
                request.password()
        );

        String token = jwtService.generarToken(usuario);

        LoginResponse respuesta = LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .email(usuario.getEmail())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .tipoActor(usuario.getTipoActor())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    public record LoginRequest(
            String email,
            String password
    ) {}
}