package com.pinwino.microservice.logisticaservice.service.impl;

import com.pinwino.microservice.logisticaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.logisticaservice.model.Almacen;
import com.pinwino.microservice.logisticaservice.repository.AlmacenRepository;
import com.pinwino.microservice.logisticaservice.service.AlmacenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlmacenServiceImpl implements AlmacenService {
    private final AlmacenRepository almacenRepository;

    @Override @Transactional(readOnly = true)
    public List<Almacen> listarTodos() { return almacenRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public Almacen buscarPorId(Long id) {
        return almacenRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Almacen", id));
    }

    @Override @Transactional
    public Almacen crear(Almacen almacen) { return almacenRepository.save(almacen); }

    @Override @Transactional
    public Almacen actualizar(Long id, Almacen datos) {
        Almacen existente = buscarPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setUbicacion(datos.getUbicacion());
        existente.setCapacidad(datos.getCapacidad());
        return almacenRepository.save(existente);
    }

    @Override @Transactional
    public void eliminar(Long id) { almacenRepository.delete(buscarPorId(id)); }
}
