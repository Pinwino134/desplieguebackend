package com.pinwino.microservices.authservice.config;

import com.pinwino.microservices.authservice.model.Usuario;
import com.pinwino.microservices.authservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Migración de arranque: hashea con BCrypt cualquier password que todavía
 * esté en texto plano (usuarios de prueba cargados antes de este cambio).
 * Idempotente: los que ya son BCrypt (prefijo $2a$/$2b$/$2y$) se dejan tal
 * cual, así que corre seguro en cada arranque.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordMigrationRunner implements CommandLineRunner {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        int migrados = 0;

        for (Usuario usuario : usuarios) {
            String passwordActual = usuario.getPassword();
            if (passwordActual != null && !BCRYPT_PATTERN.matcher(passwordActual).matches()) {
                usuario.setPassword(passwordEncoder.encode(passwordActual));
                usuarioRepository.save(usuario);
                migrados++;
            }
        }

        if (migrados > 0) {
            log.info("[PasswordMigrationRunner] Se hashearon {} password(s) que estaban en texto plano.", migrados);
        }
    }
}
