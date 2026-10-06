package com.pinwino.microservice.logisticaservice.service.impl;

import com.pinwino.microservice.logisticaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.logisticaservice.model.*;
import com.pinwino.microservice.logisticaservice.repository.AlmacenRepository;
import com.pinwino.microservice.logisticaservice.repository.EnvioRepository;
import com.pinwino.microservice.logisticaservice.repository.SeguimientoEnvioRepository;
import com.pinwino.microservice.logisticaservice.repository.TransportistaRepository;
import com.pinwino.microservice.logisticaservice.service.EnvioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvioServiceImpl implements EnvioService {
    private final EnvioRepository envioRepository;
    private final AlmacenRepository almacenRepository;
    private final TransportistaRepository transportistaRepository;
    private final SeguimientoEnvioRepository seguimientoEnvioRepository;

    @Override @Transactional(readOnly = true)
    public List<Envio> listarTodos() { return envioRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<Envio> listarPorEstado(EstadoEnvio estado) { return envioRepository.findByEstado(estado); }

    @Override @Transactional(readOnly = true)
    public Envio buscarPorId(Long id) {
        return envioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Envio", id));
    }

    @Override @Transactional(readOnly = true)
    public Envio buscarPorPedido(Long pedidoId) {
        return envioRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("No existe envio para el pedido " + pedidoId));
    }

    @Override @Transactional
    public Envio crear(Long almacenId, Long transportistaId, Envio envio) {
        Almacen almacen = almacenRepository.findById(almacenId)
                .orElseThrow(() -> new ResourceNotFoundException("Almacen", almacenId));
        envio.setAlmacen(almacen);
        if (transportistaId != null) {
            Transportista transportista = transportistaRepository.findById(transportistaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transportista", transportistaId));
            envio.setTransportista(transportista);
        }
        return envioRepository.save(envio);
    }

    @Override @Transactional
    public SeguimientoEnvio agregarSeguimiento(Long envioId, SeguimientoEnvio seguimiento) {
        Envio envio = buscarPorId(envioId);
        seguimiento.setEnvio(envio);
        return seguimientoEnvioRepository.save(seguimiento);
    }

    @Override @Transactional
    public Envio cambiarEstado(Long id, EstadoEnvio estado) {
        Envio envio = buscarPorId(id);
        envio.setEstado(estado);
        return envioRepository.save(envio);
    }
}
