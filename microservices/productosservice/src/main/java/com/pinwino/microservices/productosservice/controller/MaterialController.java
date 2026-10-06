package com.pinwino.microservices.productosservice.controller;

import com.pinwino.microservices.productosservice.model.Material;
import com.pinwino.microservices.productosservice.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/materiales")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    public ResponseEntity<List<Material>> listar() {
        return ResponseEntity.ok(materialService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                materialService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<Material> crear(
            @Valid @RequestBody Material material) {

        Material creado = materialService.crear(material);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Material> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Material material) {

        return ResponseEntity.ok(
                materialService.actualizar(id, material)
        );
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Material> activar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                materialService.activar(id)
        );
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Material> desactivar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                materialService.desactivar(id)
        );
    }
}