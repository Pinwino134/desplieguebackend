package com.pinwino.microservices.authservice;

import com.pinwino.microservices.authservice.exception.AccountLockedException;
import com.pinwino.microservices.authservice.exception.BusinessException;
import com.pinwino.microservices.authservice.model.EstadoUsuario;
import com.pinwino.microservices.authservice.model.TipoActor;
import com.pinwino.microservices.authservice.model.Usuario;
import com.pinwino.microservices.authservice.repository.UsuarioRepository;
import com.pinwino.microservices.authservice.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthserviceApplicationTests {

    @Mock
    private UsuarioRepository usuarioRepository;

    private PasswordEncoder passwordEncoder;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioServiceImpl(usuarioRepository, passwordEncoder);
    }

    @Test
    void crearUsuarioHasheaContrasenaConBCrypt() {
        Usuario nuevo = new Usuario();
        nuevo.setEmail("artesano@arteandino.pe");
        nuevo.setPassword("Secreta123!");
        nuevo.setNombres("Rosa");
        nuevo.setApellidos("Mamani");
        nuevo.setTipoActor(TipoActor.ARTESANO);

        when(usuarioRepository.existsByEmail(nuevo.getEmail())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario guardado = usuarioService.crear(nuevo);

        assertNotEquals("Secreta123!", guardado.getPassword());
        assertTrue(passwordEncoder.matches("Secreta123!", guardado.getPassword()));
    }

    @Test
    void autenticarBloqueaCuentaTrasTresIntentosFallidos() {
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("cliente@arteandino.pe");
        usuario.setPassword(passwordEncoder.encode("ClaveCorrecta1!"));
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setTipoActor(TipoActor.CLIENTE);
        usuario.setIntentosFallidos(2);

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(AccountLockedException.class,
                () -> usuarioService.autenticar(usuario.getEmail(), "ClaveIncorrecta"));
        assertNotNull(usuario.getBloqueadoHasta());
        assertEquals(3, usuario.getIntentosFallidos());
    }

    @Test
    void autenticarExitosoReiniciaIntentosFallidos() {
        Usuario usuario = new Usuario();
        usuario.setId(11L);
        usuario.setEmail("gerente@arteandino.pe");
        usuario.setPassword(passwordEncoder.encode("ClaveValida2026"));
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setTipoActor(TipoActor.GERENCIA);
        usuario.setIntentosFallidos(2);

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario autenticado = usuarioService.autenticar(usuario.getEmail(), "ClaveValida2026");

        assertEquals(0, autenticado.getIntentosFallidos());
        assertNotNull(autenticado.getUltimoAcceso());
    }

    @Test
    void autenticarRechazaUsuarioInactivo() {
        Usuario usuario = new Usuario();
        usuario.setEmail("inactivo@arteandino.pe");
        usuario.setPassword(passwordEncoder.encode("Clave123"));
        usuario.setEstado(EstadoUsuario.INACTIVO);

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(BusinessException.class,
                () -> usuarioService.autenticar(usuario.getEmail(), "Clave123"));
    }
}
