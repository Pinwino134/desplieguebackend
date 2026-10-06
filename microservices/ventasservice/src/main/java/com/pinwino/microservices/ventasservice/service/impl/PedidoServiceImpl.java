package com.pinwino.microservices.ventasservice.service.impl;

import com.pinwino.microservices.ventasservice.client.EnvioClient;
import com.pinwino.microservices.ventasservice.client.InventarioClient;
import com.pinwino.microservices.ventasservice.client.PagoClient;
import com.pinwino.microservices.ventasservice.client.ProductoClient;
import com.pinwino.microservices.ventasservice.client.dto.EnvioRequestDTO;
import com.pinwino.microservices.ventasservice.client.dto.InventarioDTO;
import com.pinwino.microservices.ventasservice.client.dto.PagoResponseDTO;
import com.pinwino.microservices.ventasservice.client.dto.ProductoDTO;
import com.pinwino.microservices.ventasservice.exception.BusinessException;
import com.pinwino.microservices.ventasservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.ventasservice.model.DetallePedido;
import com.pinwino.microservices.ventasservice.model.Direccion;
import com.pinwino.microservices.ventasservice.model.EstadoPedido;
import com.pinwino.microservices.ventasservice.model.Pedido;
import com.pinwino.microservices.ventasservice.repository.DetallePedidoRepository;
import com.pinwino.microservices.ventasservice.repository.DireccionRepository;
import com.pinwino.microservices.ventasservice.repository.PedidoRepository;
import com.pinwino.microservices.ventasservice.service.PedidoService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final DireccionRepository direccionRepository;

    private final ProductoClient productoClient;
    private final InventarioClient inventarioClient;
    private final PagoClient pagoClient;
    private final EnvioClient envioClient;

    @Value("${logistica.almacen-default-id}")
    private Long almacenDefaultId;

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() { return pedidoRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPorCliente(Long clienteId) { return pedidoRepository.findByClienteId(clienteId); }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPorEstado(EstadoPedido estado) { return pedidoRepository.findByEstado(estado); }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPorArtesano(Long artesanoId) {

        // Revisamos todos los pedidos registrados.
        return pedidoRepository.findAll()
                .stream()

                // Dejamos solo los pedidos que tengan al menos
                // un producto perteneciente al artesano.
                .filter(pedido -> pedido.getDetalles()
                        .stream()
                        .anyMatch(detalle -> {

                            try {
                                // Consultamos el producto usando su ID.
                                ProductoDTO producto =
                                        productoClient.buscarPorId(detalle.getProductoId());

                                // Comparamos el artesano del producto
                                // con el artesano que estamos buscando.
                                return producto.getArtesanoId() != null
                                        && producto.getArtesanoId().equals(artesanoId);

                            } catch (FeignException ex) {
                                // Si no se puede consultar un producto,
                                // ese detalle no se toma en cuenta.
                                return false;
                            }
                        }))
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }

    @Override
    @Transactional
    public Pedido crear(Long clienteId, Long direccionEnvioId, Pedido pedido) {
        pedido.setClienteId(clienteId);
        pedido.setDireccionEnvioId(direccionEnvioId);
        pedido.setTotal(BigDecimal.ZERO);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public DetallePedido agregarDetalle(Long pedidoId, DetallePedido detalle) {
        Pedido pedido = buscarPorId(pedidoId);

        // 1) Validar que el producto existe y esta activo (llamada sincrona a productos-service)
        ProductoDTO producto;
        try {
            producto = productoClient.buscarPorId(detalle.getProductoId());
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Producto", detalle.getProductoId());
        }
        if (!"ACTIVO".equals(producto.getEstado())) {
            throw new BusinessException("El producto " + producto.getNombre() + " no esta activo para la venta");
        }

        // 2) Validar stock disponible (llamada sincrona a logistica-service)
        List<InventarioDTO> inventarios = inventarioClient.listarPorProducto(detalle.getProductoId());
        int disponibleTotal = inventarios.stream().mapToInt(InventarioDTO::getCantidadDisponible).sum();
        if (disponibleTotal < detalle.getCantidad()) {
            throw new BusinessException("Stock insuficiente para " + producto.getNombre()
                    + " (disponible: " + disponibleTotal + ", solicitado: " + detalle.getCantidad() + ")");
        }

        detalle.setPedido(pedido);
        detalle.setPrecioUnitario(producto.getPrecioBase());
        detalle.setSubtotal(producto.getPrecioBase().multiply(BigDecimal.valueOf(detalle.getCantidad())));
        DetallePedido guardado = detallePedidoRepository.save(detalle);

        recalcularTotal(pedido);
        return guardado;
    }

    private void recalcularTotal(Pedido pedido) {
        var total = detallePedidoRepository.findByPedidoId(pedido.getId()).stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setTotal(total);
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido iniciarPago(Long pedidoId, Long metodoPagoId) {
        Pedido pedido = buscarPorId(pedidoId);
        if (pedido.getDetalles().isEmpty()) {
            throw new BusinessException("El pedido " + pedidoId + " no tiene detalles, no se puede pagar");
        }
        // Llamada sincrona a pagos-service: crea el registro de Pago en pagos_db
        PagoResponseDTO pago = pagoClient.procesar(pedido.getId(), pedido.getClienteId(), metodoPagoId, pedido.getTotal());
        // El estado real (APROBADO/RECHAZADO) llega despues via confirmarPago(),
        // que pagos-service invoca de vuelta cuando la pasarela responde.
        return pedido;
    }

    @Override
    @Transactional
    public Pedido confirmarPago(Long pedidoId, boolean aprobado) {
        Pedido pedido = buscarPorId(pedidoId);

        if (!aprobado) {
            pedido.setEstado(EstadoPedido.CANCELADO);
            return pedidoRepository.save(pedido);
        }

        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedidoRepository.save(pedido);

        // Pago aprobado: pedir a logistica-service que cree el envio (llamada sincrona)
        Direccion direccion = direccionRepository.findById(pedido.getDireccionEnvioId())
                .orElseThrow(() -> new ResourceNotFoundException("Direccion", pedido.getDireccionEnvioId()));
        String direccionTexto = direccion.getCalle() + ", " + direccion.getDistrito() + ", " + direccion.getCiudad();

        envioClient.crear(almacenDefaultId, new EnvioRequestDTO(pedido.getId(), direccionTexto));
        pedido.setEstado(EstadoPedido.EN_PROCESO);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = buscarPorId(id);
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void eliminar(Long id) { pedidoRepository.delete(buscarPorId(id)); }
}
