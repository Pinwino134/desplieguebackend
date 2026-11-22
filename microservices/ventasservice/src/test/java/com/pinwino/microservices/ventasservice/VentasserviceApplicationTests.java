package com.pinwino.microservices.ventasservice;

import com.pinwino.microservices.ventasservice.client.EnvioClient;
import com.pinwino.microservices.ventasservice.client.InventarioClient;
import com.pinwino.microservices.ventasservice.client.PagoClient;
import com.pinwino.microservices.ventasservice.client.ProductoClient;
import com.pinwino.microservices.ventasservice.client.dto.ProductoDTO;
import com.pinwino.microservices.ventasservice.model.DetallePedido;
import com.pinwino.microservices.ventasservice.model.EstadoPedido;
import com.pinwino.microservices.ventasservice.model.Pedido;
import com.pinwino.microservices.ventasservice.repository.DetallePedidoRepository;
import com.pinwino.microservices.ventasservice.repository.DireccionRepository;
import com.pinwino.microservices.ventasservice.repository.PedidoRepository;
import com.pinwino.microservices.ventasservice.service.impl.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentasserviceApplicationTests {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private DetallePedidoRepository detallePedidoRepository;
    @Mock private DireccionRepository direccionRepository;
    @Mock private ProductoClient productoClient;
    @Mock private InventarioClient inventarioClient;
    @Mock private PagoClient pagoClient;
    @Mock private EnvioClient envioClient;

    private PedidoServiceImpl pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoServiceImpl(
                pedidoRepository, detallePedidoRepository, direccionRepository,
                productoClient, inventarioClient, pagoClient, envioClient
        );
    }

    @Test
    void listarPorArtesanoUsaUnaSolaLlamadaFeignSinNMasUno() {
        ProductoDTO prodArtesano = new ProductoDTO();
        prodArtesano.setId(100L);
        prodArtesano.setArtesanoId(7L);

        DetallePedido detArtesano = new DetallePedido();
        detArtesano.setProductoId(100L);
        Pedido pedidoPropio = new Pedido();
        pedidoPropio.setId(1L);
        pedidoPropio.setDetalles(List.of(detArtesano));

        DetallePedido detOtro = new DetallePedido();
        detOtro.setProductoId(200L);
        Pedido pedidoAjeno = new Pedido();
        pedidoAjeno.setId(2L);
        pedidoAjeno.setDetalles(List.of(detOtro));

        when(productoClient.listarPorArtesano(7L)).thenReturn(List.of(prodArtesano));
        when(pedidoRepository.findAll()).thenReturn(List.of(pedidoPropio, pedidoAjeno));

        List<Pedido> resultado = pedidoService.listarPorArtesano(7L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.getFirst().getId());
        verify(productoClient, times(1)).listarPorArtesano(7L);
        verify(productoClient, never()).buscarPorId(any());
    }

    @Test
    void confirmarPagoRechazadoCancelaPedido() {
        Pedido pedido = new Pedido();
        pedido.setId(5L);
        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);

        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido actualizado = pedidoService.confirmarPago(5L, false);

        assertEquals(EstadoPedido.CANCELADO, actualizado.getEstado());
        verify(envioClient, never()).crear(any(), any());
    }
}
