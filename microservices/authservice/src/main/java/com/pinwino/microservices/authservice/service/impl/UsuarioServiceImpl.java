package com.pinwino.microservices.authservice.service.impl;

import com.pinwino.microservices.authservice.exception.AccountLockedException;
import com.pinwino.microservices.authservice.exception.BusinessException;
import com.pinwino.microservices.authservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.authservice.model.EstadoUsuario;
import com.pinwino.microservices.authservice.model.TipoActor;
import com.pinwino.microservices.authservice.model.Usuario;
import com.pinwino.microservices.authservice.repository.UsuarioRepository;
import com.pinwino.microservices.authservice.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // =========================================================
    // LISTAR TODOS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // =========================================================
    // LISTAR POR TIPO
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorTipo(TipoActor tipoActor) {
        return usuarioRepository.findByTipoActor(tipoActor);
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario", id)
                );
    }

    // =========================================================
    // BUSCAR POR EMAIL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario con email " + email + " no encontrado"
                        )
                );
    }

    // =========================================================
    // CREAR USUARIO
    // =========================================================

    @Override
    @Transactional
    public Usuario crear(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {

            throw new BusinessException(
                    "Ya existe un usuario registrado con el email "
                            + usuario.getEmail()
            );
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    // =========================================================
    // ACTUALIZAR USUARIO
    // =========================================================

    @Override
    @Transactional
    public Usuario actualizar(Long id, Usuario datos) {

        Usuario existente = buscarPorId(id);

        existente.setNombres(datos.getNombres());
        existente.setApellidos(datos.getApellidos());
        existente.setEstado(datos.getEstado());
        existente.setTipoActor(datos.getTipoActor());

        if (datos.getPassword() != null
                && !datos.getPassword().isBlank()) {

            existente.setPassword(passwordEncoder.encode(datos.getPassword()));
        }

        return usuarioRepository.save(existente);
    }

    // =========================================================
    // ELIMINAR USUARIO
    // =========================================================

    @Override
    @Transactional
    public void eliminar(Long id) {

        usuarioRepository.delete(buscarPorId(id));
    }

    // =========================================================
    // AUTENTICAR USUARIO
    // =========================================================
    //
    // IMPORTANTE:
    //
    // BusinessException y AccountLockedException normalmente
    // provocan rollback porque son RuntimeException.
    //
    // Aquí indicamos que NO se haga rollback cuando ocurran
    // estas excepciones, para que los intentos fallidos sí
    // queden guardados en PostgreSQL.
    //
    // =========================================================

    @Override
    @Transactional(
            noRollbackFor = {
                    BusinessException.class,
                    AccountLockedException.class
            }
    )
    public Usuario autenticar(String email, String password) {

        // =====================================================
        // 1. BUSCAR USUARIO
        // =====================================================

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BusinessException(
                                "Correo electrónico o contraseña incorrectos."
                        )
                );

        LocalDateTime ahora = LocalDateTime.now();

        // =====================================================
        // 2. VERIFICAR SI LA CUENTA ESTÁ BLOQUEADA
        // =====================================================

        if (usuario.getBloqueadoHasta() != null) {

            // Todavía está bloqueada
            if (ahora.isBefore(usuario.getBloqueadoHasta())) {

                throw new AccountLockedException(
                        "La cuenta se encuentra bloqueada temporalmente. " +
                        "Intenta nuevamente después de " +
                        usuario.getBloqueadoHasta()
                );
            }

            // =================================================
            // El bloqueo ya terminó
            // =================================================

            usuario.setBloqueadoHasta(null);
            usuario.setIntentosFallidos(0);

            usuarioRepository.save(usuario);
        }

        // =====================================================
        // 3. VALIDAR CONTRASEÑA
        // =====================================================

        if (!passwordEncoder.matches(password, usuario.getPassword())) {

            // Aumentar contador
            int intentos = usuario.getIntentosFallidos() + 1;

            usuario.setIntentosFallidos(intentos);

            // =================================================
            // TERCER INTENTO FALLIDO
            // =================================================

            if (intentos >= 3) {

                LocalDateTime bloqueoHasta =
                        ahora.plusHours(1);

                usuario.setBloqueadoHasta(bloqueoHasta);

                usuarioRepository.save(usuario);

                throw new AccountLockedException(
                        "Has superado el número máximo de intentos. " +
                        "Tu cuenta ha sido bloqueada durante 1 hora."
                );
            }

            // =================================================
            // PRIMER O SEGUNDO INTENTO FALLIDO
            // =================================================

            usuarioRepository.save(usuario);

            throw new BusinessException(
                    "Correo electrónico o contraseña incorrectos. " +
                    "Intento " + intentos + " de 3."
            );
        }

        // =====================================================
        // 4. VERIFICAR ESTADO DEL USUARIO
        // =====================================================

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {

            throw new BusinessException(
                    "El usuario se encuentra inactivo."
            );
        }

        // =====================================================
        // 5. LOGIN CORRECTO
        // =====================================================

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuario.setUltimoAcceso(ahora);

        return usuarioRepository.save(usuario);
    }
}