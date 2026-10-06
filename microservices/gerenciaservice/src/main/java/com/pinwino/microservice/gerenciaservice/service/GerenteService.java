package com.pinwino.microservice.gerenciaservice.service;

import com.pinwino.microservice.gerenciaservice.model.Gerente;
import java.util.List;

public interface GerenteService {
    List<Gerente> listarTodos();
    Gerente buscarPorId(Long id);
    Gerente crear(Gerente gerente);
    Gerente actualizar(Long id, Gerente gerente);
    void eliminar(Long id);
}
