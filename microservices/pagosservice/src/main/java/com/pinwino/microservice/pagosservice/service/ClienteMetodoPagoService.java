package com.pinwino.microservice.pagosservice.service;

import com.pinwino.microservice.pagosservice.model.ClienteMetodoPago;
import java.util.List;

public interface ClienteMetodoPagoService {
    List<ClienteMetodoPago> listarPorCliente(Long clienteId);
    ClienteMetodoPago buscarPorId(Long id);
    ClienteMetodoPago crear(ClienteMetodoPago metodo);
    void eliminar(Long id);
}
