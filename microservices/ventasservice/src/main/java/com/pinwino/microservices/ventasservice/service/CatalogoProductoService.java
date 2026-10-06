package com.pinwino.microservices.ventasservice.service;

import com.pinwino.microservices.ventasservice.model.CatalogoProducto;
import java.util.List;

public interface CatalogoProductoService {
    List<CatalogoProducto> listarTodos();
    List<CatalogoProducto> listarPorCategoria(String categoria);
    CatalogoProducto buscarPorId(Long productoId);
    // Estos dos metodos los invoca normalmente un listener de eventos
    // (ProductoCreado/Actualizado/StockActualizado), no un usuario final.
    CatalogoProducto upsert(CatalogoProducto producto);
    void eliminar(Long productoId);
}
