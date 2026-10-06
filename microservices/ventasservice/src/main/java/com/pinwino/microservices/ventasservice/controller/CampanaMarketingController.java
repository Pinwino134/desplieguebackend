package com.pinwino.microservices.ventasservice.controller;

import com.pinwino.microservices.ventasservice.model.CampanaMarketing;
import com.pinwino.microservices.ventasservice.service.CampanaMarketingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/campanas")
@RequiredArgsConstructor
public class CampanaMarketingController {
    private final CampanaMarketingService campanaMarketingService;

    @GetMapping
    public ResponseEntity<List<CampanaMarketing>> listar() { return ResponseEntity.ok(campanaMarketingService.listarTodas()); }

    @GetMapping("/{id}")
    public ResponseEntity<CampanaMarketing> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(campanaMarketingService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CampanaMarketing> crear(@Valid @RequestBody CampanaMarketing campana) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campanaMarketingService.crear(campana));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampanaMarketing> actualizar(@PathVariable Long id, @Valid @RequestBody CampanaMarketing campana) {
        return ResponseEntity.ok(campanaMarketingService.actualizar(id, campana));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        campanaMarketingService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
