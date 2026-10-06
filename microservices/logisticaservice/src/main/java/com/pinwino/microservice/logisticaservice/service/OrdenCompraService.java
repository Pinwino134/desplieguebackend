package com.pinwino.microservice.logisticaservice.service;

import com.pinwino.microservice.logisticaservice.model.DetalleOrdenCompra;
import com.pinwino.microservice.logisticaservice.model.EstadoOrdenCompra;
import com.pinwino.microservice.logisticaservice.model.OrdenCompra;
import java.util.List;

public interface OrdenCompraService {
    List<OrdenCompra> listarTodas();
    List<OrdenCompra> listarPorProveedor(Long proveedorId);
    OrdenCompra buscarPorId(Long id);
    OrdenCompra crear(Long proveedorId, OrdenCompra orden);
    DetalleOrdenCompra agregarDetalle(Long ordenId, DetalleOrdenCompra detalle);
    OrdenCompra cambiarEstado(Long id, EstadoOrdenCompra estado);
}
