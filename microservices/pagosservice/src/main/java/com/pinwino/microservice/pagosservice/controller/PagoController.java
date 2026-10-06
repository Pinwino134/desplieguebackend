package com.pinwino.microservice.pagosservice.controller;

import com.pinwino.microservice.pagosservice.model.Comprobante;
import com.pinwino.microservice.pagosservice.model.EstadoPago;
import com.pinwino.microservice.pagosservice.model.Pago;
import com.pinwino.microservice.pagosservice.model.Reembolso;
import com.pinwino.microservice.pagosservice.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {
    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<Pago>> listar(@RequestParam(required = false) Long clienteId) {
        if (clienteId != null) return ResponseEntity.ok(pagoService.listarPorCliente(clienteId));
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pago> buscarPorId(@PathVariable Long id) { return ResponseEntity.ok(pagoService.buscarPorId(id)); }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Pago> buscarPorPedido(@PathVariable Long pedidoId) { return ResponseEntity.ok(pagoService.buscarPorPedido(pedidoId)); }

    @PostMapping
    public ResponseEntity<Pago> procesar(
            @RequestParam Long pedidoId,
            @RequestParam Long clienteId,
            @RequestParam(required = false) Long metodoPagoId,
            @RequestParam BigDecimal monto) {
        Pago pago = pagoService.procesar(pedidoId, clienteId, metodoPagoId, monto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pago);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Pago> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPago estado,
            @RequestParam(required = false) String referenciaTransaccion) {
        return ResponseEntity.ok(pagoService.actualizarEstado(id, estado, referenciaTransaccion));
    }

    @PostMapping("/{id}/comprobante")
    public ResponseEntity<Comprobante> emitirComprobante(@PathVariable Long id, @Valid @RequestBody Comprobante comprobante) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.emitirComprobante(id, comprobante));
    }

    @PostMapping("/{id}/reembolsos")
    public ResponseEntity<Reembolso> solicitarReembolso(@PathVariable Long id, @Valid @RequestBody Reembolso reembolso) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.solicitarReembolso(id, reembolso));
    }
}
