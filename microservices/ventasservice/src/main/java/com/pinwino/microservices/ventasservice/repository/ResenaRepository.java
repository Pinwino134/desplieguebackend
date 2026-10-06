package com.pinwino.microservices.ventasservice.repository;

import com.pinwino.microservices.ventasservice.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByProductoId(Long productoId);
    List<Resena> findByClienteId(Long clienteId);
}
