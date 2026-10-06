package com.pinwino.microservice.gerenciaservice.service;

import com.pinwino.microservice.gerenciaservice.model.Presupuesto;
import java.util.List;

public interface PresupuestoService {
    List<Presupuesto> listarTodos();
    List<Presupuesto> listarPorPeriodo(String periodo);
    Presupuesto buscarPorId(Long id);
    Presupuesto crear(Presupuesto presupuesto);
    Presupuesto actualizar(Long id, Presupuesto presupuesto);
    void eliminar(Long id);
}
