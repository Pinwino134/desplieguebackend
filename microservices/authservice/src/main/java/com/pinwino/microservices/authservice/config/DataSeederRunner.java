package com.pinwino.microservices.authservice.config;

import com.pinwino.microservices.authservice.model.EstadoUsuario;
import com.pinwino.microservices.authservice.model.TipoActor;
import com.pinwino.microservices.authservice.model.Usuario;
import com.pinwino.microservices.authservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea automáticamente 4 cuentas base (uno por cada actor del negocio)
 * únicamente cuando la tabla usuario está vacía (entornos locales nuevos o DRP).
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataSeederRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        String hashDemo = passwordEncoder.encode("Arte123!");

        crearSiNoExiste("admin@arteandino.pe", "Carlos", "Admin", TipoActor.ADMINISTRADOR, hashDemo);
        crearSiNoExiste("gerencia@arteandino.pe", "Lucia", "Gerencia", TipoActor.GERENCIA, hashDemo);
        crearSiNoExiste("artesano@arteandino.pe", "Rosa", "Mamani", TipoActor.ARTESANO, hashDemo);
        crearSiNoExiste("cliente@arteandino.pe", "Maria", "Lopez", TipoActor.CLIENTE, hashDemo);

        log.info("[DataSeederRunner] Se crearon las 4 cuentas iniciales de demostración en authdb.");
    }

    private void crearSiNoExiste(String email, String nombres, String apellidos, TipoActor rol, String hash) {
        if (!usuarioRepository.existsByEmail(email)) {
            Usuario u = Usuario.builder()
                    .email(email)
                    .nombres(nombres)
                    .apellidos(apellidos)
                    .tipoActor(rol)
                    .estado(EstadoUsuario.ACTIVO)
                    .password(hash)
                    .intentosFallidos(0)
                    .build();
            usuarioRepository.save(u);
        }
    }
}
