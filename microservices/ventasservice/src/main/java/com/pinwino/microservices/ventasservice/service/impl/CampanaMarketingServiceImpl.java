package com.pinwino.microservices.ventasservice.service.impl;

import com.pinwino.microservices.ventasservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.ventasservice.model.CampanaMarketing;
import com.pinwino.microservices.ventasservice.repository.CampanaMarketingRepository;
import com.pinwino.microservices.ventasservice.service.CampanaMarketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampanaMarketingServiceImpl implements CampanaMarketingService {
    private final CampanaMarketingRepository campanaRepository;

    @Override @Transactional(readOnly = true)
    public List<CampanaMarketing> listarTodas() { return campanaRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public CampanaMarketing buscarPorId(Long id) {
        return campanaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("CampanaMarketing", id));
    }

    @Override @Transactional
    public CampanaMarketing crear(CampanaMarketing campana) { return campanaRepository.save(campana); }

    @Override @Transactional
    public CampanaMarketing actualizar(Long id, CampanaMarketing datos) {
        CampanaMarketing existente = buscarPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setCanal(datos.getCanal());
        existente.setFechaInicio(datos.getFechaInicio());
        existente.setFechaFin(datos.getFechaFin());
        existente.setPresupuesto(datos.getPresupuesto());
        return campanaRepository.save(existente);
    }

    @Override @Transactional
    public void eliminar(Long id) { campanaRepository.delete(buscarPorId(id)); }
}
