package com.pinwino.microservice.gerenciaservice.controller;

import com.pinwino.microservice.gerenciaservice.model.KPI;
import com.pinwino.microservice.gerenciaservice.service.KPIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kpis")
@RequiredArgsConstructor
public class KPIController {
    private final KPIService kpiService;

    @GetMapping
    public ResponseEntity<List<KPI>> listar(@RequestParam(required = false) String periodo) {
        if (periodo != null) return ResponseEntity.ok(kpiService.listarPorPeriodo(periodo));
        return ResponseEntity.ok(kpiService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KPI> buscarPorId(@PathVariable Long id) { return ResponseEntity.ok(kpiService.buscarPorId(id)); }

    @PostMapping
    public ResponseEntity<KPI> crear(@Valid @RequestBody KPI kpi) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kpiService.crear(kpi));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KPI> actualizar(@PathVariable Long id, @Valid @RequestBody KPI kpi) {
        return ResponseEntity.ok(kpiService.actualizar(id, kpi));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        kpiService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
