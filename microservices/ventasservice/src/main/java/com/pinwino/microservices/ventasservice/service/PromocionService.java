package com.pinwino.microservices.ventasservice.service;

import com.pinwino.microservices.ventasservice.model.ProductoPromocion;
import com.pinwino.microservices.ventasservice.model.Promocion;
import java.util.List;

public interface PromocionService {
    List<Promocion> listarPorCampana(Long campanaId);
    Promocion buscarPorId(Long id);
    Promocion crear(Long campanaId, Promocion promocion);
    void eliminar(Long id);
    ProductoPromocion aplicarAProducto(Long promocionId, Long productoId);
    void quitarDeProducto(Long productoPromocionId);
}
