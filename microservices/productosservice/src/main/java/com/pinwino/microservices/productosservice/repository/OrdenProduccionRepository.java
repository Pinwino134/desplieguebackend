package com.pinwino.microservices.productosservice.repository;

import com.pinwino.microservices.productosservice.model.OrdenProduccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenProduccionRepository
        extends JpaRepository<OrdenProduccion, Long> {

    // Busca las producciones que pertenecen a un artesano.
    List<OrdenProduccion> findByArtesanoId(Long artesanoId);

    // Busca las producciones relacionadas con un pedido.
    List<OrdenProduccion> findByPedidoId(Long pedidoId);
}