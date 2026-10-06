package com.pinwino.microservice.gerenciaservice.service.impl;

import com.pinwino.microservice.gerenciaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.gerenciaservice.model.Gerente;
import com.pinwino.microservice.gerenciaservice.repository.GerenteRepository;
import com.pinwino.microservice.gerenciaservice.service.GerenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GerenteServiceImpl implements GerenteService {
    private final GerenteRepository gerenteRepository;

    @Override @Transactional(readOnly = true)
    public List<Gerente> listarTodos() { return gerenteRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public Gerente buscarPorId(Long id) {
        return gerenteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Gerente", id));
    }

    @Override @Transactional
    public Gerente crear(Gerente gerente) { return gerenteRepository.save(gerente); }

    @Override @Transactional
    public Gerente actualizar(Long id, Gerente datos) {
        Gerente existente = buscarPorId(id);
        existente.setNombres(datos.getNombres());
        existente.setApellidos(datos.getApellidos());
        existente.setCargo(datos.getCargo());
        return gerenteRepository.save(existente);
    }

    @Override @Transactional
    public void eliminar(Long id) { gerenteRepository.delete(buscarPorId(id)); }
}
