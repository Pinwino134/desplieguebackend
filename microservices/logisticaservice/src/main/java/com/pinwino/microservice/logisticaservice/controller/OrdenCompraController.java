package com.pinwino.microservice.logisticaservice.controller;

import com.pinwino.microservice.logisticaservice.model.DetalleOrdenCompra;
import com.pinwino.microservice.logisticaservice.model.EstadoOrdenCompra;
import com.pinwino.microservice.logisticaservice.model.OrdenCompra;
import com.pinwino.microservice.logisticaservice.service.OrdenCompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes-compra")
@RequiredArgsConstructor
public class OrdenCompraController {
    private final OrdenCompraService ordenCompraService;

    @GetMapping
    public ResponseEntity<List<OrdenCompra>> listar(@RequestParam(required = false) Long proveedorId) {
        if (proveedorId != null) return ResponseEntity.ok(ordenCompraService.listarPorProveedor(proveedorId));
        return ResponseEntity.ok(ordenCompraService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompra> buscarPorId(@PathVariable Long id) { return ResponseEntity.ok(ordenCompraService.buscarPorId(id)); }

    @PostMapping
    public ResponseEntity<OrdenCompra> crear(@RequestParam Long proveedorId, @Valid @RequestBody OrdenCompra orden) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenCompraService.crear(proveedorId, orden));
    }

    @PostMapping("/{id}/detalles")
    public ResponseEntity<DetalleOrdenCompra> agregarDetalle(@PathVariable Long id, @Valid @RequestBody DetalleOrdenCompra detalle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenCompraService.agregarDetalle(id, detalle));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenCompra> cambiarEstado(@PathVariable Long id, @RequestParam EstadoOrdenCompra estado) {
        return ResponseEntity.ok(ordenCompraService.cambiarEstado(id, estado));
    }
}
