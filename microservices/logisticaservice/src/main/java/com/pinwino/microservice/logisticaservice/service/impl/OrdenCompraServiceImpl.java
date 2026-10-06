package com.pinwino.microservice.logisticaservice.service.impl;

import com.pinwino.microservice.logisticaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.logisticaservice.model.*;
import com.pinwino.microservice.logisticaservice.repository.DetalleOrdenCompraRepository;
import com.pinwino.microservice.logisticaservice.repository.OrdenCompraRepository;
import com.pinwino.microservice.logisticaservice.repository.ProveedorRepository;
import com.pinwino.microservice.logisticaservice.service.OrdenCompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenCompraServiceImpl implements OrdenCompraService {
    private final OrdenCompraRepository ordenCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final DetalleOrdenCompraRepository detalleOrdenCompraRepository;

    @Override @Transactional(readOnly = true)
    public List<OrdenCompra> listarTodas() { return ordenCompraRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<OrdenCompra> listarPorProveedor(Long proveedorId) { return ordenCompraRepository.findByProveedorId(proveedorId); }

    @Override @Transactional(readOnly = true)
    public OrdenCompra buscarPorId(Long id) {
        return ordenCompraRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("OrdenCompra", id));
    }

    @Override @Transactional
    public OrdenCompra crear(Long proveedorId, OrdenCompra orden) {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", proveedorId));
        orden.setProveedor(proveedor);
        return ordenCompraRepository.save(orden);
    }

    @Override @Transactional
    public DetalleOrdenCompra agregarDetalle(Long ordenId, DetalleOrdenCompra detalle) {
        OrdenCompra orden = buscarPorId(ordenId);
        detalle.setOrdenCompra(orden);
        DetalleOrdenCompra guardado = detalleOrdenCompraRepository.save(detalle);
        recalcularTotal(orden);
        return guardado;
    }

    private void recalcularTotal(OrdenCompra orden) {
        var total = detalleOrdenCompraRepository.findByOrdenCompraId(orden.getId()).stream()
                .map(d -> d.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(d.getCantidad())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        orden.setTotal(total);
        ordenCompraRepository.save(orden);
    }

    @Override @Transactional
    public OrdenCompra cambiarEstado(Long id, EstadoOrdenCompra estado) {
        OrdenCompra orden = buscarPorId(id);
        orden.setEstado(estado);
        return ordenCompraRepository.save(orden);
    }
}
