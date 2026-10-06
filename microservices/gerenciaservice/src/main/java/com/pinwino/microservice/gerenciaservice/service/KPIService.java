package com.pinwino.microservice.gerenciaservice.service;

import com.pinwino.microservice.gerenciaservice.model.KPI;
import java.util.List;

public interface KPIService {
    List<KPI> listarTodos();
    List<KPI> listarPorPeriodo(String periodo);
    KPI buscarPorId(Long id);
    KPI crear(KPI kpi);
    KPI actualizar(Long id, KPI kpi);
    void eliminar(Long id);
}
