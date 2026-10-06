package com.pinwino.microservices.productosservice.service;

import com.pinwino.microservices.productosservice.model.EstadoProduccion;
import com.pinwino.microservices.productosservice.model.OrdenProduccion;

import java.util.List;

public interface OrdenProduccionService {

    // Lista las órdenes de producción de un artesano.
    List<OrdenProduccion> listarPorArtesano(Long artesanoId);

    // Busca una orden de producción por su ID.
    OrdenProduccion buscarPorId(Long id);

    // Crea una nueva orden de producción.
    OrdenProduccion crear(
            Long productoId,
            OrdenProduccion ordenProduccion
    );

    // Actualiza el porcentaje de avance de la producción.
    OrdenProduccion actualizarAvance(
            Long id,
            Integer avance
    );

    // Cambia el estado de la producción.
    OrdenProduccion cambiarEstado(
            Long id,
            EstadoProduccion estado
    );
}