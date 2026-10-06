package com.pinwino.microservice.logisticaservice.service.impl;

import com.pinwino.microservice.logisticaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.logisticaservice.model.Transportista;
import com.pinwino.microservice.logisticaservice.repository.TransportistaRepository;
import com.pinwino.microservice.logisticaservice.service.TransportistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportistaServiceImpl implements TransportistaService {
    private final TransportistaRepository transportistaRepository;

    @Override @Transactional(readOnly = true)
    public List<Transportista> listarTodos() { return transportistaRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public Transportista buscarPorId(Long id) {
        return transportistaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transportista", id));
    }

    @Override @Transactional
    public Transportista crear(Transportista transportista) { return transportistaRepository.save(transportista); }

    @Override @Transactional
    public Transportista actualizar(Long id, Transportista datos) {
        Transportista existente = buscarPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setEmpresa(datos.getEmpresa());
        existente.setContacto(datos.getContacto());
        return transportistaRepository.save(existente);
    }

    @Override @Transactional
    public void eliminar(Long id) { transportistaRepository.delete(buscarPorId(id)); }
}
