package com.pinwino.microservices.ventasservice.service.impl;

import com.pinwino.microservices.ventasservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.ventasservice.model.Cliente;
import com.pinwino.microservices.ventasservice.model.Direccion;
import com.pinwino.microservices.ventasservice.repository.ClienteRepository;
import com.pinwino.microservices.ventasservice.repository.DireccionRepository;
import com.pinwino.microservices.ventasservice.service.DireccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DireccionServiceImpl implements DireccionService {
    private final DireccionRepository direccionRepository;
    private final ClienteRepository clienteRepository;

    @Override @Transactional(readOnly = true)
    public List<Direccion> listarPorCliente(Long clienteId) { return direccionRepository.findByClienteId(clienteId); }

    @Override @Transactional(readOnly = true)
    public Direccion buscarPorId(Long id) {
        return direccionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Direccion", id));
    }

    @Override @Transactional
    public Direccion crear(Long clienteId, Direccion direccion) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
        direccion.setCliente(cliente);
        return direccionRepository.save(direccion);
    }

    @Override @Transactional
    public Direccion actualizar(Long id, Direccion datos) {
        Direccion existente = buscarPorId(id);
        existente.setCalle(datos.getCalle());
        existente.setDistrito(datos.getDistrito());
        existente.setCiudad(datos.getCiudad());
        existente.setReferencia(datos.getReferencia());
        existente.setEsPrincipal(datos.getEsPrincipal());
        return direccionRepository.save(existente);
    }

    @Override @Transactional
    public void eliminar(Long id) { direccionRepository.delete(buscarPorId(id)); }
}
