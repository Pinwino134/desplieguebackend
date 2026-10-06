package com.pinwino.microservices.ventasservice.service;

import com.pinwino.microservices.ventasservice.model.Resena;
import java.util.List;

public interface ResenaService {
    List<Resena> listarPorProducto(Long productoId);
    Resena crear(Resena resena);
    void eliminar(Long id);
}
