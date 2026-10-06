package com.pinwino.microservice.gerenciaservice.service;

import com.pinwino.microservice.gerenciaservice.model.DecisionEstrategica;
import java.util.List;

public interface DecisionEstrategicaService {
    List<DecisionEstrategica> listarPorGerente(Long gerenteId);
    DecisionEstrategica buscarPorId(Long id);
    DecisionEstrategica crear(Long gerenteId, DecisionEstrategica decision);
    void eliminar(Long id);
}
