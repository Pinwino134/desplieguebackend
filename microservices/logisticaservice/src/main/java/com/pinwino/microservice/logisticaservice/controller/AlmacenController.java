package com.pinwino.microservice.logisticaservice.controller;

import com.pinwino.microservice.logisticaservice.model.Almacen;
import com.pinwino.microservice.logisticaservice.service.AlmacenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/almacenes")
@RequiredArgsConstructor
public class AlmacenController {
    private final AlmacenService almacenService;

    @GetMapping
    public ResponseEntity<List<Almacen>> listar() { return ResponseEntity.ok(almacenService.listarTodos()); }

    @GetMapping("/{id}")
    public ResponseEntity<Almacen> buscarPorId(@PathVariable Long id) { return ResponseEntity.ok(almacenService.buscarPorId(id)); }

    @PostMapping
    public ResponseEntity<Almacen> crear(@Valid @RequestBody Almacen almacen) {
        return ResponseEntity.status(HttpStatus.CREATED).body(almacenService.crear(almacen));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Almacen> actualizar(@PathVariable Long id, @Valid @RequestBody Almacen almacen) {
        return ResponseEntity.ok(almacenService.actualizar(id, almacen));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        almacenService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
