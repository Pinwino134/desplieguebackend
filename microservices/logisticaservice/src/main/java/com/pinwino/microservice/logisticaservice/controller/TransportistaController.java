package com.pinwino.microservice.logisticaservice.controller;

import com.pinwino.microservice.logisticaservice.model.Transportista;
import com.pinwino.microservice.logisticaservice.service.TransportistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transportistas")
@RequiredArgsConstructor
public class TransportistaController {
    private final TransportistaService transportistaService;

    @GetMapping
    public ResponseEntity<List<Transportista>> listar() { return ResponseEntity.ok(transportistaService.listarTodos()); }

    @GetMapping("/{id}")
    public ResponseEntity<Transportista> buscarPorId(@PathVariable Long id) { return ResponseEntity.ok(transportistaService.buscarPorId(id)); }

    @PostMapping
    public ResponseEntity<Transportista> crear(@Valid @RequestBody Transportista transportista) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transportistaService.crear(transportista));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transportista> actualizar(@PathVariable Long id, @Valid @RequestBody Transportista transportista) {
        return ResponseEntity.ok(transportistaService.actualizar(id, transportista));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        transportistaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
