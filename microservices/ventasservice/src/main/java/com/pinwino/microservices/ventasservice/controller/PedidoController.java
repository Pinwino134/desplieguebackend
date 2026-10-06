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

        // Si se envía el ID del artesano,
        // mostramos solamente sus pedidos.
        if (artesanoId != null) {
            return ResponseEntity.ok(
                    pedidoService.listarPorArtesano(artesanoId)
            );
        }

        // Permite consultar los pedidos de un cliente.
        if (clienteId != null) {
            return ResponseEntity.ok(
                    pedidoService.listarPorCliente(clienteId)
            );
        }

        // Permite filtrar los pedidos por su estado.
        if (estado != null) {
            return ResponseEntity.ok(
                    pedidoService.listarPorEstado(estado)
            );
        }

        // Si no se envía ningún filtro,
        // mostramos todos los pedidos.
        return ResponseEntity.ok(
                pedidoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) { return ResponseEntity.ok(pedidoService.buscarPorId(id)); }

    @PostMapping
    public ResponseEntity<Pedido> crear(
            @RequestParam Long clienteId,
            @RequestParam Long direccionEnvioId,
            @Valid @RequestBody Pedido pedido) {
        Pedido creado = pedidoService.crear(clienteId, direccionEnvioId, pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/{pedidoId}/detalles")
    public ResponseEntity<DetallePedido> agregarDetalle(@PathVariable Long pedidoId, @Valid @RequestBody DetallePedido detalle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.agregarDetalle(pedidoId, detalle));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Pedido> cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado));
    }

    @PostMapping("/{id}/pago")
    public ResponseEntity<Pedido> iniciarPago(@PathVariable Long id, @RequestParam(required = false) Long metodoPagoId) {
        return ResponseEntity.ok(pedidoService.iniciarPago(id, metodoPagoId));
    }

    // Uso interno: lo invoca pagos-service via Feign cuando el pago se aprueba/rechaza.
    // En un entorno real esto deberia protegerse (red interna / token de servicio a servicio).
    @PatchMapping("/{id}/confirmar-pago")
    public ResponseEntity<Pedido> confirmarPago(@PathVariable Long id, @RequestParam boolean aprobado) {
        return ResponseEntity.ok(pedidoService.confirmarPago(id, aprobado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
