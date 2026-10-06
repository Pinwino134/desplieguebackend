package com.pinwino.microservices.ventasservice.controller;

import com.pinwino.microservices.ventasservice.model.ProductoPromocion;
import com.pinwino.microservices.ventasservice.model.Promocion;
import com.pinwino.microservices.ventasservice.service.PromocionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PromocionController {
    private final PromocionService promocionService;

    @GetMapping("/campanas/{campanaId}/promociones")
    public ResponseEntity<List<Promocion>> listarPorCampana(@PathVariable Long campanaId) {
        return ResponseEntity.ok(promocionService.listarPorCampana(campanaId));
    }

    @PostMapping("/campanas/{campanaId}/promociones")
    public ResponseEntity<Promocion> crear(@PathVariable Long campanaId, @Valid @RequestBody Promocion promocion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promocionService.crear(campanaId, promocion));
    }

    @DeleteMapping("/promociones/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        promocionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/promociones/{promocionId}/productos/{productoId}")
    public ResponseEntity<ProductoPromocion> aplicarAProducto(@PathVariable Long promocionId, @PathVariable Long productoId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promocionService.aplicarAProducto(promocionId, productoId));
    }

    @DeleteMapping("/promociones/productos/{productoPromocionId}")
    public ResponseEntity<Void> quitarDeProducto(@PathVariable Long productoPromocionId) {
        promocionService.quitarDeProducto(productoPromocionId);
        return ResponseEntity.noContent().build();
    }
}
