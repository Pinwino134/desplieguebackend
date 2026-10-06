package com.pinwino.microservice.gerenciaservice.service.impl;

import com.pinwino.microservice.gerenciaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.gerenciaservice.model.DecisionEstrategica;
import com.pinwino.microservice.gerenciaservice.model.Gerente;
import com.pinwino.microservice.gerenciaservice.repository.DecisionEstrategicaRepository;
import com.pinwino.microservice.gerenciaservice.repository.GerenteRepository;
import com.pinwino.microservice.gerenciaservice.service.DecisionEstrategicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DecisionEstrategicaServiceImpl implements DecisionEstrategicaService {
    private final DecisionEstrategicaRepository decisionRepository;
    private final GerenteRepository gerenteRepository;

    @Override @Transactional(readOnly = true)
    public List<DecisionEstrategica> listarPorGerente(Long gerenteId) { return decisionRepository.findByGerenteId(gerenteId); }

    @Override @Transactional(readOnly = true)
    public DecisionEstrategica buscarPorId(Long id) {
        return decisionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("DecisionEstrategica", id));
    }

    @Override @Transactional
    public DecisionEstrategica crear(Long gerenteId, DecisionEstrategica decision) {
        Gerente gerente = gerenteRepository.findById(gerenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Gerente", gerenteId));
        decision.setGerente(gerente);
        return decisionRepository.save(decision);
    }

    @Override @Transactional
    public void eliminar(Long id) { decisionRepository.delete(buscarPorId(id)); }
}
