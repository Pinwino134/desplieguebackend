package com.pinwino.microservices.ventasservice.service.impl;

import com.pinwino.microservices.ventasservice.client.EnvioClient;
import com.pinwino.microservices.ventasservice.client.InventarioClient;
import com.pinwino.microservices.ventasservice.client.PagoClient;
import com.pinwino.microservices.ventasservice.client.ProductoClient;
import com.pinwino.microservices.ventasservice.client.dto.EnvioRequestDTO;
import com.pinwino.microservices.ventasservice.client.dto.InventarioDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    /**
     * Incluye tanto pedidos de catálogo con productos del artesano (1 sola llamada Feign, sin N+1)
     * como pedidos PERSONALIZADO asignados directamente al artesano.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPorArtesano(Long artesanoId) {
        Set<Long> productosDelArtesano = Collections.emptySet();
        try {
            List<ProductoDTO> productos = productoClient.listarPorArtesano(artesanoId);
            if (productos != null && !productos.isEmpty()) {
                productosDelArtesano = productos.stream()
                        .map(ProductoDTO::getId)
                        .collect(Collectors.toSet());
            }
        } catch (FeignException ex) {
            log.warn("No se pudieron consultar los productos del artesano {}: {}", artesanoId, ex.getMessage());
        }

        final Set<Long> idsProductos = productosDelArtesano;
        return pedidoRepository.findAll()
                .stream()
                .filter(pedido ->
                        (pedido.getArtesanoId() != null && pedido.getArtesanoId().equals(artesanoId))
                        || pedido.getDetalles().stream().anyMatch(d -> idsProductos.contains(d.getProductoId()))
                )
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
        pedido.setTipo("CATALOGO");
        pedido.setTotal(BigDecimal.ZERO);
        pedido.setPagado(false);
        pedido.setAvanceProduccion(0);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido crearPersonalizado(
            Long clienteId,
            Long artesanoId,
            String descripcion,
            BigDecimal presupuestoReferencial,
            Long direccionEnvioId) {

        if (artesanoId == null) {
            throw new BusinessException("Debes seleccionar un artesano para el pedido personalizado");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new BusinessException("Describe el diseño, medidas o materiales del pedido personalizado");
        }

        Pedido pedido = Pedido.builder()
                .clienteId(clienteId)
                .artesanoId(artesanoId)
                .direccionEnvioId(direccionEnvioId)
                .tipo("PERSONALIZADO")
                .estado(EstadoPedido.PENDIENTE_REVISION)
                .descripcionPersonalizada(descripcion.trim())
                .presupuestoReferencial(presupuestoReferencial)
                .total(presupuestoReferencial != null ? presupuestoReferencial : BigDecimal.ZERO)
                .pagado(false)
                .avanceProduccion(0)
                .build();

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido cotizarPersonalizado(
            Long pedidoId,
            boolean aceptar,
            BigDecimal precioCotizado,
            Integer diasEstimados,
            String notaArtesano) {

        Pedido pedido = buscarPorId(pedidoId);
        if (!"PERSONALIZADO".equalsIgnoreCase(pedido.getTipo())) {
            throw new BusinessException("Solo los pedidos personalizados requieren cotización");
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE_REVISION && pedido.getEstado() != EstadoPedido.COTIZADO) {
            throw new BusinessException("Este pedido ya no está pendiente de revisión/cotización");
        }

        if (!aceptar) {
            pedido.setEstado(EstadoPedido.RECHAZADO);
            pedido.setNotaArtesano(notaArtesano != null && !notaArtesano.isBlank()
                    ? notaArtesano.trim() : "El artesano no puede tomar este encargo actualmente.");
            return pedidoRepository.save(pedido);
        }

        if (precioCotizado == null || precioCotizado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Debes indicar un precio cotizado mayor a 0");
        }

        pedido.setPrecioCotizado(precioCotizado);
        pedido.setTotal(precioCotizado);
        pedido.setDiasEstimados(diasEstimados != null && diasEstimados > 0 ? diasEstimados : 7);
        pedido.setNotaArtesano(notaArtesano != null ? notaArtesano.trim() : "Cotización lista para aprobación del cliente.");
        pedido.setEstado(EstadoPedido.COTIZADO);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido responderCotizacion(Long pedidoId, boolean aceptar, Long direccionEnvioId) {
        Pedido pedido = buscarPorId(pedidoId);
        if (pedido.getEstado() != EstadoPedido.COTIZADO) {
            throw new BusinessException("El pedido no tiene una cotización pendiente de respuesta");
        }
        if (!aceptar) {
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedido.setMotivoCancelacion("El cliente rechazó la cotización del artesano.");
            pedido.setFechaCancelacion(LocalDateTime.now());
            return pedidoRepository.save(pedido);
        }
        if (direccionEnvioId != null) {
            pedido.setDireccionEnvioId(direccionEnvioId);
        }
        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public DetallePedido agregarDetalle(Long pedidoId, DetallePedido detalle) {
        Pedido pedido = buscarPorId(pedidoId);

        ProductoDTO producto;
        try {
            producto = productoClient.buscarPorId(detalle.getProductoId());
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Producto", detalle.getProductoId());
        }
        if (!"ACTIVO".equals(producto.getEstado())) {
            throw new BusinessException("El producto " + producto.getNombre() + " no esta activo para la venta");
        }

        // 2) Validar stock disponible directamente contra productosservice (fuente viva de stock)
        int disponibleProducto = producto.getStock() != null ? producto.getStock() : 0;
        if (disponibleProducto < detalle.getCantidad()) {
            throw new BusinessException("Stock insuficiente para " + producto.getNombre()
                    + " (disponible: " + disponibleProducto + ", solicitado: " + detalle.getCantidad() + ")");
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
        boolean esPersonalizado = "PERSONALIZADO".equalsIgnoreCase(pedido.getTipo());

        if (!esPersonalizado && pedido.getDetalles().isEmpty()) {
            throw new BusinessException("El pedido " + pedidoId + " no tiene detalles, no se puede pagar");
        }
        if (esPersonalizado && (pedido.getTotal() == null || pedido.getTotal().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new BusinessException("El pedido personalizado aún no tiene una cotización válida para pagar");
        }

        pagoClient.procesar(pedido.getId(), pedido.getClienteId(), metodoPagoId, pedido.getTotal());
        return pedido;
    }

    @Override
    @Transactional
    public Pedido confirmarPago(Long pedidoId, boolean aprobado) {
        Pedido pedido = buscarPorId(pedidoId);

        if (!aprobado) {
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedido.setMotivoCancelacion("Pago rechazado por la pasarela.");
            pedido.setFechaCancelacion(LocalDateTime.now());
            return pedidoRepository.save(pedido);
        }

        boolean yaPagadoAntes = Boolean.TRUE.equals(pedido.getPagado());
        pedido.setPagado(true);
        if (pedido.getNumeroBoleta() == null || pedido.getNumeroBoleta().isBlank()) {
            pedido.setNumeroBoleta("BOL-" + Year.now().getValue() + "-" + ThreadLocalRandom.current().nextInt(10000, 99999));
        }

        // Descontar stock real en productosservice cuando se confirma un pedido de catálogo por primera vez
        if (!yaPagadoAntes && pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
            for (DetallePedido det : pedido.getDetalles()) {
                try {
                    productoClient.ajustarStock(det.getProductoId(), -det.getCantidad());
                } catch (Exception ex) {
                    log.warn("No se pudo descontar stock para producto {}: {}", det.getProductoId(), ex.getMessage());
                }
            }
        }

        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedidoRepository.save(pedido);

        if (pedido.getDireccionEnvioId() != null) {
            direccionRepository.findById(pedido.getDireccionEnvioId()).ifPresent(direccion -> {
                try {
                    String direccionTexto = direccion.getCalle() + ", " + direccion.getDistrito() + ", " + direccion.getCiudad();
                    envioClient.crear(almacenDefaultId, new EnvioRequestDTO(pedido.getId(), direccionTexto));
                } catch (Exception ex) {
                    log.warn("No se pudo crear envío automático en logistica para pedido {}: {}", pedido.getId(), ex.getMessage());
                }
            });
        }

        // En pedidos de catálogo pasa a EN_PROCESO; en personalizados queda CONFIRMADO (pagado) listo para que el artesano inicie producción.
        if (!"PERSONALIZADO".equalsIgnoreCase(pedido.getTipo())) {
            pedido.setEstado(EstadoPedido.EN_PROCESO);
        }
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido actualizarProduccion(Long pedidoId, Integer avance, String nota) {
        Pedido pedido = buscarPorId(pedidoId);

        // Regla estricta del negocio: NO se puede iniciar producción si el cliente no ha pagado.
        if (!Boolean.TRUE.equals(pedido.getPagado())
                && pedido.getEstado() != EstadoPedido.CONFIRMADO
                && pedido.getEstado() != EstadoPedido.EN_PROCESO) {
            throw new BusinessException("No se puede iniciar ni actualizar la producción: el cliente aún no ha pagado este pedido.");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO || pedido.getEstado() == EstadoPedido.RECHAZADO) {
            throw new BusinessException("No se puede producir un pedido cancelado o rechazado.");
        }

        int pct = avance != null ? Math.max(0, Math.min(100, avance)) : 0;
        pedido.setAvanceProduccion(pct);
        if (nota != null && !nota.isBlank()) {
            pedido.setNotaArtesano(nota.trim());
        }
        if (pedido.getEstado() == EstadoPedido.CONFIRMADO) {
            pedido.setEstado(EstadoPedido.EN_PROCESO);
        }
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido registrarEnvio(Long pedidoId, String empresaEnvio, String codigoSeguimiento) {
        Pedido pedido = buscarPorId(pedidoId);
        if (!Boolean.TRUE.equals(pedido.getPagado())
                && pedido.getEstado() != EstadoPedido.CONFIRMADO
                && pedido.getEstado() != EstadoPedido.EN_PROCESO) {
            throw new BusinessException("No se puede enviar un pedido que aún no está pagado/confirmado.");
        }
        pedido.setEmpresaEnvio(empresaEnvio != null && !empresaEnvio.isBlank() ? empresaEnvio.trim() : "Olva Courier");
        pedido.setCodigoSeguimiento(codigoSeguimiento != null && !codigoSeguimiento.isBlank()
                ? codigoSeguimiento.trim() : "TRK-" + Year.now().getValue() + "-" + ThreadLocalRandom.current().nextInt(10000, 99999));
        pedido.setFechaEnvio(LocalDateTime.now());
        pedido.setAvanceProduccion(100);
        pedido.setEstado(EstadoPedido.ENVIADO);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido cancelarConReembolso(Long pedidoId, String motivo, String banco, String cuenta) {
        Pedido pedido = buscarPorId(pedidoId);

        if (pedido.getEstado() == EstadoPedido.ENVIADO || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new BusinessException("No es posible cancelar el pedido porque ya fue enviado o entregado.");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new BusinessException("Este pedido ya se encuentra cancelado.");
        }

        boolean estabaPagado = Boolean.TRUE.equals(pedido.getPagado())
                || pedido.getEstado() == EstadoPedido.CONFIRMADO
                || pedido.getEstado() == EstadoPedido.EN_PROCESO;

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setMotivoCancelacion(motivo != null && !motivo.isBlank() ? motivo.trim() : "Cancelado por solicitud del cliente.");
        pedido.setFechaCancelacion(LocalDateTime.now());

        if (estabaPagado && pedido.getTotal() != null && pedido.getTotal().compareTo(BigDecimal.ZERO) > 0) {
            pedido.setBancoReembolso(banco != null && !banco.isBlank() ? banco.trim() : "BCP");
            pedido.setCuentaReembolso(cuenta != null && !cuenta.isBlank() ? cuenta.trim() : "Cuenta registrada del cliente");
            pedido.setMontoReembolsado(pedido.getTotal());
            pedido.setReferenciaReembolso("DEV-BCO-" + Year.now().getValue() + "-" + ThreadLocalRandom.current().nextInt(100000, 999999));
        }

        // Si el pedido de catálogo ya había descontado stock, reponer el stock al cancelar
        if (estabaPagado && pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
            for (DetallePedido det : pedido.getDetalles()) {
                try {
                    productoClient.ajustarStock(det.getProductoId(), det.getCantidad());
                } catch (Exception ex) {
                    log.warn("No se pudo reponer stock para producto {}: {}", det.getProductoId(), ex.getMessage());
                }
            }
        }

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = buscarPorId(id);
        if (nuevoEstado == EstadoPedido.CANCELADO
                && (pedido.getEstado() == EstadoPedido.ENVIADO || pedido.getEstado() == EstadoPedido.ENTREGADO)) {
            throw new BusinessException("No se puede cancelar un pedido que ya fue enviado o entregado.");
        }
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void eliminar(Long id) { pedidoRepository.delete(buscarPorId(id)); }
}
