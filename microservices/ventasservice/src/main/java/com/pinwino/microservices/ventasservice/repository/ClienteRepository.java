package com.pinwino.microservices.ventasservice.repository;

import com.pinwino.microservices.ventasservice.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findByUsuarioId(Long usuarioId);
    boolean existsByDni(String dni);
}
