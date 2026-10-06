package com.pinwino.microservices.ventasservice.service.impl;

import com.pinwino.microservices.ventasservice.client.UsuarioClient;
import com.pinwino.microservices.ventasservice.exception.BusinessException;
import com.pinwino.microservices.ventasservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.ventasservice.model.Cliente;
import com.pinwino.microservices.ventasservice.repository.ClienteRepository;
import com.pinwino.microservices.ventasservice.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clienteRepository;
    private final UsuarioClient usuarioClient;

    @Override @Transactional(readOnly = true)
    public List<Cliente> listarTodos() { return clienteRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    @Override @Transactional
    public Cliente buscarPorUsuarioId(Long usuarioId) {
        return clienteRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearDesdeUsuario(usuarioId));
    }

    // Primera vez que un usuario (CLIENTE) toca algo de ventasservice
    // (ej. su carrito): le creamos su registro de Cliente automáticamente,
    // trayendo nombres/apellidos reales de authservice. El DNI queda nulo
    // hasta que lo complete (ej. al pagar).
    private Cliente crearDesdeUsuario(Long usuarioId) {
        UsuarioClient.UsuarioDTO usuario = usuarioClient.buscarPorId(usuarioId);

        Cliente nuevo = Cliente.builder()
                .usuarioId(usuarioId)
                .nombres(usuario.nombres())
                .apellidos(usuario.apellidos())
                .build();

        return clienteRepository.save(nuevo);
    }

    @Override @Transactional
    public Cliente crear(Cliente cliente) {
        if (cliente.getDni() != null && !cliente.getDni().isBlank()
                && clienteRepository.existsByDni(cliente.getDni())) {
            throw new BusinessException("Ya existe un cliente con DNI " + cliente.getDni());
        }
        return clienteRepository.save(cliente);
    }

    @Override @Transactional
    public Cliente actualizar(Long id, Cliente datos) {
        Cliente existente = buscarPorId(id);
        existente.setNombres(datos.getNombres());
        existente.setApellidos(datos.getApellidos());
        existente.setTelefono(datos.getTelefono());
        return clienteRepository.save(existente);
    }

    @Override @Transactional
    public void eliminar(Long id) { clienteRepository.delete(buscarPorId(id)); }
}
