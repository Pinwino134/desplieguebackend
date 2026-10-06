package com.pinwino.microservice.logisticaservice.service;

import com.pinwino.microservice.logisticaservice.model.Almacen;
import java.util.List;

public interface AlmacenService {
    List<Almacen> listarTodos();
    Almacen buscarPorId(Long id);
    Almacen crear(Almacen almacen);
    Almacen actualizar(Long id, Almacen almacen);
    void eliminar(Long id);
}
