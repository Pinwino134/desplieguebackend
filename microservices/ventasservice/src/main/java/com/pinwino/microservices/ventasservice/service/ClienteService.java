package com.pinwino.microservices.ventasservice.service;

import com.pinwino.microservices.ventasservice.model.Cliente;
import java.util.List;

public interface ClienteService {
    List<Cliente> listarTodos();
    Cliente buscarPorId(Long id);
    Cliente buscarPorUsuarioId(Long usuarioId);
    Cliente crear(Cliente cliente);
    Cliente actualizar(Long id, Cliente cliente);
    void eliminar(Long id);
}
