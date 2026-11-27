package com.pinwino.microservices.ventasservice.controller;

import com.pinwino.microservices.ventasservice.model.DetallePedido;
import com.pinwino.microservices.ventasservice.model.EstadoPedido;
import com.pinwino.microservices.ventasservice.model.Pedido;
import com.pinwino.microservices.ventasservice.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<Pedido>> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long artesanoId,
            @RequestParam(required = false) EstadoPedido estado) {

        if (artesanoId != null) {
            return ResponseEntity.ok(pedidoService.listarPorArtesano(artesanoId));
        }
        if (clienteId != null) {
            return ResponseEntity.ok(pedidoService.listarPorCliente(clienteId));
        }
        if (estado != null) {
            return ResponseEntity.ok(pedidoService.listarPorEstado(estado));
        }
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(
            @RequestParam Long clienteId,
            @RequestParam Long direccionEnvioId,
            @Valid @RequestBody Pedido pedido) {
        Pedido creado = pedidoService.crear(clienteId, direccionEnvioId, pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /** Cliente solicita un pedido personalizado con cotización al artesano. */
    @PostMapping("/personalizado")
    public ResponseEntity<Pedido> crearPersonalizado(@RequestBody PedidoPersonalizadoRequest req) {
        Pedido creado = pedidoService.crearPersonalizado(
                req.clienteId(),
                req.artesanoId(),
                req.descripcion(),
                req.presupuestoReferencial(),
                req.direccionEnvioId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /** Artesano evalúa la solicitud personalizada: cotiza (aceptar=true) o rechaza (aceptar=false). */
    @PatchMapping("/{id}/cotizar")
    public ResponseEntity<Pedido> cotizarPersonalizado(
            @PathVariable Long id,
            @RequestBody CotizacionRequest req) {
        return ResponseEntity.ok(pedidoService.cotizarPersonalizado(
                id, req.aceptar(), req.precioCotizado(), req.diasEstimados(), req.notaArtesano()
        ));
    }

    /** Cliente acepta (pasa a PENDIENTE_PAGO) o rechaza (CANCELADO) la cotización del artesano. */
    @PatchMapping("/{id}/responder-cotizacion")
    public ResponseEntity<Pedido> responderCotizacion(
            @PathVariable Long id,
            @RequestBody RespuestaCotizacionRequest req) {
        return ResponseEntity.ok(pedidoService.responderCotizacion(id, req.aceptar(), req.direccionEnvioId()));
    }

    @PostMapping("/{pedidoId}/detalles")
    public ResponseEntity<DetallePedido> agregarDetalle(
            @PathVariable Long pedidoId,
            @Valid @RequestBody DetallePedido detalle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.agregarDetalle(pedidoId, detalle));
    }

    // PATCH y POST: el frontend y los clientes Feign no usan el mismo método, y así ninguno recibe 405
    @RequestMapping(value = "/{id}/estado", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<Pedido> cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado));
    }

    @PostMapping("/{id}/pago")
    public ResponseEntity<Pedido> iniciarPago(
            @PathVariable Long id,
            @RequestParam(required = false) Long metodoPagoId) {
        return ResponseEntity.ok(pedidoService.iniciarPago(id, metodoPagoId));
    }

    // PATCH (frontend) y POST (Feign desde pagos-service cuando el pago se aprueba/rechaza)
    @RequestMapping(value = "/{id}/confirmar-pago", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<Pedido> confirmarPago(@PathVariable Long id, @RequestParam boolean aprobado) {
        return ResponseEntity.ok(pedidoService.confirmarPago(id, aprobado));
    }

    /** Artesano registra avance de producción (0-100%) SOLO si el pedido está pagado. */
    @PatchMapping("/{id}/produccion")
    public ResponseEntity<Pedido> actualizarProduccion(
            @PathVariable Long id,
            @RequestBody ProduccionRequest req) {
        return ResponseEntity.ok(pedidoService.actualizarProduccion(id, req.avance(), req.nota()));
    }

    /** Artesano o Administrador registra el envío (pasa a ENVIADO y bloquea cancelaciones). */
    @PatchMapping("/{id}/enviar")
    public ResponseEntity<Pedido> registrarEnvio(
            @PathVariable Long id,
            @RequestBody(required = false) EnvioDatosRequest req) {
        String empresa = req != null ? req.empresaEnvio() : null;
        String tracking = req != null ? req.codigoSeguimiento() : null;
        return ResponseEntity.ok(pedidoService.registrarEnvio(id, empresa, tracking));
    }

    /** Cancela un pedido (si no fue ENVIADO/ENTREGADO) y genera reembolso bancario simulado si ya estaba pagado. */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Pedido> cancelarConReembolso(
            @PathVariable Long id,
            @RequestBody(required = false) CancelacionRequest req) {
        String motivo = req != null ? req.motivo() : null;
        String banco = req != null ? req.banco() : null;
        String cuenta = req != null ? req.cuenta() : null;
        return ResponseEntity.ok(pedidoService.cancelarConReembolso(id, motivo, banco, cuenta));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    public record PedidoPersonalizadoRequest(
            Long clienteId,
            Long artesanoId,
            String descripcion,
            BigDecimal presupuestoReferencial,
            Long direccionEnvioId
    ) {}

    public record CotizacionRequest(
            boolean aceptar,
            BigDecimal precioCotizado,
            Integer diasEstimados,
            String notaArtesano
    ) {}

    public record RespuestaCotizacionRequest(boolean aceptar, Long direccionEnvioId) {}

    public record ProduccionRequest(Integer avance, String nota) {}

    public record EnvioDatosRequest(String empresaEnvio, String codigoSeguimiento) {}

    public record CancelacionRequest(String motivo, String banco, String cuenta) {}
}
