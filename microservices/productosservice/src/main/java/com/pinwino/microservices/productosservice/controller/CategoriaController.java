package com.pinwino.microservices.productosservice.controller;

import com.pinwino.microservices.productosservice.model.Categoria;
import com.pinwino.microservices.productosservice.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pinwino.microservices.productosservice.model.EstadoCategoria;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Categoria> crear(
            @Valid @RequestBody Categoria categoria) {

        Categoria creada = categoriaService.crear(categoria);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Categoria categoria) {

        return ResponseEntity.ok(
                categoriaService.actualizar(id, categoria)
        );
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Categoria> activar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                categoriaService.activar(id)
        );
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Categoria> desactivar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                categoriaService.desactivar(id)
        );
    }
}