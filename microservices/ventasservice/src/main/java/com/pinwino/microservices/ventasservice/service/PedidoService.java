package com.pinwino.microservices.ventasservice.service;

import com.pinwino.microservices.ventasservice.model.DetallePedido;
import com.pinwino.microservices.ventasservice.model.EstadoPedido;
import com.pinwino.microservices.ventasservice.model.Pedido;
import java.util.List;

public interface PedidoService {
    List<Pedido> listarTodos();
    List<Pedido> listarPorCliente(Long clienteId);
    List<Pedido> listarPorEstado(EstadoPedido estado);
 // Lista los pedidos que contienen productos de un artesano.
    List<Pedido> listarPorArtesano(Long artesanoId);
    Pedido buscarPorId(Long id);
    Pedido crear(Long clienteId, Long direccionEnvioId, Pedido pedido);
    Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado);
    // Valida contra productos-service (existe, precio) y logistica-service (stock) via Feign
    DetallePedido agregarDetalle(Long pedidoId, DetallePedido detalle);
    // Dispara el pago en pagos-service via Feign
    Pedido iniciarPago(Long pedidoId, Long metodoPagoId);
    // Lo llama pagos-service (Feign) cuando el pago queda APROBADO o RECHAZADO
    Pedido confirmarPago(Long pedidoId, boolean aprobado);
    void eliminar(Long id);
}
