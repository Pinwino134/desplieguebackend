package com.pinwino.microservice.logisticaservice.service;

import com.pinwino.microservice.logisticaservice.model.Transportista;
import java.util.List;

public interface TransportistaService {
    List<Transportista> listarTodos();
    Transportista buscarPorId(Long id);
    Transportista crear(Transportista transportista);
    Transportista actualizar(Long id, Transportista transportista);
    void eliminar(Long id);
}
