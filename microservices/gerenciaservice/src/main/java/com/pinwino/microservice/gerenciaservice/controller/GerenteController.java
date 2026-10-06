package com.pinwino.microservice.gerenciaservice.controller;

import com.pinwino.microservice.gerenciaservice.model.Gerente;
import com.pinwino.microservice.gerenciaservice.service.GerenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gerentes")
@RequiredArgsConstructor
public class GerenteController {
    private final GerenteService gerenteService;

    @GetMapping
    public ResponseEntity<List<Gerente>> listar() { return ResponseEntity.ok(gerenteService.listarTodos()); }

    @GetMapping("/{id}")
    public ResponseEntity<Gerente> buscarPorId(@PathVariable Long id) { return ResponseEntity.ok(gerenteService.buscarPorId(id)); }

    @PostMapping
    public ResponseEntity<Gerente> crear(@Valid @RequestBody Gerente gerente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gerenteService.crear(gerente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gerente> actualizar(@PathVariable Long id, @Valid @RequestBody Gerente gerente) {
        return ResponseEntity.ok(gerenteService.actualizar(id, gerente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        gerenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
