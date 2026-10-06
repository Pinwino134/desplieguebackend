package com.pinwino.microservices.productosservice.service.impl;

import com.pinwino.microservices.productosservice.exception.BusinessException;
import com.pinwino.microservices.productosservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.productosservice.model.EstadoProduccion;
import com.pinwino.microservices.productosservice.model.OrdenProduccion;
import com.pinwino.microservices.productosservice.model.Producto;
import com.pinwino.microservices.productosservice.repository.OrdenProduccionRepository;
import com.pinwino.microservices.productosservice.repository.ProductoRepository;
import com.pinwino.microservices.productosservice.service.OrdenProduccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenProduccionServiceImpl
        implements OrdenProduccionService {

    private final OrdenProduccionRepository ordenProduccionRepository;
    private final ProductoRepository productoRepository;

    // Lista las órdenes de producción de un artesano.
    @Override
    @Transactional(readOnly = true)
    public List<OrdenProduccion> listarPorArtesano(Long artesanoId) {

        return ordenProduccionRepository
                .findByArtesanoId(artesanoId);
    }

    // Busca una orden de producción por su ID.
    @Override
    @Transactional(readOnly = true)
    public OrdenProduccion buscarPorId(Long id) {

        return ordenProduccionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Orden de producción",
                                id
                        )
                );
    }

    // Crea una nueva orden de producción.
    @Override
    @Transactional
    public OrdenProduccion crear(
            Long productoId,
            OrdenProduccion ordenProduccion) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Producto",
                                productoId
                        )
                );

        ordenProduccion.setProducto(producto);

        // Una nueva producción siempre comienza con 0%.
        ordenProduccion.setAvance(0);

        // Una nueva producción comienza pendiente.
        ordenProduccion.setEstado(
                EstadoProduccion.PENDIENTE
        );

        // Registramos la fecha de inicio.
        if (ordenProduccion.getFechaInicio() == null) {
            ordenProduccion.setFechaInicio(LocalDate.now());
        }

        return ordenProduccionRepository.save(ordenProduccion);
    }

    // Actualiza el porcentaje de avance.
    @Override
    @Transactional
    public OrdenProduccion actualizarAvance(
            Long id,
            Integer avance) {

        OrdenProduccion orden = buscarPorId(id);

        if (avance == null || avance < 0 || avance > 100) {
            throw new BusinessException(
                    "El avance debe estar entre 0 y 100"
            );
        }

        orden.setAvance(avance);

        // 100% significa producción terminada.
        if (avance == 100) {

            orden.setEstado(
                    EstadoProduccion.TERMINADO
            );

        } else if (avance > 0) {

            orden.setEstado(
                    EstadoProduccion.EN_PROCESO
            );
        }

        return ordenProduccionRepository.save(orden);
    }

    // Cambia el estado de una producción.
    @Override
    @Transactional
    public OrdenProduccion cambiarEstado(
            Long id,
            EstadoProduccion estado) {

        OrdenProduccion orden = buscarPorId(id);

        if (estado == null) {
            throw new BusinessException(
                    "El estado de producción es obligatorio"
            );
        }

        orden.setEstado(estado);

        // Si se marca como terminado,
        // el avance debe ser 100%.
        if (estado == EstadoProduccion.TERMINADO) {
            orden.setAvance(100);
        }

        return ordenProduccionRepository.save(orden);
    }
}