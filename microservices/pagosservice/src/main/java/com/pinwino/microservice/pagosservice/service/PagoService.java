package com.pinwino.microservice.pagosservice.service;

import com.pinwino.microservice.pagosservice.model.Comprobante;
import com.pinwino.microservice.pagosservice.model.EstadoPago;
import com.pinwino.microservice.pagosservice.model.Pago;
import com.pinwino.microservice.pagosservice.model.Reembolso;
import java.util.List;

public interface PagoService {
    List<Pago> listarTodos();
    List<Pago> listarPorCliente(Long clienteId);
    Pago buscarPorId(Long id);
    Pago buscarPorPedido(Long pedidoId);
    // Se dispara al escuchar el evento PedidoCreado desde ventas_db
    Pago procesar(Long pedidoId, Long clienteId, Long metodoPagoId, java.math.BigDecimal monto);
    // El resultado de la pasarela llega como callback/webhook y actualiza el estado
    Pago actualizarEstado(Long id, EstadoPago estado, String referenciaTransaccion);
    Comprobante emitirComprobante(Long pagoId, Comprobante comprobante);
    Reembolso solicitarReembolso(Long pagoId, Reembolso reembolso);
}
