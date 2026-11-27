package com.pinwino.microservices.ventasservice.service;

import com.pinwino.microservices.ventasservice.model.DetallePedido;
import com.pinwino.microservices.ventasservice.model.EstadoPedido;
import com.pinwino.microservices.ventasservice.model.Pedido;

import java.math.BigDecimal;
import java.util.List;

public interface PedidoService {
    List<Pedido> listarTodos();
    List<Pedido> listarPorCliente(Long clienteId);
    List<Pedido> listarPorEstado(EstadoPedido estado);
    List<Pedido> listarPorArtesano(Long artesanoId);
    Pedido buscarPorId(Long id);
    Pedido crear(Long clienteId, Long direccionEnvioId, Pedido pedido);
    Pedido crearPersonalizado(Long clienteId, Long artesanoId, String descripcion, BigDecimal presupuestoReferencial, Long direccionEnvioId);
    Pedido cotizarPersonalizado(Long pedidoId, boolean aceptar, BigDecimal precioCotizado, Integer diasEstimados, String notaArtesano);
    Pedido responderCotizacion(Long pedidoId, boolean aceptar, Long direccionEnvioId);
    DetallePedido agregarDetalle(Long pedidoId, DetallePedido detalle);
    Pedido iniciarPago(Long pedidoId, Long metodoPagoId);
    Pedido confirmarPago(Long pedidoId, boolean aprobado);
    Pedido actualizarProduccion(Long pedidoId, Integer avance, String nota);
    Pedido registrarEnvio(Long pedidoId, String empresaEnvio, String codigoSeguimiento);
    Pedido cancelarConReembolso(Long pedidoId, String motivo, String banco, String cuenta);
    Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado);
    void eliminar(Long id);
}
