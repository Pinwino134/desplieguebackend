package com.pinwino.microservice.logisticaservice.controller;

import com.pinwino.microservice.logisticaservice.model.EstadoEnvio;
import com.pinwino.microservice.logisticaservice.model.Envio;
import com.pinwino.microservice.logisticaservice.model.SeguimientoEnvio;
import com.pinwino.microservice.logisticaservice.service.EnvioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/envios")
@RequiredArgsConstructor
public class EnvioController {
    private final EnvioService envioService;

    @GetMapping
    public ResponseEntity<List<Envio>> listar(@RequestParam(required = false) EstadoEnvio estado) {
        if (estado != null) return ResponseEntity.ok(envioService.listarPorEstado(estado));
        return ResponseEntity.ok(envioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Envio> buscarPorId(@PathVariable Long id) { return ResponseEntity.ok(envioService.buscarPorId(id)); }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Envio> buscarPorPedido(@PathVariable Long pedidoId) { return ResponseEntity.ok(envioService.buscarPorPedido(pedidoId)); }

    @PostMapping
    public ResponseEntity<Envio> crear(
            @RequestParam Long almacenId,
            @RequestParam(required = false) Long transportistaId,
            @Valid @RequestBody Envio envio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(envioService.crear(almacenId, transportistaId, envio));
    }

    @PostMapping("/{id}/seguimientos")
    public ResponseEntity<SeguimientoEnvio> agregarSeguimiento(@PathVariable Long id, @Valid @RequestBody SeguimientoEnvio seguimiento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(envioService.agregarSeguimiento(id, seguimiento));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Envio> cambiarEstado(@PathVariable Long id, @RequestParam EstadoEnvio estado) {
        return ResponseEntity.ok(envioService.cambiarEstado(id, estado));
    }
}
