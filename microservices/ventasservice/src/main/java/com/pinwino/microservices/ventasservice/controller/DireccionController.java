package com.pinwino.microservices.ventasservice.controller;

import com.pinwino.microservices.ventasservice.model.Direccion;
import com.pinwino.microservices.ventasservice.service.DireccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DireccionController {
    private final DireccionService direccionService;

    @GetMapping("/clientes/{clienteId}/direcciones")
    public ResponseEntity<List<Direccion>> listar(@PathVariable Long clienteId) {
        return ResponseEntity.ok(direccionService.listarPorCliente(clienteId));
    }

    @PostMapping("/clientes/{clienteId}/direcciones")
    public ResponseEntity<Direccion> crear(@PathVariable Long clienteId, @Valid @RequestBody Direccion direccion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(direccionService.crear(clienteId, direccion));
    }

    @PutMapping("/direcciones/{id}")
    public ResponseEntity<Direccion> actualizar(@PathVariable Long id, @Valid @RequestBody Direccion direccion) {
        return ResponseEntity.ok(direccionService.actualizar(id, direccion));
    }

    @DeleteMapping("/direcciones/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        direccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
