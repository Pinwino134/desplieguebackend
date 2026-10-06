package com.pinwino.microservice.pagosservice.repository;

import com.pinwino.microservice.pagosservice.model.ClienteMetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteMetodoPagoRepository extends JpaRepository<ClienteMetodoPago, Long> {
    List<ClienteMetodoPago> findByClienteId(Long clienteId);
}
