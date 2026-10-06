package com.pinwino.microservice.pagosservice.service.impl;

import com.pinwino.microservice.pagosservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.pagosservice.model.ClienteMetodoPago;
import com.pinwino.microservice.pagosservice.repository.ClienteMetodoPagoRepository;
import com.pinwino.microservice.pagosservice.service.ClienteMetodoPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteMetodoPagoServiceImpl implements ClienteMetodoPagoService {
    private final ClienteMetodoPagoRepository metodoPagoRepository;

    @Override @Transactional(readOnly = true)
    public List<ClienteMetodoPago> listarPorCliente(Long clienteId) { return metodoPagoRepository.findByClienteId(clienteId); }

    @Override @Transactional(readOnly = true)
    public ClienteMetodoPago buscarPorId(Long id) {
        return metodoPagoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ClienteMetodoPago", id));
    }

    @Override @Transactional
    public ClienteMetodoPago crear(ClienteMetodoPago metodo) { return metodoPagoRepository.save(metodo); }

    @Override @Transactional
    public void eliminar(Long id) { metodoPagoRepository.delete(buscarPorId(id)); }
}
