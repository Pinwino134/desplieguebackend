package com.pinwino.microservice.gerenciaservice.service.impl;

import com.pinwino.microservice.gerenciaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.gerenciaservice.model.KPI;
import com.pinwino.microservice.gerenciaservice.repository.KPIRepository;
import com.pinwino.microservice.gerenciaservice.service.KPIService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KPIServiceImpl implements KPIService {
    private final KPIRepository kpiRepository;

    @Override @Transactional(readOnly = true)
    public List<KPI> listarTodos() { return kpiRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<KPI> listarPorPeriodo(String periodo) { return kpiRepository.findByPeriodo(periodo); }

    @Override @Transactional(readOnly = true)
    public KPI buscarPorId(Long id) {
        return kpiRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("KPI", id));
    }

    @Override @Transactional
    public KPI crear(KPI kpi) { return kpiRepository.save(kpi); }

    @Override @Transactional
    public KPI actualizar(Long id, KPI datos) {
        KPI existente = buscarPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setValor(datos.getValor());
        existente.setMeta(datos.getMeta());
        existente.setPeriodo(datos.getPeriodo());
        return kpiRepository.save(existente);
    }

    @Override @Transactional
    public void eliminar(Long id) { kpiRepository.delete(buscarPorId(id)); }
}
