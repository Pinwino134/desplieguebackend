package com.pinwino.microservice.logisticaservice.service.impl;

import com.pinwino.microservice.logisticaservice.exception.BusinessException;
import com.pinwino.microservice.logisticaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.logisticaservice.model.*;
import com.pinwino.microservice.logisticaservice.repository.AlmacenRepository;
import com.pinwino.microservice.logisticaservice.repository.InventarioRepository;
import com.pinwino.microservice.logisticaservice.repository.MovimientoInventarioRepository;
import com.pinwino.microservice.logisticaservice.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {
    private final InventarioRepository inventarioRepository;
    private final AlmacenRepository almacenRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Override @Transactional(readOnly = true)
    public List<Inventario> listarPorAlmacen(Long almacenId) { return inventarioRepository.findByAlmacenId(almacenId); }

    @Override @Transactional(readOnly = true)
    public List<Inventario> listarPorProducto(Long productoId) { return inventarioRepository.findByProductoId(productoId); }

    @Override @Transactional(readOnly = true)
    public Inventario buscarPorId(Long id) {
        return inventarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventario", id));
    }

    @Override @Transactional
    public Inventario crear(Long almacenId, Inventario inventario) {
        Almacen almacen = almacenRepository.findById(almacenId)
                .orElseThrow(() -> new ResourceNotFoundException("Almacen", almacenId));
        inventario.setAlmacen(almacen);
        return inventarioRepository.save(inventario);
    }

    @Override @Transactional
    public MovimientoInventario registrarMovimiento(Long inventarioId, TipoMovimiento tipo, Integer cantidad, String motivo, Long usuarioResponsableId) {
        Inventario inventario = buscarPorId(inventarioId);

        switch (tipo) {
            case ENTRADA, DEVOLUCION -> inventario.setCantidadDisponible(inventario.getCantidadDisponible() + cantidad);
            case SALIDA -> {
                if (inventario.getCantidadDisponible() < cantidad) {
                    throw new BusinessException("Stock insuficiente en el inventario " + inventarioId);
                }
                inventario.setCantidadDisponible(inventario.getCantidadDisponible() - cantidad);
            }
            case AJUSTE -> inventario.setCantidadDisponible(cantidad);
        }
        inventarioRepository.save(inventario);

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .inventario(inventario)
                .tipo(tipo)
                .cantidad(cantidad)
                .motivo(motivo)
                .usuarioResponsableId(usuarioResponsableId)
                .build();
        // TODO: publicar evento StockActualizado al bus (Kafka/RabbitMQ) para que ventas_db actualice su catalogo
        return movimientoInventarioRepository.save(movimiento);
    }
}
