package com.pinwino.microservices.ventasservice.controller;

import com.pinwino.microservices.ventasservice.model.CatalogoProducto;
import com.pinwino.microservices.ventasservice.service.CatalogoProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo")
@RequiredArgsConstructor
public class CatalogoProductoController {
    private final CatalogoProductoService catalogoProductoService;

    @GetMapping
    public ResponseEntity<List<CatalogoProducto>> listar(@RequestParam(required = false) String categoria) {
        if (categoria != null) return ResponseEntity.ok(catalogoProductoService.listarPorCategoria(categoria));
        return ResponseEntity.ok(catalogoProductoService.listarTodos());
    }

    @GetMapping("/{productoId}")
    public ResponseEntity<CatalogoProducto> buscarPorId(@PathVariable Long productoId) {
        return ResponseEntity.ok(catalogoProductoService.buscarPorId(productoId));
    }

    // Uso interno: lo invoca el listener de eventos de productos_db / logistica_db
    @PutMapping("/{productoId}")
    public ResponseEntity<CatalogoProducto> upsert(@PathVariable Long productoId, @RequestBody CatalogoProducto producto) {
        producto.setProductoId(productoId);
        return ResponseEntity.ok(catalogoProductoService.upsert(producto));
    }

    @DeleteMapping("/{productoId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long productoId) {
        catalogoProductoService.eliminar(productoId);
        return ResponseEntity.noContent().build();
    }
}
