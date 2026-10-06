package com.pinwino.microservice.pagosservice.service.impl;

import com.pinwino.microservice.pagosservice.client.PedidoClient;
import com.pinwino.microservice.pagosservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.pagosservice.model.*;
import com.pinwino.microservice.pagosservice.repository.ClienteMetodoPagoRepository;
import com.pinwino.microservice.pagosservice.repository.ComprobanteRepository;
import com.pinwino.microservice.pagosservice.repository.PagoRepository;
import com.pinwino.microservice.pagosservice.repository.ReembolsoRepository;
import com.pinwino.microservice.pagosservice.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {
    private final PagoRepository pagoRepository;
    private final ClienteMetodoPagoRepository metodoPagoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final ReembolsoRepository reembolsoRepository;
    private final PedidoClient pedidoClient;

    @Override @Transactional(readOnly = true)
    public List<Pago> listarTodos() { return pagoRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<Pago> listarPorCliente(Long clienteId) { return pagoRepository.findByClienteId(clienteId); }

    @Override @Transactional(readOnly = true)
    public Pago buscarPorId(Long id) {
        return pagoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pago", id));
    }

    @Override @Transactional(readOnly = true)
    public Pago buscarPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("No existe pago para el pedido " + pedidoId));
    }

    @Override @Transactional
    public Pago procesar(Long pedidoId, Long clienteId, Long metodoPagoId, java.math.BigDecimal monto) {
        Pago pago = Pago.builder()
                .pedidoId(pedidoId)
                .clienteId(clienteId)
                .monto(monto)
                .estado(EstadoPago.PENDIENTE)
                .build();
        if (metodoPagoId != null) {
            ClienteMetodoPago metodo = metodoPagoRepository.findById(metodoPagoId)
                    .orElseThrow(() -> new ResourceNotFoundException("ClienteMetodoPago", metodoPagoId));
            pago.setMetodoPago(metodo);
        }
        // TODO: llamar a la pasarela real (Culqi/Niubiz/MercadoPago) aqui o via webhook async
        return pagoRepository.save(pago);
    }

    @Override @Transactional
    public Pago actualizarEstado(Long id, EstadoPago estado, String referenciaTransaccion) {
        Pago pago = buscarPorId(id);
        pago.setEstado(estado);
        if (referenciaTransaccion != null) {
            pago.setReferenciaTransaccion(referenciaTransaccion);
        }
        Pago guardado = pagoRepository.save(pago);

        // Avisar de vuelta a ventas-service (llamada sincrona via Feign) para que
        // confirme o cancele el pedido segun el resultado del pago.
        if (estado == EstadoPago.APROBADO || estado == EstadoPago.RECHAZADO) {
            boolean aprobado = estado == EstadoPago.APROBADO;
            try {
                pedidoClient.confirmarPago(guardado.getPedidoId(), aprobado);
            } catch (feign.FeignException ex) {
                // Si ventas-service no responde, el pago queda registrado igual;
                // aqui conviene loguear y tener un job de reconciliacion (o migrar a eventos).
                throw new com.pinwino.microservice.pagosservice.exception.BusinessException(
                        "Pago actualizado pero no se pudo notificar a ventas-service: " + ex.getMessage());
            }
        }
        return guardado;
    }

    @Override @Transactional
    public Comprobante emitirComprobante(Long pagoId, Comprobante comprobante) {
        Pago pago = buscarPorId(pagoId);
        comprobante.setPago(pago);
        if (comprobante.getMontoTotal() == null) {
            comprobante.setMontoTotal(pago.getMonto());
        }
        return comprobanteRepository.save(comprobante);
    }

    @Override @Transactional
    public Reembolso solicitarReembolso(Long pagoId, Reembolso reembolso) {
        Pago pago = buscarPorId(pagoId);
        reembolso.setPago(pago);
        return reembolsoRepository.save(reembolso);
    }
}
