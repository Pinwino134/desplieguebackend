package com.pinwino.microservice.pagosservice.repository;

import com.pinwino.microservice.pagosservice.model.EstadoPago;
import com.pinwino.microservice.pagosservice.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByPedidoId(Long pedidoId);
    List<Pago> findByClienteId(Long clienteId);
    List<Pago> findByEstado(EstadoPago estado);
}
