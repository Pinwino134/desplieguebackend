package com.pinwino.microservices.productosservice.controller;

import com.pinwino.microservices.productosservice.dto.ProductoDTO;
import com.pinwino.microservices.productosservice.model.EstadoProducto;
import com.pinwino.microservices.productosservice.model.Producto;
import com.pinwino.microservices.productosservice.model.ProductoMaterial;
import com.pinwino.microservices.productosservice.model.ProcesoProduccion;
import com.pinwino.microservices.productosservice.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // Lista productos con filtros opcionales.
    @GetMapping
    public ResponseEntity<List<Producto>> listar(
            @RequestParam(required = false) Long artesanoId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) EstadoProducto estado) {

        if (artesanoId != null) {
            return ResponseEntity.ok(
                    productoService.listarPorArtesano(artesanoId)
            );
        }

        if (categoriaId != null) {
            return ResponseEntity.ok(
                    productoService.listarPorCategoria(categoriaId)
            );
        }

        if (estado != null) {
            return ResponseEntity.ok(
                    productoService.listarPorEstado(estado)
            );
        }

        return ResponseEntity.ok(
                productoService.listarTodos()
        );
    }

    // Busca un producto por ID.
    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productoService.buscarPorId(id)
        );
    }

    // Crea un producto.
    @PostMapping
    public ResponseEntity<Producto> crear(
            @RequestParam Long artesanoId,
            @RequestParam Long categoriaId,
            @RequestBody Producto producto) {

        Producto creado =
                productoService.crear(
                        artesanoId,
                        categoriaId,
                        producto
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    // Actualiza los datos del producto.
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id,
            @RequestBody Producto producto) {

        return ResponseEntity.ok(
                productoService.actualizar(id, producto)
        );
    }

    // Activa un producto.
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Producto> activar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productoService.activar(id)
        );
    }

    // Desactiva un producto.
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Producto> desactivar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productoService.desactivar(id)
        );
    }

    // =========================================================
    // Materiales del producto
    // =========================================================

    @PostMapping("/{productoId}/materiales")
    public ResponseEntity<ProductoMaterial> agregarMaterial(
            @PathVariable Long productoId,
            @RequestParam Long materialId,
            @Valid @RequestBody ProductoMaterial detalle) {

        ProductoMaterial creado =
                productoService.agregarMaterial(
                        productoId,
                        materialId,
                        detalle
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    // Quita la relación entre el producto y el material.
    @DeleteMapping("/{productoId}/materiales/{productoMaterialId}")
    public ResponseEntity<Void> quitarMaterial(
            @PathVariable Long productoId,
            @PathVariable Long productoMaterialId) {

        productoService.quitarMaterial(
                productoId,
                productoMaterialId
        );

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // Procesos de producción
    // =========================================================

    @PostMapping("/{productoId}/procesos")
    public ResponseEntity<ProcesoProduccion> agregarProceso(
            @PathVariable Long productoId,
            @Valid @RequestBody ProcesoProduccion proceso) {

        ProcesoProduccion creado =
                productoService.agregarProceso(
                        productoId,
                        proceso
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    // Quita la relación del proceso con el producto.
    @DeleteMapping("/{productoId}/procesos/{procesoId}")
    public ResponseEntity<Void> quitarProceso(
            @PathVariable Long productoId,
            @PathVariable Long procesoId) {

        productoService.quitarProceso(
                productoId,
                procesoId
        );

        return ResponseEntity.noContent().build();
    }
}