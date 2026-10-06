package com.pinwino.microservice.gerenciaservice.controller;

import com.pinwino.microservice.gerenciaservice.model.DecisionEstrategica;
import com.pinwino.microservice.gerenciaservice.service.DecisionEstrategicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DecisionEstrategicaController {
    private final DecisionEstrategicaService decisionService;

    @GetMapping("/gerentes/{gerenteId}/decisiones")
    public ResponseEntity<List<DecisionEstrategica>> listarPorGerente(@PathVariable Long gerenteId) {
        return ResponseEntity.ok(decisionService.listarPorGerente(gerenteId));
    }

    @PostMapping("/gerentes/{gerenteId}/decisiones")
    public ResponseEntity<DecisionEstrategica> crear(@PathVariable Long gerenteId, @Valid @RequestBody DecisionEstrategica decision) {
        return ResponseEntity.status(HttpStatus.CREATED).body(decisionService.crear(gerenteId, decision));
    }

    @DeleteMapping("/decisiones/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        decisionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
