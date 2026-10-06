package com.pinwino.microservice.gerenciaservice.service.impl;

import com.pinwino.microservice.gerenciaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.gerenciaservice.model.Presupuesto;
import com.pinwino.microservice.gerenciaservice.repository.PresupuestoRepository;
import com.pinwino.microservice.gerenciaservice.service.PresupuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PresupuestoServiceImpl implements PresupuestoService {
    private final PresupuestoRepository presupuestoRepository;

    @Override @Transactional(readOnly = true)
    public List<Presupuesto> listarTodos() { return presupuestoRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<Presupuesto> listarPorPeriodo(String periodo) { return presupuestoRepository.findByPeriodo(periodo); }

    @Override @Transactional(readOnly = true)
    public Presupuesto buscarPorId(Long id) {
        return presupuestoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Presupuesto", id));
    }

    @Override @Transactional
    public Presupuesto crear(Presupuesto presupuesto) { return presupuestoRepository.save(presupuesto); }

    @Override @Transactional
    public Presupuesto actualizar(Long id, Presupuesto datos) {
        Presupuesto existente = buscarPorId(id);
        existente.setArea(datos.getArea());
        existente.setPeriodo(datos.getPeriodo());
        existente.setMontoAsignado(datos.getMontoAsignado());
        existente.setMontoEjecutado(datos.getMontoEjecutado());
        return presupuestoRepository.save(existente);
    }

    @Override @Transactional
    public void eliminar(Long id) { presupuestoRepository.delete(buscarPorId(id)); }
}
