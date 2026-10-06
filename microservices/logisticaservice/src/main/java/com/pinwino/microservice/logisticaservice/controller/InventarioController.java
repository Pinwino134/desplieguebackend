package com.pinwino.microservice.logisticaservice.controller;

import com.pinwino.microservice.logisticaservice.model.Inventario;
import com.pinwino.microservice.logisticaservice.model.MovimientoInventario;
import com.pinwino.microservice.logisticaservice.model.TipoMovimiento;
import com.pinwino.microservice.logisticaservice.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InventarioController {
    private final InventarioService inventarioService;

    @GetMapping("/almacenes/{almacenId}/inventario")
    public ResponseEntity<List<Inventario>> listarPorAlmacen(@PathVariable Long almacenId) {
        return ResponseEntity.ok(inventarioService.listarPorAlmacen(almacenId));
    }

    @GetMapping("/inventario")
    public ResponseEntity<List<Inventario>> listarPorProducto(@RequestParam Long productoId) {
        return ResponseEntity.ok(inventarioService.listarPorProducto(productoId));
    }

    @GetMapping("/inventario/{id}")
    public ResponseEntity<Inventario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.buscarPorId(id));
    }

    @PostMapping("/almacenes/{almacenId}/inventario")
    public ResponseEntity<Inventario> crear(@PathVariable Long almacenId, @Valid @RequestBody Inventario inventario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.crear(almacenId, inventario));
    }

    @PostMapping("/inventario/{id}/movimientos")
    public ResponseEntity<MovimientoInventario> registrarMovimiento(
            @PathVariable Long id,
            @RequestParam TipoMovimiento tipo,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) Long usuarioResponsableId) {
        MovimientoInventario movimiento = inventarioService.registrarMovimiento(id, tipo, cantidad, motivo, usuarioResponsableId);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimiento);
    }
}
