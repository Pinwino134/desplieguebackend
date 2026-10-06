package com.pinwino.microservice.gerenciaservice.controller;

import com.pinwino.microservice.gerenciaservice.model.Presupuesto;
import com.pinwino.microservice.gerenciaservice.service.PresupuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/presupuestos")
@RequiredArgsConstructor
public class PresupuestoController {
    private final PresupuestoService presupuestoService;

    @GetMapping
    public ResponseEntity<List<Presupuesto>> listar(@RequestParam(required = false) String periodo) {
        if (periodo != null) return ResponseEntity.ok(presupuestoService.listarPorPeriodo(periodo));
        return ResponseEntity.ok(presupuestoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Presupuesto> buscarPorId(@PathVariable Long id) { return ResponseEntity.ok(presupuestoService.buscarPorId(id)); }

    @PostMapping
    public ResponseEntity<Presupuesto> crear(@Valid @RequestBody Presupuesto presupuesto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.crear(presupuesto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Presupuesto> actualizar(@PathVariable Long id, @Valid @RequestBody Presupuesto presupuesto) {
        return ResponseEntity.ok(presupuestoService.actualizar(id, presupuesto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        presupuestoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
