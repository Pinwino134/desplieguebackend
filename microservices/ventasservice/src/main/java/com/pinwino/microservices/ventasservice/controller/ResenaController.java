package com.pinwino.microservices.ventasservice.controller;

import com.pinwino.microservices.ventasservice.model.Resena;
import com.pinwino.microservices.ventasservice.service.ResenaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resenas")
@RequiredArgsConstructor
public class ResenaController {
    private final ResenaService resenaService;

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<Resena>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(resenaService.listarPorProducto(productoId));
    }

    @PostMapping
    public ResponseEntity<Resena> crear(@Valid @RequestBody Resena resena) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaService.crear(resena));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        resenaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
