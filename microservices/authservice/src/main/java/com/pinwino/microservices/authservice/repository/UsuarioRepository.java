package com.pinwino.microservices.authservice.repository;

import com.pinwino.microservices.authservice.model.TipoActor;
import com.pinwino.microservices.authservice.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByTipoActor(TipoActor tipoActor);
}
