package com.pinwino.microservices.ventasservice.repository;

import com.pinwino.microservices.ventasservice.model.EstadoPedido;
import com.pinwino.microservices.ventasservice.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByEstado(EstadoPedido estado);
}
