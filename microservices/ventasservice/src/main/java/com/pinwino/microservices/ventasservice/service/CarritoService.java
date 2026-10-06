package com.pinwino.microservices.ventasservice.service;

import com.pinwino.microservices.ventasservice.model.Carrito;
import com.pinwino.microservices.ventasservice.model.CarritoItem;

public interface CarritoService {
    Carrito obtenerOCrearPorCliente(Long clienteId);
    Carrito buscarPorId(Long id);
    CarritoItem agregarItem(Long carritoId, CarritoItem item);
    CarritoItem actualizarItem(Long itemId, CarritoItem datos);
    void quitarItem(Long itemId);
    void vaciar(Long carritoId);
}
