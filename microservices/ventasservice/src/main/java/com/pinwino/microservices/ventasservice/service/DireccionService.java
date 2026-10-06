package com.pinwino.microservices.ventasservice.service;

import com.pinwino.microservices.ventasservice.model.Direccion;
import java.util.List;

public interface DireccionService {
    List<Direccion> listarPorCliente(Long clienteId);
    Direccion buscarPorId(Long id);
    Direccion crear(Long clienteId, Direccion direccion);
    Direccion actualizar(Long id, Direccion direccion);
    void eliminar(Long id);
}
