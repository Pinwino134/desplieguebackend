package com.pinwino.microservice.logisticaservice.service;

import com.pinwino.microservice.logisticaservice.model.Inventario;
import com.pinwino.microservice.logisticaservice.model.MovimientoInventario;
import com.pinwino.microservice.logisticaservice.model.TipoMovimiento;
import java.util.List;

public interface InventarioService {
    List<Inventario> listarPorAlmacen(Long almacenId);
    List<Inventario> listarPorProducto(Long productoId);
    Inventario buscarPorId(Long id);
    Inventario crear(Long almacenId, Inventario inventario);
    // Registra un movimiento y ajusta cantidad_disponible; publica el evento StockActualizado (a implementar)
    MovimientoInventario registrarMovimiento(Long inventarioId, TipoMovimiento tipo, Integer cantidad, String motivo, Long usuarioResponsableId);
}
