package com.pinwino.microservices.productosservice.controller;

import com.pinwino.microservices.productosservice.exception.BusinessException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    // Crea un producto (valida entrada y propiedad si el actor es ARTESANO).
    @PostMapping
    public ResponseEntity<?> crear(
            @RequestParam Long artesanoId,
            @RequestParam Long categoriaId,
            @RequestBody Producto producto,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {

        if (esArtesanoAjeno(tipoActor, usuarioId, artesanoId)) {
            return prohibido("Un artesano solo puede crear productos bajo su propia cuenta");
        }
        validarDatosProducto(producto);

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
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Producto producto,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {

        Producto actual = productoService.buscarPorId(id);
        if (esArtesanoAjeno(tipoActor, usuarioId, actual.getArtesanoId())) {
            return prohibido("No puedes editar un producto que pertenece a otro artesano");
        }
        validarDatosProducto(producto);

        return ResponseEntity.ok(
                productoService.actualizar(id, producto)
        );
    }

    // Activa un producto.
    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {

        Producto actual = productoService.buscarPorId(id);
        if (esArtesanoAjeno(tipoActor, usuarioId, actual.getArtesanoId())) {
            return prohibido("No puedes activar un producto que pertenece a otro artesano");
        }

        return ResponseEntity.ok(
                productoService.activar(id)
        );
    }

    // Desactiva un producto.
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
            @RequestHeader(value = "X-Usuario-Tipo", required = false) String tipoActor) {

        Producto actual = productoService.buscarPorId(id);
        if (esArtesanoAjeno(tipoActor, usuarioId, actual.getArtesanoId())) {
            return prohibido("No puedes desactivar un producto que pertenece a otro artesano");
        }

        return ResponseEntity.ok(
                productoService.desactivar(id)
        );
    }

    // Ajuste de stock interno (invocado por ventasservice al confirmar pago o cancelar pedido).
    @PostMapping("/{id}/stock")
    public ResponseEntity<Producto> ajustarStock(
            @PathVariable Long id,
            @RequestParam Integer delta) {
        return ResponseEntity.ok(productoService.ajustarStock(id, delta));
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

    private void validarDatosProducto(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank() || producto.getNombre().length() > 150) {
            throw new BusinessException("El nombre del producto es obligatorio (máximo 150 caracteres)");
        }
        if (producto.getPrecioBase() == null || producto.getPrecioBase().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio base del producto debe ser mayor a 0");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new BusinessException("El stock del producto no puede ser negativo");
        }
    }

    private boolean esArtesanoAjeno(String tipoActor, String usuarioId, Long artesanoPropietarioId) {
        if (!"ARTESANO".equalsIgnoreCase(tipoActor) || usuarioId == null || artesanoPropietarioId == null) {
            return false;
        }
        try {
            return !Long.valueOf(usuarioId).equals(artesanoPropietarioId);
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    private ResponseEntity<Map<String, Object>> prohibido(String mensaje) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", 403,
                "mensaje", mensaje
        ));
    }
}
