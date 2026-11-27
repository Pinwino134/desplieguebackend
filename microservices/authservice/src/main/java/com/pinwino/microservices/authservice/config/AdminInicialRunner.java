package com.pinwino.microservices.authservice.config;

import com.pinwino.microservices.authservice.model.EstadoUsuario;
import com.pinwino.microservices.authservice.model.TipoActor;
import com.pinwino.microservices.authservice.model.Usuario;
import com.pinwino.microservices.authservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el primer administrador al arrancar, solo si todavía no existe
 * ninguno. El registro público ya no permite crear cuentas ADMINISTRADOR,
 * así que esta es la forma de tener el primero.
 *
 * Se configura con las variables de entorno ADMIN_EMAIL y ADMIN_PASSWORD
 * (en el .env junto al docker-compose.yml). Si faltan, no hace nada.
 * Idempotente: si ya hay un administrador, no toca nada.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInicialRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String email;

    @Value("${ADMIN_PASSWORD:}")
    private String password;

    @Value("${ADMIN_NOMBRES:Administrador}")
    private String nombres;

    @Value("${ADMIN_APELLIDOS:Arte Andino}")
    private String apellidos;

    @Override
    public void run(String... args) {
        if (!usuarioRepository.findByTipoActor(TipoActor.ADMINISTRADOR).isEmpty()) {
            return;
        }

        if (email.isBlank() || password.isBlank()) {
            log.warn("[AdminInicialRunner] No hay ningún administrador y faltan "
                    + "ADMIN_EMAIL / ADMIN_PASSWORD para crear el primero.");
            return;
        }

        if (usuarioRepository.existsByEmail(email)) {
            log.warn("[AdminInicialRunner] El correo {} ya existe con otro rol; "
                    + "no se creó el administrador.", email);
            return;
        }

        usuarioRepository.save(Usuario.builder()
                .nombres(nombres)
                .apellidos(apellidos)
                .email(email.trim())
                .password(passwordEncoder.encode(password))
                .tipoActor(TipoActor.ADMINISTRADOR)
                .estado(EstadoUsuario.ACTIVO)
                .build());

        log.info("[AdminInicialRunner] Administrador inicial creado: {}", email);
    }
}
